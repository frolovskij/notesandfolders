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
import android.os.Bundle;
import android.widget.ListView;

public class ExplorerActivity extends BaseActivity {
	NodeHelper fh;
	ListView lv;
	List<Node> items;
	NodeAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		fh = new NodeHelper(this);

		setContentView(R.layout.explorer);

		lv = (ListView) findViewById(R.id.explorer_listview);
		items = fh.getChildrenById(1);
		adapter = new NodeAdapter(this, R.layout.explorer_item, items);
		lv.setAdapter(adapter);
	}
}
