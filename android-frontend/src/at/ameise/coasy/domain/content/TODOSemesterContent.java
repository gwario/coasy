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
package at.ameise.coasy.domain.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ameise.coasy.domain.Course;
import at.ameise.coasy.domain.Student;
import at.ameise.coasy.domain.TODOSemester;
import at.ameise.coasy.domain.dto.StudentDto;

import com.google.gson.Gson;

/**
 * Provides instances of {@link Course}s for various purposes.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public final class TODOSemesterContent {

	private TODOSemesterContent() {
	}

	/**
	 * @return content for demoing.
	 */
	public static final Map<Course, List<Student>> demoCourses() {

		Map<Course, List<Student>> mapping = new HashMap<Course, List<Student>>();
		List<Course> courses = CourseContent.demoCourses();

		Gson gson = new Gson();

		mapping.put(courses.get(0), createWith(new long[] { 0, 1, 2 }, gson));
		mapping.put(courses.get(2), createWith(new long[] { 3, 4 }, gson));
		mapping.put(courses.get(3), createWith(new long[] { 5 }, gson));
		mapping.put(courses.get(4), createWith(new long[] { 6, 7 }, gson));
		mapping.put(courses.get(5), createWith(new long[] { 8, 0, 1 }, gson));
		mapping.put(courses.get(6), createWith(new long[] { 2, 3, 4, 5, 6 }, gson));

		return mapping;
	}

	/**
	 * Creates a List {@link TODOSemester} instance with the specified values.
	 * 
	 * @param studentIds
	 * @param gson
	 *            optionally pass a gson instance to convert from dto to domain.
	 *            use it when creating many instances at a time to reduce memory
	 *            allocation time.
	 * @return
	 */
	private static final List<Student> createWith(long[] studentIds, Gson gson) {

		if (gson == null)
			gson = new Gson();

		List<Student> students = new ArrayList<Student>();

		for (long id : studentIds) {

			StudentDto s = new StudentDto();
			s.id = id;

			students.add(gson.fromJson(gson.toJson(s), Student.class));
		}

		return students;
	}
}
