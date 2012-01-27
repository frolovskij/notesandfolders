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
import java.util.Stack;

import com.notesandfolders.dataaccess.NodeHelper;
import com.tani.app.ui.IconContextMenu;

import de.marcreichelt.android.RealViewSwitcher;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ExplorerActivity extends BaseActivity implements
		IconContextMenu.IconContextMenuOnClickListener, OnItemClickListener {
	private static final int CONTEXT_MENU_ID = 0;
	private static final int MENU_PROPERTIES = 6;
	private static final int MENU_DELETE = 5;
	private static final int MENU_PASTE = 4;
	private static final int MENU_CUT = 3;
	private static final int MENU_COPY = 2;
	private static final int MENU_RENAME = 1;
	private NodeHelper nh;
	private ListView lv;
	private TextView path;
	private List<Node> items;
	private NodeAdapter adapter;
	private IconContextMenu iconContextMenu = null;
	private Node selectedNode = null;

	/**
	 * This stack holds ids of items being opened
	 * 
	 * peek() - current folder's or note's id
	 */
	private Stack<Long> nodeIdStack;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		nh = new NodeHelper(this, getIntent().getExtras().getString("password"));
		nodeIdStack = new Stack<Long>();

		// set as content view
		setContentView(R.layout.explorer);
		path = (TextView) findViewById(R.id.explorer_path);
		lv = (ListView) findViewById(R.id.explorer_listview);

		lv.setOnItemLongClickListener(itemLongClickHandler);
		lv.setOnItemClickListener(this);

		createContextMenu();

		openDir(0L);
	}

	public void createContextMenu() {
		Resources res = getResources();

		iconContextMenu = new IconContextMenu(this, CONTEXT_MENU_ID);
		iconContextMenu.addItem(res, R.string.rename, R.drawable.rename,
				MENU_RENAME);
		iconContextMenu.addItem(res, R.string.copy, R.drawable.copy, MENU_COPY);
		iconContextMenu.addItem(res, R.string.cut, R.drawable.cut, MENU_CUT);
		iconContextMenu.addItem(res, R.string.paste, R.drawable.paste,
				MENU_PASTE);
		iconContextMenu.addItem(res, R.string.delete, R.drawable.delete,
				MENU_DELETE);
		iconContextMenu.addItem(res, R.string.properties,
				R.drawable.properties, MENU_PROPERTIES);

		iconContextMenu.setOnClickListener(this);
	}

	public void openDir(long id) {
		Node node = nh.getNodeById(id);

		if (node.getType() == NodeType.FOLDER) {
			nodeIdStack.push(id);
			update();
		}
	}

	private OnItemLongClickListener itemLongClickHandler = new OnItemLongClickListener() {
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			selectedNode = (Node) lv.getItemAtPosition(position);

			showDialog(CONTEXT_MENU_ID);

			return true;
		}
	};

	/**
	 * create context menu
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == CONTEXT_MENU_ID) {
			return iconContextMenu.createMenu(getText(
					R.string.explorer_context_menu_title).toString());
		}
		return super.onCreateDialog(id);
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

	public void update() {
		items = nh.getChildrenById(nodeIdStack.peek());
		adapter = new NodeAdapter(this, R.layout.explorer_item, items);
		lv.setAdapter(adapter);

		path.setText(nh.getFullPathById(nodeIdStack.peek()));
	}

	public void onOpen() {
		Node node = nh.getNodeById(nodeIdStack.peek());
		if (node.getType() == NodeType.FOLDER) {
			openDir(node.getId());
		} else {
			// open note
		}
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

								Node parent = nh.getNodeById(nodeIdStack.peek());
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

	public void onRename() {

		final EditText input = new EditText(this);
		input.setText(selectedNode.getName());

		new AlertDialog.Builder(this)
				.setTitle(R.string.explorer_rename_title)
				.setMessage(R.string.explorer_rename_prompt)
				.setView(input)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								if (selectedNode != null) {
									nh.renameNodeById(selectedNode.getId(),
											input.getText().toString());

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

	public void onDelete() {
		final EditText input = new EditText(this);
		input.setText(selectedNode.getName());

		new AlertDialog.Builder(this)
				.setTitle(R.string.explorer_delete_title)
				.setMessage(R.string.explorer_delete_prompt)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								if (selectedNode != null) {
									nh.deleteNodeById(selectedNode.getId());

									update();
								}
							}
						})
				.setNegativeButton(R.string.no,
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

								Node parent = nh.getNodeById(nodeIdStack.peek());
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

	public void onClick(int menuId) {
		switch (menuId) {
		case MENU_RENAME:
			onRename();
			break;

		case MENU_DELETE:
			onDelete();
			break;
		}
	}

	public void onItemClick(AdapterView<?> parentView, View childView,
			int position, long id) {
		Node selected = (Node) lv.getItemAtPosition(position);
		nodeIdStack.add(selected.getId());
		onOpen();
	}
}
