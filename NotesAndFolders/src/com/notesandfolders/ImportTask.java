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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.os.AsyncTask;

public class ImportTask extends AsyncTask<Void, Integer, Integer> {

	private ImportActivity mExplorer;
	private boolean completed;
	private Integer result;
	private File mFile;

	public ImportTask(ImportActivity explorer, File file) {
		mExplorer = explorer;
		mFile = file;
	}

	protected void onProgressUpdate(Integer... progress) {
		if (progress.length == 1) {
			mExplorer.getImportingDialog().setProgress(progress[0]);
		}

		if (progress.length == 2) {
			mExplorer.getImportingDialog().setProgress(progress[0]);
			mExplorer.getImportingDialog().setMax(progress[1]);
		}
	}

	@Override
	protected Integer doInBackground(Void... arg0) {
		final NodeHelper nh = new NodeHelper(mExplorer, new TempStorage(
				mExplorer).getPassword());

		Node importRoot = nh.createFolder(
				nh.getRootFolder(),
				"Imported at "
						+ new SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
								.format(new Date()));

		final List<Node> nodes = ImportHelper.getFiles(mFile.getAbsolutePath(),
				nh.getLastId() + 1, importRoot.getId());

		// if there's nothing to import
		if (nodes.size() == 0) {
			nh.deleteNodeById(importRoot.getId());
		} else {

			int nodesCount = nodes.size();
			publishProgress(0, nodesCount);

			for (int i = 0; i < nodesCount; i++) {
				Node n = nodes.get(i);

				nh.insertNode(n);
				publishProgress(i + 1, nodesCount);
			}
		}

		return 0;
	}

	@Override
	protected void onPreExecute() {
		mExplorer.showDialog(ImportActivity.IMPORTING_DIALOG_ID);
	}

	@Override
	protected void onPostExecute(Integer res) {
		mExplorer.dismissDialog(ImportActivity.IMPORTING_DIALOG_ID);

		notifyActivityTaskCompleted();
	}

	public void setActivity(ImportActivity explorer) {
		this.mExplorer = explorer;
		if (completed) {
			notifyActivityTaskCompleted();
		}
	}

	/**
	 * Helper method to notify the activity that this task was completed.
	 */
	private void notifyActivityTaskCompleted() {
		if (null != mExplorer) {
			mExplorer.onCopyTaskCompleted(result);
		}
	}
}