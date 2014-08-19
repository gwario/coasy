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
import android.widget.Toast;
import at.ameise.coasy.R;
import at.ameise.coasy.activity.MainActivity;
import at.ameise.coasy.domain.Course;
import at.ameise.coasy.domain.persistence.IPersistenceManager;
import at.ameise.coasy.domain.persistence.ProductionPersistenceManager;
import at.ameise.coasy.util.AsyncAddressSuggestionLoader;
import at.ameise.coasy.util.Logger;
import at.ameise.coasy.util.TimeoutTextWatcher;

/**
 * The course list.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public class CourseNewFragment extends Fragment implements OnClickListener {

	public static final String TAG = "CourseNewF";

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
	public static CourseNewFragment newInstance() {

		CourseNewFragment fragment = new CourseNewFragment();

		Bundle args = new Bundle();
		fragment.setArguments(args);

		return fragment;
	}

	public CourseNewFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		geocoder = new Geocoder(getActivity(), Locale.getDefault());

		pm = ProductionPersistenceManager.getInstance(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_course_new, container, false);

		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (getActivity() instanceof MainActivity)
			((MainActivity) getActivity()).onFragmentAttached(getString(R.string.title_activity_newcourse));
		else
			getActivity().setTitle(getString(R.string.title_activity_newcourse));

		etTitle = (EditText) view.findViewById(R.id.fragment_course_new_etTitle);
		etDescription = (EditText) view.findViewById(R.id.fragment_course_new_etDescription);
		etAddress = (AutoCompleteTextView) view.findViewById(R.id.fragment_course_new_etAddress);
		etAddress.addTextChangedListener(new TimeoutTextWatcher(2000) {
			@Override
			public void onTextChangedTimeout(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChangedTimeout(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChangedTimeout(Editable s) {
				if (getActivity() != null)// this is in case of device sleep
					new AsyncAddressSuggestionLoader(getActivity(), geocoder, 5, etAddress, R.layout.fragment_course_new_address_suggestion_list_item,
							R.id.listitem_course_new_address).execute(s.toString());
			}
		});
		bDone = (Button) view.findViewById(R.id.fragment_course_new_bDone);

		bDone.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {

		case R.id.fragment_course_new_bDone:
			if (validateInputs())
				if (createCourse())
					getActivity().finish();
				else
					Toast.makeText(getActivity(), "Failed to create Course!", Toast.LENGTH_SHORT).show();
			break;

		default:
			break;
		}
	}

	/**
	 * This method not only validates the input fields, it also sets the error
	 * messages.
	 * 
	 * @return true if the entered data is valid to create a {@link Course},
	 *         false otherwise.
	 */
	private boolean validateInputs() {

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
	 * Creates a new {@link Course} from the provided data.
	 * 
	 * @return true if the course was created, otherwise false.
	 */
	private boolean createCourse() {

		return pm.create(new Course(etTitle.getText().toString(), etDescription.getText().toString()));
	}

}
