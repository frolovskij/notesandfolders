package com.notesandfolders;

public class AlertDialogListItem {
	public final String text;
	public final int icon;

	public AlertDialogListItem(String text, Integer icon) {
		this.text = text;
		this.icon = icon;
	}

	@Override
	public String toString() {
		return text;
	}
}
