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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends BaseActivity implements OnClickListener {
	private Login login;
	private Settings settings;
	private Button okButton;
	private EditText password;

	@Override
	public void onResume() {
		super.onResume();

		// if going back from explorer && no password is set
		if (login.isEmptyPassword()) {
			this.finish();
		} else {
			password.setText("");
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		settings = new Settings(this);
		login = new Login(settings);

		// Start automatically if password is empty
		if (login.isEmptyPassword()) {
			Intent explorer = new Intent(this, ExplorerActivity.class);
			explorer.putExtra("password", Settings.EMPTY_PASSWORD);
			startActivity(explorer);
		}

		setContentView(R.layout.login);
		okButton = (Button) findViewById(R.id.btnLogin);
		password = (EditText) findViewById(R.id.login_password);

		okButton.setOnClickListener(this);
	}

	public void onClick(View v) {
		if (v == okButton) {
			if (login.isPasswordValid(password.getText().toString())) {

				Intent explorer = new Intent(this, ExplorerActivity.class);
				explorer.putExtra("password", password.getText().toString());
				startActivity(explorer);

			} else {
				showAlert(R.string.login_msg_password_incorrect);
			}
		}
	}
}