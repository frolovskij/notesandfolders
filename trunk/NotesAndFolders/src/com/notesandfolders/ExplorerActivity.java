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
import com.notesandfolders.dataaccess.NodeHelper;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ExplorerActivity extends BaseActivity {
	private NodeHelper nh;
	private ListView lv;
	private List<Node> items;
	private NodeAdapter adapter;

	/**
	 * Id of the folder which items are currently being listed in explorer
	 */
	private long currentFolderId;

	/**
	 * Id of the node context menu is shown for
	 */
	private long selectedNodeId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		nh = new NodeHelper(this, getIntent().getExtras().getString("password"));
		currentFolderId = 0;

		setContentView(R.layout.explorer);

		update();

		registerForContextMenu(lv);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.explorer_options, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.explorer_options_add:
			onNew();
			return true;

		case R.id.explorer_options_settings:
			Intent settings = new Intent(this, SettingsActivity.class);
			startActivity(settings);
			return true;

		case R.id.explorer_options_close:
			finish();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.explorer_context, menu);

		Node selected = (Node) lv
				.getItemAtPosition(((AdapterContextMenuInfo) menuInfo).position);
		selectedNodeId = selected.getId();

		Log.i("test", selected.toString());
	}

	public void update() {
		lv = (ListView) findViewById(R.id.explorer_listview);
		items = nh.getChildrenById(currentFolderId);
		adapter = new NodeAdapter(this, R.layout.explorer_item, items);
		lv.setAdapter(adapter);
	}

	public void onNewFolder() {
		final EditText input = new EditText(this);

		new AlertDialog.Builder(this)
				.setTitle(R.string.explorer_newfolder_title)
				.setMessage(R.string.explorer_newfolder_prompt)
				.setView(input)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								String folderName = input.getText().toString();

								Node parent = nh.getNodeById(currentFolderId);
								if ((parent != null)
										&& parent.getType() == NodeType.FOLDER) {
									nh.createFolder(parent, folderName);
									update();
								}
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// Do nothing.
							}
						}).show();
	}

	public void onNewNote() {
		final EditText input = new EditText(this);

		new AlertDialog.Builder(this)
				.setTitle(R.string.explorer_newnote_title)
				.setMessage(R.string.explorer_newnote_prompt)
				.setView(input)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								String noteName = input.getText().toString();

								Node parent = nh.getNodeById(currentFolderId);
								if ((parent != null)
										&& parent.getType() == NodeType.FOLDER) {
									nh.createNode(parent, noteName, "",
											NodeType.NOTE);
									update();
								}
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// Do nothing.
							}
						}).show();
	}

	public void onNew() {
		final IconListItem[] items = {
				new IconListItem(getText(R.string.create_folder).toString(),
						R.drawable.folder),
				new IconListItem(getText(R.string.create_note).toString(),
						R.drawable.note),
				new IconListItem(getText(R.string.create_checklist).toString(),
						R.drawable.note) };

		ListAdapter adapter = new IconListItemAdapter(this,
				android.R.layout.select_dialog_item, items);

		new AlertDialog.Builder(this).setTitle(getText(R.string.create_new))
				.setAdapter(adapter, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						switch (item) {
						case 0:
							onNewFolder();
							break;
						case 1:
							onNewNote();
							break;
						case 2:
							Toast.makeText(getApplicationContext(),
									getText(R.string.msg_not_implemented_yet),
									Toast.LENGTH_SHORT).show();
							break;
						}

					}
				}).show();
	}
}
