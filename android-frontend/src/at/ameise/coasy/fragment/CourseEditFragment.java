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

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import at.ameise.coasy.R;
import at.ameise.coasy.activity.MainActivity;
import at.ameise.coasy.domain.Course;
import at.ameise.coasy.domain.persistence.IPersistenceManager;
import at.ameise.coasy.domain.persistence.ProductionPersistenceManager;
import at.ameise.coasy.domain.persistence.database.CourseTable;
import at.ameise.coasy.domain.persistence.database.ILoader;
import at.ameise.coasy.exception.CoasyError;
import at.ameise.coasy.util.AsyncAddressSuggestionLoader;
import at.ameise.coasy.util.Logger;
import at.ameise.coasy.util.TimeoutTextWatcher;

/**
 * The course list.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public class CourseEditFragment extends Fragment implements OnClickListener, LoaderCallbacks<Cursor> {

	public static final String TAG = "CourseEditF";

	/**
	 * The {@link Course} to be displayed.
	 */
	public static final String ARG_COURSE_ID = "course_id";

	public static final int REQUEST_CODE_ADD_CONTACT = 1;

	/**
	 * The {@link Course} to be displayed.
	 */
	private Course mCourse = null;

	private EditText etTitle;
	private EditText etDescription;
	private AutoCompleteTextView etAddress;

	private Button bAddStudent;
	private Button bRemoveStudent;
	private Button bDone;

	private Geocoder geocoder;

	private IPersistenceManager pm;

	/**
	 * Returns a new instance of this fragment. <br>
	 * <br>
	 * Use this method when displaying the fragment in its own activity!
	 */
	public static CourseEditFragment newInstance(long courseId) {

		CourseEditFragment fragment = new CourseEditFragment();

		Bundle args = new Bundle();
		args.putLong(ARG_COURSE_ID, courseId);
		fragment.setArguments(args);

		return fragment;
	}

	public CourseEditFragment() {
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
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (getActivity() instanceof MainActivity)
			((MainActivity) getActivity()).onFragmentAttached(getString(R.string.title_activity_editcourse));
		else
			getActivity().setTitle(getString(R.string.title_activity_editcourse));

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
		bAddStudent = (Button) view.findViewById(R.id.fragment_course_edit_bAddStudent);
		bRemoveStudent = (Button) view.findViewById(R.id.fragment_course_edit_bRemoveStudents);

		bDone.setOnClickListener(this);
		bAddStudent.setOnClickListener(this);
		bRemoveStudent.setOnClickListener(this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Logger.verbose(TAG, "onCreateLoader: id = " + id);

		switch (id) {

		case ILoader.COURSE_DETAIL_LOADER_ID:
			return pm.courseCursorLoader(getArguments().getLong(ARG_COURSE_ID));

		default:
			throw new CoasyError("Unhandled loader! id: " + id);
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

		switch (loader.getId()) {

		case ILoader.COURSE_DETAIL_LOADER_ID:
			cursor.moveToFirst();
			mCourse = CourseTable.fromCoursesCursor(cursor);

			etTitle.setText(mCourse.getTitle());
			etDescription.setText(mCourse.getDescription());
			etAddress.setText(mCourse.getAddress());
			break;

		default:
			throw new CoasyError("Unhandled loader! id: " + loader.getId());
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

		switch (loader.getId()) {

		case ILoader.COURSE_DETAIL_LOADER_ID:
			mCourse = null;

			etTitle.setText("Loading...");
			etDescription.setText("Loading...");
			break;

		default:
			throw new CoasyError("Unhandled loader! id: " + loader.getId());
		}

	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {

		case R.id.fragment_course_edit_bDone:
			
			if (hasDataChanged()) {
				
				if (isDataValid()) {
					
					if (updateCourse())
						getActivity().finish();
					else
						Toast.makeText(getActivity(), "Failed to save Course!", Toast.LENGTH_SHORT).show();
				}
				
			} else {
				
				getActivity().finish();
			}
			break;

		case R.id.fragment_course_edit_bAddStudent:
			Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
			startActivityForResult(contactPickerIntent, REQUEST_CODE_ADD_CONTACT);
			break;

		case R.id.fragment_course_edit_bRemoveStudents:
			getFragmentManager().beginTransaction()
					.add(R.id.fragment_course_edit_container, ContactsListFragment.newInstance(mCourse.getId(), true), ContactsListFragment.TAG)
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
			break;

		default:
			break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_ADD_CONTACT) {

			if (resultCode == Activity.RESULT_OK) {

				Uri resultUri = data.getData();
				Logger.debug(TAG, resultUri.getLastPathSegment());

				boolean success = pm.addStudentToCourse(Long.valueOf(resultUri.getLastPathSegment()), getArguments().getLong(ARG_COURSE_ID));
				if (!success) {
					Toast.makeText(getActivity(), "Failed to add student to course(.", Toast.LENGTH_SHORT).show();
				}

			} else {

				Logger.warn(TAG, "Add contact returned " + resultCode);
			}
		} else {

			throw new CoasyError("Unhandled requestCode: " + requestCode);
		}
	}

	/**
	 * @return true if the data of the {@link Course} has changed, false
	 *         otherwise.
	 */
	private boolean hasDataChanged() {

		return !StringUtils.equals(etTitle.getText(), mCourse.getTitle())//
				|| !StringUtils.equals(etDescription.getText(), mCourse.getDescription())//
				|| !StringUtils.equals(etAddress.getText(), mCourse.getAddress());
	}

	/**
	 * @return true if the entered data is valid to create a {@link Course},
	 *         false otherwise.
	 */
	private boolean isDataValid() {

		return Course.validateTitle(etTitle)//
				&& Course.validateDescription(etDescription)//
				&& Course.validateAddress(etAddress);
	}

	/**
	 * Updates the {@link Course} from the provided data.
	 * 
	 * @return true if the course was created, otherwise false.
	 */
	private boolean updateCourse() {

		mCourse.setTitle(etTitle.getText().toString());
		mCourse.setDescription(etDescription.getText().toString());
		mCourse.setAddress(etAddress.getText().toString());

		return pm.save(mCourse);
	}

}
