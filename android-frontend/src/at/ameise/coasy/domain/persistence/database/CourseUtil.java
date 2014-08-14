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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import at.ameise.coasy.domain.Course;
import at.ameise.coasy.util.IUtilTags;
import at.ameise.coasy.util.Logger;

/**
 * Contains methods to deal with {@link CourseTable}.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 *
 */
public final class CourseUtil {
	
	private CourseUtil() {
	}

	/**
	 * @param context
	 * @return a {@link List} of all courses.
	 */
	public static final List<Course> getAllCourses(Context context) {

		final List<Course> courses = new ArrayList<Course>();

		final Cursor courseCursor = getAllCoursesAsCursor(context);
		Logger.debug(IUtilTags.TAG_CONTACT_CONTRACT_UTIL, "Got " + courseCursor.getCount() + " courses.");

		if (courseCursor.moveToFirst()) {

			do {

				courses.add(CourseTable.fromCoursesCursor(courseCursor));

			} while (courseCursor.moveToNext());
		}

		courseCursor.close();

		return courses;
	}
	
	/**
	 * @param context
	 * @return {@link Cursor} on all {@link Course}s from {@link CourseTable}.
	 */
	public static Cursor getAllCoursesAsCursor(Context context) {
		return context.getContentResolver().query(PerformanceDatabaseContentProvider.CONTENT_URI_COURSE, null, null, null, null);
	}

}
