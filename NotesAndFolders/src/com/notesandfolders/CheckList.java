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
}
