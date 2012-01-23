package com.notesandfolders;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
	Settings s;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Settings s = new Settings(this);
		addPreferencesFromResource(R.xml.preferences);
	}
}