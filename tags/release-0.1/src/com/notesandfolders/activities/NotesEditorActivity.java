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

import java.text.SimpleDateFormat;
import java.util.Date;

import com.notesandfolders.Login;
import com.notesandfolders.R;
import com.notesandfolders.dataaccess.NodeHelper;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
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
	private String initialText;
	private long id;

	public void save() {
		nh.setTextContentById(id, textContent.getText().toString());
	}

	final private OnClickListener saveButtonOnClickListener = new OnClickListener() {
		public void onClick(View v) {
			if (v != null && v == saveButton) {
				save();
				finish();
			}
		}
	};

	public void superOnBackPressed() {
		super.onBackPressed();
	}

	@Override
	public void onBackPressed() {
		if (textContent.getText().toString().equals(initialText)) {
			// if text wasn't changed
			superOnBackPressed();
		} else {
			// if was changed
			new AlertDialog.Builder(this).setTitle(R.string.noteseditor_title)
					.setMessage(R.string.noteseditor_msg_save_before_exit)
					.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							save();
							superOnBackPressed();
						}
					}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							superOnBackPressed();
						}
					}).
					// setNeutralButton(R.string.cancel, new
					// DialogInterface.OnClickListener() {
					// public void onClick(DialogInterface dialog, int
					// whichButton) {
					// dialog.cancel();
					// }
					// }).
					show();
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.noteseditor);

		nh = new NodeHelper(this, Login.getPlainTextPasswordFromTempStorage(this));

		id = getIntent().getExtras().getLong("note_id");
		initialText = nh.getTextContentById(id);

		textContent = (EditText) findViewById(R.id.noteseditor_note_text);
		textContent.setText(initialText);
		registerForContextMenu(textContent);

		name = (TextView) findViewById(R.id.noteseditor_name);
		name.setText(nh.getFullPathById(id));

		saveButton = (Button) findViewById(R.id.noteseditor_save_button);
		saveButton.setOnClickListener(saveButtonOnClickListener);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenu.ContextMenuInfo menuInfo) {
		if (view == textContent) {
			menu.add(Menu.NONE, Menu.NONE, Menu.NONE, R.string.insert_date_time)
					.setOnMenuItemClickListener(new OnMenuItemClickListener() {
						public boolean onMenuItemClick(MenuItem item) {
							textContent.append(new SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
									.format(new Date()));
							return false;
						}
					});
		}

		super.onCreateContextMenu(menu, view, menuInfo);
	}
}