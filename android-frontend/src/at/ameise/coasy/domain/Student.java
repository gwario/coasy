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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import android.provider.ContactsContract;

/**
 * Representation of a student. All fields are populated from the contacts
 * database.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
@ToString
public final class Student {

	/**
	 * The id in the {@link ContactsContract}.
	 */
	@Getter
	private long id = -1;

	@Getter
	@Setter
	private String displayName;

	@Getter
	@Setter
	private Date dayOfBirth;

	/**
	 * Name of the contact. This is intended for the name of a child's parents.
	 */
	@Getter
	@Setter
	private String contactName;

	@Getter
	@Setter
	private Map<String, String> email = new HashMap<String, String>();

	@Getter
	@Setter
	private Map<String, String> phone = new HashMap<String, String>();

	@Getter
	@Setter
	private String address;

	// /**
	// * The karate grade. I.e. 8th kyu, 1st dan,...
	// */
	// @Getter
	// @Setter
	// private String grade;

	private Student() {
	}

	/**
	 * @param displayName
	 * @param dayOfBirth
	 * @param contactName
	 * @param email
	 * @param phone
	 * @param address
	 */
	public Student(String displayName, Date dayOfBirth, String contactName, Map<String, String> email, Map<String, String> phone, String address) {
		this();
		
		if(displayName == null || displayName.isEmpty())
			throw new IllegalArgumentException("displayname must not be null or empty!");
		
		this.displayName = displayName;
		this.dayOfBirth = dayOfBirth;
		this.contactName = contactName;
		this.email = email;
		this.phone = phone;
		this.address = address;
	}

}
