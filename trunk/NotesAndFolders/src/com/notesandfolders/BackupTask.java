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

import android.os.AsyncTask;

public class BackupTask extends AsyncTask<Void, Integer, Integer> {

	public static final int BACKUP_OK = 0;
	public static final int BACKUP_CANT_CREATE_OUTPUT_DIRECTORY = 1;
	public static final int BACKUP_CANT_CREATE_OUTPUT_FILE = 2;

	private BackupActivity ba;
	private NodeHelper nh;
	private boolean completed;
	private Integer result;

	public BackupTask(BackupActivity ba, NodeHelper nh) {
		this.ba = ba;
		this.nh = nh;
	}

	@Override
	protected Integer doInBackground(Void... arg0) {
		int nodesCount = (int) nh.getNodesCount();

		for (int i = 0; i < nodesCount; i++) {
			try {
				Thread.sleep(50);
				publishProgress(i + 1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		result = BACKUP_OK;

		return result;
	}

	protected void onProgressUpdate(Integer... progress) {
		if (progress.length > 0) {
			ba.getImportingDialog().setProgress(progress[0]);
		}
	}

	@Override
	protected void onPreExecute() {
		ba.showDialog(BackupActivity.DIALOG_BACKUP);
	}

	@Override
	protected void onPostExecute(Integer res) {
		ba.dismissDialog(BackupActivity.DIALOG_BACKUP);

		notifyActivityTaskCompleted();
	}

	public void setActivity(BackupActivity explorer) {
		this.ba = explorer;
		if (completed) {
			notifyActivityTaskCompleted();
		}
	}

	private void notifyActivityTaskCompleted() {
		if (ba != null) {
			ba.onBackupTaskCompleted(result);
		}
	}
}