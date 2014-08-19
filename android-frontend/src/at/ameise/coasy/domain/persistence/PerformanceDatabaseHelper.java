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
import android.net.Uri;
import at.ameise.coasy.domain.Course;
import at.ameise.coasy.domain.Student;
import at.ameise.coasy.domain.persistence.database.CourseStudentTable;
import at.ameise.coasy.domain.persistence.database.CourseTable;
import at.ameise.coasy.domain.persistence.database.PerformanceDatabaseContentProvider;
import at.ameise.coasy.domain.persistence.database.StudentTable;
import at.ameise.coasy.exception.CreateDatabaseException;
import at.ameise.coasy.exception.DatabaseError;

/**
 * Contains helper methods for courses.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public final class PerformanceDatabaseHelper {

	private PerformanceDatabaseHelper() {
	}

	/**
	 * Creates the given {@link Course} in the database.<br>
	 * <br>
	 * Note: This method does not check if the course does already exist!
	 * 
	 * @param context
	 * @param course
	 * @throws CreateDatabaseException
	 */
	public static void createCourse(Context context, Course course) throws CreateDatabaseException {

		if (course.getId() < 0)
			throw new CreateDatabaseException("Failed to create contact group for " + course);

		final ContentValues values = CourseTable.from(course);

		final Uri returnUri = context.getContentResolver().insert(PerformanceDatabaseContentProvider.CONTENT_URI_COURSE, values);
		final long id = Long.parseLong(returnUri.getLastPathSegment());

		if (id != course.getId()) {

			throw new CreateDatabaseException("Insert of Course failed!");
		}
	}

	/**
	 * @param context
	 * @return a {@link CursorLoader} on all {@link Student}s of all
	 *         {@link Course}s.
	 */
	public static Loader<Cursor> getAllStudentsCursorLoader(Context context) {

		return new CursorLoader(context, PerformanceDatabaseContentProvider.CONTENT_URI_STUDENT, null, null, null, StudentTable.SORT_ORDER_DISPLAY_NAME_ASC);
	}

	/**
	 * @param context
	 * @return a {@link CursorLoader} on all {@link Course}s.
	 */
	public static Loader<Cursor> getAllCoursesCursorLoader(Context context) {

		return new CursorLoader(context, PerformanceDatabaseContentProvider.CONTENT_URI_COURSE, null, null, null, CourseTable.SORT_ORDER_TITLE_ASC);
	}

	/**
	 * Adds the student to the course.<br>
	 * <br>
	 * Note: This method does not check if the mapping does already exist!
	 * 
	 * @param context
	 * @param studentId
	 * @param courseId
	 */
	public static void addStudentToCourse(Context context, long studentId, long courseId) {

		ContentValues values = new ContentValues();
		values.put(CourseStudentTable.COL_STUDENT_ID, studentId);
		values.put(CourseStudentTable.COL_COURSE_ID, courseId);

		final Uri returnUri = context.getContentResolver()
				.insert(PerformanceDatabaseContentProvider.getCONTENT_URI_COURSE_STUDENT(courseId, studentId), values);
		final long rowId = Long.valueOf(returnUri.getLastPathSegment());

		if (rowId < 0)
			throw new DatabaseError("Failed to insert the student-course mapping in CourseStudentTable!");
	}

	/**
	 * Creates the student.<br>
	 * <br>
	 * Note: This method does not check if the student does already exist!
	 * 
	 * @param context
	 * @param student
	 * @throws CreateDatabaseException
	 */
	public static void createStudent(Context context, Student student) throws CreateDatabaseException {

		final ContentValues values = StudentTable.from(student);

		final Uri returnUri = context.getContentResolver().insert(Uri.parse(PerformanceDatabaseContentProvider.CONTENT_URI_STUDENT + "/" + student.getId()),
				values);
		final long id = Long.parseLong(returnUri.getLastPathSegment());

		if (id != student.getId()) {

			throw new CreateDatabaseException("Insert of Student failed!");
		}
	}

	/**
	 * @param context
	 * @param studentId
	 * @return true if the student with the specified id exits, false otherwise.
	 */
	public static boolean doesStudentExist(Context context, long studentId) {

		Cursor studentCursor = context.getContentResolver().query(PerformanceDatabaseContentProvider.CONTENT_URI_STUDENT,//
				null,//
				StudentTable.COL_ID + " = ?",//
				new String[] { String.valueOf(studentId), },//
				null);

		boolean studentExists = studentCursor.moveToFirst();

		studentCursor.close();

		return studentExists;
	}

	/**
	 * @param context
	 * @param courseId
	 * @return a {@link CursorLoader} on all {@link Student}s of the specified
	 *         {@link Course}s.
	 */
	public static Loader<Cursor> getStudentsCursorLoader(Context context, long courseId) {

		return new CursorLoader(context, PerformanceDatabaseContentProvider.getCONTENT_URI_COURSE_STUDENTS(courseId), null, null, null, StudentTable.SORT_ORDER_DISPLAY_NAME_ASC);
	}

	/**
	 * @param context
	 * @param studentId
	 * @param courseId
	 */
	public static void removeStudentFromCourse(Context context, long studentId, long courseId) {
		
		final int deletedRows = context.getContentResolver()
				.delete(PerformanceDatabaseContentProvider.getCONTENT_URI_COURSE_STUDENT(courseId, studentId), null, null);

		if (deletedRows < 0)
			throw new DatabaseError("Failed to delete the student-course mapping in CourseStudentTable!");
	}
}
