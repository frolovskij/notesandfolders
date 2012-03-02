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

public class SearchParameters {
	private String text;
	private long folderId;
	private boolean caseSensitive;
	private boolean searchInNames;
	private boolean searchInText;

	public SearchParameters() {
		text = "";
		folderId = 0;
		caseSensitive = false;
		searchInNames = true;
		searchInText = true;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public long getFolderId() {
		return folderId;
	}

	public void setFolderId(long folderId) {
		this.folderId = folderId;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	public boolean isSearchInNames() {
		return searchInNames;
	}

	public void setSearchInNames(boolean searchInNames) {
		this.searchInNames = searchInNames;
	}

	public boolean isSearchInText() {
		return searchInText;
	}

	public void setSearchInText(boolean searchInText) {
		this.searchInText = searchInText;
	}
}
