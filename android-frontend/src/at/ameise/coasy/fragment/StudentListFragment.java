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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import at.ameise.coasy.R;
import at.ameise.coasy.activity.MainActivity;
import at.ameise.coasy.domain.database.ILoader;
import at.ameise.coasy.util.ContactContractUtil;

/**
 * The student list.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public class StudentListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	@SuppressLint("InlinedApi")
	private static final String[] PROJECTION = { Contacts._ID, Contacts.LOOKUP_KEY,
			Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? Contacts.DISPLAY_NAME_PRIMARY : Contacts.DISPLAY_NAME

	};

	// // The column index for the _ID column
	// private static final int CONTACT_ID_INDEX = 0;
	// // The column index for the LOOKUP_KEY column
	// private static final int CONTACT_KEY_INDEX = 1;

	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";

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
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getLoaderManager().initLoader(ILoader.STUDENT_LOADER_ID, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_students_list, container, false);

		final String[] from = new String[] { ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, };
		final int[] to = new int[] { R.id.listitem_student_tv_id, R.id.listitem_student_tv_displayname, };

		setListAdapter(new SimpleCursorAdapter(getActivity(), R.layout.fragment_students_listitem, null, from, to, 0));

		return rootView;
	}

	// @Override
	// public void onListItemClick(ListView l, View v, int position, long id) {
	// // Get the Cursor
	// Cursor cursor = parent.getAdapter().getCursor();
	// // Move to the selected contact
	// cursor.moveToPosition(position);
	// // Get the _ID value
	// long contactId = getLong(CONTACT_ID_INDEX);
	// // Get the selected LOOKUP KEY
	// String contactKey = getString(CONTACT_KEY_INDEX);
	// // Create the contact's content Uri
	// Uri mContactUri = Contacts.getLookupUri(contactId, contactKey);
	// /*
	// * You can use mContactUri as the content URI for retrieving the details
	// * for a contact.
	// */
	// }

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return ContactContractUtil.getAllStudents(getActivity());
//		return ContactContractUtil.getMyContacts(getActivity());
	}

//	/**
//	 * @return all contact ids of all students that are or have been enrolled
//	 *         for any course.
//	 */
//	private String[] getAllContactIds() {
//		Cursor studentCursor = getActivity().getContentResolver().query(CoasyContentProvider.CONTENT_URI_STUDENT, TODOSemesterTable.ALL_COLUMNS, null, null,
//				null);
//
//		String[] contactIds = new String[studentCursor.getCount()];
//		int i = 0;
//		if (studentCursor.moveToFirst()) {
//			do {
//				contactIds[i++] = String.valueOf(TODOSemesterTable.from(studentCursor).getContactId());
//			} while (studentCursor.moveToNext());
//		}
//
//		studentCursor.close();
//
//		return contactIds;
//	}

	@Override
	public void onResume() {
		super.onResume();
		getLoaderManager().restartLoader(ILoader.STUDENT_LOADER_ID, null, this);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		((SimpleCursorAdapter) getListAdapter()).swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		((SimpleCursorAdapter) getListAdapter()).swapCursor(null);
	}
}
