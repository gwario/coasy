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

import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import at.ameise.coasy.R;
import at.ameise.coasy.activity.CourseDetailsActivity;
import at.ameise.coasy.activity.MainActivity;
import at.ameise.coasy.domain.database.CourseTable;
import at.ameise.coasy.domain.database.ILoader;
import at.ameise.coasy.util.ContactContractUtil;

/**
 * The course list.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public class CourseListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static CourseListFragment newInstance(int sectionNumber) {

		CourseListFragment fragment = new CourseListFragment();

		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);

		return fragment;
	}

	public CourseListFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_courses, container, false);

		String[] from = new String[] { CourseTable.COL_TITLE, CourseTable.COL_DESCRIPTION, };
		int[] to = new int[] { R.id.listitem_course_tv_title, R.id.listitem_course_tv_description, };

		getLoaderManager().initLoader(ILoader.COURSES_LOADER_ID, null, this);

		setListAdapter(new SimpleCursorAdapter(getActivity(), R.layout.fragment_courses_listitem, null, from, to, 0));

		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Cursor c = ((SimpleCursorAdapter) getListAdapter()).getCursor();
		c.move(position);

		boolean isHandset = true;
		if (isHandset) {

			Intent i = new Intent(getActivity(), CourseDetailsActivity.class);
			i.setAction(Intent.ACTION_PICK);
			i.putExtra(CourseDetailsFragment.ARG_COURSE_ID, CourseTable.fromCourses(c).getId());
			startActivity(i);

		} else {

			getFragmentManager().beginTransaction()
					.add(R.id.container, CourseDetailsFragment.newInstance(-1, CourseTable.fromCourses(c)), CourseDetailsFragment.TAG)
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).addToBackStack(CourseDetailsFragment.TAG).commit();
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return ContactContractUtil.getCoursesLoader(getActivity());
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
