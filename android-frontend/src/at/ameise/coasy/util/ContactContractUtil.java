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
package at.ameise.coasy.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import at.ameise.coasy.ICoasySettings;
import at.ameise.coasy.domain.Course;
import at.ameise.coasy.domain.Student;
import at.ameise.coasy.domain.database.CoasyContentProvider;
import at.ameise.coasy.domain.database.CoasyDatabaseHelper;
import at.ameise.coasy.domain.database.CourseTable;
import at.ameise.coasy.exception.DatabaseError;

import com.google.gson.Gson;

/**
 * Contains methods to deal with {@link ContactsContract.Groups} and {@link ContactsContract.Contacts}.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public final class ContactContractUtil {

	/**
	 * This is the prefix of the title of all coasy managed contact groups.
	 */
	public static final String CONTACTS_GROUP_TITLE_PREFIX = "coasy+";

	private static final String CONTACTS_GROUP_TITLE_PREFIX_WO_PLUS = CONTACTS_GROUP_TITLE_PREFIX.substring(0, CONTACTS_GROUP_TITLE_PREFIX.length() - 1);

	private ContactContractUtil() {
	}

	/**
	 * This method does only create the contact group. you have to take care of
	 * already existing groups by yourself.<br>
	 * <br>
	 * NOTE: This method sets the {@link Course#getId()}. TODO make
	 * transactional
	 * 
	 * @param context
	 * @param course
	 * @return the id of the new contact group.
	 */
	public static final long createCoasyContactGroup(Context context, Course course) {

		if (course.getId() > -1)
			throw new IllegalArgumentException("Course has already a contact group id!");
		
		/*
		 * Create the contact group.
		 */
		final Gson gson = new Gson();
		final ContentValues groupValues = new ContentValues();
		final long timestamp = System.currentTimeMillis();

		groupValues.put(ContactsContract.Groups.TITLE, CONTACTS_GROUP_TITLE_PREFIX + timestamp);
		groupValues.put(ContactsContract.Groups.ACCOUNT_NAME, AccountUtil.getSelectedGoogleAccount(context).name);
		groupValues.put(ContactsContract.Groups.ACCOUNT_TYPE, AccountUtil.ACCOUNT_TYPE_GOOGLE);
		groupValues.put(ContactsContract.Groups.SHOULD_SYNC, CoasyDatabaseHelper.SQLITE_VALUE_TRUE);
		groupValues.put(ContactsContract.Groups.NOTES, gson.toJson(course));
		if (ICoasySettings.MODE_DEBUG)
			groupValues.put(ContactsContract.Groups.GROUP_VISIBLE, CoasyDatabaseHelper.SQLITE_VALUE_TRUE);
		else
			groupValues.put(ContactsContract.Groups.GROUP_VISIBLE, CoasyDatabaseHelper.SQLITE_VALUE_FALSE);

		final Uri returnUri = context.getContentResolver().insert(ContactsContract.Groups.CONTENT_URI, groupValues);
		final String id = returnUri.getLastPathSegment();

		/*
		 * Change the title back to the id. we do this cause the id is
		 * immutable...
		 */
		final ContentValues newTitleValues = new ContentValues();
		newTitleValues.put(ContactsContract.Groups.TITLE, CONTACTS_GROUP_TITLE_PREFIX + id);

		context.getContentResolver().update(ContactsContract.Groups.CONTENT_URI, newTitleValues, ContactsContract.Groups._ID + " = ?", new String[] { id });

		try {

			ReflectionUtil.setFieldValue(course, "id", Long.valueOf(id));

		} catch (NumberFormatException e) {

			throw new DatabaseError("Failed to set the value of the id field!", e);

		} catch (NoSuchFieldException e) {

			throw new DatabaseError("Failed to set the value of the id field!", e);

		} catch (IllegalAccessException e) {

			throw new DatabaseError("Failed to set the value of the id field!", e);

		} catch (IllegalArgumentException e) {

			throw new DatabaseError("Failed to set the value of the id field!", e);
		}

		return course.getId();
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
							CONTACTS_GROUP_TITLE_PREFIX + "%",//
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
							CONTACTS_GROUP_TITLE_PREFIX + "%",//
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
			return new CursorLoader(context, CoasyContentProvider.CONTENT_URI_COURSE, null, CourseTable.COL_ID + " IN ("
					+ CoasyDatabaseHelper.makePlaceholders(courseIds.size()) + ")", courseIds.toArray(new String[courseIds.size()]),
					CourseTable.SORT_ORDER_TITLE_ASC);
		else
			return new CursorLoader(context, CoasyContentProvider.CONTENT_URI_COURSE, null, CourseTable.COL_ID + " = -1", null, null);
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
				&& titleParts[0].equals(CONTACTS_GROUP_TITLE_PREFIX_WO_PLUS)//
				&& titleParts[1].matches("[0-9]+")) {

			return true;

		} else {

			return false;
		}
	}

	/**
	 * See {@link AccountUtil#getSelectedGoogleAccount(Context)}
	 * 
	 * @param context
	 * @param course
	 * @return true if the group with that title already exits in the contacts
	 *         database of the selected google account.
	 */
	public static boolean doesCoasyContactGroupExist(Context context, Course course) {

		boolean ret;

		final Cursor courseCursor = context.getContentResolver().query(//
				CoasyContentProvider.CONTENT_URI_COURSE,//
				null, CourseTable.COL_TITLE + " = ?", new String[] { course.getTitle(), }, null);

		if (courseCursor.moveToFirst()) {

			final Cursor coasyContactGroupCursor = context.getContentResolver().query(//
					ContactsContract.Groups.CONTENT_URI,//
					null, ""//
							+ ContactsContract.Groups.TITLE + " = ? AND "//
							+ ContactsContract.Groups.ACCOUNT_NAME + " = ? AND "//
							+ ContactsContract.Groups.ACCOUNT_TYPE + " = ?",//
					new String[] {//
					CONTACTS_GROUP_TITLE_PREFIX + courseCursor.getLong(courseCursor.getColumnIndexOrThrow(CourseTable.COL_ID)),//
							AccountUtil.getSelectedGoogleAccount(context).name,//
							AccountUtil.ACCOUNT_TYPE_GOOGLE,//
					},//
					null);

			if (coasyContactGroupCursor.moveToFirst())
				ret = true;
			else
				ret = false;

			coasyContactGroupCursor.close();

		} else {

			ret = false;
		}

		courseCursor.close();

		return ret;
	}

	/**
	 * This method writes the mapping into the {@link ContactsContract}.
	 * 
	 * @param context
	 * @param student
	 *            to add to the course.
	 * @param course
	 *            to add the student to.
	 */
	public static void addContactToCoasyContactGroup(Context context, Student student, Course course) {

		if (course.getId() < 0)
			throw new IllegalArgumentException("Course has no contact group id set!");
		if (student.getId() < 0)
			throw new IllegalArgumentException("Student has no contact id set!");

		ContentValues values = new ContentValues();
		values.put(ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID, student.getId());
		values.put(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID, course.getId());
		values.put(ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE, ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE);

		final Uri returnUri = context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
		final long rowId = Long.valueOf(returnUri.getLastPathSegment());

		if (rowId < 0)
			throw new DatabaseError("Failed to store the student course in ContactsContract.CommonDataKinds.GroupMembership!");
	}

	/**
	 * This method deletes the mapping from the {@link ContactsContract} if it
	 * exists.
	 * 
	 * @param context
	 * @param student
	 * @param course
	 */
	public static void removeContactFromCoasyContactGroup(Context context, Student student, Course course) {

		if (course.getId() < 0)
			throw new IllegalArgumentException("Course has no contact group id set!");
		if (student.getId() < 0)
			throw new IllegalArgumentException("Student has no contact id set!");

		final int deletedRows = context.getContentResolver().delete(
				ContactsContract.Data.CONTENT_URI,//
				ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID + " = ? AND " + ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID
						+ " = ?",//
				new String[] { String.valueOf(student.getId()), String.valueOf(course.getId()), });

		if (deletedRows > 1)
			throw new DatabaseError(
					"Duplicated mapping! There were two rows representing the student group mapping! This maybe the result of a programming error!");

	}

	/**
	 * @param context
	 * @param course
	 * @return a {@link CursorLoader} for students in course.
	 */
	public static CursorLoader getAllStudentsOfCourse(Context context, Course course) {

		Cursor studentIdsCursor = context.getContentResolver().query(//
				ContactsContract.Data.CONTENT_URI,//
				new String[] { //
				ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,//
						ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID },//
				ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + " = ?", //
				new String[] { String.valueOf(course.getId()) }, null);

		final String[] studentIds = new String[studentIdsCursor.getCount()];
		int i = 0;

		if (studentIdsCursor.moveToFirst()) {

			do {

				studentIds[i++] = String.valueOf(studentIdsCursor.getLong(studentIdsCursor
						.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID)));

			} while (studentIdsCursor.moveToNext());
		}

		studentIdsCursor.close();

		if (studentIds.length > 0) {

			return new CursorLoader(context, Contacts.CONTENT_URI,//
					null, //
					ContactsContract.Contacts._ID + " IN (" + CoasyDatabaseHelper.makePlaceholders(studentIds.length) + ")",//
					studentIds,//
					ContactsContract.Contacts.DISPLAY_NAME + " asc");

		} else {

			return new CursorLoader(context, Contacts.CONTENT_URI,//
					null, //
					ContactsContract.Contacts._ID + " = -1",//
					null,//
					ContactsContract.Contacts.DISPLAY_NAME + " asc");
		}

	}

	/**
	 * @param context
	 * @param course
	 * @return a {@link CursorLoader} for all students in courses.
	 */
	public static CursorLoader getAllStudents(Context context) {

		final List<String> courseIds = new ArrayList<String>();

		final Cursor courseCursor = CourseUtil.getAllCoursesAsCursor(context);
		Logger.debug(IUtilTags.TAG_CONTACT_CONTRACT_UTIL, "Got " + courseCursor.getCount() + " courses.");

		if (courseCursor.moveToFirst()) {

			do {

				courseIds.add(String.valueOf(courseCursor.getLong(courseCursor.getColumnIndexOrThrow(CourseTable.COL_ID))));

			} while (courseCursor.moveToNext());
		}

		courseCursor.close();

		if (courseIds.size() > 0) {

			Cursor studentIdsCursor = context.getContentResolver().query(//
					ContactsContract.Data.CONTENT_URI,//
					new String[] { //
					ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,//
							ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID },//
					ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + " IN (" + CoasyDatabaseHelper.makePlaceholders(courseIds.size()) + ")", //
					courseIds.toArray(new String[courseIds.size()]), null);
			Logger.debug(IUtilTags.TAG_CONTACT_CONTRACT_UTIL, "Got " + studentIdsCursor.getCount() + " course members.");

			final String[] studentIds = new String[studentIdsCursor.getCount()];
			int i = 0;

			if (studentIdsCursor.moveToFirst()) {

				do {

					studentIds[i++] = String.valueOf(studentIdsCursor.getLong(studentIdsCursor
							.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID)));

				} while (studentIdsCursor.moveToNext());
			}

			studentIdsCursor.close();

			if (studentIds.length > 0) {

				return new CursorLoader(context, Contacts.CONTENT_URI,//
						null, //
						ContactsContract.Contacts._ID + " IN (" + CoasyDatabaseHelper.makePlaceholders(studentIds.length) + ")",//
						studentIds,//
						ContactsContract.Contacts.DISPLAY_NAME + " asc");

			} else {

				return new CursorLoader(context, Contacts.CONTENT_URI,//
						null, //
						ContactsContract.Contacts._ID + " = -1",//
						null,//
						ContactsContract.Contacts.DISPLAY_NAME + " asc");
			}

		} else {

			return new CursorLoader(context, Contacts.CONTENT_URI,//
					null, //
					ContactsContract.Contacts._ID + " = -1",//
					null,//
					ContactsContract.Contacts.DISPLAY_NAME + " asc");
		}

	}

	/**
	 * @param context
	 * @param course
	 * @return a {@link CursorLoader} for all contacts of the default
	 *         "My Contacts" group.
	 */
	public static CursorLoader getMyContacts(Context context) {

		Cursor contactIdsCursor = context.getContentResolver().query(//
				ContactsContract.Data.CONTENT_URI,//
				new String[] { //
				ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,//
						ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID },//
				ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + " = ?", //
				new String[] { "1", }, null);

		final String[] contactIds = new String[contactIdsCursor.getCount()];
		int i = 0;

		if (contactIdsCursor.moveToFirst()) {

			do {

				contactIds[i++] = String.valueOf(contactIdsCursor.getLong(contactIdsCursor
						.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID)));

			} while (contactIdsCursor.moveToNext());
		}

		contactIdsCursor.close();

		if (contactIds.length > 0) {

			return new CursorLoader(context, Contacts.CONTENT_URI,//
					null, //
					ContactsContract.Contacts._ID + " IN (" + CoasyDatabaseHelper.makePlaceholders(contactIds.length) + ")",//
					contactIds,//
					ContactsContract.Contacts.DISPLAY_NAME + " asc");

		} else {

			return new CursorLoader(context, Contacts.CONTENT_URI,//
					null, //
					ContactsContract.Contacts._ID + " = -1",//
					null,//
					ContactsContract.Contacts.DISPLAY_NAME + " asc");
		}
	}

	/**
	 * Removes all coasy managed contact groups from the
	 * {@link ContactsContract.Groups}.
	 * 
	 * @param context
	 */
	public static void removeAllCoasyContactGroups(Context context) {

		int deletedCourses;

		if (ICoasySettings.MODE_DEBUG) {

			deletedCourses = context.getContentResolver().delete(//
					ContactsContract.Groups.CONTENT_URI, ""//
							+ ContactsContract.Groups.SHOULD_SYNC + " = ? AND "//
							+ ContactsContract.Groups.GROUP_VISIBLE + " = ? AND "//
							+ ContactsContract.Groups.TITLE + " LIKE ? AND "//
							+ ContactsContract.Groups.ACCOUNT_NAME + " = ? AND "//
							+ ContactsContract.Groups.ACCOUNT_TYPE + " = ? ",//
					new String[] {//
					CoasyDatabaseHelper.SQLITE_VALUE_TRUE,//
							CoasyDatabaseHelper.SQLITE_VALUE_TRUE,//
							CONTACTS_GROUP_TITLE_PREFIX + "%",//
							AccountUtil.getSelectedGoogleAccount(context).name,//
							AccountUtil.ACCOUNT_TYPE_GOOGLE,//
					});
		} else {

			deletedCourses = context.getContentResolver().delete(//
					ContactsContract.Groups.CONTENT_URI, ""//
							+ ContactsContract.Groups.SHOULD_SYNC + " = ? AND "//
							+ ContactsContract.Groups.GROUP_VISIBLE + " = ? AND "//
							+ ContactsContract.Groups.TITLE + " LIKE ? AND "//
							+ ContactsContract.Groups.ACCOUNT_NAME + " = ? AND "//
							+ ContactsContract.Groups.ACCOUNT_TYPE + " = ? ",//
					new String[] {//
					CoasyDatabaseHelper.SQLITE_VALUE_TRUE,//
							CoasyDatabaseHelper.SQLITE_VALUE_FALSE,//
							CONTACTS_GROUP_TITLE_PREFIX + "%",//
							AccountUtil.getSelectedGoogleAccount(context).name,//
							AccountUtil.ACCOUNT_TYPE_GOOGLE,//
					});
		}

		Logger.debug(IUtilTags.TAG_CONTACT_CONTRACT_UTIL, "Removed " + deletedCourses + " groups.");
	}

	/**
	 * Removes all student mappings to all coasy courses.
	 * 
	 * @param context
	 */
	public static void removeAllCourseStudentMappings(Context context) {

		final Cursor coasyContactGroupCursor = getAllCoasyContactGroups(context);

		if (coasyContactGroupCursor.moveToFirst()) {

			do {

				final String groupTitle = coasyContactGroupCursor.getString(coasyContactGroupCursor.getColumnIndexOrThrow(ContactsContract.Groups.TITLE));

				if (isCoasyContactGroup(groupTitle)) {

					final String contactGroupId = groupTitle.split("\\+")[1];
					final int deletedRows = context.getContentResolver().delete(ContactsContract.Data.CONTENT_URI,//
							ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + " = ?",//
							new String[] { contactGroupId, });

					Logger.debug(IUtilTags.TAG_CONTACT_CONTRACT_UTIL, "Removed " + deletedRows + " student mappings for group " + contactGroupId + ".");
				}

			} while (coasyContactGroupCursor.moveToNext());
		}

		coasyContactGroupCursor.close();
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
							CONTACTS_GROUP_TITLE_PREFIX + "%",//
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
							CONTACTS_GROUP_TITLE_PREFIX + "%",//
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
	static long getFirstGroupId(Context context) {

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
