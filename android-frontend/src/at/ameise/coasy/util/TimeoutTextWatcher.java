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

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;

/**
 * Provides a timeout based callbacks of {@link TextWatcher}.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public abstract class TimeoutTextWatcher implements TextWatcher {

	private long timeout;
	private Handler afterTextChangedHandler;
	private Handler beforeTextChangedHandler;
	private Handler onTextChangedHandler;

	/**
	 * Provides a timeout based callbacks of {@link TextWatcher}.
	 * 
	 * @param timeout
	 *            timeout in milliseconds.
	 */
	public TimeoutTextWatcher(long timeout) {
		this.timeout = timeout;
		this.afterTextChangedHandler = new Handler();
		this.beforeTextChangedHandler = new Handler();
		this.onTextChangedHandler = new Handler();
	}

	/**
	 * Called after the specified timeout.<br>
	 * See {@link TextWatcher#onTextChanged(CharSequence, int, int, int)}
	 * 
	 * @param s
	 * @param start
	 * @param before
	 * @param count
	 */
	public abstract void onTextChangedTimeout(CharSequence s, int start, int before, int count);

	/**
	 * Called after the specified timeout.<br>
	 * See
	 * {@link TextWatcher#beforeTextChanged(CharSequence, int, int, int)}
	 * 
	 * @param s
	 * @param start
	 * @param count
	 * @param after
	 */
	public abstract void beforeTextChangedTimeout(CharSequence s, int start, int count, int after);

	/**
	 * Called after the specified timeout.<br>
	 * See {@link TextWatcher#afterTextChanged(Editable)}
	 * 
	 * @param s
	 */
	public abstract void afterTextChangedTimeout(final Editable s);

	@Override
	public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
		
		onTextChangedHandler.removeCallbacksAndMessages(null);
		onTextChangedHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				onTextChangedTimeout(s, start, before, count);
			}
		}, timeout);
	}

	@Override
	public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
		
		beforeTextChangedHandler.removeCallbacksAndMessages(null);
		beforeTextChangedHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				beforeTextChangedTimeout(s, start, count, after);
			}
		}, timeout);
	}

	@Override
	public void afterTextChanged(final Editable s) {

		afterTextChangedHandler.removeCallbacksAndMessages(null);
		afterTextChangedHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				afterTextChangedTimeout(s);
			}
		}, timeout);
	}

}