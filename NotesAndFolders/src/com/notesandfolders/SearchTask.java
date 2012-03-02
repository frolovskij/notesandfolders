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

public class SearchTask extends AsyncTask<Void, String, Integer> {
	private SearchParameters mParameters;
	private SearchActivity mSearchActivity;
	private NodeHelper mNh;
	private boolean completed;
	private Integer result;

	public SearchTask(SearchActivity searchActivity, NodeHelper nh,
			SearchParameters searchParameters) {
		mNh = nh;
		mSearchActivity = searchActivity;
		mParameters = searchParameters;
	}

	@Override
	protected Integer doInBackground(Void... arg0) {
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	@Override
	protected void onPreExecute() {
		mSearchActivity.showDialog(SearchActivity.DIALOG_SEARCH);
	}

	@Override
	protected void onPostExecute(Integer res) {
		mSearchActivity.dismissDialog(SearchActivity.DIALOG_SEARCH);

		notifyActivityTaskCompleted();
	}

	public void setActivity(SearchActivity searchActivity) {
		this.mSearchActivity = searchActivity;
		if (completed) {
			notifyActivityTaskCompleted();
		}
	}

	private void notifyActivityTaskCompleted() {
		if (null != mSearchActivity) {
			mSearchActivity.onSearchTaskCompleted(result);
		}
	}
}