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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import at.ameise.coasy.R;

/**
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public final class AccountUtil {

	/**
	 * The account type used by google accounts.<br>
	 * <br>
	 * See {@link android.provider.ContactsContract.SyncColumns#ACCOUNT_TYPE}
	 */
	public static final String ACCOUNT_TYPE_GOOGLE = "com.google";

	private AccountUtil() {
	}

	// /**
	// * @param context
	// * @return a {@link Set} of all google accounts with contacts.
	// */
	// public static final Set<CharSequence> getAllGoogleAccountNames(Context
	// context) {
	//
	// Cursor accountNameCursor = context.getContentResolver().query(//
	// ContactsContract.Groups.CONTENT_URI,//
	// new String[] { ContactsContract.Groups.ACCOUNT_NAME, },//
	// ContactsContract.Groups.ACCOUNT_TYPE + " = ?",//
	// new String[] { ACCOUNT_TYPE_GOOGLE, },//
	// null);
	//
	// Set<CharSequence> accountNamesSet = new HashSet<CharSequence>();
	//
	// if (accountNameCursor.moveToFirst()) {
	// do {
	// accountNamesSet.add(accountNameCursor.getString(accountNameCursor.getColumnIndexOrThrow(ContactsContract.Groups.ACCOUNT_NAME)));
	// } while (accountNameCursor.moveToNext());
	// }
	//
	// accountNameCursor.close();
	//
	// return accountNamesSet;
	// }

	// /**
	// * @param context
	// * @return the first account with contacts.
	// * @deprecated Use {@link
	// AccountUtil#getPrimaryGoogleAccountName(Context)}
	// * instead!
	// */
	// @Deprecated
	// private static final CharSequence getFirstGoogleAccountName(Context
	// context) {
	//
	// Set<CharSequence> allAccounts = getAllGoogleAccountNames(context);
	//
	// if (allAccounts.isEmpty())
	// throw new CoasyError("No accounts found!");
	//
	// return allAccounts.iterator().next();
	// }

	// /**
	// * @param context
	// * @return the primary google account with contacts or the first.
	// */
	// private static final CharSequence getPrimaryGoogleAccountName(Context
	// context) {
	//
	// CharSequence accountName;
	// Cursor accountNameCursor = context.getContentResolver().query(//
	// ContactsContract.Groups.CONTENT_URI,//
	// new String[] { ContactsContract.Groups.ACCOUNT_NAME, },//
	// ContactsContract.Groups.ACCOUNT_TYPE + " = ?",//
	// new String[] { ACCOUNT_TYPE_GOOGLE, },//
	// ContactsContract.CommonDataKinds.ontacts.Data.IS_PRIMARY + " DESC");
	//
	// if (accountNameCursor.moveToFirst()) {
	//
	// accountName =
	// accountNameCursor.getString(accountNameCursor.getColumnIndexOrThrow(ContactsContract.Groups.ACCOUNT_NAME));
	// accountNameCursor.close();
	//
	// } else {
	//
	// accountNameCursor.close();
	// throw new CoasyError("No accounts found!");
	// }
	//
	// return accountName;
	// }

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
	 * @return the id of the contact group selected in the user settings or null
	 */
	public static final String getSelectedGroup(Context context) {

		SharedPreferences userSettings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

		String selectedGroup = userSettings.getString(context.getString(R.string.contactGroup_key),
				String.valueOf(ContactContractUtil.getFirstGroupId(context)));
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
	 * Sets the google account to use in coasy.
	 * 
	 * @param context
	 * @param accountName
	 */
	public static void setSelectedGoogleAccount(Context context, String accountName) {

		SharedPreferences userSettings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

		userSettings.edit().putString(context.getString(R.string.accountName_key), accountName).commit();

		Logger.debug(IUtilTags.TAG_ACCOUTN_UTIL, "Set google account to " + accountName);
	}

}
