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

import java.io.File;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.notesandfolders.FileAdapter;
import com.notesandfolders.FileImporter;
import com.notesandfolders.Login;
import com.notesandfolders.Node;
import com.notesandfolders.R;
import com.notesandfolders.dataaccess.NodeHelper;
import com.tani.app.ui.IconContextMenu;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class FileSystemExplorerActivity extends BaseActivity implements
		IconContextMenu.IconContextMenuOnClickListener, OnItemClickListener,
		OnClickListener {
	private static final int CONTEXT_MENU_ID = 0;
	private static final int MENU_IMPORT = 1;
	private ListView lv;
	private TextView location;
	private List<File> items;
	private ImageButton upButton;
	private FileAdapter adapter;
	private IconContextMenu iconContextMenu = null;

	private File directory;
	private File selectedFile;
	private String path;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		path = getIntent().getExtras().getString("path");
		if (path == null) {
			path = "/";
		}

		setContentView(R.layout.fsexplorer);
		location = (TextView) findViewById(R.id.fsexplorer_path);

		lv = (ListView) findViewById(R.id.fsexplorer_listview);
		lv.setOnItemLongClickListener(itemLongClickHandler);
		lv.setOnItemClickListener(this);

		upButton = (ImageButton) findViewById(R.id.fsexplorer_up_button);
		upButton.setOnClickListener(this);

		directory = new File(path);
		update();
	}

	public void createContextMenu() {
		Resources res = getResources();

		iconContextMenu = new IconContextMenu(this, CONTEXT_MENU_ID);
		iconContextMenu.addItem(res, R.string.fsimp, R.drawable.fsimp,
				MENU_IMPORT);
		iconContextMenu.setOnClickListener(this);
	}

	private OnItemLongClickListener itemLongClickHandler = new OnItemLongClickListener() {
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			selectedFile = ((File) lv.getItemAtPosition(position));

			createContextMenu();
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

	public void update() {
		items = new ArrayList<File>();

		for (File f : directory.listFiles()) {
			items.add(f);
		}
		adapter = new FileAdapter(this, R.layout.fsexplorer_item, items);
		lv.setAdapter(adapter);

		location.setText(directory.getAbsolutePath());
	}

	public void onImport() {
		int canImportResult = FileImporter.canImport(selectedFile);
		switch (canImportResult) {
		case FileImporter.RESULT_NOT_EXISTS:
			showToast(R.string.filesystemexplorer_msg_file_doesnt_exist);
			break;
		case FileImporter.RESULT_CANT_READ:
			showToast(R.string.filesystemexplorer_msg_cant_read);
			break;
		case FileImporter.RESULT_NOT_TXT:
			showToast(R.string.filesystemexplorer_msg_not_txt_file);
			break;
		}

		if (canImportResult != FileImporter.RESULT_OK) {
			return;
		}

		final ProgressDialog pd = new ProgressDialog(
				FileSystemExplorerActivity.this);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMessage(getText(R.string.msg_importing_files));
		pd.setCancelable(false);
		pd.show();

		final Context ctx = this;

		new Thread() {
			public void run() {
				final NodeHelper nh = new NodeHelper(
						FileSystemExplorerActivity.this,
						Login.getPlainTextPasswordFromTempStorage(ctx));

				Node importRoot = nh.createFolder(
						nh.getRootFolder(),
						"Imported at "
								+ new SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
										.format(new Date()));

				final List<Node> nodes = FileImporter.getFiles(
						selectedFile.getAbsolutePath(), nh.getLastId() + 1,
						importRoot.getId());

				// if there's nothing to import
				if (nodes.size() == 0) {
					nh.deleteNodeById(importRoot.getId());
				} else {
					int nodesCount = nodes.size();

					pd.setMax(nodesCount);

					for (int i = 0; i < nodesCount; i++) {
						Node n = nodes.get(i);

						nh.insertNode(n);
						pd.setProgress(i + 1);
					}
				}

				pd.dismiss();
			}
		}.start();
	}

	public void onClick(int menuId) {
		switch (menuId) {
		case MENU_IMPORT:
			onImport();
			break;
		}
	}

	public void onItemClick(AdapterView<?> parentView, View childView,
			int position, long id) {
		File clicked = (File) lv.getItemAtPosition(position);
		if (clicked.isDirectory() && clicked.canRead()) {
			directory = clicked;
			update();
		}
	}

	public void onClick(View v) {
		if (v == upButton) {
			File parent = directory.getParentFile();
			if (parent != null) {
				directory = parent;
				update();
			}
		}
	}
}
