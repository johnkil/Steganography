package com.shishkin.steganographie.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.shishkin.steganographie.R;

/**
 * Starting activity to configure the application
 * 
 * @author e.shishkin
 *
 */
public class PreferencesActivity extends SherlockPreferenceActivity {
	private static final String LOG_TAG = PreferencesActivity.class.getSimpleName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v(LOG_TAG, "onCreate() called");
		super.onCreate(savedInstanceState);
		// inflate data from xml
		addPreferencesFromResource(R.xml.preferences);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.v(LOG_TAG, "onOptionsItemSelected() called");
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent homeIntent = new Intent(this, MainActivity.class);
			homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(homeIntent);
			break;

		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

}
