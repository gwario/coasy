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

import java.util.Locale;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.database.Cursor;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import at.ameise.coasy.R;
import at.ameise.coasy.activity.MainActivity;
import at.ameise.coasy.domain.Course;
import at.ameise.coasy.domain.persistence.IPersistenceManager;
import at.ameise.coasy.domain.persistence.ProductionPersistenceManager;
import at.ameise.coasy.domain.persistence.database.CourseTable;
import at.ameise.coasy.domain.persistence.database.ILoader;
import at.ameise.coasy.util.AsyncAddressSuggestionLoader;
import at.ameise.coasy.util.Logger;
import at.ameise.coasy.util.TimeoutTextWatcher;

/**
 * The course list.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public class EditCourseFragment extends Fragment implements OnClickListener, LoaderCallbacks<Cursor> {

	public static final String TAG = "EditCourseF";

	/**
	 * The {@link Course} to be displayed.
	 */
	public static final String ARG_COURSE_ID = "course_id";

	/**
	 * The {@link Course} to be displayed.
	 */
	private Course mCourse = null;
	
	private static final String PATTERN_TITLE = "[\\d\\s\\w]+";
	private static final String PATTERN_DESCRIPTION = ".*";

	private static final CharSequence ERROR_DESCRIPTION = "";

	private static final CharSequence ERROR_TITLE = "Invalid title: Use only alphanumeric characters!";

	private EditText etTitle;
	private EditText etDescription;
	private AutoCompleteTextView etAddress;

	private Button bDone;

	private Geocoder geocoder;
	
	private IPersistenceManager pm;

	/**
	 * Returns a new instance of this fragment. <br>
	 * <br>
	 * Use this method when displaying the fragment in its own activity!
	 */
	public static EditCourseFragment newInstance(long courseId) {

		EditCourseFragment fragment = new EditCourseFragment();

		Bundle args = new Bundle();
		args.putLong(ARG_COURSE_ID, courseId);
		fragment.setArguments(args);

		return fragment;
	}
	
	public EditCourseFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		geocoder = new Geocoder(getActivity(), Locale.getDefault());
		pm = ProductionPersistenceManager.getInstance(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_course_edit, container, false);

		if (getLoaderManager().getLoader(ILoader.COURSE_DETAIL_LOADER_ID) != null)
			getLoaderManager().restartLoader(ILoader.COURSE_DETAIL_LOADER_ID, getArguments(), this);
		else
			getLoaderManager().initLoader(ILoader.COURSE_DETAIL_LOADER_ID, null, this);
		
		return rootView;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Logger.verbose(TAG, "onCreateLoader: id = " + id);

		return pm.courseCursorLoader(getArguments().getLong(ARG_COURSE_ID));//new CursorLoader(getActivity(), Uri.parse(CoasyContentProvider.CONTENT_URI_COURSE + "/" + getArguments().getLong(ARG_COURSE_ID)), null, null, null, null);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (getActivity() instanceof MainActivity)
			((MainActivity) getActivity()).onFragmentAttached(getString(R.string.title_activity_newcourse));
		else
			getActivity().setTitle(getString(R.string.title_activity_newcourse));

		etTitle = (EditText) view.findViewById(R.id.fragment_course_edit_etTitle);
		etDescription = (EditText) view.findViewById(R.id.fragment_course_edit_etDescription);
		etAddress = (AutoCompleteTextView) view.findViewById(R.id.fragment_course_edit_etAddress);
		etAddress.addTextChangedListener(new TimeoutTextWatcher(2000) {
			@Override
			public void afterTextChangedTimeout(Editable s) {
				if (getActivity() != null)// this is in case of device sleep
					new AsyncAddressSuggestionLoader(getActivity(), geocoder, 5, etAddress, R.layout.fragment_course_edit_address_suggestion_list_item,
							R.id.listitem_course_edit_address).execute(s.toString());
			}
			@Override
			public void onTextChangedTimeout(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void beforeTextChangedTimeout(CharSequence s, int start, int count, int after) {
			}
		});
		bDone = (Button) view.findViewById(R.id.fragment_course_edit_bDone);

		bDone.setOnClickListener(this);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

		cursor.moveToFirst();
		mCourse = CourseTable.fromCoursesCursor(cursor);

		etTitle.setText(mCourse.getTitle());
		etDescription.setText(mCourse.getDescription());
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mCourse = null;

		etTitle.setText("Loading...");
		etDescription.setText("Loading...");
	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {

		case R.id.fragment_course_edit_bDone:
			if (isDataValid())
				if (updateCourse())
					getActivity().finish();
			break;
			
		case R.id.fragment_course_edit_bAddContacts:
			getFragmentManager().beginTransaction().add(R.id.fragment_course_edit_container, ContactsListFragment.newInstance(mCourse.getId()), ContactsListFragment.TAG)
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
			break;

		default:
			break;
		}
	}

	/**
	 * @return true if the entered data is valid to create a {@link Course},
	 *         false otherwise.
	 */
	private boolean isDataValid() {

		boolean valid = true;

		if (!etTitle.getText().toString().trim().matches(PATTERN_TITLE)) {
			valid = false;
			Logger.warn(TAG, "Title('" + etTitle.getText() + "') does not match pattern!");
			etTitle.requestFocus();
			etTitle.setError(ERROR_TITLE);
		}

		if (!etDescription.getText().toString().trim().matches(PATTERN_DESCRIPTION)) {
			valid = false;
			Logger.warn(TAG, "Description('" + etDescription.getText() + "') does not match pattern!");
			etDescription.requestFocus();
			etDescription.setError(ERROR_DESCRIPTION);
		}

		return valid;
	}

	/**
	 * Updates the {@link Course} from the provided data.
	 * 
	 * @return true if the course was created, otherwise false.
	 */
	private boolean updateCourse() {
		// TODO Auto-generated method stub
		return true;
	}

}
