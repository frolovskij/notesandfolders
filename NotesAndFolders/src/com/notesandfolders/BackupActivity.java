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

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class BackupActivity extends Activity implements OnClickListener {
	public static final int DIALOG_BACKUP = 1;
	private TextView info;
	private Button backupButton;

	private BackupTask backupTask;
	private boolean mShownDialog;
	private ProgressDialog backupDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.backup);

		info = (TextView) findViewById(R.id.backup_info);
		info.setText(Html.fromHtml(getText(R.string.backup_info).toString()));

		backupButton = (Button) findViewById(R.id.backup_button);
		backupButton.setOnClickListener(this);
	}

	public ProgressDialog getImportingDialog() {
		return backupDialog;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
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
		return super.onCreateDialog(id);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		if (id == DIALOG_BACKUP) {
			mShownDialog = true;
		}
	}

	public void onBackupTaskCompleted(BackupResult result) {
		if (mShownDialog) {
			checkBackupResult(result);
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
		System.out.println(result);
	}

	public void onClick(View v) {
		backupTask = new BackupTask(this, new NodeHelper(this, new TempStorage(
				this).getPassword()));
		backupTask.execute();
	}

	public void showToast(int stringResId) {
		Toast toast = Toast.makeText(getApplicationContext(), stringResId,
				Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
}