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

public class CopyTask extends AsyncTask<Void, String, Integer> {

	private ExplorerActivity mExplorer;
	private NodeHelper mNh;
	private long mIdToCopy;
	private long mNewParentId;
	private boolean completed;
	private Integer result;

	public CopyTask(ExplorerActivity explorer, NodeHelper nh, long idToCopy,
			long newParentId) {
		mNh = nh;
		mIdToCopy = idToCopy;
		mNewParentId = newParentId;
		mExplorer = explorer;
	}

	@Override
	protected Integer doInBackground(Void... arg0) {
		result = mNh.copy(mIdToCopy, mNewParentId);
		return result;
	}

	@Override
	protected void onPreExecute() {
		mExplorer.showDialog(ExplorerActivity.DIALOG_COPY);
	}

	@Override
	protected void onPostExecute(Integer res) {
		mExplorer.dismissDialog(ExplorerActivity.DIALOG_COPY);

		notifyActivityTaskCompleted();
	}

	public void setActivity(ExplorerActivity explorer) {
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