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
package at.ameise.coasy.domain.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import at.ameise.coasy.ICoasySettings;
import at.ameise.coasy.domain.content.CourseContent;

/**
 * Provides lifecycle management operations for the database.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public final class CoasyDatabaseHelper extends SQLiteOpenHelper {

	/**
	 * Name of the database file.
	 */
	private static final String DATABASE_NAME = "coasy.db";

	/**
	 * The version of the schema. This is the logical conjunction (&) of every
	 * tables schema.
	 */
	private static final int SCHEMA_VERSION = CourseTable.SCHEMA_VERSION;

	/**
	 * See {@link CoasyDatabaseHelper#SCHEMA_VERSION}
	 */
	private static final int DATABASE_VERSION = 1 + SCHEMA_VERSION;

	public CoasyDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		CourseTable.create(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		if (ICoasySettings.MODE_DEBUG) {
			
			CourseTable.reCreate(db);
			CourseTable.insertDebugData(db, CourseContent.demoCourses());

		} else if (oldVersion < newVersion) {

			CourseTable.upgrade(db, oldVersion, newVersion);
		}
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);

		if (ICoasySettings.MODE_DEBUG) {
			
			CourseTable.reCreate(db);
			CourseTable.insertDebugData(db, CourseContent.demoCourses());
		}
	}

}
