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

import java.io.Serializable;

public class CheckListItem implements Serializable {
	private static final long serialVersionUID = 488624307828105499L;

	private boolean isChecked;

	private String text;

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public CheckListItem(String text, boolean isChecked) {
		this.text = text;
		this.isChecked = isChecked;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		if (!(o instanceof CheckListItem)) {
			return false;
		}
		CheckListItem otherObj = (CheckListItem) o;
		return (isChecked() == otherObj.isChecked() && getText().equals(
				otherObj.getText()));
	}

	public int hashCode() {
		int result = 17;
		result = 31 * result + getText().hashCode();
		result = 31 * result + (isChecked() ? 1 : 0);
		return result;
	}

	public CheckListItem clone() {
		return (CheckListItem) Serializer.deserializeObject(Serializer
				.serializeObject(this));
	}
	
	@Override
	public String toString() {
		return String.format("%s - %b", text, isChecked);
	}
}
