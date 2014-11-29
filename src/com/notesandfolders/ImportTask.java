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

import com.notesandfolders.ImportHelper.ImportListener;

import android.content.Context;
import android.os.AsyncTask;

public class ImportTask extends AsyncTask<Void, Integer, Integer> implements ImportListener {

	private ImportActivity ia;
	private boolean completed;
	private Integer result;
	private File mFile;

	public ImportTask(ImportActivity ia, File file) {
		this.ia = ia;
		mFile = file;
	}

	protected void onProgressUpdate(Integer... progress) {
		if (progress.length == 1) {
			ia.getImportingDialog().setProgress(progress[0]);
		}

		if (progress.length == 2) {
			ia.getImportingDialog().setProgress(progress[0]);
			ia.getImportingDialog().setMax(progress[1]);
		}
	}

	@Override
	protected Integer doInBackground(Void... arg0) {
		ImportHelper.doImport(mFile, this);
		return 0;
	}

	@Override
	protected void onPreExecute() {
		ia.showDialog(ImportActivity.IMPORTING_DIALOG_ID);
	}

	@Override
	protected void onPostExecute(Integer res) {
		ia.dismissDialog(ImportActivity.IMPORTING_DIALOG_ID);

		notifyActivityTaskCompleted();
	}

	public void setActivity(ImportActivity explorer) {
		this.ia = explorer;
		if (completed) {
			notifyActivityTaskCompleted();
		}
	}

	/**
	 * Helper method to notify the activity that this task was completed.
	 */
	private void notifyActivityTaskCompleted() {
		if (null != ia) {
			ia.onCopyTaskCompleted(result);
		}
	}

	public Context getContext() {
		return ia;
	}

	public void publishProgress(int processed, int nodesCount) {
		super.publishProgress(processed, nodesCount);
	}
}