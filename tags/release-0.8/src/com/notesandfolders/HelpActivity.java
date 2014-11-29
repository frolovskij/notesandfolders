package com.notesandfolders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.notesandfolders.R;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class HelpActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.documentation);

		WebView wv = (WebView) findViewById(R.id.documentation);
		wv.loadDataWithBaseURL(null, readRawTextFile(R.raw.documentation), "text/html", "utf-8",
				null);
	}

	private String readRawTextFile(int resourceId) {
		InputStream inputStream = getResources().openRawResource(resourceId);

		InputStreamReader inputreader = new InputStreamReader(inputStream);
		BufferedReader buffreader = new BufferedReader(inputreader);
		String line;
		StringBuilder text = new StringBuilder();

		try {
			while ((line = buffreader.readLine()) != null) {
				text.append(line);
				text.append('\r');
			}
		} catch (IOException e) {
			return null;
		}
		return text.toString();
	}
}
