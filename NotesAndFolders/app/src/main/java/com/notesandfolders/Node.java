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
import java.util.Date;

public class Node implements Serializable {
	private static final long serialVersionUID = 1249482606261653819L;

	private long id;
	private long parentId;
	private String name;
	private String textContent;

	/**
	 * <ul>
	 * <li>0 - encrypted the old way on Android < 4.2, still can be read on new Androids</li>
	 * <li>1 - the right way</li>
	 * </ul>
	 */
	private int encryptVersion;

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

	public int getEncryptVersion() {
		return encryptVersion;
	}

	public void setEncryptVersion(int encryptVersion) {
		this.encryptVersion = encryptVersion;
	}

	public String toString() {
		return String
				.format("{id: '%d', parent_id: '%s', name: '%s', date_created: '%d', date_modified: '%d', type: '%d', encrypt_version: '%d'}",
						getId(),
						getParentId(),
						getName(),
						getDateCreated().getTime(),
						getDateModified().getTime(),
						getType().ordinal(),
						getEncryptVersion());
	}

	public boolean equalsTo(Node other) {
		if (other == null) {
			return false;
		}

		return getId() == other.getId() &&
				getParentId() == other.getParentId() &&
				getType() == other.getType() &&
				getName().equals(other.getName()) &&
				getDateCreated().equals(other.getDateCreated()) &&
				getDateModified().equals(other.getDateModified()) &&
				getEncryptVersion() == other.getEncryptVersion();
	}
}
