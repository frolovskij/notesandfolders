package com.notesandfolders;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.notesandfolders.activities.ExplorerActivity;
import com.notesandfolders.dataaccess.NodeHelper;

public final class CopyTask extends AsyncTask<Void, String, Integer> {

	private final ExplorerActivity mExplorer;
	private NodeHelper mNh;
	private long mIdToCopy;
	private long mNewParentId;

	private ProgressDialog pd;

	public CopyTask(ExplorerActivity explorer, NodeHelper nh, long idToCopy,
			long newParentId) {
		mNh = nh;
		mIdToCopy = idToCopy;
		mNewParentId = newParentId;
		mExplorer = explorer;
	}

	@Override
	protected Integer doInBackground(Void... arg0) {
		return mNh.copy(mIdToCopy, mNewParentId);
	}

	@Override
	protected void onPreExecute() {
		pd = new ProgressDialog(mExplorer);
		pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pd.setMessage(mExplorer.getText(R.string.explorer_msg_copying_files));
		pd.setCancelable(false);
		pd.show();
	}

	@Override
	protected void onPostExecute(Integer res) {
		pd.dismiss();

		mExplorer.checkPasteResult(res);
	}
}