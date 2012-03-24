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

import java.util.Date;

public class Node {

	private long id;
	private long parentId;
	private String name;
	private String textContent;
	private Date dateCreated;
	private Date dateModified;
	private NodeType type;

	public NodeType getType() {
		return type;
	}

	public void setType(NodeType type) {
		this.type = type;
	}

	public long getParentId() {
		return parentId;
	}

	public void setParentId(long parentId) {
		this.parentId = parentId;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTextContent() {
		return textContent;
	}

	public void setTextContent(String textContent) {
		this.textContent = textContent;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getDateModified() {
		return dateModified;
	}

	public void setDateModified(Date dateModified) {
		this.dateModified = dateModified;
	}

	public String toString() {
		return String
				.format("{id: '%d', parent_id: '%s', name: '%s', date_created: '%d', date_modified: '%d', type: '%d'}",
						getId(), getParentId(), getName(), getDateCreated().getTime(),
						getDateModified().getTime(), getType().ordinal());
	}

	public boolean equalsTo(Node file) {
		if (file == null) {
			return false;
		}

		return (getId() == file.getId() && getParentId() == file.getParentId()
				&& getType() == file.getType() && getName().equals(file.getName())
				&& getDateCreated().equals(file.getDateCreated()) && getDateModified().equals(
				file.getDateModified()));
	}

}
