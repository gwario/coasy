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

import lombok.AllArgsConstructor;
import lombok.ToString;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import at.ameise.coasy.ICoasySettings;
import at.ameise.coasy.R;
import at.ameise.coasy.domain.persistence.ContactsContractHelper;
import at.ameise.coasy.domain.persistence.database.CoasyDatabaseHelper;
import at.ameise.coasy.exception.UpdateContactsException;

/**
 * Contains methods to save and load settings.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public final class SettingsUtil {

	private static final String TAG = "SettingsUtil";
	
	/**
	 * The account type used by google accounts.<br>
	 * <br>
	 * See {@link android.provider.ContactsContract.SyncColumns#ACCOUNT_TYPE}
	 */
	public static final String ACCOUNT_TYPE_GOOGLE = "com.google";

	private SettingsUtil() {
	}

	/**
	 * The purpose of this class is to serialize all coasy settings to the
	 * {@link ContactsContract.Groups} table.
	 * 
	 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
	 * 
	 */
	@AllArgsConstructor@ToString
	public static final class CoasySettings {

		public String selectedAccount;
		public long selectedGroup;
	}

	/**
	 * @param context
	 * @return {@link Cursor} on the coasy settings groups.
	 */
	public static final Cursor getCoasySettingsGroup(Context context) {

		return context.getContentResolver().query(//
				ContactsContract.Groups.CONTENT_URI,//
				null, ""//
						+ ContactsContract.Groups.DELETED + " = ? AND "//
						+ ContactsContract.Groups.SHOULD_SYNC + " = ? AND "//
						+ ContactsContract.Groups.GROUP_VISIBLE + " = ? AND "//
						+ ContactsContract.Groups.TITLE + " = ? AND "//
						+ ContactsContract.Groups.ACCOUNT_TYPE + " = ?",//
				new String[] {//
						CoasyDatabaseHelper.SQLITE_VALUE_FALSE,//
						CoasyDatabaseHelper.SQLITE_VALUE_TRUE,//
						ICoasySettings.MODE_DEBUG ? CoasyDatabaseHelper.SQLITE_VALUE_TRUE : CoasyDatabaseHelper.SQLITE_VALUE_FALSE,//
						ContactsContractHelper.SETTINGS_TITLE,//
						ACCOUNT_TYPE_GOOGLE,//
				},//
				null);
	}

	/**
	 * Saves the contact group as settings in the {@link SharedPreferences}.
	 * 
	 * @param context
	 * @param coasySettingsGroupCursor
	 */
	public static final void saveAsPreferences(Context context, Cursor coasySettingsGroupCursor) {

		final String settingsJsonEsc = coasySettingsGroupCursor.getString(coasySettingsGroupCursor.getColumnIndex(ContactsContract.Groups.NOTES));
		final CoasySettings settings = CoasyDatabaseHelper.fromEscapedJson(settingsJsonEsc, CoasySettings.class);
		
		setGoogleAccountPreferences(context, settings.selectedAccount);
		setContactGroupPreferences(context, settings.selectedGroup);
	}

	/**
	 * Creates or updates the contact group which holds the preferences.<br>
	 * This method takes the settings from the {@link SharedPreferences}!
	 * 
	 * @param context
	 * @throws UpdateContactsException
	 */
	private static final void createOrUpdateCoasySettingsGroup(Context context) throws UpdateContactsException {

		final ContentValues values = new ContentValues();
		final CoasySettings settings = new CoasySettings(getSelectedGoogleAccount(context).name, getSelectedGroup(context));
		
		values.put(ContactsContract.Groups.NOTES, CoasyDatabaseHelper.toEscapedJson(settings));
		
		Cursor coasySettingsGroup = getCoasySettingsGroup(context);
		if (coasySettingsGroup.moveToFirst()) {

			int updated;

			updated = context.getContentResolver().update(//
					ContactsContract.Groups.CONTENT_URI,//
					values, ""//
							+ ContactsContract.Groups.DELETED + " = ? AND "//
							+ ContactsContract.Groups.SHOULD_SYNC + " = ? AND "//
							+ ContactsContract.Groups.GROUP_VISIBLE + " = ? AND "//
							+ ContactsContract.Groups.TITLE + " = ? ",//
					new String[] {//
							CoasyDatabaseHelper.SQLITE_VALUE_FALSE,//
							CoasyDatabaseHelper.SQLITE_VALUE_TRUE,//
							ICoasySettings.MODE_DEBUG ? CoasyDatabaseHelper.SQLITE_VALUE_TRUE : CoasyDatabaseHelper.SQLITE_VALUE_FALSE,//
							ContactsContractHelper.SETTINGS_TITLE,//
					});

			if (updated != 1)
				throw new UpdateContactsException("Failed to update settings group!");

		} else {

			// add the other columns
			values.put(ContactsContract.Groups.DELETED, CoasyDatabaseHelper.SQLITE_VALUE_FALSE);
			values.put(ContactsContract.Groups.SHOULD_SYNC, CoasyDatabaseHelper.SQLITE_VALUE_TRUE);
			values.put(ContactsContract.Groups.TITLE, ContactsContractHelper.SETTINGS_TITLE);
			values.put(ContactsContract.Groups.ACCOUNT_NAME, getSelectedGoogleAccount(context).name);
			values.put(ContactsContract.Groups.ACCOUNT_TYPE, ACCOUNT_TYPE_GOOGLE);

			values.put(ContactsContract.Groups.GROUP_VISIBLE,//
					ICoasySettings.MODE_DEBUG ? CoasyDatabaseHelper.SQLITE_VALUE_TRUE : CoasyDatabaseHelper.SQLITE_VALUE_FALSE);

			context.getContentResolver().insert(ContactsContract.Groups.CONTENT_URI, values);
		}
		
		coasySettingsGroup.close();
	}

	/**
	 * @param context
	 * @param name
	 * @return the google account with the specified name or null.
	 */
	private static final Account getAccount(Context context, String name) {

		AccountManager am = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
		for (Account a : am.getAccountsByType(ACCOUNT_TYPE_GOOGLE))
			if (a.name.equals("" + name))
				return a;

		return null;
	}

	/**
	 * Use {@link SettingsUtil#isAccountSelected(Context)} to check if account
	 * was set first!
	 * 
	 * @param context
	 * @return the account selected in the user settings or null.
	 */
	public static final Account getSelectedGoogleAccount(Context context) {

		SharedPreferences userSettings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

		String selectedAccountName = userSettings.getString(context.getString(R.string.accountName_key), "");

		Account selectedAccount = getAccount(context, selectedAccountName);

		Logger.debug(IUtilTags.TAG_ACCOUTN_UTIL, "Selected google account: " + (selectedAccount != null ? selectedAccount.toString() : null));

		return selectedAccount;
	}

	/**
	 * @param context
	 * @return the id of the contact group selected in the user settings or the
	 *         first group. See
	 *         {@link ContactContractUtil#getFirstGroupId(Context)}
	 */
	public static final long getSelectedGroup(Context context) {

		SharedPreferences userSettings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

		long selectedGroup = userSettings.getLong(context.getString(R.string.contactGroup_key), ContactContractUtil.getFirstGroupId(context));
		Logger.debug(IUtilTags.TAG_ACCOUTN_UTIL, "Selected group=" + selectedGroup);

		return selectedGroup;
	}

	/**
	 * @return true if the user selected a google account to use in coasy.
	 */
	public static boolean isAccountSelected(Context context) {

		return getSelectedGoogleAccount(context) != null;
	}

	/**
	 * Sets the google account to use in coasy in the {@link SharedPreferences}
	 * and in the {@link ContactsContract.Groups}.
	 * 
	 * @param context
	 * @param accountName
	 * @throws UpdateContactsException
	 */
	public static void setSelectedGoogleAccount(Context context, String accountName) throws UpdateContactsException {

		setGoogleAccountPreferences(context, accountName);

		createOrUpdateCoasySettingsGroup(context);

		Logger.debug(IUtilTags.TAG_ACCOUTN_UTIL, "Set google account to " + accountName);
	}

	/**
	 * Sets the contact group to pick students of in the
	 * {@link SharedPreferences} and in the {@link ContactsContract.Groups}.
	 * 
	 * @param context
	 * @param contactGroupId
	 * @throws UpdateContactsException
	 */
	public static void setSelectedContactGroup(Context context, long contactGroupId) throws UpdateContactsException {

		setContactGroupPreferences(context, contactGroupId);

		createOrUpdateCoasySettingsGroup(context);

		Logger.debug(IUtilTags.TAG_ACCOUTN_UTIL, "Set contact group to " + contactGroupId);
	}

	/**
	 * Actually sets the accountName in the {@link SharedPreferences}.
	 * 
	 * @param context
	 * @param accountName
	 */
	private static void setGoogleAccountPreferences(Context context, String accountName) {

		SharedPreferences userSettings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

		userSettings.edit().putString(context.getString(R.string.accountName_key), accountName).commit();
	}

	/**
	 * Actually sets the contact group in the {@link SharedPreferences}.
	 * 
	 * @param context
	 * @param contactGroupId
	 */
	private static void setContactGroupPreferences(Context context, long contactGroupId) {
		
		SharedPreferences userSettings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

		userSettings.edit().putLong(context.getString(R.string.contactGroup_key), contactGroupId).commit();
	}

}
