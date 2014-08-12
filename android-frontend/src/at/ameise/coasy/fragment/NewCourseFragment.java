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

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import at.ameise.coasy.R;
import at.ameise.coasy.activity.MainActivity;
import at.ameise.coasy.domain.Course;

/**
 * The course list.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public class NewCourseFragment extends Fragment implements OnClickListener {

	public static final String TAG = "NewCourseF";

	private EditText etTitle;
	private EditText etDescription;
	private Button bDone;

	/**
	 * Returns a new instance of this fragment. <br>
	 * <br>
	 * Use this method when displaying the fragment in its own activity!
	 */
	public static NewCourseFragment newInstance() {

		NewCourseFragment fragment = new NewCourseFragment();

		Bundle args = new Bundle();
		fragment.setArguments(args);

		return fragment;
	}

	public NewCourseFragment() {
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
		bDone = (Button) view.findViewById(R.id.fragment_course_new_bDone);

		bDone.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {

		case R.id.fragment_course_new_bDone:
			if (isDataValid())
				if (createCourse())
					getActivity().finish();
				else
					highlightInvalidData();
			break;

		default:
			break;
		}
	}

	/**
	 * Highlights the invalid data.
	 */
	private void highlightInvalidData() {
		// TODO Auto-generated method stub
	}

	/**
	 * @return true if the entered data is valid to create a {@link Course},
	 *         false otherwise.
	 */
	private boolean isDataValid() {
		// TODO Auto-generated method stub
		return true;
	}

	/**
	 * Creates a new {@link Course} from the provided data.
	 * 
	 * @return true if the course was created, otherwise false.
	 */
	private boolean createCourse() {
		// TODO Auto-generated method stub
		return true;
	}

}
