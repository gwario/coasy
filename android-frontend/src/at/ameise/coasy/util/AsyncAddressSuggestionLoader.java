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
package at.ameise.coasy.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

/**
 * Provides asynchronously loaded address suggestions for
 * {@link AutoCompleteTextView}s.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public final class AsyncAddressSuggestionLoader extends AsyncTask<String, Integer, List<Address>> {

	private static final String TAG = "AsyncAddressLoader";

	private Context context;
	private Geocoder geocoder;
	private int maxResults;
	private AutoCompleteTextView textView;
	private int resource;
	private int textViewResourceId;

	/**
	 * Provides asynchronously loaded address suggestions for
	 * {@link AutoCompleteTextView}s.
	 * 
	 * @param context
	 * @param geocoder
	 * @param maxResults
	 *            See {@link Geocoder#getFromLocationName(String, int)}
	 * @param textView
	 * @param resource
	 *            See {@link ArrayAdapter#ArrayAdapter(Context, int, int)}
	 * @param textViewResourceId
	 *            See {@link ArrayAdapter#ArrayAdapter(Context, int, int)}
	 */
	public AsyncAddressSuggestionLoader(Context context, Geocoder geocoder, int maxResults, AutoCompleteTextView textView, int resource, int textViewResourceId) {
		this.context = context.getApplicationContext();
		this.geocoder = geocoder;
		this.maxResults = maxResults;
		this.textView = textView;
		this.resource = resource;
		this.textViewResourceId = textViewResourceId;
	}

	@Override
	protected List<Address> doInBackground(String... params) {

		try {
			return geocoder.getFromLocationName(params[0], maxResults);
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

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, resource, textViewResourceId,
				addressStrings.toArray(new String[addressStrings.size()]));

		textView.setAdapter(adapter);

		adapter.notifyDataSetChanged();
	}
}