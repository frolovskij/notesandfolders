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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.notesandfolders.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;

public class NotesViewerActivity extends Activity {
	private NodeHelper nh;
	private WebView textContent;
	private TextView name;
	private ImageButton editButton;
	private TextView placeholder;

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

	public String textContentToHtml(String tc, String textToHighligth) {
		SearchParameters sp = new TempStorage(this).getSearchParameters();

		StringBuilder sb = new StringBuilder(2 * tc.length());
		sb.append("<html>\n" + "<head>\n"
				+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n"
				+ "<style>\n" + "p {text-indent: 1em; text-align: justify;}\n"
				+ "span.highlight {background-color: #7FFF00;}\n" + "</style>\n" + "</head>\n"
				+ "<body>");

		for (String s : TextUtils.htmlEncode(tc).split("\n")) {
			sb.append("<p>");

			String untagged = s;
			if (textToHighligth != null) {
				if (sp.isCaseSensitive()) {
					sb.append(untagged.replaceAll(textToHighligth, "<span class=\"highlight\">"
							+ textToHighligth + "</span>"));
				} else {
					sb.append(untagged.replaceAll("(?i)" + textToHighligth,
							"<span class=\"highlight\">$0</span>"));
				}
			} else {
				sb.append(untagged);
			}

			sb.append("</p>");
		}
		sb.append("</body></html>");
		try {
			return URLEncoder.encode(sb.toString(), "utf-8").replaceAll("\\+", " ");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return "";
	}

	@Override
	public void onResume() {
		super.onRestart();

		String tc = nh.getTextContentById(id);

		SearchParameters sp = new TempStorage(this).getSearchParameters();
		String textToHighligth = (sp == null) ? null : (sp.isSearchInText()) ? sp.getText() : null;

		textContent.loadData(textContentToHtml(tc, textToHighligth), "text/html", "utf-8");
		textContent.reload();

		if (tc.equals("")) {
			placeholder.setVisibility(View.VISIBLE);
		} else {
			placeholder.setVisibility(View.GONE);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notesviewer);

		nh = new NodeHelper(this, new TempStorage(this).getPassword());
		id = getIntent().getExtras().getLong("note_id");

		textContent = (WebView) findViewById(R.id.notesviewer_note_text_view);

		name = (TextView) findViewById(R.id.notesviewer_name);
		Node n = nh.getNodeById(id);
		name.setText(n.getName());

		editButton = (ImageButton) findViewById(R.id.notesviewer_edit_button);
		editButton.setOnClickListener(editButtonOnClickListener);

		placeholder = (TextView) findViewById(R.id.notesviewer_placeholder);

	}
}