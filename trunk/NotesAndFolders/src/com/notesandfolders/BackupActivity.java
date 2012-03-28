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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

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

	public void onBackupTaskCompleted(Integer result) {
		if (mShownDialog) {
			checkBackupResult(result);
		}
	}

	public void checkBackupResult(int result) {
		System.out.println(result);
	}

	public void onClick(View v) {
		backupTask = new BackupTask(this, new NodeHelper(this, new TempStorage(
				this).getPassword()));
		backupTask.execute();
	}
}