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

import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

public class SearchActivity extends Activity implements OnClickListener {
	public static final int DIALOG_SEARCH = 0;

	private Spinner mTypeSpinner;
	private Spinner mLocationSpinner;
	private ImageButton mSearchButton;
	private EditText mTextToSearch;
	private CheckBox mCaseSensitive;
	private ArrayAdapter<CharSequence> mTypeAdapter;
	private ArrayAdapter<CharSequence> mLocationAdapter;
	private SearchTask searchTask;
	private boolean mShownDialog;

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
		mSearchButton.setOnClickListener(this);
		mTextToSearch = (EditText) findViewById(R.id.search_text);

		mTypeSpinner = (Spinner) findViewById(R.id.search_type_spinner);
		mTypeAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item,
				new CharSequence[] { getText(R.string.search_type_by_name_and_content),
						getText(R.string.search_type_by_content),
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

		Object retained = getLastNonConfigurationInstance();
		if (retained != null && retained instanceof SearchTask) {
			searchTask = (SearchTask) retained;
			searchTask.setActivity(this);
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		if (searchTask != null) {
			searchTask.setActivity(null);
			return searchTask;
		}

		return null;
	}

	public void onSearchTaskCompleted(List<Long> result) {
		if (mShownDialog) {
			Intent results = new Intent(this, SearchResultsActivity.class);
			results.putExtra("ids_list", Serializer.serialize(result));
			startActivity(results);
		}
	}

	public void onSearch() {
		SearchParameters mParameters = new SearchParameters();
		mParameters.setText(mTextToSearch.getText().toString());
		mParameters.setCaseSensitive(mCaseSensitive.isChecked());

		switch (mTypeSpinner.getSelectedItemPosition()) {
		case 0: // by name and content
			mParameters.setSearchInNames(true);
			mParameters.setSearchInText(true);
			break;
		case 1: // by content
			mParameters.setSearchInNames(false);
			mParameters.setSearchInText(true);
			break;
		case 2: // by name
			mParameters.setSearchInNames(true);
			mParameters.setSearchInText(false);
			break;
		}

		if (mLocationSpinner.getSelectedItemPosition() == 0) {
			// search in the folder that was opened in Explorer when search
			// activity was started
			mParameters.setFolderId(getIntent().getExtras().getLong("current_folder_id"));
		} else {
			// search everywhere
			mParameters.setFolderId(0);
		}

		new TempStorage(this).setSearchParameters(mParameters);

		searchTask = new SearchTask(this,
				new NodeHelper(this, new TempStorage(this).getPassword()), mParameters);
		searchTask.execute();
	}

	public void onClick(View v) {
		if (v == mSearchButton) {
			onSearch();
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == DIALOG_SEARCH) {
			ProgressDialog pd = new ProgressDialog(this);
			pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pd.setMessage(getText(R.string.search_msg_searching));
			pd.setCancelable(false);

			return pd;
		}

		return super.onCreateDialog(id);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		if (id == DIALOG_SEARCH) {
			mShownDialog = true;
		}
	}
}