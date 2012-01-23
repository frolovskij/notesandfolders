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

public interface Settings {
	final String SETTINGS_PASSWORD_SHA1_HASH = "password_sha1_hash";
	final String SETTINGS_ENCRYPTED_KEY = "encrypted_key";

	String getString(String name, String defaultValue);

	void setString(String name, String value);

	int getInt(String name, int defaultValue);

	void setInt(String name, int value);

	double getDouble(String name, double defaultValue);

	void setDouble(String name, double value);

	boolean getBoolean(String name, boolean defaultValue);

	void setBoolean(String name, boolean value);
}
