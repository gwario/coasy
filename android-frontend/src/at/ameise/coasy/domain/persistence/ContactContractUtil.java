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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.provider.ContactsContract;
import at.ameise.coasy.ICoasySettings;
import at.ameise.coasy.domain.Course;
import at.ameise.coasy.domain.persistence.database.CoasyDatabaseHelper;
import at.ameise.coasy.domain.persistence.database.CourseTable;
import at.ameise.coasy.domain.persistence.database.PerformanceDatabaseContentProvider;
import at.ameise.coasy.util.AccountUtil;
import at.ameise.coasy.util.IUtilTags;
import at.ameise.coasy.util.Logger;

/**
 * Contains methods to deal with {@link ContactsContract.Groups} and {@link ContactsContract.Contacts}.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public final class ContactContractUtil {

	
	private ContactContractUtil() {
	}

	/**
	 * @param context
	 * @return a {@link CursorLoader} with all {@link Course}s.
	 */
	public static final CursorLoader getCoursesLoader(Context context) {

		Cursor coasyContactGroupCursor;
		final List<String> courseIds = new ArrayList<String>();

		if (ICoasySettings.MODE_DEBUG) {

			coasyContactGroupCursor = context.getContentResolver().query(//
					ContactsContract.Groups.CONTENT_URI,//
					null, ""//
							+ ContactsContract.Groups.SHOULD_SYNC + " = ? AND "//
							+ ContactsContract.Groups.GROUP_VISIBLE + " = ? AND "//
							+ ContactsContract.Groups.TITLE + " LIKE ? AND "//
							+ ContactsContract.Groups.ACCOUNT_NAME + " = ? AND "//
							+ ContactsContract.Groups.ACCOUNT_TYPE + " = ?",//
					new String[] {//
							CoasyDatabaseHelper.SQLITE_VALUE_TRUE,//
							CoasyDatabaseHelper.SQLITE_VALUE_TRUE,//
							ContactsHelper.CONTACTS_GROUP_TITLE_PREFIX + "%",//
							AccountUtil.getSelectedGoogleAccount(context).name,//
							AccountUtil.ACCOUNT_TYPE_GOOGLE,//
					},//
					null);

		} else {

			coasyContactGroupCursor = context.getContentResolver().query(//
					ContactsContract.Groups.CONTENT_URI,//
					null, ""//
							+ ContactsContract.Groups.SHOULD_SYNC + " = ? AND "//
							+ ContactsContract.Groups.GROUP_VISIBLE + " = ? AND "//
							+ ContactsContract.Groups.TITLE + " LIKE ? AND "//
							+ ContactsContract.Groups.ACCOUNT_NAME + " = ? AND "//
							+ ContactsContract.Groups.ACCOUNT_TYPE + " = ?",//
					new String[] {//
					CoasyDatabaseHelper.SQLITE_VALUE_TRUE,//
							CoasyDatabaseHelper.SQLITE_VALUE_FALSE,//
							ContactsHelper.CONTACTS_GROUP_TITLE_PREFIX + "%",//
							AccountUtil.getSelectedGoogleAccount(context).name,//
							AccountUtil.ACCOUNT_TYPE_GOOGLE,//
					},//
					null);
		}

		Logger.debug(IUtilTags.TAG_CONTACT_CONTRACT_UTIL, "Got " + coasyContactGroupCursor.getCount() + " groups.");

		if (coasyContactGroupCursor.moveToFirst()) {

			do {
				String groupTitle = coasyContactGroupCursor.getString(coasyContactGroupCursor.getColumnIndexOrThrow(ContactsContract.Groups.TITLE));

				Logger.verbose(IUtilTags.TAG_CONTACT_CONTRACT_UTIL, "Checking group " + groupTitle);
				if (isCoasyContactGroup(groupTitle)) {
					Logger.verbose(IUtilTags.TAG_CONTACT_CONTRACT_UTIL, "Group " + groupTitle + " is a course.");
					courseIds.add(groupTitle.split("\\+")[1]);
				}

			} while (coasyContactGroupCursor.moveToNext());
		}

		coasyContactGroupCursor.close();

		Logger.debug(IUtilTags.TAG_CONTACT_CONTRACT_UTIL, "Got " + courseIds.size() + " courses.");

		if (courseIds.size() > 0)
			return new CursorLoader(context, PerformanceDatabaseContentProvider.CONTENT_URI_COURSE, null, CourseTable.COL_ID + " IN ("
					+ CoasyDatabaseHelper.makePlaceholders(courseIds.size()) + ")", courseIds.toArray(new String[courseIds.size()]),
					CourseTable.SORT_ORDER_TITLE_ASC);
		else
			return new CursorLoader(context, PerformanceDatabaseContentProvider.CONTENT_URI_COURSE, null, CourseTable.COL_ID + " = -1", null, null);
	}

	/**
	 * @param contactGroupTitle
	 *            the title from the {@link ContactsContract.Groups} table.
	 * @return true if the title matches the coasy group name convention.
	 */
	private static final boolean isCoasyContactGroup(String contactGroupTitle) {

		if (contactGroupTitle == null)
			throw new IllegalArgumentException("contactGroupTitle was null!");

		final String[] titleParts = contactGroupTitle.split("\\+");

		if (titleParts.length == 2//
				&& titleParts[0].equals(ContactsHelper.CONTACTS_GROUP_TITLE_PREFIX_WO_PLUS)//
				&& titleParts[1].matches("[0-9]+")) {

			return true;

		} else {

			return false;
		}
	}


	/**
	 * @param context
	 * @return all coasy managed contact {@link ContactsContract.Groups} as a
	 *         {@link Cursor}.
	 */
	private static Cursor getAllCoasyContactGroups(Context context) {

		if (ICoasySettings.MODE_DEBUG) {

			return context.getContentResolver().query(//
					ContactsContract.Groups.CONTENT_URI,//
					null, ""//
							+ ContactsContract.Groups.SHOULD_SYNC + " = ? AND "//
							+ ContactsContract.Groups.GROUP_VISIBLE + " = ? AND "//
							+ ContactsContract.Groups.TITLE + " LIKE ? AND "//
							+ ContactsContract.Groups.ACCOUNT_NAME + " = ? AND "//
							+ ContactsContract.Groups.ACCOUNT_TYPE + " = ?",//
					new String[] {//
					CoasyDatabaseHelper.SQLITE_VALUE_TRUE,//
							CoasyDatabaseHelper.SQLITE_VALUE_TRUE,//
							ContactsHelper.CONTACTS_GROUP_TITLE_PREFIX + "%",//
							AccountUtil.getSelectedGoogleAccount(context).name,//
							AccountUtil.ACCOUNT_TYPE_GOOGLE,//
					},//
					null);

		} else {

			return context.getContentResolver().query(//
					ContactsContract.Groups.CONTENT_URI,//
					null, ""//
							+ ContactsContract.Groups.SHOULD_SYNC + " = ? AND "//
							+ ContactsContract.Groups.GROUP_VISIBLE + " = ? AND "//
							+ ContactsContract.Groups.TITLE + " LIKE ? AND "//
							+ ContactsContract.Groups.ACCOUNT_NAME + " = ? AND "//
							+ ContactsContract.Groups.ACCOUNT_TYPE + " = ?",//
					new String[] {//
					CoasyDatabaseHelper.SQLITE_VALUE_TRUE,//
							CoasyDatabaseHelper.SQLITE_VALUE_FALSE,//
							ContactsHelper.CONTACTS_GROUP_TITLE_PREFIX + "%",//
							AccountUtil.getSelectedGoogleAccount(context).name,//
							AccountUtil.ACCOUNT_TYPE_GOOGLE,//
					},//
					null);
		}
	}

	/**
	 * @param context
	 * @return a {@link Map} of all contact groups of the
	 *         {@link AccountUtil#getSelectedGoogleAccount(Context)}. As id to
	 *         title mapping.
	 */
	public static Map<CharSequence, CharSequence> getAllContactGroups(Context context) {

		Map<CharSequence, CharSequence> groups = new HashMap<CharSequence, CharSequence>();

		Cursor groupsCursor = getAllContactGroupsAsCursor(context);

		if (groupsCursor.moveToFirst()) {

			do {

				groups.put(String.valueOf(groupsCursor.getLong(groupsCursor.getColumnIndexOrThrow(ContactsContract.Groups._ID))),
						groupsCursor.getString(groupsCursor.getColumnIndexOrThrow(ContactsContract.Groups.TITLE)));

			} while (groupsCursor.moveToNext());
		}

		groupsCursor.close();

		return groups;
	}

	/**
	 * @param context
	 * @return {@link Cursor} on all visible {@link ContactsContract.Groups} of
	 *         the {@link AccountUtil#getSelectedGoogleAccount(Context)}
	 */
	private static Cursor getAllContactGroupsAsCursor(Context context) {

		return context.getContentResolver().query(//
				ContactsContract.Groups.CONTENT_URI,//
				null, ""//
						+ ContactsContract.Groups.GROUP_VISIBLE + " = ? AND "//
						+ ContactsContract.Groups.ACCOUNT_NAME + " = ? AND "//
						+ ContactsContract.Groups.ACCOUNT_TYPE + " = ?",//
				new String[] {//
				CoasyDatabaseHelper.SQLITE_VALUE_TRUE,//
						AccountUtil.getSelectedGoogleAccount(context).name,//
						AccountUtil.ACCOUNT_TYPE_GOOGLE,//
				},//
				null);
	}

	/**
	 * @param context
	 * @return the id of the first contact group as of
	 *         {@link ContactContractUtil#getAllContactGroupsAsCursor(Context)}.
	 */
	public static long getFirstGroupId(Context context) {

		long id;
		final Cursor groupsCursor = getAllContactGroupsAsCursor(context);

		if (groupsCursor.moveToFirst()) {

			id = groupsCursor.getLong(groupsCursor.getColumnIndexOrThrow(ContactsContract.Groups._ID));

		} else {

			id = -1;
		}

		groupsCursor.close();

		return id;
	}
}
