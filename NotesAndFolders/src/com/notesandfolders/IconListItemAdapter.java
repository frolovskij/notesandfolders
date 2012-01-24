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

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class IconListItemAdapter extends ArrayAdapter<IconListItem> {

	private IconListItem[] items;
	private Context context;

	public IconListItemAdapter(Context context, int textViewResourceId,
			IconListItem[] items) {
		super(context, textViewResourceId, items);
		this.items = items;
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);
		TextView tv = (TextView) v.findViewById(android.R.id.text1);

		tv.setCompoundDrawablesWithIntrinsicBounds(items[position].icon, 0, 0,
				0);

		int dp5 = (int) (5 * context.getResources().getDisplayMetrics().density + 0.5f);
		tv.setCompoundDrawablePadding(dp5);

		return v;
	}
}