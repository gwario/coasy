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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import android.app.Fragment;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import at.ameise.coasy.R;
import at.ameise.coasy.activity.MainActivity;
import at.ameise.coasy.domain.Course;
import at.ameise.coasy.util.Logger;

/**
 * The course list.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public class NewCourseFragment extends Fragment implements OnClickListener {

	public static final String TAG = "NewCourseF";

	private static final String PATTERN_TITLE = "[\\d\\s\\w]+";
	private static final String PATTERN_DESCRIPTION = ".*";

	private static final CharSequence ERROR_DESCRIPTION = "";

	private static final CharSequence ERROR_TITLE = "Invalid title: Use only alphanumeric characters!";

	private EditText etTitle;
	private EditText etDescription;
	private AutoCompleteTextView etAddress;

	private Button bDone;

	private Geocoder geocoder;

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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		geocoder = new Geocoder(getActivity(), Locale.getDefault());
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
		etAddress.addTextChangedListener(new TimedTextWatcher(2000) {
			@Override
			protected void onTimeout(Editable s) {
				if (getActivity() != null)// this is in case of device sleep
					new AsyncAddressSuggestionLoader(getActivity(), geocoder, etAddress).execute(s.toString());
			}
		});
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
	 * Creates a new {@link Course} from the provided data.
	 * 
	 * @return true if the course was created, otherwise false.
	 */
	private boolean createCourse() {
		// TODO Auto-generated method stub
		return true;
	}

	private static final class AsyncAddressSuggestionLoader extends AsyncTask<String, Integer, List<Address>> {

		private Context context;
		private Geocoder geocoder;
		private AutoCompleteTextView textView;

		public AsyncAddressSuggestionLoader(Context context, Geocoder geocoder, AutoCompleteTextView textView) {
			this.context = context.getApplicationContext();
			this.geocoder = geocoder;
			this.textView = textView;
		}

		@Override
		protected List<Address> doInBackground(String... params) {

			try {
				return geocoder.getFromLocationName(params[0], 5);
			} catch (IOException e) {
				Logger.error(TAG, "Failed to get address suggestions.", e);
			}
			return new ArrayList<Address>();
		}

		@Override
		protected void onPostExecute(List<Address> addresses) {

			Logger.info(TAG, "Got " + addresses.size() + " address suggestion.");
			Set<String> addressStrings = new HashSet<String>(addresses.size());
			for (Address address : addresses) {

				if (address.getMaxAddressLineIndex() > -1) {

					List<String> parts = new ArrayList<String>();
					for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {

						String lineString = address.getAddressLine(i);
						if (!lineString.isEmpty())
							parts.add(lineString);
					}
					String addressString = StringUtils.join(parts, ", ");

					if (!addressString.isEmpty()) {
						Logger.verbose(TAG, "Suggestion: " + addressString);
						addressStrings.add(addressString);
					}
				}
			}

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.fragment_course_new_address_suggestion_listitem,
					R.id.listitem_course_new_address, addressStrings.toArray(new String[addressStrings.size()]));
			textView.setAdapter(adapter);
			adapter.notifyDataSetChanged();
		}
	}

	/**
	 * Callbacks are
	 * 
	 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
	 * 
	 */
	private static abstract class TimedTextWatcher implements TextWatcher {

		private long timeout;
		private Handler timeoutHandler;

		/**
		 * @param timeout
		 *            timeout in milliseconds.
		 */
		public TimedTextWatcher(long timeout) {
			this.timeout = timeout;
			this.timeoutHandler = new Handler();
		}

		/**
		 * Called on timeout between text changes.
		 * 
		 * @param s
		 */
		protected abstract void onTimeout(Editable s);

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(final Editable s) {

			timeoutHandler.removeCallbacksAndMessages(null);
			timeoutHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					onTimeout(s);
				}
			}, timeout);
		}

	}

}
