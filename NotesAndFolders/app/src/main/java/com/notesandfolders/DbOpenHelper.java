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

import java.util.Date;

import net.sf.andhsli.hotspotlogin.SimpleCrypto;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbOpenHelper extends SQLiteOpenHelper {
  private static final int DB_VERSION = 2;
  private static final String DB_NAME = "notesandfolders.db";

  private static final String SQL_PRAGMA = "PRAGMA foreign_keys=ON;";

  private static final String SQL_DROP_TABLE_SETTINGS = "DROP TABLE IF EXISTS 'settings';";
  private static final String SQL_CREATE_TABLE_SETTINGS = "CREATE TABLE 'settings' (\r\n"
      + "	'id' INTEGER PRIMARY KEY,\r\n" + "	'name' TEXT UNIQUE NOT NULL,\r\n"
      + "	'value' NONE NOT NULL\r\n" + ");\r\n" + "";

  private static final String SQL_DROP_TABLE_DATA = "DROP TABLE IF EXISTS 'data';";
  private static final String SQL_CREATE_TABLE_DATA = "CREATE TABLE 'data' (\r\n" +
      "	'id' INTEGER PRIMARY KEY,\r\n" +
      "	parent_id INTEGER REFERENCES 'data'('id') ON DELETE CASCADE,\r\n" +
      "	'name' TEXT NOT NULL,\r\n" +
      "	text_content TEXT,\r\n" +
      "   encrypt_version INTEGER NOT NULL DEFAULT 0," +
      "	date_created INTEGER NOT NULL,\r\n" +
      "	date_modified INTEGER NOT NULL,\r\n" +
      "	type INTEGER NOT NULL\r\n" +
      ");";

  private static final String SQL_ALTER_TABLE_DATA_ENCRYPT_VERSION = "ALTER TABLE 'data' ADD COLUMN encrypt_version INTEGER NOT NULL DEFAULT 0;";

  Context context;

  public DbOpenHelper(Context context) {
    super(context, DB_NAME, null, DB_VERSION);
    this.context = context;
  }

  public void createAllTables(SQLiteDatabase db) {
    db.execSQL(SQL_PRAGMA);
    db.execSQL(SQL_CREATE_TABLE_SETTINGS);
    db.execSQL(SQL_CREATE_TABLE_DATA);
    doInitialFill(db);
  }

  public void doInitialFill(SQLiteDatabase db) {
    long now = new Date().getTime();

    // Root folder
    db.execSQL(
        "INSERT INTO 'data' ('id', 'parent_id', 'name', 'date_created', 'date_modified', 'type') VALUES (?, ?, ?, ?, ?, ?)",
        new String[] { "0", "-1", "", Long.toString(now), Long.toString(now), "0" });

    // default password (empty, "")
    db.execSQL("INSERT INTO 'settings' ('name', 'value') VALUES (?, ?)", new String[] {
        Settings.SETTINGS_PASSWORD_SHA1_HASH, Settings.EMPTY_PASSWORD_SHA1_HASH });

    // encryption key, encrypted by password
    try {
      String password = Settings.EMPTY_PASSWORD;
      String key = KeyGenerator.getRandomKey();
      String encryptedKeyHex = SimpleCrypto.encrypt(password, key);

      db.execSQL("INSERT INTO 'settings' ('name', 'value') VALUES (?, ?)", new String[] {
          Settings.SETTINGS_ENCRYPTED_KEY, encryptedKeyHex });

      db.execSQL("INSERT INTO 'settings' ('name', 'value') VALUES (?, ?)",
          new String[] { Settings.SETTINGS_KEY_ENCRYPT_VERSION, "1" });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void dropAllTables(SQLiteDatabase db) {
    db.execSQL(SQL_PRAGMA);
    db.execSQL(SQL_DROP_TABLE_SETTINGS);
    db.execSQL(SQL_DROP_TABLE_DATA);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    if (!db.isReadOnly()) {
      try {
        createAllTables(db);
      } catch (SQLiteException ex) {
        Log.e("SQLite", "Table creation failed. Dropping all tables.");
        dropAllTables(db);
        throw ex;
      }
    } else {
      Log.e("SQLite", "Database is read only.");
      throw new SQLiteException("Database is read only.");
    }

  }

  @Override
  public void onOpen(SQLiteDatabase db) {
    if (!db.isReadOnly()) {
      db.execSQL(SQL_PRAGMA);
    }
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    switch (oldVersion) {
    case 1:
      db.execSQL(SQL_ALTER_TABLE_DATA_ENCRYPT_VERSION);

      db.execSQL("INSERT INTO 'settings' ('name', 'value') VALUES (?, ?)",
          new String[] { Settings.SETTINGS_KEY_ENCRYPT_VERSION, "0" });
    }
  }

}
