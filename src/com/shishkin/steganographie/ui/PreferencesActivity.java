package com.shishkin.steganographie.ui;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.shishkin.steganographie.R;

/**
 * 
 * @author e.shishkin
 *
 */
public class PreferencesActivity extends SherlockPreferenceActivity {
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

}
