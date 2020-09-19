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

import com.notesandfolders.BackupTask.BackupResult;
import com.notesandfolders.RestoreTask.RestoreResult;
import com.tani.app.ui.IconContextMenu;
import com.tani.app.ui.IconContextMenu.IconContextMenuOnClickListener;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

public class BackupManagerActivity extends Activity implements OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {
	private static final int DIALOG_CONTEXT_MENU = 0;
	public static final int DIALOG_BACKUP = 1;
	private static final int DIALOG_DELETE = 2;
	public static final int DIALOG_RESTORE = 3;
	public static final int DIALOG_RESTART = 4;

	private static final int MENU_RESTORE = 0;
	private static final int MENU_DELETE = 1;

	private static final int WRITE_EXTERNAL_STORAGE_GRANTED_FOR_READ = 1;

	private Button backupButton;
	private TextView emptyPlaceholder;
	private ListView lv;

	private BackupTask backupTask;
	private RestoreTask restoreTask;
	private boolean mShownDialog;
	private ProgressDialog backupDialog;
	private ProgressDialog restoreDialog;

	private File selectedFile;
	private List<File> items;
	private FileAdapter adapter;

	private IconContextMenu iconContextMenu = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.backupmanager);

		emptyPlaceholder = (TextView) findViewById(R.id.backupmanager_placeholder);

		backupButton = (Button) findViewById(R.id.backupmanager_button);
		backupButton.setOnClickListener(this);

		lv = (ListView) findViewById(R.id.backupmanager_listview);
		lv.setOnItemLongClickListener(itemLongClickHandler);

		createContextMenu();

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
					WRITE_EXTERNAL_STORAGE_GRANTED_FOR_READ);
		} else {
			refresh();
		}
	}

	public ProgressDialog getBackupDialog() {
		return backupDialog;
	}

	public ProgressDialog getRestoreDialog() {
		return restoreDialog;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == DIALOG_CONTEXT_MENU) {
			return iconContextMenu.createMenu(getText(
					R.string.explorer_context_menu_title).toString());
		}

		if (id == DIALOG_BACKUP) {
			backupDialog = new ProgressDialog(this);
			backupDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			backupDialog.setMessage(getText(R.string.backup_msg_in_progress));
			backupDialog.setCancelable(false);

			NodeHelper nh = new NodeHelper(this,
					new TempStorage(this).getPassword());
			backupDialog.setMax((int) nh.getNodesCount());

			return backupDialog;
		}

		if (id == DIALOG_RESTORE) {
			restoreDialog = new ProgressDialog(this);
			restoreDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			restoreDialog.setMessage(getText(R.string.restore_msg_in_progress));
			restoreDialog.setCancelable(false);

			return restoreDialog;
		}

		if (id == DIALOG_DELETE) {
			return new AlertDialog.Builder(this)
					.setTitle(R.string.backupmanager_delete_title)
					.setMessage(R.string.backupmanager_delete_prompt)
					.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									if (selectedFile.delete()) {
										showToast(R.string.backupmanager_msg_backup_file_deleted);
										refresh();
									} else {
										showToast(R.string.backupmanager_msg_backup_file_cant_delete);
									}
								}
							})
					.setNegativeButton(android.R.string.no,
							ExplorerActivity.DUMMY_LISTENER).create();
		}

		if (id == DIALOG_RESTART) {
			return new AlertDialog.Builder(this)
					.setTitle(R.string.backupmanager_restart_title)
					.setMessage(R.string.backupmanager_restart_text)
					.setCancelable(false)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// closing explorer and login activities
									new TempStorage(BackupManagerActivity.this)
											.setExiting();
									finish();
								}
							}).create();

		}

		return super.onCreateDialog(id);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		if (id == DIALOG_BACKUP || id == DIALOG_RESTORE) {
			mShownDialog = true;
		}
	}

	public void onBackupTaskCompleted(BackupResult result) {
		if (mShownDialog) {
			checkBackupResult(result);
			refresh();
		}
	}

	public void onRestoreTaskCompleted(RestoreResult result) {
		if (mShownDialog) {
			checkRestoreResult(result);
			refresh();
		}
	}

	public void checkBackupResult(BackupResult result) {
		switch (result) {
		case OK:
			showToast(R.string.backup_result_ok);
			break;
		case CANT_CREATE_OUTPUT_DIRECTORY:
			showToast(R.string.backup_result_cant_create_output_directory);
			break;
		case CANT_CREATE_OUTPUT_FILE:
			showToast(R.string.backup_result_cant_create_output_file);
			break;
		case CANT_WRITE_TO_OUTPUT_FILE:
			showToast(R.string.backup_result_cant_write_to_output_file);
			break;
		case FILE_ALREADY_EXISTS:
			showToast(R.string.backup_result_file_aready_exists);
			break;
		case IO_ERROR:
			showToast(R.string.backup_result_io_error);
			break;
		}
	}

	public void checkRestoreResult(RestoreResult result) {
		switch (result) {
		case OK:
			showDialog(DIALOG_RESTART);
			break;
		}
	}

	public void onClick(View v) {
		if (v == backupButton) {
			backupTask = new BackupTask(this, new NodeHelper(this,
					new TempStorage(this).getPassword()));
			backupTask.execute();
		}
	}

	public void showToast(int stringResId) {
		Toast toast = Toast.makeText(getApplicationContext(), stringResId,
				Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	private OnItemLongClickListener itemLongClickHandler = new OnItemLongClickListener() {
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			selectedFile = ((File) lv.getItemAtPosition(position));

			showDialog(DIALOG_CONTEXT_MENU);

			return true;
		}
	};

	public void createContextMenu() {
		Resources res = getResources();

		iconContextMenu = new IconContextMenu(this, DIALOG_CONTEXT_MENU);
		iconContextMenu.addItem(res, R.string.restore, R.drawable.restore,
				MENU_RESTORE);
		iconContextMenu.addItem(res, R.string.delete, R.drawable.delete,
				MENU_DELETE);
		iconContextMenu.setOnClickListener(contextMenuListener);
	}

	// context menu listener for nodes
	final IconContextMenuOnClickListener contextMenuListener = new IconContextMenuOnClickListener() {
		public void onClick(int menuId) {
			switch (menuId) {
			case MENU_RESTORE:
				new AlertDialog.Builder(BackupManagerActivity.this)
						.setTitle(R.string.restore_confirm_title)
						.setMessage(R.string.restore_confirm_text)
						.setPositiveButton(android.R.string.yes,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										restoreTask = new RestoreTask(
												BackupManagerActivity.this,
												new NodeHelper(
														BackupManagerActivity.this,
														new TempStorage(
																BackupManagerActivity.this)
																.getPassword()),
												selectedFile);
										restoreTask.execute();
									}
								})
						.setNegativeButton(android.R.string.no,
								ExplorerActivity.DUMMY_LISTENER).show();

				break;

			case MENU_DELETE:
				showDialog(DIALOG_DELETE);
				break;
			}
		}
	};

	private void refresh() {
		items = new ArrayList<File>();

		File outputDir = new File(Environment.getExternalStorageDirectory(),
				BackupTask.OUTPUT_DIR);

		if (outputDir.exists()) {
			for (File f : outputDir.listFiles()) {
				items.add(f);
			}
		}

		adapter = new FileAdapter(this, R.layout.import_list_item, items);
		lv.setAdapter(adapter);

		if (items.isEmpty()) {
			emptyPlaceholder.setVisibility(View.VISIBLE);
		} else {
			emptyPlaceholder.setVisibility(View.GONE);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == WRITE_EXTERNAL_STORAGE_GRANTED_FOR_READ) {
			if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					refresh();
				} else {
					this.finish();
				}
			}
		}
	}
}