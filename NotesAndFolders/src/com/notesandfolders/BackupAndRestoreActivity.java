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

import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class BackupAndRestoreActivity extends ActivityGroup {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.backupandrestore);

		TabHost tabHost = (TabHost) findViewById(R.id.backupandrestore_tabhost);
		tabHost.setup(getLocalActivityManager());

		TabSpec backup = tabHost.newTabSpec("backup");
		backup.setIndicator(getText(R.string.backup_title),
				getResources().getDrawable(R.drawable.ic_tab_backup));
		Intent backupIntent = new Intent(this, BackupActivity.class);
		backup.setContent(backupIntent);

		TabSpec restore = tabHost.newTabSpec("restore");
		restore.setIndicator(getText(R.string.restore_title),
				getResources().getDrawable(R.drawable.ic_tab_restore));
		Intent restoreIntent = new Intent(this, RestoreActivity.class);
		restore.setContent(restoreIntent);

		tabHost.addTab(backup);
		tabHost.addTab(restore);

		tabHost.setCurrentTab(0);
	}
}