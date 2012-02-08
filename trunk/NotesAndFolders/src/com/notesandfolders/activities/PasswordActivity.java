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

package com.notesandfolders.activities;

import com.notesandfolders.Login;
import com.notesandfolders.R;
import com.notesandfolders.Settings;
import com.notesandfolders.R.id;
import com.notesandfolders.R.layout;
import com.notesandfolders.R.string;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PasswordActivity extends BaseActivity implements OnClickListener {
	private Button saveButton;
	private EditText currentPasswordEdit;
	private EditText newPasswordEdit;
	private EditText newPasswordConfirmationEdit;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.password_edit);

		currentPasswordEdit = (EditText) findViewById(R.id.password_edit_current);
		newPasswordEdit = (EditText) findViewById(R.id.password_edit_new);
		newPasswordConfirmationEdit = (EditText) findViewById(R.id.password_edit_confirmation);

		saveButton = (Button) findViewById(R.id.password_edit_save);
		saveButton.setOnClickListener(this);
	}

	public void onClick(View v) {
		if (v == saveButton) {
			Settings s = new Settings(this);

			String realCurrentPasswordHash = s.getPasswordSha1Hash();
			String currentPasswordHash = Login.getSha1Digest(currentPasswordEdit.getText()
					.toString());
			String newPassword = newPasswordEdit.getText().toString();
			String newPasswordConfirmation = newPasswordConfirmationEdit.getText().toString();

			if (!realCurrentPasswordHash.equals(currentPasswordHash)) {
				showAlert(R.string.password_msg_wrong_password);
			} else {
				if (!newPassword.equals(newPasswordConfirmation)) {
					showAlert(R.string.password_msg_password_confirmation_do_not_match);
				} else {
					boolean result = s.setPassword(newPassword, currentPasswordEdit.getText()
							.toString());

					if (false == result) {
						Toast toast = Toast.makeText(getApplicationContext(),
								R.string.password_msg_save_error, Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					} else {
						Toast toast = Toast.makeText(getApplicationContext(),
								R.string.password_msg_password_was_saved, Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
						finish();
					}
				}
			}

		}
	}
}
