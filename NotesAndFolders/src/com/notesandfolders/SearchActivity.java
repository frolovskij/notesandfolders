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

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

public class SearchActivity extends BaseActivity {
	private Spinner mTypeSpinner;
	private Spinner mLocationSpinner;
	private ImageButton mSearchButton;
	private EditText mTextToSearch;
	private CheckBox mCaseSensitive;
	private ArrayAdapter<CharSequence> mTypeAdapter;
	private ArrayAdapter<CharSequence> mLocationAdapter;

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.search);

		mSearchButton = (ImageButton) findViewById(R.id.search_button);
		mTextToSearch = (EditText) findViewById(R.id.search_text);

		mTypeSpinner = (Spinner) findViewById(R.id.search_type_spinner);
		mTypeAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item,
				new CharSequence[] { getText(R.string.search_type_by_content),
						getText(R.string.search_type_by_name) });
		mTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mTypeSpinner.setAdapter(mTypeAdapter);

		mLocationSpinner = (Spinner) findViewById(R.id.search_where_spinner);
		mLocationAdapter = new ArrayAdapter<CharSequence>(this,
				android.R.layout.simple_spinner_item, new CharSequence[] {
						getText(R.string.search_where_current_folder),
						getText(R.string.search_where_everywhere) });
		mLocationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mLocationSpinner.setAdapter(mLocationAdapter);

		mCaseSensitive = (CheckBox) findViewById(R.id.search_case_sensitive);
	}
}