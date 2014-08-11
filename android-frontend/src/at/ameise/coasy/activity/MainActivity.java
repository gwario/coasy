package at.ameise.coasy.activity;

import roboguice.activity.RoboFragmentActivity;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import at.ameise.coasy.R;
import at.ameise.coasy.fragment.CourseListFragment;
import at.ameise.coasy.fragment.NavigationDrawerFragment;
import at.ameise.coasy.fragment.StudentListFragment;
import at.ameise.coasy.util.AccountUtil;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class MainActivity extends RoboFragmentActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!AccountUtil.isAccountSelected(this)) {

			final int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
			if (status == ConnectionResult.SUCCESS) {

				startActivityForResult(
						AccountPicker.newChooseAccountIntent(null, null, new String[] { GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE }, false, null, null, null, null),
						UserSettingsActivity.REQUEST_CODE_ACCOUNT_NAME);

			} else {

				GooglePlayServicesUtil.getErrorDialog(status, this, UserSettingsActivity.REQUEST_CODE_PLAY_SERVICES_NOT_AVAILABLE).show();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == UserSettingsActivity.REQUEST_CODE_ACCOUNT_NAME && resultCode == RESULT_OK) {
			String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
			AccountUtil.setSelectedGoogleAccount(this, accountName);
		}
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getFragmentManager();

		switch (position) {

		case 0:
			fragmentManager.beginTransaction().replace(R.id.container, CourseListFragment.newInstance(position + 1)).commit();
			break;

		case 1:
			fragmentManager.beginTransaction().replace(R.id.container, StudentListFragment.newInstance(position + 1)).commit();
			break;

		default:
			break;
		}
	}

	/**
	 * Sets the title according to the Attached section.
	 * 
	 * @param number
	 */
	public void onSectionAttached(int number) {

		switch (number) {
		case 1:
			mTitle = getString(R.string.title_section1);
			break;
		case 2:
			mTitle = getString(R.string.title_section2);
			break;
		}
	}

	/**
	 * Sets the title.
	 * 
	 * @param title
	 */
	public void onFragmentAttached(String title) {

		mTitle = title;
		restoreActionBar();
	}

	public void restoreActionBar() {

		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.main, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			startActivity(new Intent(this, UserSettingsActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
