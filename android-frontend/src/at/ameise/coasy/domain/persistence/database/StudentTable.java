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
package at.ameise.coasy.domain.persistence.database;

import java.util.Date;
import java.util.HashMap;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import at.ameise.coasy.domain.Course;
import at.ameise.coasy.domain.Student;
import at.ameise.coasy.exception.DatabaseError;
import at.ameise.coasy.util.ReflectionUtil;

/**
 * Contains definitions for the student table.<br>
 * <br>
 * NOTE: This class and its methods should only be visible to the database
 * package! Mind the visibility of constants!
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public final class StudentTable {

	private static final int INITIAL_SCHEMA = 0x000000;
	private static final int SCHEMA_MASK = 0x01100;

	static final int SCHEMA_VERSION = INITIAL_SCHEMA;

	/**
	 * id of the corresponding contact.
	 */
	public static final String COL_ID = "_id";
	public static final String COL_DISPLAY_NAME = "displayname";
	public static final String COL_CONTACT_NAME = "nameofcontact";
	public static final String COL_DAY_OF_BIRTH = "dayofbirth";
	public static final String COL_EMAIL = "email";
	public static final String COL_PHONE = "phone";
	public static final String COL_ADDRESS = "address";

	public static final String SORT_ORDER_DISPLAY_NAME_DESC = COL_DISPLAY_NAME + " desc";
	public static final String SORT_ORDER_DISPLAY_NAME_ASC = COL_DISPLAY_NAME + " asc";

	public static final String TABLE_NAME = "student";

	private static final String CREATE_STATEMENT = "CREATE TABLE " + TABLE_NAME + " ( " //
			+ COL_ID + " INTEGER PRIMARY KEY, "//
			+ COL_DISPLAY_NAME + " TEXT NOT NULL, "//
			+ COL_DAY_OF_BIRTH + " INTEGER, "//
			+ COL_CONTACT_NAME + " TEXT, "//
			+ COL_EMAIL + " TEXT, "//
			+ COL_PHONE + " TEXT, "//
			+ COL_ADDRESS + " TEXT"//
			+ " );";

	private static final String DROP_STATEMENT = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";

	public static final String[] ALL_COLUMNS = { COL_ID, COL_DISPLAY_NAME, COL_DAY_OF_BIRTH, COL_CONTACT_NAME, COL_EMAIL, COL_PHONE, COL_ADDRESS };

	/**
	 * @param course
	 * @return the {@link ContentValues} of student.
	 */
	public static ContentValues from(Student student) {

		ContentValues values = new ContentValues();

		values.put(COL_ID, student.getId());
		values.put(COL_DISPLAY_NAME, student.getDisplayName());
		values.put(COL_DAY_OF_BIRTH, student.getDayOfBirth() != null ? student.getDayOfBirth().getTime() : -1);
		values.put(COL_CONTACT_NAME, student.getContactName());
		values.put(COL_EMAIL, "student.getEmail()");
		values.put(COL_PHONE, "student.getPhone()");
		values.put(COL_ADDRESS, student.getAddress());

		return values;
	}

	/**
	 * Upgrades the schema of the course table if necessary.
	 * 
	 * @param db
	 * @param oldDatabaseVersion
	 *            the old overall database version.
	 * @param newDatabaseVersion
	 *            the new overall database version.
	 */
	static void upgrade(SQLiteDatabase db, int oldDatabaseVersion, int newDatabaseVersion) {

		final int oldTableSchemaVersion = oldDatabaseVersion & SCHEMA_MASK;
		final int newTableSchemaVersion = newDatabaseVersion & SCHEMA_MASK;

		/*
		 * TODO add if blocks like this if(oldTableSchemaVersion ==
		 * INITIAL_SCHEMA) upgradeFromInitialSchema(newTableSchemaVersion); //do
		 * the actual upgrade else throw new NotImplementedError(
		 * "The schema has changed but there was now upgrade routine implemented"
		 * )
		 */

		drop(db);
		create(db);
	}

	/**
	 * Creates the course table.
	 * 
	 * @param db
	 */
	static void create(SQLiteDatabase db) {

		db.execSQL(StudentTable.CREATE_STATEMENT);
	}

	/**
	 * Drops the course table.
	 * 
	 * @param db
	 */
	private static void drop(SQLiteDatabase db) {

		db.execSQL(StudentTable.DROP_STATEMENT);
	}

	/**
	 * Drops and creates the course table.
	 * 
	 * @param db
	 */
	static void reCreate(SQLiteDatabase db) {

		drop(db);
		create(db);
	}

	/**
	 * This cursor contains only the id and the
	 * {@link android.provider.ContactsContract.ContactNameColumns.DISPLAY_NAME_PRIMARY}
	 * It is supposed to be used in a "all contacts" List to choose which one to
	 * add to a {@link Course}.
	 * 
	 * @param c
	 *            cursor on the {@link ContactsContract.Groups}
	 * @return the {@link Student} from the c.
	 */
	public static Student fromContactsCursor(Cursor c) {

		Student student = new Student(c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)),//
				null,//
				null,//
				null,//
				null,//
				null);
		try {

			ReflectionUtil.setFieldValue(student, "id", c.getLong(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID)));

		} catch (NoSuchFieldException e) {

			throw new DatabaseError("Failed to set the id field of student!", e);

		} catch (IllegalAccessException e) {

			throw new DatabaseError("Failed to set the id field of student!", e);

		} catch (IllegalArgumentException e) {

			throw new DatabaseError("Failed to set the id field of student!", e);
		}

		return student;
	}

	/**
	 * @param c
	 *            cursor on the {@link StudentTable}
	 * @return the {@link Student} from the c.
	 */
	public static Student fromStudentsCursor(Cursor c) {

		Student student = new Student(//
				c.getString(c.getColumnIndexOrThrow(COL_DISPLAY_NAME)),//
				new Date(c.getLong(c.getColumnIndexOrThrow(COL_DAY_OF_BIRTH))),//
				c.getString(c.getColumnIndexOrThrow(COL_CONTACT_NAME)),//
				new HashMap<String, String>(),// c.getString(c.getColumnIndexOrThrow(COL_DISPLAY_NAME),//
				new HashMap<String, String>(),// c.getString(c.getColumnIndexOrThrow(COL_DISPLAY_NAME),//
				c.getString(c.getColumnIndexOrThrow(COL_ADDRESS))//
		);

		try {

			ReflectionUtil.setFieldValue(student, "id", c.getLong(c.getColumnIndexOrThrow(COL_ID)));

		} catch (NoSuchFieldException e) {

			throw new DatabaseError("Failed to set the id field of course!", e);

		} catch (IllegalAccessException e) {

			throw new DatabaseError("Failed to set the id field of course!", e);

		} catch (IllegalArgumentException e) {

			throw new DatabaseError("Failed to set the id field of course!", e);
		}

		return student;
	}

	/**
	 * @param c
	 *            cursor on the {@link StudentTable}
	 * @return only the id of the {@link Student} from the c.
	 */
	public static long idFromCoursesCursor(Cursor c) {

		return c.getLong(c.getColumnIndexOrThrow(COL_ID));
	}
}
