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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.os.AsyncTask;

public class RestoreTask extends
		AsyncTask<Void, Integer, RestoreTask.RestoreResult> {

	public enum RestoreResult {
		OK, CANT_READ_FILE, FILE_STURCTURE_IS_WRONG, IO_ERROR
	};

	public static final String OUTPUT_DIR = "NotesAndFolders";

	private BackupManagerActivity bm;
	private NodeHelper nh;
	private File backupFile;

	/**
	 * 
	 * @param bm
	 *            parent activity that holds a progress dialog
	 * @param nh
	 * @param file
	 *            backup file that contains data to restore from
	 */
	public RestoreTask(BackupManagerActivity bm, NodeHelper nh, File file) {
		this.bm = bm;
		this.nh = nh;
		this.backupFile = file;
	}

	@Override
	protected RestoreResult doInBackground(Void... arg0) {
		DataInputStream dis = null;
		try {
			dis = new DataInputStream(new BufferedInputStream(
					new FileInputStream(backupFile)));

			final String password = dis.readUTF();
			final String key = dis.readUTF();

			System.out.println(password);
			System.out.println(key);

			while (dis.available() != 0) {
				int dataLen = dis.readInt();
				if (dataLen > 0) {
					byte[] data = new byte[dataLen];
					dis.read(data);
					Node n = nh.getNodeFromByteArray(data);

					System.out.println(n.toString());
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			return RestoreResult.IO_ERROR;
		} finally {
			if (dis != null) {
				try {
					dis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return RestoreResult.OK;
	}

	protected void onProgressUpdate(Integer... progress) {
		if (progress.length > 0) {
			bm.getBackupDialog().setProgress(progress[0]);
		}
	}

	@Override
	protected void onPreExecute() {
		bm.showDialog(BackupManagerActivity.DIALOG_RESTORE);
	}

	@Override
	protected void onPostExecute(RestoreResult result) {
		bm.dismissDialog(BackupManagerActivity.DIALOG_RESTORE);

		if (bm != null) {
			bm.onRestoreTaskCompleted(result);
		}
	}

	public void setActivity(BackupManagerActivity bm) {
		this.bm = bm;
	}
}