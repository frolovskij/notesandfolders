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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.notesandfolders.IconListItem;
import com.notesandfolders.IconListItemAdapter;
import com.notesandfolders.NaturalOrderNodesComparator;
import com.notesandfolders.Node;
import com.notesandfolders.NodeAdapter;
import com.notesandfolders.NodeType;
import com.notesandfolders.R;
import com.notesandfolders.dataaccess.NodeHelper;
import com.tani.app.ui.IconContextMenu;
import com.tani.app.ui.IconContextMenu.IconContextMenuOnClickListener;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ExplorerActivity extends BaseActivity implements OnItemClickListener {
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

	// private long currentFolderId;
	private long selectedId;

	private long idToCopy;

	private long idToMove;

	// used in new & rename alert dialogs
	private EditText input;

	@Override
	protected void onResume() {
		super.onResume();

		openDir(getCurrentFolderId());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		nh = new NodeHelper(this, getIntent().getExtras().getString("password"));

		// set as content view
		setContentView(R.layout.explorer);
		path = (TextView) findViewById(R.id.explorer_path);
		lv = (ListView) findViewById(R.id.explorer_listview);

		lv.setOnItemLongClickListener(itemLongClickHandler);
		lv.setOnItemClickListener(this);

		selectedId = -1;
		idToCopy = -1;
		idToMove = -1;

		input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

		registerForContextMenu(input);
	}

	// context menu listener for nodes
	final IconContextMenuOnClickListener contextMenuListener = new IconContextMenuOnClickListener() {
		public void onClick(int menuId) {
			switch (menuId) {
			case MENU_RENAME:
				onRename();
				break;

			case MENU_DELETE:
				onDelete();
				break;

			case MENU_COPY:
				idToCopy = selectedId;
				idToMove = -1;
				break;

			case MENU_CUT:
				idToCopy = -1;
				idToMove = selectedId;
				break;
			}
		}
	};

	public void createContextMenu() {
		Resources res = getResources();

		iconContextMenu = new IconContextMenu(this, CONTEXT_MENU_ID);
		iconContextMenu.addItem(res, R.string.rename, R.drawable.rename, MENU_RENAME);
		iconContextMenu.addItem(res, R.string.copy, R.drawable.copy, MENU_COPY);
		iconContextMenu.addItem(res, R.string.cut, R.drawable.cut, MENU_CUT);
		iconContextMenu.addItem(res, R.string.delete, R.drawable.delete, MENU_DELETE);
		// iconContextMenu.addItem(res, R.string.properties,
		// R.drawable.properties, MENU_PROPERTIES);
		iconContextMenu.setOnClickListener(contextMenuListener);
	}

	private long getCurrentFolderId() {
		return getIntent().getLongExtra("current_folder", 0L);
	}

	private void setCurrentFolderId(long id) {
		getIntent().putExtra("current_folder", id);
	}

	private OnItemLongClickListener itemLongClickHandler = new OnItemLongClickListener() {
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			selectedId = ((Node) lv.getItemAtPosition(position)).getId();

			// Not showing context menu for ..'s
			Node current = nh.getNodeById(getCurrentFolderId());
			boolean showMenu = (selectedId != current.getParentId());

			if (showMenu) {
				createContextMenu();
				showDialog(CONTEXT_MENU_ID);
			}

			return true;
		}
	};

	/**
	 * create context menu
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == CONTEXT_MENU_ID) {
			return iconContextMenu.createMenu(getText(R.string.explorer_context_menu_title)
					.toString());
		}
		return super.onCreateDialog(id);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean enablePaste = (idToCopy != -1 || idToMove != -1);
		MenuItem item = menu.findItem(R.id.explorer_options_paste);
		item.setVisible(enablePaste);

		return true;
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

		case R.id.explorer_options_paste:
			if (idToCopy != -1) {
				nh.copy(idToCopy, getCurrentFolderId());
				refresh();
			} else if (idToMove != -1) {
				nh.move(idToMove, getCurrentFolderId());
				refresh();
			}

			idToCopy = -1;
			idToMove = -1;
			return true;

		case R.id.explorer_options_settings:
			Intent settings = new Intent(this, SettingsActivity.class);
			startActivity(settings);
			return true;

		case R.id.explorer_options_close:
			finish();
			return true;

		case R.id.explorer_options_import:
			Intent fsexplorer = new Intent(this, FileSystemExplorerActivity.class);
			fsexplorer.putExtra("path", "/");
			fsexplorer.putExtra("password", getIntent().getExtras().getString("password"));
			startActivity(fsexplorer);
			return true;

		case R.id.explorer_options_find:
			showAlert(R.string.msg_not_implemented_yet);
			return true;
		}
		return false; // super.onOptionsItemSelected(item);
	}

	private void refresh() {
		items = nh.getChildrenById(getCurrentFolderId());
		Collections.sort(items, new NaturalOrderNodesComparator());

		adapter = new NodeAdapter(this, R.layout.explorer_item, items);
		lv.setAdapter(adapter);

		path.setText(nh.getFullPathById(getCurrentFolderId()));
	}

	private void openDir(long id) {
		Node node = nh.getNodeById(id);

		if (node.getType() == NodeType.FOLDER) {
			setCurrentFolderId(id);
			refresh();
		}
	}

	private void openNote(long id) {
		Intent viewer = new Intent(this, NotesViewerActivity.class);
		viewer.putExtra("note_id", id);
		viewer.putExtra("password", getIntent().getExtras().getString("password"));
		startActivity(viewer);
	}

	private void onOpen(long id) {
		Node node = nh.getNodeById(id);

		switch (node.getType()) {
		case FOLDER:
			openDir(node.getId());
			break;
		case NOTE:
			openNote(node.getId());
			break;
		case CHECKLIST:
			showAlert(R.string.msg_not_implemented_yet);
			break;
		}
	}

	private void onNewFolder() {
		if (input.getParent() != null) {
			((ViewGroup) input.getParent()).removeView(input);
			input.setText("");
		}

		new AlertDialog.Builder(this).setTitle(R.string.explorer_newfolder_title)
				.setMessage(R.string.explorer_newfolder_prompt).setView(input)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String folderName = input.getText().toString();

						Node parent = nh.getNodeById(getCurrentFolderId());
						if ((parent != null) && parent.getType() == NodeType.FOLDER) {
							nh.createFolder(parent, folderName);
							refresh();
						}
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Do nothing.
					}
				}).show();
	}

	private void onNewNote() {
		if (input.getParent() != null) {
			((ViewGroup) input.getParent()).removeView(input);
			input.setText("");
		}

		new AlertDialog.Builder(this).setTitle(R.string.explorer_newnote_title)
				.setMessage(R.string.explorer_newnote_prompt).setView(input)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String noteName = input.getText().toString();

						Node parent = nh.getNodeById(getCurrentFolderId());
						if ((parent != null) && parent.getType() == NodeType.FOLDER) {
							nh.createNote(parent, noteName, "");
							refresh();
						}
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Do nothing.
					}
				}).show();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenu.ContextMenuInfo menuInfo) {
		if (view == input) {
			menu.add(Menu.NONE, Menu.NONE, Menu.NONE, R.string.insert_date_time)
					.setOnMenuItemClickListener(new OnMenuItemClickListener() {
						public boolean onMenuItemClick(MenuItem item) {
							Log.v(getClass().getName(), "Insert date+time" + item);
							input.append(new SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
									.format(new Date()));
							return false;
						}
					});
		}

		super.onCreateContextMenu(menu, view, menuInfo);
	}

	private void onRename() {
		final Node selectedNode = nh.getNodeById(selectedId);
		if (input.getParent() != null) {
			((ViewGroup) input.getParent()).removeView(input);
		}
		input.setText(selectedNode.getName());

		new AlertDialog.Builder(this).setTitle(R.string.explorer_rename_title)
				.setMessage(R.string.explorer_rename_prompt).setView(input)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (selectedNode != null) {
							nh.renameNodeById(selectedNode.getId(), input.getText().toString());

							refresh();
						}
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Do nothing.
					}
				}).show();

	}

	private void onDelete() {
		final Node selectedNode = nh.getNodeById(selectedId);

		new AlertDialog.Builder(this).setTitle(R.string.explorer_delete_title)
				.setMessage(R.string.explorer_delete_prompt)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (selectedNode != null) {
							nh.deleteNodeById(selectedNode.getId());

							refresh();
						}
					}
				}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Do nothing.
					}
				}).show();
	}

	private void onNew() {
		final IconListItem[] items = {
				new IconListItem(getText(R.string.create_folder).toString(), R.drawable.folder),
				new IconListItem(getText(R.string.create_note).toString(), R.drawable.note),
		// new IconListItem(getText(R.string.create_checklist).toString(),
		// R.drawable.note)
		};

		ListAdapter adapter = new IconListItemAdapter(this, android.R.layout.select_dialog_item,
				items);

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
									getText(R.string.msg_not_implemented_yet), Toast.LENGTH_SHORT)
									.show();
							break;
						}

					}
				}).show();
	}

	public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
		Node selected = (Node) lv.getItemAtPosition(position);
		onOpen(selected.getId());
	}
}
