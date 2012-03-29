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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import android.os.AsyncTask;
import android.os.Environment;

enum BackupResult {
	OK, CANT_CREATE_OUTPUT_DIRECTORY, CANT_CREATE_OUTPUT_FILE, FILE_ALREADY_EXISTS, CANT_WRITE_TO_OUTPUT_FILE, IO_ERROR
}

public class BackupTask extends AsyncTask<Void, Integer, BackupResult> {

	public static final int BACKUP_OK = 0;
	public static final int BACKUP_CANT_CREATE_OUTPUT_DIRECTORY = 1;
	public static final int BACKUP_CANT_CREATE_OUTPUT_FILE = 2;
	public static final int BACKUP_FILE_ALREADY_EXISTS = 3;
	public static final int BACKUP_CANT_WRITE_TO_OUTPUT_FILE = 4;
	public static final int BACKUP_IO_ERROR = 5;

	public static final String OUTPUT_DIR = "NotesAndFolders";

	private BackupActivity ba;
	private NodeHelper nh;

	public BackupTask(BackupActivity ba, NodeHelper nh) {
		this.ba = ba;
		this.nh = nh;
	}

	@Override
	protected BackupResult doInBackground(Void... arg0) {
		File root = Environment.getExternalStorageDirectory();

		File outputDir = new File(root, OUTPUT_DIR);
		if (!outputDir.exists()) {
			if (outputDir.mkdirs() == false) {
				return BackupResult.CANT_CREATE_OUTPUT_DIRECTORY;
			}
		}

		File backupFile = new File(outputDir, String.format("%d.nf1",
				new Date().getTime()));
		if (backupFile.exists()) {
			return BackupResult.FILE_ALREADY_EXISTS;
		}

		try {
			if (backupFile.createNewFile() == false) {
				return BackupResult.CANT_CREATE_OUTPUT_FILE;
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			return BackupResult.CANT_CREATE_OUTPUT_FILE;
		}

		if (backupFile.canWrite() == false) {
			return BackupResult.CANT_WRITE_TO_OUTPUT_FILE;
		}

		Settings s = new Settings(ba);
		final String password = s.getPasswordSha1Hash();
		final String key = s.getEncryptedKey();
		final int nodesCount = (int) nh.getNodesCount();

		PrintWriter pw = null;
		try {
			pw = new PrintWriter(backupFile);
			pw.println(password);
			pw.println(key);

			for (int i = 0; i < nodesCount; i++) {
				pw.println(nh.getNodeAsString(i));
				publishProgress(i + 1);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			return BackupResult.IO_ERROR;
		} finally {
			if (pw != null) {
				pw.close();
			}
		}

		return BackupResult.OK;
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
	protected void onPostExecute(BackupResult result) {
		ba.dismissDialog(BackupActivity.DIALOG_BACKUP);

		if (ba != null) {
			ba.onBackupTaskCompleted(result);
		}
	}

	public void setActivity(BackupActivity explorer) {
		this.ba = explorer;
	}
}