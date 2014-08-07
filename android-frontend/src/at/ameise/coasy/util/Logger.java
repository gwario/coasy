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
package at.ameise.coasy.util;

import android.util.Log;
import at.ameise.coasy.ICoasySettings;

/**
 * Wraps the methods of the Android logging framework {@link Log}. Before
 * logging, {@link Log#isLoggable(String, int)} is tested to make sure we leak
 * no log messages in production.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public class Logger {

	private Logger() {
	}

	/**
	 * @see Log#wtf(String, String)
	 * @param tag
	 * @param message
	 */
	public static void wtf(String tag, String message) {
		if (Log.isLoggable(tag, Log.ASSERT) || ICoasySettings.MODE_DEBUG)
			Log.wtf(tag, message);
	}

	/**
	 * @see Log#wtf(String, String, Throwable)
	 * @param tag
	 * @param message
	 */
	public static void wtf(String tag, String message, Throwable throwable) {
		if (Log.isLoggable(tag, Log.ASSERT) || ICoasySettings.MODE_DEBUG)
			Log.wtf(tag, message, throwable);
	}

	/**
	 * @see Log#e(String, String)
	 * @param tag
	 * @param message
	 */
	public static void error(String tag, String message) {
		if (Log.isLoggable(tag, Log.ERROR) || ICoasySettings.MODE_DEBUG)
			Log.e(tag, message);
	}

	/**
	 * @see Log#e(String, String, Throwable)
	 * @param tag
	 * @param message
	 * @param throwable
	 */
	public static void error(String tag, String message, Throwable throwable) {
		if (Log.isLoggable(tag, Log.ERROR) || ICoasySettings.MODE_DEBUG)
			Log.e(tag, message, throwable);
	}

	/**
	 * @see Log#w(String, String)
	 * @param tag
	 * @param message
	 */
	public static void warn(String tag, String message) {
		if (Log.isLoggable(tag, Log.WARN) || ICoasySettings.MODE_DEBUG)
			Log.w(tag, message);
	}

	/**
	 * @see Log#w(String, String, Throwable)
	 * @param tag
	 * @param message
	 * @param throwable
	 */
	public static void warn(String tag, String message, Throwable throwable) {
		if (Log.isLoggable(tag, Log.WARN) || ICoasySettings.MODE_DEBUG)
			Log.w(tag, message, throwable);
	}

	/**
	 * @see Log#i(String, String)
	 * @param tag
	 * @param message
	 */
	public static void info(String tag, String message) {
		if (Log.isLoggable(tag, Log.INFO) || ICoasySettings.MODE_DEBUG)
			Log.i(tag, message);
	}

	/**
	 * @see Log#d(String, String)
	 * @param tag
	 * @param message
	 */
	public static void debug(String tag, String message) {
		if (Log.isLoggable(tag, Log.DEBUG) || ICoasySettings.MODE_DEBUG)
			Log.d(tag, message);
	}

	/**
	 * @see Log#v(String, String)
	 * @param tag
	 * @param message
	 */
	public static void verbose(String tag, String message) {
		if (Log.isLoggable(tag, Log.VERBOSE) || ICoasySettings.MODE_DEBUG)
			Log.v(tag, message);
	}
}
