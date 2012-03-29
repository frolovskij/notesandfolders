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

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.notesandfolders.R;
import com.tani.app.ui.IconContextMenu;
import com.tani.app.ui.IconContextMenu.IconContextMenuOnClickListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ExplorerActivity extends Activity implements OnItemClickListener {

	private static final int DIALOG_CONTEXT_MENU = 0;
	public static final int DIALOG_COPY = 1;
	private static final int DIALOG_NEW = 2;
	private static final int DIALOG_NEW_FOLDER = 3;
	private static final int DIALOG_NEW_NOTE = 4;
	private static final int DIALOG_NEW_CHECKLIST = 5;
	private static final int DIALOG_RENAME = 6;
	private static final int DIALOG_DELETE = 7;
	private static final int DIALOG_PROPERTIES = 8;

	private static final int MENU_PROPERTIES = 6;
	private static final int MENU_DELETE = 5;
	private static final int MENU_CUT = 3;
	private static final int MENU_COPY = 2;
	private static final int MENU_RENAME = 1;
	private NodeHelper nh;
	private ListView lv;
	private TextView path;
	private List<Node> items;
	private NodeAdapter adapter;
	private IconContextMenu iconContextMenu = null;
	private TextView placeholder;

	private CopyTask copyTask;
	private boolean mShownDialog;

	private long selectedId;
	private long idToCopy;
	private long idToMove;
	private long idToSetFocusTo;
	private long currentFolderId;

	// used in new & rename alert dialogs
	private EditText inputNewFolder;
	private EditText inputNewNote;
	private EditText inputNewCheckList;
	private EditText inputRename;

	public static final DialogInterface.OnClickListener DUMMY_LISTENER = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
			// Do nothing.
		}
	};

	@Override
	protected void onResume() {
		super.onResume();

		openDir(currentFolderId);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		nh = new NodeHelper(this, new TempStorage(this).getPassword());

		// set as content view
		setContentView(R.layout.explorer);
		path = (TextView) findViewById(R.id.explorer_path);
		lv = (ListView) findViewById(R.id.explorer_listview);
		placeholder = (TextView) findViewById(R.id.explorer_placeholder);

		lv.setOnItemLongClickListener(itemLongClickHandler);
		lv.setOnItemClickListener(this);

		inputNewFolder = new EditText(this);
		inputNewFolder.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

		inputNewNote = new EditText(this);
		inputNewNote.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

		inputNewCheckList = new EditText(this);
		inputNewCheckList.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

		inputRename = new EditText(this);
		inputRename.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

		registerForContextMenu(inputNewFolder);
		registerForContextMenu(inputNewNote);
		registerForContextMenu(inputNewCheckList);
		registerForContextMenu(inputRename);

		createContextMenu();
	}

	public void onCopyTaskCompleted(Integer result) {
		if (mShownDialog) {
			checkPasteResult(result);
		}
	}

	public void superOnBackPressed() {
		super.onBackPressed();
	}

	@Override
	public void onBackPressed() {
		// go to the parent folder if not in root folder
		if (currentFolderId != 0) {
			Node currentFolder = nh.getNodeById(currentFolderId);
			openDir(currentFolder.getParentId());
		} else {
			superOnBackPressed();
		}
	}

	// context menu listener for nodes
	final IconContextMenuOnClickListener contextMenuListener = new IconContextMenuOnClickListener() {
		public void onClick(int menuId) {
			switch (menuId) {
			case MENU_RENAME:
				onRename();
				break;

			case MENU_DELETE:
				showDialog(DIALOG_DELETE);
				break;

			case MENU_COPY:
				idToCopy = selectedId;
				idToMove = -1;
				break;

			case MENU_CUT:
				idToCopy = -1;
				idToMove = selectedId;
				break;

			case MENU_PROPERTIES:
				showDialog(DIALOG_PROPERTIES);
				break;
			}
		}
	};

	public void createContextMenu() {
		Resources res = getResources();

		iconContextMenu = new IconContextMenu(this, DIALOG_CONTEXT_MENU);
		iconContextMenu.addItem(res, R.string.rename, R.drawable.rename,
				MENU_RENAME);
		iconContextMenu.addItem(res, R.string.copy, R.drawable.copy, MENU_COPY);
		iconContextMenu.addItem(res, R.string.cut, R.drawable.cut, MENU_CUT);
		iconContextMenu.addItem(res, R.string.delete, R.drawable.delete,
				MENU_DELETE);
		iconContextMenu.addItem(res, R.string.properties,
				R.drawable.properties, MENU_PROPERTIES);
		iconContextMenu.setOnClickListener(contextMenuListener);
	}

	private OnItemLongClickListener itemLongClickHandler = new OnItemLongClickListener() {
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			selectedId = ((Node) lv.getItemAtPosition(position)).getId();

			// Not showing context menu for ..'s
			Node current = nh.getNodeById(currentFolderId);
			boolean showMenu = (selectedId != current.getParentId());

			if (showMenu) {
				showDialog(DIALOG_CONTEXT_MENU);
			}

			return true;
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == DIALOG_CONTEXT_MENU) {
			return iconContextMenu.createMenu(getText(
					R.string.explorer_context_menu_title).toString());
		}

		if (id == DIALOG_COPY) {
			ProgressDialog pd = new ProgressDialog(this);
			pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pd.setMessage(getText(R.string.explorer_msg_copying_files));
			pd.setCancelable(false);

			return pd;
		}

		if (id == DIALOG_NEW) {
			final IconListItem[] items = {
					new IconListItem(
							getText(R.string.create_folder).toString(),
							R.drawable.folder),
					new IconListItem(getText(R.string.create_note).toString(),
							R.drawable.note),
					new IconListItem(getText(R.string.create_checklist)
							.toString(), R.drawable.checklist) };

			ListAdapter adapter = new IconListItemAdapter(this,
					android.R.layout.select_dialog_item, items);

			return new AlertDialog.Builder(this)
					.setTitle(getText(R.string.create_new))
					.setAdapter(adapter, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							switch (item) {
							case 0:
								inputNewFolder.setText("");
								showDialog(DIALOG_NEW_FOLDER);
								break;
							case 1:
								inputNewNote.setText("");
								showDialog(DIALOG_NEW_NOTE);
								break;
							case 2:
								inputNewCheckList.setText("");
								showDialog(DIALOG_NEW_CHECKLIST);
								break;
							}
						}
					}).create();
		}

		if (id == DIALOG_NEW_FOLDER) {
			return new AlertDialog.Builder(this)
					.setTitle(R.string.explorer_newfolder_title)
					.setMessage(R.string.explorer_newfolder_prompt)
					.setView(inputNewFolder)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									String folderName = inputNewFolder
											.getText().toString();

									Node parent = nh
											.getNodeById(currentFolderId);
									if ((parent != null)
											&& parent.getType() == NodeType.FOLDER) {
										Node created = nh.createFolder(parent,
												folderName);
										if (created != null) {
											ExplorerActivity.this.idToSetFocusTo = created
													.getId();
										}
										refresh();
									}
								}
							})
					.setNegativeButton(android.R.string.cancel, DUMMY_LISTENER)
					.create();
		}

		if (id == DIALOG_NEW_NOTE) {
			return new AlertDialog.Builder(this)
					.setTitle(R.string.explorer_newnote_title)
					.setMessage(R.string.explorer_newnote_prompt)
					.setView(inputNewNote)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									String noteName = inputNewNote.getText()
											.toString();

									Node parent = nh
											.getNodeById(currentFolderId);
									if ((parent != null)
											&& parent.getType() == NodeType.FOLDER) {
										Node created = nh.createNote(parent,
												noteName, "");
										if (created != null) {
											ExplorerActivity.this.idToSetFocusTo = created
													.getId();
										}

										refresh();
									}
								}
							})
					.setNegativeButton(android.R.string.cancel, DUMMY_LISTENER)
					.create();
		}

		if (id == DIALOG_NEW_CHECKLIST) {
			return new AlertDialog.Builder(this)
					.setTitle(R.string.explorer_newchecklist_title)
					.setMessage(R.string.explorer_newchecklist_prompt)
					.setView(inputNewCheckList)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									String checkListName = inputNewCheckList
											.getText().toString();

									Node parent = nh
											.getNodeById(currentFolderId);
									if ((parent != null)
											&& parent.getType() == NodeType.FOLDER) {
										Node created = nh.createCheckList(
												parent, checkListName);
										if (created != null) {
											ExplorerActivity.this.idToSetFocusTo = created
													.getId();
										}

										refresh();
									}
								}
							})
					.setNegativeButton(android.R.string.cancel, DUMMY_LISTENER)
					.create();

		}

		if (id == DIALOG_RENAME) {
			return new AlertDialog.Builder(this)
					.setTitle(R.string.explorer_rename_title)
					.setMessage(R.string.explorer_rename_prompt)
					.setView(inputRename)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									final Node selectedNode = nh
											.getNodeById(selectedId);
									if (selectedNode != null) {
										nh.renameNodeById(selectedNode.getId(),
												inputRename.getText()
														.toString());

										refresh();
									}
								}
							})
					.setNegativeButton(android.R.string.cancel, DUMMY_LISTENER)
					.create();
		}

		if (id == DIALOG_DELETE) {
			return new AlertDialog.Builder(this)
					.setTitle(R.string.explorer_delete_title)
					.setMessage(R.string.explorer_delete_prompt)
					.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									final Node selectedNode = nh
											.getNodeById(selectedId);
									if (selectedNode != null) {
										nh.deleteNodeById(selectedNode.getId());

										refresh();
									}
								}
							})
					.setNegativeButton(android.R.string.no, DUMMY_LISTENER)
					.create();
		}

		if (id == DIALOG_PROPERTIES) {
			Dialog dialog = new Dialog(ExplorerActivity.this);
			dialog.setContentView(R.layout.properties);

			LayoutParams params = dialog.getWindow().getAttributes();
			params.width = LayoutParams.FILL_PARENT;
			dialog.getWindow().setAttributes(
					(android.view.WindowManager.LayoutParams) params);

			dialog.setTitle(R.string.properties_title);
			dialog.setCancelable(true);

			return dialog;
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

	public void checkPasteResult(int result) {
		switch (result) {
		case NodeHelper.RESULT_CANT_PASTE_TO_ITSELF:
			showToast(R.string.explorer_msg_destination_is_source);
			break;

		case NodeHelper.RESULT_CANT_PASTE_TO_OWN_SUBFOLDER:
			showToast(R.string.explorer_msg_destination_is_subfolder);
			break;

		case NodeHelper.RESULT_OK:
			idToCopy = -1;
			idToMove = -1;
			refresh();
			break;
		}
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		if (id == DIALOG_COPY) {
			mShownDialog = true;
		}

		if (id == DIALOG_PROPERTIES) {
			Node n = nh.getNodeById(selectedId);

			ImageView icon = (ImageView) dialog
					.findViewById(R.id.properties_file_icon);

			if (icon != null) {
				switch (n.getType()) {
				case FOLDER:
					icon.setImageResource(R.drawable.folder);
					break;
				case NOTE:
					icon.setImageResource(R.drawable.note);
					break;
				case CHECKLIST:
					icon.setImageResource(R.drawable.checklist);
					break;
				}
			}

			TextView name = (TextView) dialog
					.findViewById(R.id.properties_file_name);
			name.setText(nh.getFullPathById(n.getId()));

			TextView dateCreated = (TextView) dialog
					.findViewById(R.id.properties_date_created);
			dateCreated.setText(new SimpleDateFormat().format(n
					.getDateCreated()));

			TextView dateModified = (TextView) dialog
					.findViewById(R.id.properties_date_modified);
			dateModified.setText(new SimpleDateFormat().format(n
					.getDateModified()));
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.explorer_options_add:
			showDialog(DIALOG_NEW);
			return true;

		case R.id.explorer_options_paste:
			if (idToCopy != -1) {
				copyTask = new CopyTask(this, nh, idToCopy, currentFolderId);
				copyTask.execute();
			} else if (idToMove != -1) {
				int result = nh.move(idToMove, currentFolderId);
				checkPasteResult(result);
			}

			return true;

		case R.id.explorer_options_search:
			Intent search = new Intent(this, SearchActivity.class);
			search.putExtra("current_folder_id", currentFolderId);
			startActivity(search);
			return true;

		case R.id.explorer_options_backupandrestore:
			Intent backupandrestore = new Intent(this,
					BackupAndRestoreActivity.class);
			startActivity(backupandrestore);
			break;

		case R.id.explorer_options_settings:
			Intent settings = new Intent(this, SettingsActivity.class);
			startActivity(settings);
			return true;

		case R.id.explorer_options_import:
			Intent fsexplorer = new Intent(this, ImportActivity.class);
			fsexplorer.putExtra("path", "/");
			startActivity(fsexplorer);
			return true;

		case R.id.explorer_options_close:
			onClose();
			return true;
		}
		return false; // super.onOptionsItemSelected(item);
	}

	private void refresh() {
		items = nh.getChildrenById(currentFolderId);
		Collections.sort(items, new NaturalOrderNodesComparator());

		// adds ".." pseudo-directory to the listing
		Node here = nh.getNodeById(currentFolderId);
		if (here.getParentId() != -1) {
			Node parent = nh.getNodeById(here.getParentId());
			if (parent != null) {
				parent.setName("..");
				items.add(0, parent);
			}
		}

		if (items.isEmpty()) {
			placeholder.setVisibility(View.VISIBLE);
		} else {
			placeholder.setVisibility(View.GONE);
		}

		adapter = new NodeAdapter(this, R.layout.explorer_item, items);
		lv.setAdapter(adapter);

		for (int i = 0; i < adapter.getCount(); i++) {
			Node n = adapter.getItem(i);
			if (n != null && n.getId() == idToSetFocusTo) {
				lv.setSelection(i);
			}
		}

		path.setText(nh.getFullPathById(currentFolderId));
	}

	private void openDir(long id) {
		Node node = nh.getNodeById(id);

		if (node.getType() == NodeType.FOLDER) {
			currentFolderId = id;
			refresh();
		}
	}

	private void openNote(long id) {
		// when going back from viewer/editor this item will be focused
		idToSetFocusTo = id;

		Intent viewer = new Intent(this, NotesViewerActivity.class);
		viewer.putExtra("note_id", id);
		startActivity(viewer);
	}

	public void openCheckList(long id) {
		// when going back from viewer/editor this item will be focused
		idToSetFocusTo = id;

		Intent viewer = new Intent(this, CheckListActivity.class);
		viewer.putExtra("checklist_id", id);
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
			openCheckList(node.getId());
			break;
		}
	}

	private void onRename() {
		final Node selectedNode = nh.getNodeById(selectedId);
		if (selectedNode != null) {
			inputRename.setText(selectedNode.getName());
		}

		showDialog(DIALOG_RENAME);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenu.ContextMenuInfo menuInfo) {
		if (view instanceof EditText) {
			final EditText edit = (EditText) view;
			menu.add(Menu.NONE, Menu.NONE, Menu.NONE, R.string.insert_date_time)
					.setOnMenuItemClickListener(new OnMenuItemClickListener() {
						public boolean onMenuItemClick(MenuItem item) {
							Log.v(getClass().getName(), "Insert date+time"
									+ item);
							edit.append(new SimpleDateFormat(
									"yyyy.MM.dd HH:mm:ss").format(new Date()));
							return false;
						}
					});
		}

		super.onCreateContextMenu(menu, view, menuInfo);
	}

	public void onItemClick(AdapterView<?> parentView, View childView,
			int position, long id) {
		Node selected = (Node) lv.getItemAtPosition(position);
		onOpen(selected.getId());
	}

	public void onClose() {
		new TempStorage(this).setExiting();

		finish();
	}

	public void showToast(int stringResId) {
		Toast toast = Toast.makeText(getApplicationContext(), stringResId,
				Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
}
