package com.notesandfolders;

import com.notesandfolders.R;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class SettingsActivity extends PreferenceActivity {
	Settings s;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.layout.preferences);
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

		if (preference != null && preference.getKey().equals("password")) {
			Intent password = new Intent(this, PasswordActivity.class);
			startActivity(password);
		}

		if (preference != null && preference.getKey().equals("documentation")) {
			Intent help = new Intent(this, HelpActivity.class);
			startActivity(help);
		}

		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

}