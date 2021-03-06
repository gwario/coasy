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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import at.ameise.coasy.domain.CourseStudent;
import at.ameise.coasy.exception.DatabaseError;
import at.ameise.coasy.util.ReflectionUtil;

/**
 * Contains definitions for the course student mappign table.<br>
 * <br>
 * NOTE: This class and its methods should only be visible to the database
 * package! Mind the visibility of constants!
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public final class CourseStudentTable {

	private static final int INITIAL_SCHEMA = 0x00000;
	private static final int SCHEMA_MASK = 0x01100;

	static final int SCHEMA_VERSION = INITIAL_SCHEMA;

	static final String COL_ID = "_id";
	public static final String COL_STUDENT_ID = "studentid";
	public static final String COL_COURSE_ID = "courseid";

	public static final String TABLE_NAME = "coursestudent";

	private static final String CREATE_STATEMENT = "CREATE TABLE " + TABLE_NAME + " ( " //
			+ COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "//
			+ COL_STUDENT_ID + " INTEGER NOT NULL, "//
			+ COL_COURSE_ID + " INTEGER NOT NULL"//
			+ " );";

	private static final String DROP_STATEMENT = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";

	public static final String[] ALL_COLUMNS = { COL_ID, COL_STUDENT_ID, COL_COURSE_ID, };

	/**
	 * @param coursestudent
	 * @return the {@link ContentValues} of student.
	 */
	static ContentValues from(CourseStudent coursestudent) {

		ContentValues values = new ContentValues();

		try {

			values.put(COL_ID, (Long) ReflectionUtil.getFieldValue(coursestudent, "id"));
			values.put(COL_COURSE_ID, coursestudent.getCourseId());
			values.put(COL_STUDENT_ID, coursestudent.getContactId());

			return values;

		} catch (NoSuchFieldException e) {

			throw new DatabaseError("Failed to get the value of the id field!", e);

		} catch (IllegalAccessException e) {

			throw new DatabaseError("Failed to get the value of the id field!", e);

		} catch (IllegalArgumentException e) {

			throw new DatabaseError("Failed to get the value of the id field!", e);
		}
	}

	/**
	 * @param cursor
	 * @return the {@link CourseStudent} object from the current cursor position.
	 */
	public static CourseStudent from(Cursor cursor) {

		return new CourseStudent(//
				cursor.getLong(cursor.getColumnIndexOrThrow(CourseStudentTable.COL_ID)),//
				cursor.getLong(cursor.getColumnIndexOrThrow(CourseStudentTable.COL_COURSE_ID)),//
				cursor.getLong(cursor.getColumnIndexOrThrow(CourseStudentTable.COL_STUDENT_ID)));
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

		db.execSQL(CourseStudentTable.CREATE_STATEMENT);
	}

	/**
	 * Drops the course table.
	 * 
	 * @param db
	 */
	private static void drop(SQLiteDatabase db) {

		db.execSQL(CourseStudentTable.DROP_STATEMENT);
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

}
