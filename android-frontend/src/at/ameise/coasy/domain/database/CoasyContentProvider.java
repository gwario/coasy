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

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

/**
 * {@link ContentProvider} for all coasy data.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public final class CoasyContentProvider extends ContentProvider {

	// database
	private CoasyDatabaseHelper database;

	private static final String AUTHORITY = "at.ameise.coasy.contentprovider";

	// used for the UriMacher ids
	private static final int COURSES = 0x001;
	private static final int COURSE_ID = 0x002;

	private static final String BASE_PATH_COURSES = "courses";
	public static final Uri CONTENT_URI_COURSES = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH_COURSES);

	public static final String CONTENT_TYPE_COURSES = ContentResolver.CURSOR_DIR_BASE_TYPE + "/courses";
	public static final String CONTENT_ITEM_TYPE_COURSE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/course";

	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		/*
		 * Uri to work on all courses
		 */
		sURIMatcher.addURI(AUTHORITY, BASE_PATH_COURSES, COURSES);
		
		/*
		 * Uri to work on on specific course
		 */
		sURIMatcher.addURI(AUTHORITY, BASE_PATH_COURSES + "/#", COURSE_ID);
	}

	@Override
	public boolean onCreate() {
		database = new CoasyDatabaseHelper(getContext());
		return false;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {

		final SQLiteDatabase sqlDb = database.getWritableDatabase();
		final int uriType = sURIMatcher.match(uri);

		int rowsDeleted = 0;

		switch (uriType) {

		case COURSES:
			rowsDeleted = sqlDb.delete(CourseTable.TABLE_NAME, selection, selectionArgs);
			break;

		case COURSE_ID:
			final String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = sqlDb.delete(CourseTable.TABLE_NAME, CourseTable.COL_ID + " = " + id, null);
			} else {
				rowsDeleted = sqlDb.delete(CourseTable.TABLE_NAME, CourseTable.COL_ID + " = " + id + " AND " + selection, selectionArgs);
			}
			break;

		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);

		return rowsDeleted;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		final SQLiteDatabase sqlDB = database.getWritableDatabase();
		final int uriType = sURIMatcher.match(uri);

		long id = 0;

		switch (uriType) {

		case COURSES:
			id = sqlDB.insert(CourseTable.TABLE_NAME, null, values);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);

		return Uri.parse(BASE_PATH_COURSES + "/" + id);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

		final SQLiteDatabase sqlDB = database.getWritableDatabase();
		final int uriType = sURIMatcher.match(uri);

		int rowsUpdated = 0;

		switch (uriType) {

		case COURSES:
			rowsUpdated = sqlDB.update(CourseTable.TABLE_NAME, values, selection, selectionArgs);
			break;

		case COURSE_ID:
			final String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update(CourseTable.TABLE_NAME, values, CourseTable.COL_ID + " = " + id, null);
			} else {
				rowsUpdated = sqlDB.update(CourseTable.TABLE_NAME, values, CourseTable.COL_ID + " = " + id + " AND " + selection, selectionArgs);
			}
			break;

		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);

		return rowsUpdated;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

		final SQLiteDatabase db = database.getWritableDatabase();
		final int uriType = sURIMatcher.match(uri);
		
		final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(CourseTable.TABLE_NAME);

		switch (uriType) {
		
		case COURSES:
			checkCourseColumns(projection);
			break;
			
		case COURSE_ID:
			checkCourseColumns(projection);
			queryBuilder.appendWhere(CourseTable.COL_ID + " = " + uri.getLastPathSegment());
			break;
			
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		final Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	/**
	 * Checks if the projection only uses the available columns.
	 * 
	 * @param projection
	 */
	private void checkCourseColumns(String[] projection) {

		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(CourseTable.ALL_COLUMNS));
			// check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException("Unknown columns in projection");
			}
		}
	}
}
