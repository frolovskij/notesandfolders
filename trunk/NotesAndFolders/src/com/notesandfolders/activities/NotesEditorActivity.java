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

package com.notesandfolders.activities;

import com.notesandfolders.R;
import com.notesandfolders.R.id;
import com.notesandfolders.R.layout;
import com.notesandfolders.dataaccess.NodeHelper;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class NotesEditorActivity extends BaseActivity {
	private NodeHelper nh;
	private EditText textContent;
	private TextView name;
	private Button saveButton;
	private long id;

	final private OnClickListener saveButtonOnClickListener = new OnClickListener() {
		public void onClick(View v) {
			if (v != null && v == saveButton) {
				nh.setTextContentById(id, textContent.getText().toString());
				finish();
			}
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.noteseditor);

		nh = new NodeHelper(this, getIntent().getExtras().getString("password"));

		id = getIntent().getExtras().getLong("note_id");
		String tc = nh.getTextContentById(id);

		textContent = (EditText) findViewById(R.id.noteseditor_note_text);
		textContent.setText(tc);

		name = (TextView) findViewById(R.id.noteseditor_name);
		name.setText(nh.getFullPathById(id));

		saveButton = (Button) findViewById(R.id.noteseditor_save_button);
		saveButton.setOnClickListener(saveButtonOnClickListener);
	}
}