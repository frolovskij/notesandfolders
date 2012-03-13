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

import com.notesandfolders.R;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class NotesViewerActivity extends BaseActivity {
	private NodeHelper nh;
	private WebView textContent;
	private TextView name;
	private ImageButton editButton;
	private TextView placeholder;

	private String textToHighligth;

	private long id;

	final private OnClickListener editButtonOnClickListener = new OnClickListener() {
		public void onClick(View v) {
			if (v != null && v == editButton) {
				Intent editor = new Intent(NotesViewerActivity.this, NotesEditorActivity.class);
				editor.putExtra("note_id", id);
				startActivity(editor);
			}
		}
	};

	@Override
	public void onResume() {
		super.onRestart();

		final long t = System.currentTimeMillis();

		String tc = nh.getTextContentById(id);

		StringBuilder sb = new StringBuilder(2 * tc.length());
		sb.append("<html><head><style>p {text-indent: 1em; text-align: justify;} span.highlight {background-color: yellow;} </style></head><body>");
		for (String s : tc.split("\n")) {
			sb.append("<p>");

			String untagged = Html.fromHtml(s).toString();
			if (textToHighligth != null) {
				sb.append(untagged.replaceAll(textToHighligth, "<span class=\"highlight\">"
						+ textToHighligth + "</span>"));
			} else {
				sb.append(untagged);
			}

			sb.append("</p>");
		}
		sb.append("</body></html>");

		textContent.loadData(sb.toString(), "text/html", "utf-8");

		System.out.println(sb.toString());

		if (tc.equals("")) {
			placeholder.setVisibility(View.VISIBLE);
		} else {
			placeholder.setVisibility(View.GONE);
		}

		System.out.println(System.currentTimeMillis() - t);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notesviewer);

		nh = new NodeHelper(this, new TempStorage(this).getPassword());
		id = getIntent().getExtras().getLong("note_id");
		textToHighligth = getIntent().getExtras().getString("highlight_text");

		textContent = (WebView) findViewById(R.id.notesviewer_note_text_view);

		name = (TextView) findViewById(R.id.notesviewer_name);
		Node n = nh.getNodeById(id);
		name.setText(n.getName());

		editButton = (ImageButton) findViewById(R.id.notesviewer_edit_button);
		editButton.setOnClickListener(editButtonOnClickListener);

		placeholder = (TextView) findViewById(R.id.notesviewer_placeholder);

	}
}