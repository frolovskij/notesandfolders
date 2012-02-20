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

import com.ericharlow.DragNDrop.DragListener;
import com.ericharlow.DragNDrop.DragNDropListView;
import com.ericharlow.DragNDrop.DropListener;
import com.ericharlow.DragNDrop.RemoveListener;
import com.notesandfolders.CheckList;
import com.notesandfolders.CheckListItem;
import com.notesandfolders.CheckListItemAdapter;
import com.notesandfolders.Login;
import com.notesandfolders.Node;
import com.notesandfolders.NodeType;
import com.notesandfolders.R;
import com.notesandfolders.dataaccess.NodeHelper;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class CheckListActivity extends ListActivity {
	private CheckList initialCheckList;
	CheckListItemAdapter adapter;
	private CheckList checkList;
	private TextView name;
	private NodeHelper nh;
	private long id;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		nh = new NodeHelper(this,
				Login.getPlainTextPasswordFromTempStorage(this));
		id = getIntent().getExtras().getLong("checklist_id");
		initialCheckList = CheckList.deserialize(nh.getTextContentById(id));

		checkList = CheckList.deserialize(nh.getTextContentById(id));

		setContentView(R.layout.checklist);

		name = (TextView) findViewById(R.id.checklist_name);

		ListView listView = getListView();

		if (listView instanceof DragNDropListView) {
			((DragNDropListView) listView).setDropListener(mDropListener);
			((DragNDropListView) listView).setRemoveListener(mRemoveListener);
			((DragNDropListView) listView).setDragListener(mDragListener);
		}

		refresh();
	}

	public void refresh() {
		if (adapter != null) {
			for (int i = 0; i < adapter.getCount(); i++) {
				CheckListItem item = (CheckListItem) adapter.getItem(i);
				Log.i("1", Boolean.toString(item.isChecked()));
			}
		}

		adapter = new CheckListItemAdapter(this,
				new int[] { R.layout.checklist_item },
				new int[] { R.id.checklist_item_item }, checkList);

		setListAdapter(adapter);

		name.setText(nh.getFullPathById(id));
	}

	public void superOnBackPressed() {
		super.onBackPressed();
	}

	@Override
	public void onBackPressed() {
		if (checkList.equals(initialCheckList)) {
			// wasn't changed
			superOnBackPressed();
		} else {
			// if was changed
			new AlertDialog.Builder(this)
					.setTitle(R.string.noteseditor_title)
					.setMessage(R.string.checklist_msg_save_before_exit)
					.setPositiveButton(R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// save();
									superOnBackPressed();
								}
							})
					.setNegativeButton(R.string.no,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.checklist_options, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.checklist_options_add:
			onNew();
			return true;
		}
		return false; // super.onOptionsItemSelected(item);
	}

	public void onNew() {
		final EditText edit = new EditText(this);

		new AlertDialog.Builder(this)
				.setTitle(R.string.checklist_newitem_title)
				.setMessage(R.string.checklist_newitem_prompt)
				.setView(edit)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								String itemName = edit.getText().toString();
								checkList
										.add(new CheckListItem(itemName, false));
								refresh();
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
			ImageView iv = (ImageView) itemView.findViewById(R.id.imageView01);
			if (iv != null)
				iv.setVisibility(View.INVISIBLE);
		}

		public void onStopDrag(View itemView) {
			itemView.setVisibility(View.VISIBLE);
			itemView.setBackgroundColor(defaultBackgroundColor);
			ImageView iv = (ImageView) itemView.findViewById(R.id.imageView01);
			if (iv != null)
				iv.setVisibility(View.VISIBLE);
		}

	};
}