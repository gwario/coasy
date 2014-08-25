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

import android.widget.EditText;
import at.ameise.coasy.util.Logger;

/**
 * Contains methods to validate a {@link Course}.<br>
 * The main purpose of this class is to keep the domain class clean.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public final class CourseValidation {

	private static final String TAG = "CourseValidation";

	/**
	 * @param etTitle
	 * @return
	 */
	public static boolean validateTitle(EditText etTitle) {

		if (etTitle.getText().toString().trim().matches(Course.PATTERN_TITLE)) {

			Logger.debug(TAG, "Title validates.");
			return true;

		} else {

			Logger.warn(TAG, "Title('" + etTitle.getText() + "') does not match pattern!");
			etTitle.requestFocus();
			etTitle.setError(Course.ERROR_TITLE);
			return false;
		}
	}

	/**
	 * @param etDescription
	 * @return
	 */
	public static boolean validateDescription(EditText etDescription) {

		if (etDescription.getText().toString().trim().matches(Course.PATTERN_DESCRIPTION)) {

			Logger.debug(TAG, "Decription validates.");
			return true;

		} else {

			Logger.warn(TAG, "Description('" + etDescription.getText() + "') does not match pattern!");
			etDescription.requestFocus();
			etDescription.setError(Course.ERROR_DESCRIPTION);
			return false;
		}
	}

	/**
	 * @param etAddress
	 * @return
	 */
	public static boolean validateAddress(EditText etAddress) {

		if (etAddress.getText().toString().trim().matches(Course.PATTERN_ADDRESS)) {

			Logger.debug(TAG, "Address validates.");
			return true;

		} else {

			Logger.warn(TAG, "Address('" + etAddress.getText() + "') does not match pattern!");
			etAddress.requestFocus();
			etAddress.setError(Course.ERROR_ADDRESS);
			return false;
		}
	}

}
