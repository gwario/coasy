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

import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import at.ameise.coasy.R;
import at.ameise.coasy.domain.Course;
import at.ameise.coasy.domain.persistence.IPersistenceManager;
import at.ameise.coasy.domain.persistence.ProductionPersistenceManager;
import at.ameise.coasy.domain.persistence.database.ILoader;
import at.ameise.coasy.domain.persistence.database.StudentTable;
import at.ameise.coasy.exception.CoasyError;
import at.ameise.coasy.util.Logger;

/**
 * A list of all contacts to add to a course.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public class ContactsListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, OnClickListener {

	public static final String TAG = "ContactsListF";

	/**
	 * The {@link Course} to be displayed.
	 */
	public static final String ARG_COURSE_ID = "course_id";
	/**
	 * Flag which indicates wheter or not the user is able to remove contacts
	 * from the course.
	 */
	private static final String ARG_REMOVE = "remove";

	private TextView tvEmptyList;
	private Button bDone;

	private IPersistenceManager pm;

	/**
	 * Returns a new instance of this fragment. <br>
	 * <br>
	 * Use this method when showing or removing contacts from a course!
	 * 
	 * @param courseId
	 * @param remove
	 *            if true, on click, the student is removed from the course.
	 * @return
	 */
	public static ContactsListFragment newInstance(long courseId, boolean remove) {

		ContactsListFragment fragment = new ContactsListFragment();

		Bundle args = new Bundle();
		args.putLong(ARG_COURSE_ID, courseId);
		args.putBoolean(ARG_REMOVE, remove);
		fragment.setArguments(args);

		return fragment;
	}

	/**
	 * Returns a new instance of this fragment. <br>
	 * <br>
	 * Use this method when adding contacts to a course!
	 * 
	 * @param courseId
	 * @return
	 */
	public static ContactsListFragment newInstance(long courseId) {

		ContactsListFragment fragment = new ContactsListFragment();

		Bundle args = new Bundle();
		args.putLong(ARG_COURSE_ID, courseId);
		fragment.setArguments(args);

		return fragment;
	}

	public ContactsListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		pm = ProductionPersistenceManager.getInstance(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final View rootView = inflater.inflate(R.layout.fragment_contacts_list, container, false);
		final String[] from;
		final int[] to;

		if (getArguments().containsKey(ARG_REMOVE)) {

			from = new String[] { StudentTable.COL_ID, StudentTable.COL_DISPLAY_NAME, };
			to = new int[] { R.id.listitem_contacts_tvId, R.id.listitem_contacts_tvTitle, };

			if (getLoaderManager().getLoader(ILoader.IN_COURSE_CONTACTS_LOADER_ID) != null)
				getLoaderManager().restartLoader(ILoader.IN_COURSE_CONTACTS_LOADER_ID, getArguments(), this);
			else
				getLoaderManager().initLoader(ILoader.IN_COURSE_CONTACTS_LOADER_ID, null, this);

		} else {

			from = new String[] { ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, };
			to = new int[] { R.id.listitem_contacts_tvId, R.id.listitem_contacts_tvTitle, };

			if (getLoaderManager().getLoader(ILoader.NOT_IN_COURSE_CONTACTS_LOADER_ID) != null)
				getLoaderManager().restartLoader(ILoader.NOT_IN_COURSE_CONTACTS_LOADER_ID, getArguments(), this);
			else
				getLoaderManager().initLoader(ILoader.NOT_IN_COURSE_CONTACTS_LOADER_ID, null, this);
		}

		setListAdapter(new SimpleCursorAdapter(getActivity(), R.layout.fragment_contacts_list_item, null, from, to, 0));

		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		tvEmptyList = (TextView) view.findViewById(android.R.id.empty);
		bDone = (Button) view.findViewById(R.id.fragment_contacts_bClose);

		bDone.setOnClickListener(this);

		if (getArguments().containsKey(ARG_REMOVE)) {

			tvEmptyList.setText(R.string.fragment_contacts_students_emptyList);

		} else {

			tvEmptyList.setText(R.string.fragment_contacts_contacts_emptyList);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		Logger.verbose(TAG, "onCreateLoader: id = " + id);

		switch (id) {

		case ILoader.IN_COURSE_CONTACTS_LOADER_ID:
			return pm.studentsInCourseCoursorLoader(getArguments().getLong(ARG_COURSE_ID));

		case ILoader.NOT_IN_COURSE_CONTACTS_LOADER_ID:
			return pm.contactsNotInCourseCursorLoader(getArguments().getLong(ARG_COURSE_ID));

		default:
			throw new CoasyError("Unhandled loader! id: " + id);
		}
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
	public void onListItemClick(ListView l, View v, int position, long contactId) {
		super.onListItemClick(l, v, position, contactId);

		if (getArguments().containsKey(ARG_REMOVE)) {
			// we want to show or remove contacts

			if (getArguments().getBoolean(ARG_REMOVE)) {
				// we want to remove
				boolean success = pm.removeStudentFromCourse(contactId, getArguments().getLong(ARG_COURSE_ID));
				if (!success) {
					Toast.makeText(getActivity(), "Failed to remove student from course.", Toast.LENGTH_SHORT).show();
				}

			} else {
				// we want to show
				// do nothing....
			}

		} else {
			// we want to add contacts
			boolean success = pm.addStudentToCourse(contactId, getArguments().getLong(ARG_COURSE_ID));
			if (!success) {
				Toast.makeText(getActivity(), "Failed to add student to course.", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {

		case R.id.fragment_contacts_bClose:
			getFragmentManager().beginTransaction().remove(this).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE).commit();
			break;

		default:
			break;
		}
	}
}
