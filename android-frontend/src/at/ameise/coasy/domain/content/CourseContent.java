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

/**
 * Provides instances of {@link Course}s for various purposes.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public final class CourseContent {

	private static List<Course> demoCourses;
	
	private CourseContent() {
	}

	/**
	 * @return content for demoing.
	 */
	public static final List<Course> demoCourses() {

		if(demoCourses == null || demoCourses.isEmpty()) {
			
			demoCourses = new ArrayList<Course>();
			
			demoCourses.add(new Course("Title0", "Desciption0"));
			demoCourses.add(new Course("Title1", "Desciption1"));
			demoCourses.add(new Course("Title2", "Desciption2"));
			demoCourses.add(new Course("Title3", "Desciption3"));
			demoCourses.add(new Course("Title4", "Desciption4"));
			demoCourses.add(new Course("Title5", "Desciption5"));
			demoCourses.add(new Course("Title6", "Desciption6"));
			demoCourses.add(new Course("Title7", "Desciption7"));
		}

		return demoCourses;
	}
}
