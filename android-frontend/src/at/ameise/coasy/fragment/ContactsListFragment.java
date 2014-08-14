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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import at.ameise.coasy.R;
import at.ameise.coasy.domain.Course;
import at.ameise.coasy.domain.persistence.IPersistenceManager;
import at.ameise.coasy.domain.persistence.ProductionPersistenceManager;
import at.ameise.coasy.domain.persistence.database.ILoader;
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

	private Button bDone;
	
	private IPersistenceManager pm;

	/**
	 * Returns a new instance of this fragment. <br>
	 * <br>
	 * Use this method when displaying the fragment in its own!
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

		View rootView = inflater.inflate(R.layout.fragment_contacts_list, container, false);

		if (getLoaderManager().getLoader(ILoader.CONTACTS_LOADER_ID) != null)
			getLoaderManager().restartLoader(ILoader.CONTACTS_LOADER_ID, getArguments(), this);
		else
			getLoaderManager().initLoader(ILoader.CONTACTS_LOADER_ID, null, this);

		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		bDone = (Button) view.findViewById(R.id.fragment_contacts_bDone);

		bDone.setOnClickListener(this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// return ContactContractUtil.getCoursesLoader(getActivity());
		Logger.verbose(TAG, "onCreateLoader: id = " + id);
		
		return pm.contactsCoursorLoader();// new CursorLoader(getActivity(), Uri.parse(ContactsContract.Contacts.CONTENT_URI + "/" + getArguments().getLong(ARG_COURSE_ID)), null, null, null, null);
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

		case R.id.fragment_contacts_bDone:
			/*
			 * TODO add all checked contacts to the group or set and unset them
			 * in click list item.
			 */
			getFragmentManager().beginTransaction().remove(this).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE).commit();
			break;

		default:
			break;
		}
	}
}
