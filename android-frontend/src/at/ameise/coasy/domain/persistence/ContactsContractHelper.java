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
import android.database.DatabaseUtils;
import android.net.Uri;
import android.provider.ContactsContract;
import at.ameise.coasy.ICoasySettings;
import at.ameise.coasy.domain.Course;
import at.ameise.coasy.domain.Student;
import at.ameise.coasy.domain.persistence.database.CoasyDatabaseHelper;
import at.ameise.coasy.domain.persistence.database.PerformanceDatabaseContentProvider;
import at.ameise.coasy.domain.persistence.database.StudentTable;
import at.ameise.coasy.exception.ContactsError;
import at.ameise.coasy.exception.CreateContactsException;
import at.ameise.coasy.exception.UpdateContactsException;
import at.ameise.coasy.util.Logger;
import at.ameise.coasy.util.ReflectionUtil;
import at.ameise.coasy.util.SettingsUtil;

/**
 * Contains helper methods for contacts.
 * 
 * TODO maybe we want a static gson instance for this class.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public final class ContactsContractHelper {

	private static final String TAG = "ContactsHelper";

	/**
	 * This is the prefix of the title of all coasy managed contact groups.
	 */
	public static final String CONTACTS_GROUP_TITLE_PREFIX = "coasy+";

	/**
	 * This is the prefix of the title of the coasy settings contact groups.
	 */
	public static final String SETTINGS_TITLE = "coasysettings+";

	public static final String CONTACTS_GROUP_TITLE_PREFIX_WO_PLUS = CONTACTS_GROUP_TITLE_PREFIX.substring(0, CONTACTS_GROUP_TITLE_PREFIX.length() - 1);

	private ContactsContractHelper() {
	}

	/**
	 * @param context
	 * @param groupId
	 * @return a cursor on the ids of all contacts of the specified group.
	 */
	static Cursor getContactIdsOfGroupCursor(Context context, long groupId) {

		Cursor contactIdsOfGroupCursor = context.getContentResolver().query(//
				ContactsContract.Data.CONTENT_URI,//
				new String[] { //
						ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,//
						ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID },//
				ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + " = ?", //
				new String[] { String.valueOf(groupId), }, null);
		
		Logger.debug(TAG, DatabaseUtils.dumpCursorToString(contactIdsOfGroupCursor));
		
		return contactIdsOfGroupCursor;
	}

	/**
	 * @param context
	 * @param id
	 *            the id of the {@link Course} to load.
	 * @return a {@link CursorLoader} on a single {@link Course}.
	 */
	static Loader<Cursor> getCourseCursorLoader(Context context, long id) {

		return new CursorLoader(context, Uri.withAppendedPath(PerformanceDatabaseContentProvider.CONTENT_URI_COURSE, String.valueOf(id)), null, null, null,
				null);
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
	static void createContactGroup(Context context, Course course) throws UpdateContactsException, CreateContactsException {

		if (course.getId() > -1)
			throw new CreateContactsException("Course has already an id!");

		/*
		 * Create the contact group
		 */
		final long timestamp = System.currentTimeMillis();
		final String tempTitle = CONTACTS_GROUP_TITLE_PREFIX + timestamp;

		final ContentValues groupValues = new ContentValues();
		groupValues.put(ContactsContract.Groups.TITLE, String.valueOf(tempTitle));
		groupValues.put(ContactsContract.Groups.ACCOUNT_NAME, SettingsUtil.getSelectedGoogleAccount(context).name);
		groupValues.put(ContactsContract.Groups.ACCOUNT_TYPE, SettingsUtil.ACCOUNT_TYPE_GOOGLE);
		groupValues.put(ContactsContract.Groups.SHOULD_SYNC, CoasyDatabaseHelper.SQLITE_VALUE_TRUE);
		groupValues.put(ContactsContract.Groups.GROUP_VISIBLE, ICoasySettings.MODE_DEBUG ? CoasyDatabaseHelper.SQLITE_VALUE_TRUE : CoasyDatabaseHelper.SQLITE_VALUE_FALSE);

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
		newValues.put(ContactsContract.Groups.NOTES, CoasyDatabaseHelper.toEscapedJson(course));

		int updated = context.getContentResolver().update(ContactsContract.Groups.CONTENT_URI, newValues, ContactsContract.Groups._ID + " = ?",
				new String[] { id, });

		if (updated != 1) {

			throw new UpdateContactsException("Failed to update group!");
		}
	}

	/**
	 * Adds the contact to the group.
	 * 
	 * @param context
	 * @param contactId
	 * @param groupRowId
	 */
	static void addContactToGroup(Context context, long contactId, long groupRowId) {

		ContentValues values = new ContentValues();
		values.put(ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID, ContactsContractHelper.getRawContactId(context, contactId));
		values.put(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID, groupRowId);
		values.put(ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE, ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE);

		final Uri returnUri = context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
		final long rowId = Long.valueOf(returnUri.getLastPathSegment());

		if (rowId < 0)
			throw new ContactsError("Failed to insert the contact-group mapping in ContactsContract.CommonDataKinds.GroupMembership!");
	}

	/**
	 * Removes the contact from the group
	 * 
	 * @param context
	 * @param contactId
	 * @param groupId
	 */
	static void removeContactFromGroup(Context context, long contactId, long groupId) {

		long rawContactId = ContactsContractHelper.getRawContactId(context, contactId);

		// didn't work either
		Cursor c = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
				null,//
				ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID
						+ " = ? AND "//
						+ ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + " = ? AND "
						+ ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE + " = ?",//
				new String[] { String.valueOf(groupId), String.valueOf(rawContactId), ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE },//
				null);
		Logger.debug(TAG, "GroupMembership " + DatabaseUtils.dumpCursorToString(c));

		int deletedRows = context.getContentResolver().delete(ContactsContract.Data.CONTENT_URI,//
				ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID + " = ? AND " + //
						ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + " = ?",//
				new String[] { String.valueOf(rawContactId), String.valueOf(groupId), });

		if (deletedRows != 1)
			throw new ContactsError("Failed to delete the contact-group mapping in ContactsContract.CommonDataKinds.GroupMembership!");
	}

	/**
	 * @param context
	 * @param contactId
	 * @return the student object of the specified contact id.
	 */
	static Student getContactAsStudent(Context context, long contactId) {

		Cursor contactCursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,//
				null,//
				ContactsContract.Contacts._ID + " = ?",//
				new String[] { String.valueOf(contactId), },//
				null);

		if (contactCursor.moveToFirst()) {

			Student student = StudentTable.fromContactsCursor(contactCursor);

			contactCursor.close();

			return student;

		} else {

			throw new ContactsError("Contact with id " + contactId + " does not exist!");
		}
	}

	/**
	 * TODO this may cause trouble when more raw contacts exist and the order
	 * changes...
	 * 
	 * @param context
	 * @param contactId
	 * @return an arbitrary raw_contact_id of the contact with contactId.
	 */
	static long getRawContactId(Context context, long contactId) {

		Cursor rawContactCursor = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,//
				null,//
				ContactsContract.RawContacts.CONTACT_ID + " = ?",//
				new String[] { String.valueOf(contactId), },//
				null);

		if (rawContactCursor.moveToFirst()) {

			long rawContactId = rawContactCursor.getLong(rawContactCursor.getColumnIndex(ContactsContract.RawContacts._ID));
			Logger.debug(TAG, "Contact with contact_id " + contactId + " consists of " + rawContactCursor.getCount() + " raw contacts. Using raw_contact_id "
					+ rawContactId);

			rawContactCursor.close();

			return rawContactId;

		} else {

			throw new ContactsError("Failed to get raw_contact_id of contact with contact_id " + contactId + "!");
		}
	}

	/**
	 * Updates the specified {@link Course} in the contacts group. This method
	 * saves the json representation of the course in the
	 * {@link ContactsContract.GroupsColumns#NOTES}.
	 * 
	 * @param context
	 * @param course
	 * @throws UpdateContactsException
	 */
	public static void updateContactGroup(Context context, Course course) throws UpdateContactsException {

		if (course.getId() < 0)
			throw new UpdateContactsException("Course has no id!");

		final ContentValues groupValues = new ContentValues();
		groupValues.put(ContactsContract.Groups.NOTES, CoasyDatabaseHelper.toEscapedJson(course));

		int updated = context.getContentResolver().update(ContactsContract.Groups.CONTENT_URI, groupValues, ContactsContract.Groups._ID + " = ?",
				new String[] { String.valueOf(course.getId()), });

		if (updated != 1) {

			throw new UpdateContactsException("Failed to update group!");
		}
	}

	/**
	 * Removes all contacts from the specified group.
	 * 
	 * @param context
	 * @param groupRowId
	 */
	public static void removeContactsFromGroup(Context context, long groupRowId) {

		context.getContentResolver().delete(ContactsContract.Data.CONTENT_URI, ""//
				+ ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + " = ? AND "//
				+ ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE + " = ?",//
				new String[] { String.valueOf(groupRowId), ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE, });
	}

	/**
	 * @param context
	 * @return {@link Cursor} on the coasy managed groups.
	 */
	public static Cursor getAllCoasyGroups(Context context) {

		if (ICoasySettings.MODE_DEBUG) {

			return context.getContentResolver().query(//
					ContactsContract.Groups.CONTENT_URI,//
					null, ""//
							+ ContactsContract.Groups.DELETED + " = ? AND "//
							+ ContactsContract.Groups.SHOULD_SYNC + " = ? AND "//
							+ ContactsContract.Groups.GROUP_VISIBLE + " = ? AND "//
							+ ContactsContract.Groups.TITLE + " LIKE ? AND "//
							+ ContactsContract.Groups.ACCOUNT_NAME + " = ? AND "//
							+ ContactsContract.Groups.ACCOUNT_TYPE + " = ?",//
					new String[] {//
							CoasyDatabaseHelper.SQLITE_VALUE_FALSE,//
							CoasyDatabaseHelper.SQLITE_VALUE_TRUE,//
							CoasyDatabaseHelper.SQLITE_VALUE_TRUE,//
							ContactsContractHelper.CONTACTS_GROUP_TITLE_PREFIX + "%",//
							SettingsUtil.getSelectedGoogleAccount(context).name,//
							SettingsUtil.ACCOUNT_TYPE_GOOGLE,//
					},//
					null);

		} else {

			return context.getContentResolver().query(//
					ContactsContract.Groups.CONTENT_URI,//
					null, ""//
							+ ContactsContract.Groups.DELETED + " = ? AND "//
							+ ContactsContract.Groups.SHOULD_SYNC + " = ? AND "//
							+ ContactsContract.Groups.GROUP_VISIBLE + " = ? AND "//
							+ ContactsContract.Groups.TITLE + " LIKE ? AND "//
							+ ContactsContract.Groups.ACCOUNT_NAME + " = ? AND "//
							+ ContactsContract.Groups.ACCOUNT_TYPE + " = ?",//
					new String[] {//
							CoasyDatabaseHelper.SQLITE_VALUE_FALSE,//
							CoasyDatabaseHelper.SQLITE_VALUE_TRUE,//
							CoasyDatabaseHelper.SQLITE_VALUE_FALSE,//
							ContactsContractHelper.CONTACTS_GROUP_TITLE_PREFIX + "%",//
							SettingsUtil.getSelectedGoogleAccount(context).name,//
							SettingsUtil.ACCOUNT_TYPE_GOOGLE,//
					},//
					null);
		}
	}
	
}
