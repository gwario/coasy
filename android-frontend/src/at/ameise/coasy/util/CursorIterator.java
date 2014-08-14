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

import android.database.Cursor;

/**
 * Iterates over a {@link Cursor}.
 * Note: This implementation closes the cursor even in case of an exception.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 */
public abstract class CursorIterator {

	private Cursor cursor;

	/**
	 * Iterates over a {@link Cursor}.
	 * 
	 * @param cursor
	 */
	public CursorIterator(Cursor cursor) {

		this.cursor = cursor;
	}

	/**
	 * Starts iterating.<br>
	 * Note: This implementation closes the cursor even in case of an exception.
	 */
	public void iterate() {

		try {

			int index = 0;

			if (cursor.moveToFirst()) {

				do {

					next(index++, cursor);

				} while (cursor.moveToNext());
			}

		} finally {

			cursor.close();
		}
	}

	/**
	 * Called for every row of the cursor.
	 * 
	 * @param index
	 *            index of the row.
	 * @param cursor
	 */
	protected abstract void next(int index, Cursor cursor);
}