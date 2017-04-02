package com.notesandfolders;

public class IconListItem {
	public final String text;
	public final int icon;

	public IconListItem(String text, Integer icon) {
		this.text = text;
		this.icon = icon;
	}

	@Override
	public String toString() {
		return text;
	}
}
