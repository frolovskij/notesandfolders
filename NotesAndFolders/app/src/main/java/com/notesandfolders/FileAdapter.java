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

import java.io.File;
import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ImageView;

public class FileAdapter extends ArrayAdapter<File> {

	private List<File> items;

	public FileAdapter(Context context, int textViewResourceId, List<File> items) {
		super(context, textViewResourceId, items);
		this.items = items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;

		if (v == null) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.import_list_item, null);
		}

		File f = items.get(position);
		if (f != null) {
			TextView fileName = (TextView) v.findViewById(R.id.fsexplorer_file_name);
			if (fileName != null) {
				fileName.setText(f.getName());
			}

			ImageView icon = (ImageView) v.findViewById(R.id.fsexplorer_file_icon);
			if (icon != null) {
				if (f.isDirectory()) {
					if (f.canRead()) {
						icon.setImageResource(R.drawable.fsfolder);
					} else {
						icon.setImageResource(R.drawable.fsfolder_locked);
					}
				} else {
					if (f.canRead()) {
						icon.setImageResource(R.drawable.fsfile);
					} else {
						icon.setImageResource(R.drawable.fsfile_locked);
					}
				}
			}
		}
		return v;
	}
}