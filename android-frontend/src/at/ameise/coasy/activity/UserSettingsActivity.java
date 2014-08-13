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
package at.ameise.coasy.activity;

import java.util.List;
import java.util.Map;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.widget.Button;
import at.ameise.coasy.R;
import at.ameise.coasy.util.AccountUtil;
import at.ameise.coasy.util.ContactContractUtil;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Contains settings to be changed by the user.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public class UserSettingsActivity extends PreferenceActivity {

	/**
	 * Code to determine the request in {@link Activity#onActivityResult()}.
	 */
	public static final int REQUEST_CODE_ACCOUNT_NAME = 1;
	public static final int REQUEST_CODE_PLAY_SERVICES_NOT_AVAILABLE = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Add a button to the header list.
		if (hasHeaders()) {
			Button button = new Button(this);
			button.setText("Some action");
			setListFooter(button);
		}
	}

	/**
	 * Populate the activity with the top-level headers.
	 */
	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.preference_headers, target);
	}

	@Override
	protected boolean isValidFragment(String fragmentName) {
		return true;
	}

	/**
	 * This fragment shows the preferences for the first header.
	 */
	public static class Prefs1Fragment extends PreferenceFragment implements OnPreferenceClickListener {

		private Preference btnSelectAccount;
		private ListPreference lpContactGroup;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Make sure default values are applied. In a real app, you would
			// want this in a shared function that is used to retrieve the
			// SharedPreferences wherever they are needed.
			// PreferenceManager.setDefaultValues(getActivity(),
			// R.xml.advanced_preferences, false);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.activity_usersettings);

			btnSelectAccount = findPreference("prefBtnSelectGoogleAccount");

			lpContactGroup = (ListPreference) findPreference("prefContactGroup");

			btnSelectAccount.setOnPreferenceClickListener(this);

			Map<CharSequence, CharSequence> groups = ContactContractUtil.getAllContactGroups(getActivity());

			lpContactGroup.setEntries(groups.values().toArray(new CharSequence[groups.size()]));
			lpContactGroup.setEntryValues(groups.keySet().toArray(new CharSequence[groups.size()]));
		}

		@Override
		public boolean onPreferenceClick(Preference preference) {

			if (preference.getKey().equals(getString(R.string.btnSelectGoogleAccount_key))) {

				final int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity().getApplicationContext());
				if (status == ConnectionResult.SUCCESS) {

					startActivityForResult(AccountPicker.newChooseAccountIntent(AccountUtil.getSelectedGoogleAccount(getActivity()), null,
							new String[] { GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE }, false, null, null, null, null), UserSettingsActivity.REQUEST_CODE_ACCOUNT_NAME);
					return true;

				} else {

					GooglePlayServicesUtil.getErrorDialog(status, getActivity(), UserSettingsActivity.REQUEST_CODE_PLAY_SERVICES_NOT_AVAILABLE).show();
				}
			}
			return false;
		}

		// /**
		// * This fragment contains a second-level set of preference that you
		// * can get to by tapping an item in the first preferences fragment.
		// */
		// public static class Prefs1FragmentInner extends PreferenceFragment {
		// @Override
		// public void onCreate(Bundle savedInstanceState) {
		// super.onCreate(savedInstanceState);
		//
		// // Can retrieve arguments from preference XML.
		// Logger.info("args", "Arguments: " + getArguments());
		//
		// // Load the preferences from an XML resource
		// addPreferencesFromResource(R.xml.fragmented_preferences_inner);
		// }
	}

	// /**
	// * This fragment shows the preferences for the second header.
	// */
	// public static class Prefs2Fragment extends PreferenceFragment {
	// @Override
	// public void onCreate(Bundle savedInstanceState) {
	// super.onCreate(savedInstanceState);
	//
	// // Can retrieve arguments from headers XML.
	// Logger.info("args", "Arguments: " + getArguments());
	//
	// // Load the preferences from an XML resource
	// addPreferencesFromResource(R.xml.preference_dependencies);
	// }
	// }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == REQUEST_CODE_ACCOUNT_NAME && resultCode == RESULT_OK) {
			String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
			AccountUtil.setSelectedGoogleAccount(this, accountName);
		}
	}

}
