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

import com.ericharlow.DragNDrop.DragListener;
import com.ericharlow.DragNDrop.DragNDropListView;
import com.ericharlow.DragNDrop.DropListener;
import com.ericharlow.DragNDrop.RemoveListener;
import com.notesandfolders.R;
import com.tani.app.ui.IconContextMenu;
import com.tani.app.ui.IconContextMenu.IconContextMenuOnClickListener;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;

public class CheckListActivity extends ListActivity {
	private static final int CONTEXT_MENU_ID = 0;
	private static final int MENU_RENAME = 1;
	private static final int MENU_COPY = 2;
	private static final int MENU_DELETE = 3;

	private static final int DIALOG_NEW = 4;
	private static final int DIALOG_RENAME = 5;
	private static final int DIALOG_DELETE = 6;
	private static final int DIALOG_CLOSE = 7;

	private CheckList initialCheckList;
	CheckListItemAdapter adapter;
	private CheckList checkList;
	private TextView name;
	private NodeHelper nh;
	private long id;
	private IconContextMenu iconContextMenu = null;
	private ListView lv;
	private TextView placeholder;

	private EditText inputNewItem;
	private EditText inputRename;

	private int selectedIndex;
	
	private int positionToSetFocusTo = -1;
	private int yToSetFocusTo;


	private int getSelectedIndex() {
		return selectedIndex;
	}

	private void setSelectedIndex(int selectedIndex) {
		this.selectedIndex = selectedIndex;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		nh = new NodeHelper(this, new TempStorage(this).getPassword());
		id = getIntent().getExtras().getLong("checklist_id");

		String tc = nh.getTextContentById(id);
		initialCheckList = (CheckList) Serializer.deserialize(tc);
		checkList = (CheckList) Serializer.deserialize(tc);

		setContentView(R.layout.checklist);

		name = (TextView) findViewById(R.id.checklist_name);
		placeholder = (TextView) findViewById(R.id.checklist_placeholder);

		lv = getListView();

		if (lv instanceof DragNDropListView) {
			((DragNDropListView) lv).setDropListener(mDropListener);
			((DragNDropListView) lv).setRemoveListener(mRemoveListener);
			((DragNDropListView) lv).setDragListener(mDragListener);
		}

		createContextMenu();
		lv.setOnItemLongClickListener(itemLongClickHandler);
		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				try {
					CheckListItem ctw = adapter.getItem(position);
					ctw.setChecked(!ctw.isChecked());
					
					positionToSetFocusTo = position;
					yToSetFocusTo = view.getTop();

					refresh();
				} catch (IndexOutOfBoundsException ex) {
				}
			}
		});

		inputNewItem = new EditText(this);
		inputNewItem.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

		inputRename = new EditText(this);
		inputRename
				.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

		refresh();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.checklist_context, menu);
	}

	public void refresh() {
		if (checkList.isEmpty()) {
			placeholder.setVisibility(View.VISIBLE);
		} else {
			placeholder.setVisibility(View.GONE);
		}

		// loading checkList from adapter
		if (adapter != null) {
			checkList = new CheckList();
			for (int i = 0; i < adapter.getCount(); i++) {
				checkList.add((CheckListItem) adapter.getItem(i));
			}
		}

		adapter = new CheckListItemAdapter(this, new int[] { R.layout.checklist_item }, new int[] {
				R.id.checklist_item_check, R.id.checklist_item_text }, checkList);

		setListAdapter(adapter);
		
		if (positionToSetFocusTo != -1) {
			lv.setSelectionFromTop(positionToSetFocusTo, yToSetFocusTo);
		}

		Node n = nh.getNodeById(id);
		name.setText(n.getName());
	}

	public void onSave() {
		initialCheckList = checkList.clone();
		nh.setTextContentById(id, Serializer.serialize(checkList));
	}

	public void superOnBackPressed() {
		super.onBackPressed();
	}

	@Override
	public void onBackPressed() {
		if (checkList.equals(initialCheckList)) {
			superOnBackPressed();
		} else {
			showDialog(DIALOG_CLOSE);
		}
	}

	private OnItemLongClickListener itemLongClickHandler = new OnItemLongClickListener() {
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			setSelectedIndex(position);
			Log.i("NF", ""+position);
			showDialog(CONTEXT_MENU_ID);

			return false;
		}
	};

	public void createContextMenu() {
		Resources res = getResources();

		iconContextMenu = new IconContextMenu(this, CONTEXT_MENU_ID);
		iconContextMenu.addItem(res, R.string.rename, R.drawable.rename, MENU_RENAME);
		iconContextMenu.addItem(res, R.string.copy, R.drawable.copy, MENU_COPY);
		iconContextMenu.addItem(res, R.string.delete, R.drawable.delete, MENU_DELETE);
		iconContextMenu.setOnClickListener(contextMenuListener);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == CONTEXT_MENU_ID) {
			return iconContextMenu.createMenu(getText(R.string.explorer_context_menu_title)
					.toString());
		}

		if (id == DIALOG_CLOSE) {
			return new AlertDialog.Builder(this).setTitle(R.string.checklist_title)
					.setMessage(R.string.checklist_msg_save_before_exit)
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							onSave();
							superOnBackPressed();
						}
					})
					.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							superOnBackPressed();
						}
					}).create();

		}

		if (id == DIALOG_NEW) {

			return new AlertDialog.Builder(this)
					.setTitle(R.string.checklist_newitem_title)
					.setMessage(R.string.checklist_newitem_prompt)
					.setView(inputNewItem)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							String itemName = inputNewItem.getText().toString();
							checkList.add(new CheckListItem(itemName, false));
							refresh();
						}
					})
					.setNegativeButton(android.R.string.cancel, EMPTY_CLICK_LISTENER).create();
		}

		if (id == DIALOG_RENAME) {
			return new AlertDialog.Builder(this)
					.setTitle(R.string.checklist_rename_title)
					.setMessage(R.string.checklist_rename_prompt)
					.setView(inputRename)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							CheckListItem selectedItem = CheckListActivity.this.adapter
									.getItem(getSelectedIndex());
							if (selectedItem != null) {
								selectedItem.setText(inputRename.getText().toString());
								refresh();
							}
						}
					}).setNegativeButton(android.R.string.cancel, EMPTY_CLICK_LISTENER).create();
		}

		if (id == DIALOG_DELETE) {
			final CheckListItem selectedItem = this.adapter.getItem(getSelectedIndex());

			if (selectedItem != null) {
				return new AlertDialog.Builder(this)
						.setTitle(R.string.checklist_delete_title)
						.setMessage(R.string.checklist_delete_prompt)
						.setPositiveButton(android.R.string.yes,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int whichButton) {
										if (selectedItem != null) {
											checkList.remove(getSelectedIndex());

											refresh();
										}
									}
								})
						.setNegativeButton(android.R.string.no, EMPTY_CLICK_LISTENER).create();
			}
		}

		return super.onCreateDialog(id);
	}
	
	final DialogInterface.OnClickListener EMPTY_CLICK_LISTENER = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
			// Do nothing.
		}
	}; 

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
				new TempStorage(CheckListActivity.this)
						.setCheckListItem(CheckListActivity.this.adapter
								.getItem(getSelectedIndex()));
				break;
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.checklist_options, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem item = menu.findItem(R.id.checklist_options_paste);
		item.setVisible(new TempStorage(this).getCheckListItem() != null);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.checklist_options_add:
			onNew();
			return true;
		case R.id.checklist_options_save:
			onSave();
			return true;
		case R.id.checklist_options_paste:
			onPaste();
			return true;
		}
		return false; // super.onOptionsItemSelected(item);
	}

	public void onNew() {
		inputNewItem.setText("");
		showDialog(DIALOG_NEW);
	}

	private void onRename() {
		final CheckListItem selectedItem = this.adapter.getItem(getSelectedIndex());

		if (selectedItem != null) {
			inputRename.setText(selectedItem.getText());
		}

		showDialog(DIALOG_RENAME);
	}

	public void onDelete() {
		final CheckListItem selectedItem = this.adapter.getItem(getSelectedIndex());
		if (selectedItem != null) {
			showDialog(DIALOG_DELETE);
		}
	}

	private void onPaste() {
		CheckListItem itemToPaste = new TempStorage(this).getCheckListItem();
		if (itemToPaste != null) {
			checkList.add(itemToPaste);
			refresh();
		}
	}

	private DropListener mDropListener = new DropListener() {
		public void onDrop(int from, int to) {
			ListAdapter adapter = getListAdapter();
			if (adapter instanceof CheckListItemAdapter) {
				((CheckListItemAdapter) adapter).onDrop(from, to);
				getListView().invalidateViews();
			}
		}
	};

	private RemoveListener mRemoveListener = new RemoveListener() {
		public void onRemove(int which) {
			ListAdapter adapter = getListAdapter();
			if (adapter instanceof CheckListItemAdapter) {
				((CheckListItemAdapter) adapter).onRemove(which);
				getListView().invalidateViews();
			}
		}
	};

	private DragListener mDragListener = new DragListener() {

		int backgroundColor = 0x00000000;
		int defaultBackgroundColor;

		public void onDrag(int x, int y, ListView listView) {
			// TODO Auto-generated method stub
		}

		public void onStartDrag(View itemView) {
			itemView.setVisibility(View.INVISIBLE);
			defaultBackgroundColor = itemView.getDrawingCacheBackgroundColor();
			itemView.setBackgroundColor(backgroundColor);
		}

		public void onStopDrag(View itemView) {
			itemView.setVisibility(View.VISIBLE);
			itemView.setBackgroundColor(defaultBackgroundColor);
		}

	};
}