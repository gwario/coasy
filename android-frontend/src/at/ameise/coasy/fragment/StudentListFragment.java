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
package at.ameise.coasy.fragment;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import at.ameise.coasy.R;
import at.ameise.coasy.activity.MainActivity;
import at.ameise.coasy.activity.UserSettingsActivity;
import at.ameise.coasy.domain.persistence.IPersistenceManager;
import at.ameise.coasy.domain.persistence.ProductionPersistenceManager;
import at.ameise.coasy.domain.persistence.database.ILoader;
import at.ameise.coasy.domain.persistence.database.StudentTable;
import at.ameise.coasy.exception.CoasyError;
import at.ameise.coasy.util.AccountUtil;
import at.ameise.coasy.util.Logger;

/**
 * The student list.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public class StudentListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, OnClickListener {

	private static final String TAG = "StudentListF";
	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";

	private static final int REQUEST_CODE_CREATE_CONTACT = 100;

	private IPersistenceManager pm;

	private Button bCreateStudent;

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static StudentListFragment newInstance(int sectionNumber) {

		StudentListFragment fragment = new StudentListFragment();

		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);

		return fragment;
	}

	public StudentListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		pm = ProductionPersistenceManager.getInstance(getActivity());
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getLoaderManager().initLoader(ILoader.STUDENTS_LOADER_ID, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_students_list, container, false);

		final String[] from = new String[] { StudentTable.COL_ID, StudentTable.COL_DISPLAY_NAME, };
		final int[] to = new int[] { R.id.listitem_student_tv_id, R.id.listitem_student_tv_displayname, };

		setListAdapter(new SimpleCursorAdapter(getActivity(), R.layout.fragment_students_list_item, null, from, to, 0));

		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		bCreateStudent = (Button) view.findViewById(R.id.fragment_students_bCreateStudent);

		bCreateStudent.setOnClickListener(this);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(id))));
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return pm.allStudentsCursorLoader();
	}

	@Override
	public void onResume() {
		super.onResume();
		
		if (!AccountUtil.isAccountSelected(getActivity())) {

			final int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity().getApplicationContext());
			if (status == ConnectionResult.SUCCESS) {

				startActivityForResult(
						AccountPicker.newChooseAccountIntent(null, null, new String[] { GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE }, false, null, null, null, null),
						UserSettingsActivity.REQUEST_CODE_ACCOUNT_NAME);

			} else {

				GooglePlayServicesUtil.getErrorDialog(status, getActivity(), UserSettingsActivity.REQUEST_CODE_PLAY_SERVICES_NOT_AVAILABLE).show();
			}
		}
		
		getLoaderManager().restartLoader(ILoader.STUDENTS_LOADER_ID, null, this);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		((SimpleCursorAdapter) getListAdapter()).swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		((SimpleCursorAdapter) getListAdapter()).swapCursor(null);
	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {

		case R.id.fragment_students_bCreateStudent:
			// // Add listener so your activity gets called back upon completion
			// of action,
			// // in this case with ability to get handle to newly added contact
//			 getActivity().addActivityListener(this);
			// add custom fields after creation

			Intent intent = new Intent(Intent.ACTION_INSERT);
			intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
			if (Integer.valueOf(Build.VERSION.SDK_INT) > 14)
				intent.putExtra("finishActivityOnSaveCompleted", true); // Fix for 4.0.3 +
			// // Just two examples of information you can send to pre-fill out
			// data for the
//			ContentValues row2 = new ContentValues();
//			row2.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
//			row2.put(Phone.TYPE, Phone.TYPE_CUSTOM);
//			row2.put(Phone.LABEL, "Contact Name");
//			row2.put(Phone., "android@android.com");
//			data.add(row2);
//			intent.putParcelableArrayListExtra(Insert.DATA, data);

			startActivityForResult(intent, REQUEST_CODE_CREATE_CONTACT);
			break;

		default:
			throw new CoasyError("Unhandled click event.");
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_CREATE_CONTACT) {

			if (resultCode == Activity.RESULT_OK) {

				Uri resultUri = data.getData();
				boolean success = pm.createStudentContact(Long.valueOf(resultUri.getLastPathSegment()));
				if (!success) {
					Toast.makeText(getActivity(), "Failed to create student.", Toast.LENGTH_SHORT).show();
				}

			} else {

				Logger.warn(TAG, "Add contact returned " + resultCode);
			}
		} else {

			throw new CoasyError("Unhandled requestCode: " + requestCode);
		}
	}
}
