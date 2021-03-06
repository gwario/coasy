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

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import at.ameise.coasy.R;
import at.ameise.coasy.activity.CourseEditActivity;
import at.ameise.coasy.activity.MainActivity;
import at.ameise.coasy.domain.Course;
import at.ameise.coasy.domain.persistence.IPersistenceManager;
import at.ameise.coasy.domain.persistence.ProductionPersistenceManager;
import at.ameise.coasy.domain.persistence.database.CourseTable;
import at.ameise.coasy.domain.persistence.database.ILoader;
import at.ameise.coasy.util.Logger;

/**
 * The a single course.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public class CourseDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, OnClickListener {

	public static final String TAG = "CourseDetailsF";

	/**
	 * The {@link Course} to be displayed.
	 */
	public static final String ARG_COURSE_ID = "course_id";

	private TextView tvDescription;
	private TextView tvAddress;
	
	private Button bShowContacts;

	/**
	 * The {@link Course} to be displayed.
	 */
	private Course mCourse = null;

	private IPersistenceManager pm;

	/**
	 * Returns a new instance of this fragment. <br>
	 * <br>
	 * Use this method when displaying the fragment in its own activity!
	 */
	public static CourseDetailsFragment newInstance(long courseId) {

		CourseDetailsFragment fragment = new CourseDetailsFragment();

		Bundle args = new Bundle();
		args.putLong(ARG_COURSE_ID, courseId);
		fragment.setArguments(args);

		return fragment;
	}

	public CourseDetailsFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);

		pm = ProductionPersistenceManager.getInstance(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_course_details, container, false);

		if (getLoaderManager().getLoader(ILoader.COURSE_DETAIL_LOADER_ID) != null)
			getLoaderManager().restartLoader(ILoader.COURSE_DETAIL_LOADER_ID, getArguments(), this);
		else
			getLoaderManager().initLoader(ILoader.COURSE_DETAIL_LOADER_ID, getArguments(), this);

		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		tvDescription = (TextView) view.findViewById(R.id.fragment_course_detail_tvDescription);
		tvAddress = (TextView) view.findViewById(R.id.fragment_course_detail_tvAddress);
		bShowContacts = (Button) view.findViewById(R.id.fragment_course_detail_bShowStudents);

		bShowContacts.setOnClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		
		getLoaderManager().restartLoader(ILoader.COURSE_DETAIL_LOADER_ID, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Logger.verbose(TAG, "onCreateLoader: id = " + id);

		return pm.courseCursorLoader(getArguments().getLong(ARG_COURSE_ID));
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

		cursor.moveToFirst();
		mCourse = CourseTable.fromCoursesCursor(cursor);

		if (getActivity() instanceof MainActivity)
			((MainActivity) getActivity()).onFragmentAttached(mCourse.getTitle());
		else {
			getActivity().setTitle(mCourse.getTitle());
		}

		tvDescription.setText(mCourse.getDescription());
		tvAddress.setText(mCourse.getAddress());
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mCourse = null;

		tvDescription.setText("Loading...");
		tvAddress.setText("Loading...");
	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {

		case R.id.fragment_course_detail_bShowStudents:
			getFragmentManager().beginTransaction().add(R.id.fragment_course_details_container, ContactsListFragment.newInstance(mCourse.getId(), false), ContactsListFragment.TAG)
			.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
			break;

		default:
			break;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.course_details, menu);

		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.action_edit) {
			Intent i = new Intent(getActivity(), CourseEditActivity.class);
			i.putExtra(ARG_COURSE_ID, mCourse.getId());
			i.setAction(Intent.ACTION_EDIT);
			startActivity(i);

			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
