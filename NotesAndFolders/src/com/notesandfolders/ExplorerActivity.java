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

import java.util.List;
import com.notesandfolders.dataaccess.NodeHelper;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ExplorerActivity extends BaseActivity {
	NodeHelper fh;
	ListView lv;
	List<Node> items;
	NodeAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		fh = new NodeHelper(this, getIntent().getExtras().getString("password"));

		setContentView(R.layout.explorer);

		lv = (ListView) findViewById(R.id.explorer_listview);
		items = fh.getChildrenById(0);
		adapter = new NodeAdapter(this, R.layout.explorer_item, items);
		lv.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.explorer_options, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.explorer_options_add:
			onNew();
			return true;

		case R.id.explorer_options_settings:
			Intent settings = new Intent(this, SettingsActivity.class);
			startActivity(settings);
			return true;

		case R.id.explorer_options_close:
			finish();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void onNew() {
		final IconListItem[] items = {
				new IconListItem(getText(R.string.create_folder).toString(),
						R.drawable.folder),
				new IconListItem(getText(R.string.create_note).toString(),
						R.drawable.note),
				new IconListItem(getText(R.string.create_checklist).toString(),
						R.drawable.note) };

		ListAdapter adapter = new IconListItemAdapter(this,
				android.R.layout.select_dialog_item, items);

		new AlertDialog.Builder(this).setTitle(getText(R.string.create_new))
				.setAdapter(adapter, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						Toast.makeText(getApplicationContext(),
								getText(R.string.msg_not_implemented_yet),
								Toast.LENGTH_SHORT).show();
					}
				}).show();
	}

}
