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

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import com.notesandfolders.R;
import com.tani.app.ui.IconContextMenu;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ImportActivity extends Activity implements
		IconContextMenu.IconContextMenuOnClickListener, OnItemClickListener, OnClickListener {
	private static final int CONTEXT_MENU_ID = 0;
	public static final int IMPORTING_DIALOG_ID = 1;
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

	private ImportTask importTask;
	private boolean mShownDialog;
	private ProgressDialog pd;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		path = getIntent().getExtras().getString("path");
		if (path == null) {
			path = "/";
		}

		setContentView(R.layout.import_);
		location = (TextView) findViewById(R.id.fsexplorer_path);

		lv = (ListView) findViewById(R.id.fsexplorer_listview);
		lv.setOnItemLongClickListener(itemLongClickHandler);
		lv.setOnItemClickListener(this);

		upButton = (ImageButton) findViewById(R.id.fsexplorer_up_button);
		upButton.setOnClickListener(this);

		directory = new File(path);
		update();

		createContextMenu();

		Object retained = getLastNonConfigurationInstance();
		if (retained != null && retained instanceof ImportTask) {
			importTask = (ImportTask) retained;
			importTask.setActivity(this);
		}
	}

	public ProgressDialog getImportingDialog() {
		return pd;
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		if (importTask != null) {
			importTask.setActivity(null);
			return importTask;
		}

		return null;
	}

	public void onCopyTaskCompleted(Integer result) {
		if (mShownDialog) {
			//
		}
	}

	public void createContextMenu() {
		Resources res = getResources();

		iconContextMenu = new IconContextMenu(this, CONTEXT_MENU_ID);
		iconContextMenu.addItem(res, R.string.fsimp, R.drawable.fsimp, MENU_IMPORT);
		iconContextMenu.setOnClickListener(this);
	}

	private OnItemLongClickListener itemLongClickHandler = new OnItemLongClickListener() {
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
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
			return iconContextMenu.createMenu(getText(R.string.explorer_context_menu_title)
					.toString());
		}

		if (id == IMPORTING_DIALOG_ID) {
			pd = new ProgressDialog(ImportActivity.this);
			pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pd.setMessage(getText(R.string.import_msg_importing_files));
			pd.setCancelable(false);
			pd.show();

			return pd;
		}
		return super.onCreateDialog(id);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		if (id == IMPORTING_DIALOG_ID) {
			mShownDialog = true;
		}
	}

	public void update() {
		items = new ArrayList<File>();

		for (File f : directory.listFiles()) {
			items.add(f);
		}
		adapter = new FileAdapter(this, R.layout.import_list_item, items);
		lv.setAdapter(adapter);

		location.setText(directory.getAbsolutePath());
	}

	public void onImport() {
		int canImportResult = ImportHelper.canImport(selectedFile);
		switch (canImportResult) {
		case ImportHelper.RESULT_NOT_EXISTS:
			showToast(R.string.import_msg_file_doesnt_exist);
			break;
		case ImportHelper.RESULT_CANT_READ:
			showToast(R.string.import_msg_cant_read);
			break;
		case ImportHelper.RESULT_NOT_TXT:
			showToast(R.string.import_msg_not_txt_file);
			break;
		}

		if (canImportResult != ImportHelper.RESULT_OK) {
			return;
		}

		importTask = new ImportTask(this, selectedFile);
		importTask.execute();
	}

	public void onClick(int menuId) {
		switch (menuId) {
		case MENU_IMPORT:
			onImport();
			break;
		}
	}

	public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
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

	public void showToast(int stringResId) {
		Toast toast = Toast.makeText(getApplicationContext(), stringResId, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
}
