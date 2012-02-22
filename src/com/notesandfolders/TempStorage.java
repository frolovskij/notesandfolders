/*
Copyright 2012 Фроловский Алексей

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

This file is a part of Notes & Folders project.
 */

package com.notesandfolders;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Temporal storage that do not persist across session The data is stored in
 * Shared Preferences
 */
public class TempStorage {
	private SharedPreferences mPreferences;
	private static final String PREF_NAME = "temp";

	public TempStorage(Context ctx) {
		mPreferences = ctx
				.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
	}

	public String getPassword() {
		return mPreferences.getString("password", Settings.EMPTY_PASSWORD);
	}

	public void setPassword(String password) {
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putString("password", password);
		editor.commit();
	}

	public void setExiting() {
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putBoolean("exiting", true);
		editor.commit();
	}

	public boolean isExiting() {
		return mPreferences.getBoolean("exiting", false);
	}

	public void deleteAll() {
		SharedPreferences.Editor editor = mPreferences.edit();
		for (String key : mPreferences.getAll().keySet()) {
			editor.remove(key);
		}
		editor.commit();
	}

}
