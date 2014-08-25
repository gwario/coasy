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
import java.util.List;

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
import at.ameise.coasy.domain.persistence.database.CourseTable;
import at.ameise.coasy.exception.AbstractContactsException;
import at.ameise.coasy.exception.AbstractDatabaseException;
import at.ameise.coasy.exception.CreateDatabaseException;
import at.ameise.coasy.util.CursorIterator;
import at.ameise.coasy.util.Logger;
import at.ameise.coasy.util.SettingsUtil;

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

		return DatabaseHelper.getAllStudentsCursorLoader(mContext);
	}

	@Override
	public Loader<Cursor> allCoursesCursorLoader() {

		return DatabaseHelper.getAllCoursesCursorLoader(mContext);
	}

	@Override
	public synchronized boolean create(Course course) {

		if (course.getId() > -1)
			throw new IllegalArgumentException("Course has already an id!");

		try {

			ContactsContractHelper.createContactGroup(mContext, course);

			DatabaseHelper.createCourse(mContext, course);

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

		return ContactsContractHelper.getCourseCursorLoader(mContext, id);
	}

	@Override
	public Loader<Cursor> contactsNotInCourseCursorLoader(long courseId) {

		Cursor studentIdsCursor = ContactsContractHelper.getContactIdsOfGroupCursor(mContext, courseId);

		final String[] studentIds = new String[studentIdsCursor.getCount()];
		new CursorIterator(studentIdsCursor) {
			@Override
			protected void next(int index, Cursor cursor) {

				studentIds[index] = String
						.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID)));
			}
		}.iterate();

		Cursor contactIdsCursor = ContactsContractHelper.getContactIdsOfGroupCursor(mContext, SettingsUtil.getSelectedGroup(mContext));

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

		ContactsContractHelper.removeContactFromGroup(mContext, contactId, courseId);

		DatabaseHelper.removeStudentFromCourse(mContext, contactId, courseId);

		return true;
	}

	@Override
	public synchronized boolean addStudentToCourse(long contactId, long courseId) {

		try {

			Student student = ContactsContractHelper.getContactAsStudent(mContext, contactId);

			if (!DatabaseHelper.doesStudentExist(mContext, contactId)) {

				Logger.debug(TAG, "There is no student for the contact, creating it!");
				DatabaseHelper.createStudent(mContext, student);
			}

			ContactsContractHelper.addContactToGroup(mContext, contactId, courseId);

			DatabaseHelper.addStudentToCourse(mContext, contactId, courseId);

			return true;

		} catch (AbstractDatabaseException e) {

			Logger.error(TAG, "Failed to add student to course!", e);
		}

		return false;
	}

	@Override
	public synchronized boolean createStudent(long contactId) {

		try {

			Student student = ContactsContractHelper.getContactAsStudent(mContext, contactId);

			DatabaseHelper.createStudent(mContext, student);

			return true;

		} catch (CreateDatabaseException e) {

			Logger.error(TAG, "Failed to add student from contact!");
		}

		return false;
	}

	@Override
	public Loader<Cursor> studentsInCourseCoursorLoader(long courseId) {

		return DatabaseHelper.getStudentsCursorLoader(mContext, courseId);
	}

	@Override
	public boolean save(Course course) {

		if (course.getId() < 0)
			throw new IllegalArgumentException("Course has no id!");

		try {
			
			ContactsContractHelper.updateContactGroup(mContext, course);
			
			DatabaseHelper.updateCourse(mContext, course);
			
			return true;
			
		} catch (AbstractContactsException e) {

			Logger.error(TAG, "Failed to update contacts group!", e);

		} catch (AbstractDatabaseException e) {

			Logger.error(TAG, "Failed to update course!", e);
		}

		return false;
	}

	@Override
	public void refreshDatabaseFromContacts() throws AbstractDatabaseException {

		//check if coasy data exists
		if(!SettingsUtil.isAccountSelected(mContext)) {
			
			Logger.info(TAG, "No account selected, trying to recreate settings.");

			//get settings object from groups
			Cursor coasySettingsCursor = SettingsUtil.getCoasySettingsGroup(mContext);
			if(coasySettingsCursor.moveToFirst()) {
				
				Logger.info(TAG, "Coasy data found, recovering settings.");
				SettingsUtil.saveAsPreferences(mContext, coasySettingsCursor);
				
			} else {
				
				Logger.info(TAG, "No account found, aborting recreate since coasy was not installed previously!");
				return;
			}
			
			coasySettingsCursor.close();
			
		} else {
			
			Logger.info(TAG, "Coasy account setting found, refreshing database.");
		}
		
		Logger.info(TAG, "Reloading courses and students.");
		
		//get all coasy groups
		Cursor coasyGroupCursor = ContactsContractHelper.getAllCoasyGroups(mContext);
		if(coasyGroupCursor.moveToFirst()) {
			
			final List<String> courseIds = new ArrayList<String>();
			final List<String> studentIds = new ArrayList<String>();
			
			do {
				long groupRowId = coasyGroupCursor.getLong(coasyGroupCursor.getColumnIndex(ContactsContract.Groups._ID));
				courseIds.add(String.valueOf(groupRowId));
				
				Course course = CourseTable.fromContactsCursor(coasyGroupCursor);
				
				//create or update the groups in the database
				if(DatabaseHelper.doesCourseExist(mContext, groupRowId)) {

					Logger.debug(TAG, "Course '"+course.getTitle()+"' does exist, updating it.");
					//update course
					DatabaseHelper.updateCourse(mContext, course);
					
				} else {

					Logger.debug(TAG, "Course '"+course.getTitle()+"' does not exist, creating it.");
					//create course
					DatabaseHelper.createCourse(mContext, course);
				}
				
				//get the students from these groups
				Cursor contactsOfGroupCursor = ContactsContractHelper.getContactIdsOfGroupCursor(mContext, course.getId());
				if(contactsOfGroupCursor.moveToFirst()) {
					
					do {
						
						long contactId = contactsOfGroupCursor.getLong(contactsOfGroupCursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID));
						studentIds.add(String.valueOf(contactId));
						
						if(DatabaseHelper.doesStudentExist(mContext, contactId)) {
							
							Logger.debug(TAG, "Student does exist, updating it.");
							DatabaseHelper.updateStudent(mContext, ContactsContractHelper.getContactAsStudent(mContext, contactId));
							
						} else {
							
							Logger.debug(TAG, "Student does not yet exist, creating it.");
							createStudent(contactId);
						}
						
						//add all students from the course
						DatabaseHelper.addStudentToCourse(mContext, contactId, groupRowId);
						
					} while(contactsOfGroupCursor.moveToNext());
				}
				
				contactsOfGroupCursor.close();
				
			} while(coasyGroupCursor.moveToNext());
			
			Logger.debug(TAG, "Removing obsolete courses and students.");
			DatabaseHelper.removeAllCoursesExcept(mContext, courseIds);
			DatabaseHelper.removeAllStudentsExcept(mContext, studentIds);
			
		} else {
			
			Logger.info(TAG, "No coasy groups found.");
			Logger.debug(TAG, "Removing all courses and students.");
			DatabaseHelper.removeAllCourses(mContext);
			DatabaseHelper.removeAllStudents(mContext);
		}
		
		coasyGroupCursor.close();
	}

}
