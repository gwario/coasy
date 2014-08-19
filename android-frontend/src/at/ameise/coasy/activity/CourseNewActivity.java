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
package at.ameise.coasy.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import at.ameise.coasy.fragment.CourseNewFragment;

/**
 * {@link Activity} for adding a new course on handsets.
 * 
 * @author Mario Gastegger <mario DOT gastegger AT gmail DOT com>
 * 
 */
public final class CourseNewActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {

			loadFragment(getIntent());

		} else {

			reloadFragment(savedInstanceState);
		}
	}

	/**
	 * Loads the fragment.
	 * 
	 * @param intent
	 */
	private void loadFragment(Intent intent) {

		getFragmentManager().beginTransaction().add(android.R.id.content, CourseNewFragment.newInstance()).commit();
	}

	/**
	 * Reloads the fragment.
	 * 
	 * @param bundle
	 */
	private void reloadFragment(Bundle bundle) {

		getFragmentManager().beginTransaction().replace(android.R.id.content, CourseNewFragment.newInstance()).commit();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		
		if(getIntent() != null && getIntent().getExtras() != null)
			outState.putAll(getIntent().getExtras());
		
		super.onSaveInstanceState(outState);
	}

	/*
	 * This method is called after onStart() when the activity is being
	 * re-initialized from a previously saved state, given here in
	 * savedInstanceState. Most implementations will simply use onCreate(Bundle)
	 * to restore their state, but it is sometimes convenient to do it here
	 * after all of the initialization has been done or to allow subclasses to
	 * decide whether to use your default implementation.
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		reloadFragment(intent.getExtras());
	}

}
