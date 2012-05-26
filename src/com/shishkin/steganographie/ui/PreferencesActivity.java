package com.shishkin.steganographie.ui;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.shishkin.steganographie.R;

/**
 * Starting activity to configure the application
 * 
 * @author e.shishkin
 *
 */
public class PreferencesActivity extends SherlockPreferenceActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// inflate data from xml
		addPreferencesFromResource(R.xml.preferences);
	}

}
