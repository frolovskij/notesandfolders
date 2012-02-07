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

import com.notesandfolders.dataaccess.NodeHelper;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class NotesViewerActivity extends BaseActivity {
	private NodeHelper nh;
	private TextView textContent;
	private TextView name;
	private Button editButton;

	private long id;

	final private OnClickListener editButtonOnClickListener = new OnClickListener() {
		public void onClick(View v) {
			if (v != null && v == editButton) {
				Intent editor = new Intent(NotesViewerActivity.this, NotesEditorActivity.class);
				editor.putExtra("note_id", id);
				editor.putExtra("password", getIntent().getExtras().getString("password"));
				startActivity(editor);
			}
		}
	};

	@Override
	public void onResume() {
		super.onRestart();

		String tc = nh.getTextContentById(id);
		textContent.setText(tc);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notesviewer);

		nh = new NodeHelper(this, getIntent().getExtras().getString("password"));
		id = getIntent().getExtras().getLong("note_id");

		textContent = (TextView) findViewById(R.id.notesviewer_note_text_view);

		name = (TextView) findViewById(R.id.notesviewer_name);
		name.setText(nh.getFullPathById(id));

		editButton = (Button) findViewById(R.id.notesviewer_edit_button);
		editButton.setOnClickListener(editButtonOnClickListener);
	}
}