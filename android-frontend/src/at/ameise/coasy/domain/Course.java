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
package at.ameise.coasy.domain;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import android.provider.ContactsContract;
import android.widget.EditText;

/**
 * Representation of a course.<br>
 * <br>
 * The {@link Course#contactGroupId} is the {@link ContactsContract.Groups#_ID},
 * all other fields are stored in the coasy database.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
@ToString
public final class Course {

	static final String PATTERN_TITLE = "[\\d\\s\\w]+";
	static final CharSequence ERROR_TITLE = "Invalid title: Use only alphanumeric characters!";

	static final String PATTERN_DESCRIPTION = ".*";
	static final CharSequence ERROR_DESCRIPTION = "";

	static final String PATTERN_ADDRESS = ".*";
	static final CharSequence ERROR_ADDRESS = "";
	
	/**
	 * The id in the {@link ContactsContract.Groups}.
	 */
	@Getter
	private long id = -1;

	@Getter
	@Setter
	@NonNull
	private String title;
	@Getter
	@Setter
	private String description = null;

	@Getter
	@Setter
	private String address = null;

	private Course() {
	}

	/**
	 * @param title
	 *            the title
	 * @param description
	 *            optional
	 */
	public Course(String title, String description, String address) {
		this();

		if (title == null || title.isEmpty())
			throw new IllegalArgumentException("title must not be null or empty!");

		this.title = title;
		this.description = description;
		this.address = address;
	}

	/**
	 * Validates the title. If the text in etTitle does not validate,
	 * {@link EditText#setError(CharSequence)} is called and the focus is set to
	 * the {@link EditText}.
	 * 
	 * @param etTitle
	 * @return true if the title validates.
	 */
	public static final boolean validateTitle(EditText etTitle) {

		return CourseValidation.validateTitle(etTitle);
	}

	/**
	 * Validates the description. If the text in etDescription does not validate,
	 * {@link EditText#setError(CharSequence)} is called and the focus is set to
	 * the {@link EditText}.
	 * 
	 * @param etDescription
	 * @return true if the title validates.
	 */
	public static final boolean validateDescription(EditText etDescription) {
		
		return CourseValidation.validateDescription(etDescription);
	}
	
	/**
	 * Validates the address. If the text in etAddress does not validate,
	 * {@link EditText#setError(CharSequence)} is called and the focus is set to
	 * the {@link EditText}.
	 * 
	 * @param etDescription
	 * @return true if the title validates.
	 */
	public static final boolean validateAddress(EditText etAddress) {
		
		return CourseValidation.validateAddress(etAddress);
	}
}
