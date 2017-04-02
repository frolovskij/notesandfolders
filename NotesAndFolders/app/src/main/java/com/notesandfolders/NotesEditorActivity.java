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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NotesEditorActivity extends Activity {
  private static final int DIALOG_SAVE = 0;

  private NodeHelper nh;
  private EditText textContent;
  private TextView name;
  private ImageButton saveButton;
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
      showDialog(DIALOG_SAVE);
    }
  }

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.noteseditor);

    nh = new NodeHelper(this, new TempStorage(this).getPassword());

    id = getIntent().getExtras().getLong("note_id");
    initialText = nh.getTextContentById(id);

    textContent = (EditText) findViewById(R.id.noteseditor_note_text);
    textContent.setText(initialText);

    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    if (sharedPrefs.getBoolean("editor_cursor_to_end", true)) {
      textContent.setSelection(textContent.getText().length());
    }

    name = (TextView) findViewById(R.id.noteseditor_name);
    Node n = nh.getNodeById(id);
    name.setText(n.getName());

    saveButton = (ImageButton) findViewById(R.id.noteseditor_save_button);
    saveButton.setOnClickListener(saveButtonOnClickListener);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.noteseditor_options, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.noteseditor_options_insert_datetime:
        String datetime = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date());
        textContent.getText().insert(textContent.getSelectionStart(), datetime);
        return true;
    }
    return false;
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    if (id == DIALOG_SAVE) {
      return new AlertDialog.Builder(this).setTitle(R.string.noteseditor_title)
          .setMessage(R.string.noteseditor_msg_save_before_exit)
          .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
              save();
              superOnBackPressed();
            }
          }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
              superOnBackPressed();
            }
          }).create();
    }
    return super.onCreateDialog(id);
  }
}