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
		mPreferences = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
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

	public void setCheckListItem(CheckListItem item) {
		if (item != null) {
			SharedPreferences.Editor editor = mPreferences.edit();
			editor.putString("checklist_item_text", item.getText());
			editor.putBoolean("checklist_item_checked", item.isChecked());
			editor.commit();
		}
	}

	public CheckListItem getCheckListItem() {
		String text = mPreferences.getString("checklist_item_text", null);
		boolean isChecked = mPreferences.getBoolean("checklist_item_checked", false);

		return (text == null) ? null : new CheckListItem(text, isChecked);
	}

	public SearchParameters getSearchParameters() {
		return (SearchParameters) Serializer.deserialize(mPreferences.getString(
				"search_parameters", ""));
	}

	public void setSearchParameters(SearchParameters sp) {
		if (sp != null) {
			SharedPreferences.Editor editor = mPreferences.edit();
			editor.putString("search_parameters", Serializer.serialize(sp));
			editor.commit();
		}
	}

	public void deleteSearchParameters() {
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.remove("search_parameters");
		editor.commit();
	}

}
