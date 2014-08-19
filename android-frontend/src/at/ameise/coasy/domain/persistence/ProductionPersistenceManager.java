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

import org.apache.commons.lang3.ArrayUtils;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import at.ameise.coasy.domain.Course;
import at.ameise.coasy.domain.Student;
import at.ameise.coasy.domain.persistence.database.CoasyDatabaseHelper;
import at.ameise.coasy.exception.AbstractContactsException;
import at.ameise.coasy.exception.AbstractDatabaseException;
import at.ameise.coasy.exception.CreateDatabaseException;
import at.ameise.coasy.util.AccountUtil;
import at.ameise.coasy.util.CursorIterator;
import at.ameise.coasy.util.Logger;

/**
 * Production implementation of the {@link IPersistenceManager}.<br>
 * <br>
 * This implementation stores the courses as contact groups and the master copy
 * of the course in the contact groups notes column as json object.<br>
 * <br>
 * For performance reasons, a single database is kept to hold all coasy data. It
 * is a copy of the data optimized for fast data retrieval and cross table
 * joins. All modification operations operate on both data sources. The read
 * operations operate on the performance database.<br>
 * <br>
 * It is encouraged to do a periodical "write-back" from the contacts to the
 * performance database. And also a, maybe synchronous, write-back on
 * application start. <br>
 * <br>
 * If its possible to receive broadcasts when google is synchronizing its
 * contacts, a write-back afterwards would be optimal.<br>
 * <br>
 * Further more, all modification operations are synchronized methods.
 * 
 * TODO make transactional
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public final class ProductionPersistenceManager implements IPersistenceManager {

	private static final String TAG = "ProdPersistenceMgr";

	private static IPersistenceManager instance = null;

	/**
	 * @param context
	 *            internally, the {@link Context#getApplicationContext()} is
	 *            uses.
	 * @return a singleton instance of the {@link ProductionPersistenceManager}.
	 */
	public static final IPersistenceManager getInstance(Context context) {

		if (instance == null)
			instance = new ProductionPersistenceManager(context);

		return instance;
	}

	/**
	 * The application context.
	 */
	private Context mContext;

	private ProductionPersistenceManager(Context context) {
		mContext = context;
	}

	@Override
	public Loader<Cursor> allStudentsCursorLoader() {

		return PerformanceDatabaseHelper.getAllStudentsCursorLoader(mContext);
	}

	@Override
	public Loader<Cursor> allCoursesCursorLoader() {

		return PerformanceDatabaseHelper.getAllCoursesCursorLoader(mContext);
	}

	@Override
	public synchronized boolean create(Course course) {

		if (course.getId() > -1)
			throw new IllegalArgumentException("Course has already an id!");

		try {

			ContactsHelper.createContactGroup(mContext, course);

			PerformanceDatabaseHelper.createCourse(mContext, course);

			return true;

		} catch (AbstractContactsException e) {

			Logger.error(TAG, "Failed to create contacts group!", e);

		} catch (CreateDatabaseException e) {

			Logger.error(TAG, "Failed to create course!", e);
		}

		return false;
	}

	@Override
	public Loader<Cursor> courseCursorLoader(long id) {

		return ContactsHelper.getCourseCursorLoader(mContext, id);
	}

	// @Override
	// public Loader<Cursor> contactsCursorLoader() {
	//
	// Cursor contactIdsCursor =
	// ContactsHelper.getContactIdsOfGroupCursor(mContext,
	// AccountUtil.getSelectedGroup(mContext));
	//
	// final String[] contactIds = new String[contactIdsCursor.getCount()];
	// new CursorIterator(contactIdsCursor) {
	// @Override
	// protected void next(int index, Cursor cursor) {
	//
	// contactIds[index] = String
	// .valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID)));
	// }
	// }.iterate();
	//
	// if (contactIds.length > 0) {
	//
	// return new CursorLoader(mContext, Contacts.CONTENT_URI,//
	// null, //
	// ContactsContract.Contacts._ID + " IN (" +
	// CoasyDatabaseHelper.makePlaceholders(contactIds.length) + ")",//
	// contactIds,//
	// ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " asc");
	//
	// } else {
	//
	// return new CursorLoader(mContext, Contacts.CONTENT_URI,//
	// null, //
	// ContactsContract.Contacts._ID + " = -1",//
	// null,//
	// ContactsContract.Contacts.DISPLAY_NAME + " asc");
	// }
	// }

	@Override
	public Loader<Cursor> contactsNotInCourseCursorLoader(long courseId) {

		Cursor studentIdsCursor = ContactsHelper.getContactIdsOfGroupCursor(mContext, String.valueOf(courseId));

		final String[] studentIds = new String[studentIdsCursor.getCount()];
		new CursorIterator(studentIdsCursor) {
			@Override
			protected void next(int index, Cursor cursor) {

				studentIds[index] = String
						.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID)));
			}
		}.iterate();

		Cursor contactIdsCursor = ContactsHelper.getContactIdsOfGroupCursor(mContext, AccountUtil.getSelectedGroup(mContext));

		final String[] contactIds = new String[contactIdsCursor.getCount()];
		new CursorIterator(contactIdsCursor) {
			@Override
			protected void next(int index, Cursor cursor) {

				contactIds[index] = String
						.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID)));
			}
		}.iterate();

		if (contactIds.length > 0) {

			String selection = null;
			if (studentIds.length > 0) {

				selection = ContactsContract.Contacts._ID + " IN (" + CoasyDatabaseHelper.makePlaceholders(contactIds.length) + ") AND "
						+ ContactsContract.Contacts._ID + " NOT IN (" + CoasyDatabaseHelper.makePlaceholders(studentIds.length) + ")";

			} else {

				selection = ContactsContract.Contacts._ID + " IN (" + CoasyDatabaseHelper.makePlaceholders(contactIds.length) + ")";
			}

			return new CursorLoader(mContext, Contacts.CONTENT_URI,//
					null, //
					selection,//
					ArrayUtils.addAll(contactIds, studentIds),//
					ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " asc");

		} else {

			return new CursorLoader(mContext, Contacts.CONTENT_URI,//
					null, //
					ContactsContract.Contacts._ID + " = -1",//
					null,//
					ContactsContract.Contacts.DISPLAY_NAME + " asc");
		}
	}

	@Override
	public synchronized boolean removeStudentFromCourse(long contactId, long courseId) {

		ContactsHelper.removeContactFromGroup(mContext, contactId, courseId);

		PerformanceDatabaseHelper.removeStudentFromCourse(mContext, contactId, courseId);

		return true;
	}

	@Override
	public synchronized boolean addStudentToCourse(long contactId, long courseId) {

		try {

			Student student = ContactsHelper.getContactAsStudent(mContext, contactId);

			if (!PerformanceDatabaseHelper.doesStudentExist(mContext, contactId)) {

				Logger.debug(TAG, "There is no student for the contact, creating it!");
				PerformanceDatabaseHelper.createStudent(mContext, student);
			}

			ContactsHelper.addContactToGroup(mContext, ContactsHelper.getRawContactId(mContext, contactId), courseId);

			PerformanceDatabaseHelper.addStudentToCourse(mContext, contactId, courseId);

			return true;

		} catch (AbstractDatabaseException e) {

			Logger.error(TAG, "Failed to add student to course!", e);
		}

		return false;
	}

	@Override
	public synchronized boolean createStudentContact(long contactId) {

		try {

			Student student = ContactsHelper.getContactAsStudent(mContext, contactId);

			PerformanceDatabaseHelper.createStudent(mContext, student);

			return true;

		} catch (CreateDatabaseException e) {

			Logger.error(TAG, "Failed to add student from contact!");
		}

		return false;
	}

	@Override
	public Loader<Cursor> studentsInCourseCoursorLoader(long courseId) {

		return PerformanceDatabaseHelper.getStudentsCursorLoader(mContext, courseId);
	}

}
