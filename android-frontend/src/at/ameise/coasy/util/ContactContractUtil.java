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

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import at.ameise.coasy.domain.persistence.database.CoasyDatabaseHelper;

/**
 * Contains methods to deal with {@link ContactsContract.Groups} and
 * {@link ContactsContract.Contacts}.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public final class ContactContractUtil {

	private ContactContractUtil() {
	}

	/**
	 * @param context
	 * @return a {@link Map} of all contact groups of the
	 *         {@link AccountUtil#getSelectedGoogleAccount(Context)}. As id to
	 *         title mapping.
	 */
	public static Map<CharSequence, CharSequence> getAllContactGroups(Context context) {

		Map<CharSequence, CharSequence> groups = new HashMap<CharSequence, CharSequence>();

		Cursor groupsCursor = getAllContactGroupsAsCursor(context);

		if (groupsCursor.moveToFirst()) {

			do {

				groups.put(String.valueOf(groupsCursor.getLong(groupsCursor.getColumnIndexOrThrow(ContactsContract.Groups._ID))),
						groupsCursor.getString(groupsCursor.getColumnIndexOrThrow(ContactsContract.Groups.TITLE)));

			} while (groupsCursor.moveToNext());
		}

		groupsCursor.close();

		return groups;
	}

	/**
	 * @param context
	 * @return the id of the first contact group as of
	 *         {@link ContactContractUtil#getAllContactGroupsAsCursor(Context)}.
	 */
	public static long getFirstGroupId(Context context) {

		long id;
		final Cursor groupsCursor = getAllContactGroupsAsCursor(context);

		if (groupsCursor.moveToFirst()) {

			id = groupsCursor.getLong(groupsCursor.getColumnIndexOrThrow(ContactsContract.Groups._ID));

		} else {

			id = -1;
		}

		groupsCursor.close();

		return id;
	}

	/**
	 * @param context
	 * @return {@link Cursor} on all visible {@link ContactsContract.Groups} of
	 *         the {@link AccountUtil#getSelectedGoogleAccount(Context)}
	 */
	private static Cursor getAllContactGroupsAsCursor(Context context) {

		return context.getContentResolver().query(//
				ContactsContract.Groups.CONTENT_URI,//
				null, ""//
						+ ContactsContract.Groups.GROUP_VISIBLE + " = ? AND "//
						+ ContactsContract.Groups.ACCOUNT_NAME + " = ? AND "//
						+ ContactsContract.Groups.ACCOUNT_TYPE + " = ?",//
				new String[] {//
						CoasyDatabaseHelper.SQLITE_VALUE_TRUE,//
						SettingsUtil.getSelectedGoogleAccount(context).name,//
						SettingsUtil.ACCOUNT_TYPE_GOOGLE,//
				},//
				null);
	}

}
