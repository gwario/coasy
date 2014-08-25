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

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import at.ameise.coasy.domain.Course;
import at.ameise.coasy.domain.Student;
import at.ameise.coasy.exception.AbstractDatabaseException;

/**
 * Facade of the persistence layer.
 * 
 * TODO think carefully whether to use exceptions on error or the boolean return
 * value...
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public interface IPersistenceManager {

	/**
	 * @return a {@link CursorLoader} on all students of all courses.
	 */
	public Loader<Cursor> allStudentsCursorLoader();

	/**
	 * @return a {@link CursorLoader} on all courses.
	 */
	public Loader<Cursor> allCoursesCursorLoader();

	/**
	 * Creates the specified {@link Course}.<br>
	 * This method is synchronized.
	 * 
	 * @param course
	 * @return true on success, false otherwise.
	 */
	public boolean create(Course course);

	/**
	 * Saves the specified {@link Course}.<br>
	 * This method is synchronized.
	 * 
	 * @param course
	 * @return true on success, false otherwise.
	 */
	public boolean save(Course course);

	/**
	 * Creates the {@link Student} from the specified contactId.<br>
	 * This method is synchronized.
	 * 
	 * @param contactId
	 * @return true on success, false otherwise.
	 */
	public boolean createStudent(long contactId);

	/**
	 * @param id
	 * @return a {@link CursorLoader} on the {@link Course} specified by id.
	 */
	public Loader<Cursor> courseCursorLoader(long id);

	// /**
	// * @return a {@link CursorLoader} on the contacts of the "selected" group.
	// */
	// public Loader<Cursor> contactsCursorLoader();

	/**
	 * @return a {@link CursorLoader} on the contacts of the "selected" group
	 *         without the users of the specified group ids.
	 */
	public Loader<Cursor> contactsNotInCourseCursorLoader(long courseId);

	/**
	 * @return a {@link CursorLoader} on the students of the course.
	 */
	public Loader<Cursor> studentsInCourseCoursorLoader(long courseId);

	/**
	 * Removes the student from the course.<br>
	 * This method is synchronized.
	 * 
	 * @param contactId
	 * @param courseId
	 * @return true on success, false otherwise.
	 */
	public boolean removeStudentFromCourse(long contactId, long courseId);

	/**
	 * Adds the student to the course.<br>
	 * This method is synchronized.
	 * 
	 * @param contactId
	 * @param courseId
	 * @return true on success, false otherwise.
	 */
	public boolean addStudentToCourse(long contactId, long courseId);

	/**
	 * Refreshes the performance database from the contacts.
	 * @throws AbstractDatabaseException 
	 */
	public void refreshDatabaseFromContacts() throws AbstractDatabaseException;

}
