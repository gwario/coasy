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
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import at.ameise.coasy.domain.Course;
import at.ameise.coasy.exception.DatabaseError;
import at.ameise.coasy.util.Logger;
import at.ameise.coasy.util.ReflectionUtil;

/**
 * Contains definitions for the course table.<br>
 * <br>
 * NOTE: This class and its methods should only be visible to the database
 * package! Mind the visibility of constants!
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public final class CourseTable {

	private static final String TAG = "CourseTable";
	
	private static final int INITIAL_SCHEMA = 0x000000;
	private static final int SCHEMA_MASK = 0x00011;

	static final int SCHEMA_VERSION = INITIAL_SCHEMA;

	/**
	 * id of the corresponding contact group.
	 */
	public static final String COL_ID = "_id";
	public static final String COL_TITLE = "title";
	public static final String COL_DESCRIPTION = "description";
	public static final String COL_ADDRESS = "address";

	public static final String SORT_ORDER_TITLE_DESC = COL_TITLE + " desc";
	public static final String SORT_ORDER_TITLE_ASC = COL_TITLE + " asc";

	public static final String TABLE_NAME = "course";

	private static final String CREATE_STATEMENT = "CREATE TABLE " + TABLE_NAME + " ( " //
			+ COL_ID + " INTEGER PRIMARY KEY, "//
			+ COL_TITLE + " TEXT NOT NULL, "//
			+ COL_DESCRIPTION + " TEXT, "//
			+ COL_ADDRESS + " TEXT"//
			+ " );";

	private static final String DROP_STATEMENT = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";

	public static final String[] ALL_COLUMNS = { COL_ID, COL_TITLE, COL_DESCRIPTION, COL_ADDRESS, };

	/**
	 * @param course
	 * @return the {@link ContentValues} of course.
	 */
	public static ContentValues from(Course course) {

		final ContentValues values = new ContentValues();

		try {

			values.put(COL_ID, course.getId());
			values.put(COL_TITLE, course.getTitle());
			values.put(COL_DESCRIPTION, course.getDescription());
			values.put(COL_ADDRESS, course.getAddress());

			return values;

		} catch (IllegalArgumentException e) {

			throw new DatabaseError("Failed to get the value of the id field!", e);
		}
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
		Logger.debug(TAG, "Upgrading Course table from version "+oldDatabaseVersion+" to "+newDatabaseVersion);
		
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
		Logger.debug(TAG, "Creating Course table");
		
		db.execSQL(CourseTable.CREATE_STATEMENT);
	}

	/**
	 * Drops the course table.
	 * 
	 * @param db
	 */
	private static void drop(SQLiteDatabase db) {
		Logger.debug(TAG, "Dropping Course table");
		
		db.execSQL(CourseTable.DROP_STATEMENT);
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
	 * @param c
	 *            cursor on the {@link ContactsContract.Groups}
	 * @return the {@link Course} from the c.
	 */
	public static Course fromContactsCursor(Cursor c) {

		Logger.debug(TAG, "Converting cursor to course: " +DatabaseUtils.dumpCurrentRowToString(c));
		
		Course course = CoasyDatabaseHelper.fromEscapedJson(c.getString(c.getColumnIndexOrThrow(ContactsContract.Groups.NOTES)), Course.class);

		return course;
	}

	/**
	 * @param c
	 *            cursor on the {@link CourseTable}
	 * @return the {@link Course} from the c.
	 */
	public static Course fromCoursesCursor(Cursor c) {

		Course course = new Course(//
				c.getString(c.getColumnIndexOrThrow(COL_TITLE)),//
				c.getString(c.getColumnIndexOrThrow(COL_DESCRIPTION)),//
				c.getString(c.getColumnIndexOrThrow(COL_ADDRESS)));

		try {

			ReflectionUtil.setFieldValue(course, "id", c.getLong(c.getColumnIndexOrThrow(COL_ID)));

		} catch (NoSuchFieldException e) {

			throw new DatabaseError("Failed to set the id field of course!", e);

		} catch (IllegalAccessException e) {

			throw new DatabaseError("Failed to set the id field of course!", e);

		} catch (IllegalArgumentException e) {

			throw new DatabaseError("Failed to set the id field of course!", e);
		}

		return course;
	}

	/**
	 * @param c
	 *            cursor on the {@link CourseTable}
	 * @return only the id of the {@link Course} from the c.
	 */
	public static long idFromCoursesCursor(Cursor c) {

		return c.getLong(c.getColumnIndexOrThrow(COL_ID));
	}
}
