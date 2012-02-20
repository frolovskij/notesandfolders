package com.notesandfolders;

import java.io.Serializable;
import java.util.ArrayList;
import biz.source_code.base64Coder.Base64Coder;

public class CheckList extends ArrayList<CheckListItem> implements Serializable {
	private static final long serialVersionUID = 328947806452179738L;

	public String serialize() {
		byte[] serialized = Serializer.serializeObject(this);
		return new String(Base64Coder.encode(serialized));
	}

	public static CheckList deserialize(String serializedAsString) {
		byte[] serialized = Base64Coder.decode(serializedAsString);
		return (CheckList) Serializer.deserializeObject(serialized);
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		if (!(o instanceof CheckList)) {
			return false;
		}
		CheckList otherObj = (CheckList) o;

		if (otherObj.size() != this.size()) {
			return false;
		}

		for (int i = 0; i < size(); i++) {
			if (!get(i).equals(otherObj.get(i))) {
				return false;
			}
		}

		return true;
	}

	public int hashCode() {
		int result = 17;
		for (int i = 0; i < size(); i++) {
			result = 31 * result + get(i).hashCode();
		}

		return result;
	}

	public CheckList clone() {
		return CheckList.deserialize(serialize());
	}
}
