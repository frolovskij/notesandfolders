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

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;

public class SearchTask extends AsyncTask<Void, String, List<Long>> {
	private SearchParameters mParameters;
	private SearchActivity mSearchActivity;
	private NodeHelper mNh;
	private boolean completed;
	private List<Long> result;

	public SearchTask(SearchActivity searchActivity, NodeHelper nh,
			SearchParameters searchParameters) {
		mNh = nh;
		mSearchActivity = searchActivity;
		mParameters = searchParameters;
	}

	public List<Long> getSearchResults() {
		if (mParameters.isCaseSensitive() == false) {
			mParameters.setText(mParameters.getText().toLowerCase());
		}
		result = new ArrayList<Long>();
		return search(mParameters.getFolderId());
	}

	private List<Long> search(long folderId) {
		List<Long> nodesId = mNh.getChildrenIdsById(folderId);
		for (Long id : nodesId) {
			if (id == null) {
				continue;
			}

			Node n = mNh.getNodeById(id);

			boolean match = false;

			if (mParameters.isSearchInText() && n.getType() != NodeType.FOLDER) {
				String tc = mNh.getTextContentById(id);

				if (n.getType() == NodeType.NOTE) {
					if (mParameters.isCaseSensitive() == false) {
						tc = tc.toLowerCase();
					}

					if (tc.contains(mParameters.getText())) {
						match = true;
					}
				} else if (n.getType() == NodeType.CHECKLIST) {
					CheckList cl = (CheckList) Serializer.deserialize(tc);
					for (CheckListItem item : cl) {
						String text = item.getText();
						if (mParameters.isCaseSensitive() == false) {
							text = text.toLowerCase();
						}
						if (text.contains(mParameters.getText())) {
							match = true;
							break;
						}
					}

				}
			}

			if (match == false && mParameters.isSearchInNames()) {
				String name = n.getName();
				if (mParameters.isCaseSensitive() == false) {
					name = name.toLowerCase();
				}

				if (name.contains(mParameters.getText())) {
					match = true;
				}
			}

			if (match) {
				result.add(n.getId());
			}

			if (n.getType() == NodeType.FOLDER) {
				search(n.getId());
			}
		}

		return result;
	}

	@Override
	protected List<Long> doInBackground(Void... arg0) {
		return getSearchResults();
	}

	@Override
	protected void onPreExecute() {
		mSearchActivity.showDialog(SearchActivity.DIALOG_SEARCH);
	}

	@Override
	protected void onPostExecute(List<Long> res) {
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