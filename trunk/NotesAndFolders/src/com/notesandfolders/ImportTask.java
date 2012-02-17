package com.notesandfolders;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.os.AsyncTask;

import com.notesandfolders.activities.ExplorerActivity;
import com.notesandfolders.activities.ImportActivity;
import com.notesandfolders.dataaccess.NodeHelper;

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
		final NodeHelper nh = new NodeHelper(mExplorer,
				Login.getPlainTextPasswordFromTempStorage(mExplorer));

		Node importRoot = nh.createFolder(nh.getRootFolder(), "Imported at "
				+ new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date()));

		final List<Node> nodes = FileImporter.getFiles(mFile.getAbsolutePath(), nh.getLastId() + 1,
				importRoot.getId());

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