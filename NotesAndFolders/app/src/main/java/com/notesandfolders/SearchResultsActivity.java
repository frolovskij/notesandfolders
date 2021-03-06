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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.notesandfolders.R;
import com.tani.app.ui.IconContextMenu;
import com.tani.app.ui.IconContextMenu.IconContextMenuOnClickListener;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SearchResultsActivity extends Activity implements OnItemClickListener {

	private static final int DIALOG_CONTEXT_MENU = 0;
	private static final int DIALOG_PROPERTIES = 8;
	private static final int MENU_PROPERTIES = 1;

	private NodeHelper nh;
	private ListView lv;
	private List<Node> items;
	private NodeAdapter adapter;
	private IconContextMenu iconContextMenu = null;
	private TextView placeholder;

	// selected_id is id of the selected node to pass to context menu operation
	private long getSelectedId() {
		return getIntent().getLongExtra("selected_id", -1);
	}

	private void setSelectedId(long selectedId) {
		getIntent().putExtra("selected_id", selectedId);
	}

	// refresh would set list's focus to the node with this id
	private long getIdToSetFocusTo() {
		return getIntent().getLongExtra("id_to_set_focus", -1);
	}

	private void setIdToSetFocusTo(long idToSetFocusTo) {
		getIntent().putExtra("id_to_set_focus", idToSetFocusTo);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		new TempStorage(this).deleteSearchParameters();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		nh = new NodeHelper(this, new TempStorage(this).getPassword());

		// set as content view
		setContentView(R.layout.searchresults);
		lv = (ListView) findViewById(R.id.searchresults_listview);
		placeholder = (TextView) findViewById(R.id.searchresults_placeholder);

		lv.setOnItemLongClickListener(itemLongClickHandler);
		lv.setOnItemClickListener(this);

		createContextMenu();

		refresh();
	}

	public void refresh() {
		List<Long> idsToDisplay = (List<Long>) Serializer.deserialize(getIntent().getExtras()
				.getString("ids_list"));

		items = new ArrayList<Node>();
		for (Long id : idsToDisplay) {
			if (id != null) {
				items.add(nh.getNodeById(id));
			}
		}

		if (items.isEmpty()) {
			placeholder.setVisibility(View.VISIBLE);
		} else {
			placeholder.setVisibility(View.GONE);
		}

		Collections.sort(items, new NaturalOrderNodesComparator());

		adapter = new NodeAdapter(this, R.layout.explorer_item, items);
		lv.setAdapter(adapter);

		for (int i = 0; i < adapter.getCount(); i++) {
			Node n = adapter.getItem(i);
			if (n != null && n.getId() == getIdToSetFocusTo()) {
				lv.setSelection(i);
			}
		}
	}

	// context menu listener for nodes
	final IconContextMenuOnClickListener contextMenuListener = new IconContextMenuOnClickListener() {
		public void onClick(int menuId) {
			switch (menuId) {
			case MENU_PROPERTIES:
				showDialog(DIALOG_PROPERTIES);
				break;
			}
		}
	};

	public void createContextMenu() {
		Resources res = getResources();

		iconContextMenu = new IconContextMenu(this, DIALOG_CONTEXT_MENU);
		iconContextMenu.addItem(res, R.string.properties, R.drawable.properties, MENU_PROPERTIES);
		iconContextMenu.setOnClickListener(contextMenuListener);
	}

	private OnItemLongClickListener itemLongClickHandler = new OnItemLongClickListener() {
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			setSelectedId(((Node) lv.getItemAtPosition(position)).getId());
			showDialog(DIALOG_CONTEXT_MENU);
			return true;
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == DIALOG_CONTEXT_MENU) {
			return iconContextMenu.createMenu(getText(R.string.explorer_context_menu_title)
					.toString());
		}

		if (id == DIALOG_PROPERTIES) {
			Dialog dialog = new Dialog(SearchResultsActivity.this);
			dialog.setContentView(R.layout.properties);

			LayoutParams params = dialog.getWindow().getAttributes();
			params.width = LayoutParams.FILL_PARENT;
			dialog.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

			dialog.setTitle(R.string.properties_title);
			dialog.setCancelable(true);

			return dialog;
		}

		return super.onCreateDialog(id);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);

		if (id == DIALOG_PROPERTIES) {
			Node n = nh.getNodeById(getSelectedId());

			ImageView icon = (ImageView) dialog.findViewById(R.id.properties_file_icon);

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

			TextView name = (TextView) dialog.findViewById(R.id.properties_file_name);
			name.setText(nh.getFullPathById(n.getId()));

			TextView dateCreated = (TextView) dialog.findViewById(R.id.properties_date_created);
			dateCreated.setText(new SimpleDateFormat().format(n.getDateCreated()));

			TextView dateModified = (TextView) dialog.findViewById(R.id.properties_date_modified);
			dateModified.setText(new SimpleDateFormat().format(n.getDateModified()));
		}
	}

	private void openNote(long id) {
		// when going back from viewer/editor this item will be focused
		setIdToSetFocusTo(id);

		Intent viewer = new Intent(this, NotesViewerActivity.class);
		viewer.putExtra("note_id", id);
		startActivity(viewer);
	}

	public void openCheckList(long id) {
		// when going back from viewer/editor this item will be focused
		setIdToSetFocusTo(id);

		Intent viewer = new Intent(this, CheckListActivity.class);
		viewer.putExtra("checklist_id", id);
		startActivity(viewer);
	}

	private void onOpen(long id) {
		Node node = nh.getNodeById(id);

		switch (node.getType()) {
		case NOTE:
			openNote(node.getId());
			break;
		case CHECKLIST:
			openCheckList(node.getId());
			break;
		}
	}

	public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
		Node selected = (Node) lv.getItemAtPosition(position);
		onOpen(selected.getId());
	}
}
