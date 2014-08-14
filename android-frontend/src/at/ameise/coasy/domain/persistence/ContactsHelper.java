/*
 * Copyright (c) 2014, Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * 
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package at.ameise.coasy.domain.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import at.ameise.coasy.ICoasySettings;
import at.ameise.coasy.domain.Course;
import at.ameise.coasy.domain.persistence.database.PerformanceDatabaseContentProvider;
import at.ameise.coasy.domain.persistence.database.CoasyDatabaseHelper;
import at.ameise.coasy.exception.ContactsError;
import at.ameise.coasy.exception.CreateContactsException;
import at.ameise.coasy.exception.UpdateContactsException;
import at.ameise.coasy.util.AccountUtil;
import at.ameise.coasy.util.ReflectionUtil;

import com.google.gson.Gson;

/**
 * Contains helper methods for contacts.
 * 
 * TODO maybe we want a static gson instance for this class.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
final class ContactsHelper {

	/**
	 * This is the prefix of the title of all coasy managed contact groups.
	 */
	public static final String CONTACTS_GROUP_TITLE_PREFIX = "coasy+";

	static final String CONTACTS_GROUP_TITLE_PREFIX_WO_PLUS = CONTACTS_GROUP_TITLE_PREFIX.substring(0, CONTACTS_GROUP_TITLE_PREFIX.length() - 1);

	
	private ContactsHelper() {
	}

	/**
	 * @param mContext
	 * @param groupId
	 * @return a cursor on the ids of all contacts of the specified group.
	 */
	public static Cursor getContactIdsOfGroupCursor(Context mContext, String groupId) {

		return mContext.getContentResolver().query(//
				ContactsContract.Data.CONTENT_URI,//
				new String[] { //
				ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,//
						ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID },//
				ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + " = ?", //
				new String[] { groupId, }, null);
	}

	/**
	 * @param mContext
	 * @param id
	 *            the id of the {@link Course} to load.
	 * @return a {@link CursorLoader} on a single {@link Course}.
	 */
	public static Loader<Cursor> getCourseCursorLoader(Context mContext, long id) {

		return new CursorLoader(mContext, Uri.parse(PerformanceDatabaseContentProvider.CONTENT_URI_COURSE + "/" + id), null, null, null, null);
	}

	/**
	 * Creates a group in {@link ContactsContract.Groups}.<br>
	 * <br>
	 * NOTE: This method sets the {@link Course#getId()}.
	 * 
	 * @param course
	 * @throws UpdateContactsException
	 * @throws CreateContactsException
	 */
	public static void createContactGroup(Context context, Course course) throws UpdateContactsException, CreateContactsException {

		if (course.getId() > -1)
			throw new CreateContactsException("Course has already an id!");

		final Gson gson = new Gson();

		/*
		 * Create the contact group
		 */
		final long timestamp = System.currentTimeMillis();
		final String tempTitle = CONTACTS_GROUP_TITLE_PREFIX + timestamp;

		final ContentValues groupValues = new ContentValues();
		groupValues.put(ContactsContract.Groups.TITLE, tempTitle);
		groupValues.put(ContactsContract.Groups.ACCOUNT_NAME, AccountUtil.getSelectedGoogleAccount(context).name);
		groupValues.put(ContactsContract.Groups.ACCOUNT_TYPE, AccountUtil.ACCOUNT_TYPE_GOOGLE);
		groupValues.put(ContactsContract.Groups.SHOULD_SYNC, CoasyDatabaseHelper.SQLITE_VALUE_TRUE);

		if (ICoasySettings.MODE_DEBUG)
			groupValues.put(ContactsContract.Groups.GROUP_VISIBLE, CoasyDatabaseHelper.SQLITE_VALUE_TRUE);
		else
			groupValues.put(ContactsContract.Groups.GROUP_VISIBLE, CoasyDatabaseHelper.SQLITE_VALUE_FALSE);

		final Uri returnUri = context.getContentResolver().insert(ContactsContract.Groups.CONTENT_URI, groupValues);
		final String id = returnUri.getLastPathSegment();

		/*
		 * Set the id in the course object.
		 */
		try {

			ReflectionUtil.setFieldValue(course, "id", Long.valueOf(id));

		} catch (NumberFormatException e) {

			throw new CreateContactsException("Failed to set the value of the id field!", e);

		} catch (NoSuchFieldException e) {

			throw new ContactsError("Failed to set the value of the id field!", e);

		} catch (IllegalAccessException e) {

			throw new ContactsError("Failed to set the value of the id field!", e);

		} catch (IllegalArgumentException e) {

			throw new ContactsError("Failed to set the value of the id field!", e);
		}

		/*
		 * Change the title back to the id. We do this cause the id is
		 * invariant.
		 */
		final ContentValues newValues = new ContentValues();
		newValues.put(ContactsContract.Groups.TITLE, CONTACTS_GROUP_TITLE_PREFIX + id);
		newValues.put(ContactsContract.Groups.NOTES, gson.toJson(course));

		int updated = context.getContentResolver().update(ContactsContract.Groups.CONTENT_URI, newValues, ContactsContract.Groups._ID + " = ?",
				new String[] { id, });

		if (updated != 1) {

			throw new UpdateContactsException("Failed to update group!");
		}
	}

}
