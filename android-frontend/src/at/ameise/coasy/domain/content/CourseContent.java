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
import java.util.List;

import at.ameise.coasy.domain.Course;
import at.ameise.coasy.domain.dto.CourseDto;

import com.google.gson.Gson;

/**
 * Provides instances of {@link Course}s for various purposes.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public final class CourseContent {

	private CourseContent() {
	}

	/**
	 * @return content for demoing.
	 */
	public static final List<Course> demoCourses() {

		List<Course> courses = new ArrayList<Course>();

		Gson gson = new Gson();

		courses.add(createWith(0, "Title0", "Desciption0", gson));
		courses.add(createWith(1, "Title1", "Desciption1", gson));
		courses.add(createWith(2, "Title2", "Desciption2", gson));
		courses.add(createWith(3, "Title3", "Desciption3", gson));
		courses.add(createWith(4, "Title4", "Desciption4", gson));
		courses.add(createWith(5, "Title5", "Desciption5", gson));
		courses.add(createWith(6, "Title6", "Desciption6", gson));
		courses.add(createWith(7, "Title7", "Desciption7", gson));
		courses.add(createWith(8, "Title8", "Desciption8", gson));
		courses.add(createWith(9, "Title9", "Desciption9", gson));
		courses.add(createWith(10, "Title10", "Desciption10", gson));
		courses.add(createWith(11, "Title11", "Desciption11", gson));
		courses.add(createWith(12, "Title12", "Desciption12", gson));
		courses.add(createWith(13, "Title13", "Desciption13", gson));
		courses.add(createWith(14, "Title14", "Desciption14", gson));
		courses.add(createWith(15, "Title15", "Desciption15", gson));

		return courses;
	}

	/**
	 * Creates a {@link CourseContent} instance with the specified values.
	 * 
	 * @param id
	 * @param title
	 * @param description
	 * @param gson
	 *            optionally pass a gson instance to convert from dto to domain.
	 *            use it when creating many instances at a time to reduce memory
	 *            allocation time.
	 * @return
	 */
	private static final Course createWith(long id, String title, String description, Gson gson) {

		if (gson == null)
			gson = new Gson();

		return gson.fromJson(gson.toJson(new CourseDto(id, title, description)), Course.class);
	}
}
