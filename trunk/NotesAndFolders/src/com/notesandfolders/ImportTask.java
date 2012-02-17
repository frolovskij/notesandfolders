package com.notesandfolders;

import android.os.AsyncTask;

import com.notesandfolders.activities.ExplorerActivity;
import com.notesandfolders.dataaccess.NodeHelper;

public class ImportTask extends AsyncTask<Void, String, Integer> {

	private ExplorerActivity mExplorer;
	private NodeHelper mNh;
	private long mIdToCopy;
	private long mNewParentId;
	private boolean completed;
	private Integer result;

	public ImportTask(ExplorerActivity explorer, NodeHelper nh, long idToCopy,
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
		mExplorer.showDialog(ExplorerActivity.COPYING_DIALOG_ID);
	}

	@Override
	protected void onPostExecute(Integer res) {
		mExplorer.dismissDialog(ExplorerActivity.COPYING_DIALOG_ID);

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