package com.notesandfolders.activities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.notesandfolders.R;

import android.os.Bundle;
import android.webkit.WebView;

public class HelpActivity extends BaseActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.documentation);

		WebView wv = (WebView) findViewById(R.id.documentation);
		try {
			wv.loadData(URLEncoder.encode(readRawTextFile(R.raw.documentation), "utf-8")
					.replaceAll("\\+", "%20"), "text/html", "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
