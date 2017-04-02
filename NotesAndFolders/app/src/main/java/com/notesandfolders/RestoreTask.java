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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

public class RestoreTask extends
    AsyncTask<Void, Integer, RestoreTask.RestoreResult> {

  public enum RestoreResult {
    OK, CANT_READ_FILE, FILE_STURCTURE_IS_WRONG, IO_ERROR
  };

  public static final String OUTPUT_DIR = "NotesAndFolders";

  private BackupManagerActivity bm;
  private NodeHelper nh;
  private File backupFile;
  private int encryptVersion;

  /**
   * 
   * @param bm
   *          parent activity that holds a progress dialog
   * @param nh
   * @param file
   *          backup file that contains data to restore from
   */
  public RestoreTask(BackupManagerActivity bm, NodeHelper nh, File file) {
    this.bm = bm;
    this.nh = nh;
    this.backupFile = file;

    if (file.getName().toLowerCase().endsWith("nf1")) {
      this.encryptVersion = 0;
    } else {
      // nf2
      this.encryptVersion = 1;
    }
  }

  @Override
  protected RestoreResult doInBackground(Void... arg0) {
    SQLiteDatabase db = null;
    DataInputStream dis = null;
    try {
      // Clean database
      DbOpenHelper dbOpenHelper = new DbOpenHelper(bm);
      db = dbOpenHelper.getWritableDatabase();
      db.execSQL("delete from data");
      db.execSQL("delete from settings");
      db.execSQL("PRAGMA foreign_keys=OFF;");

      dis = new DataInputStream(new BufferedInputStream(
          new FileInputStream(backupFile)));

      // insert hash of the password
      final String password = dis.readUTF();
      db.execSQL(
          "INSERT INTO 'settings' ('name', 'value') VALUES (?, ?)",
          new String[] { Settings.SETTINGS_PASSWORD_SHA1_HASH,
              password });

      // insert encrypted encryption key
      final String key = dis.readUTF();
      db.execSQL(
          "INSERT INTO 'settings' ('name', 'value') VALUES (?, ?)",
          new String[] { Settings.SETTINGS_ENCRYPTED_KEY, key });

      db.execSQL(
          "INSERT INTO 'settings' ('name', 'value') VALUES (?, ?)",
          new String[] { Settings.SETTINGS_KEY_ENCRYPT_VERSION, Integer.toString(encryptVersion) });

      // insert nodes
      while (dis.available() != 0) {
        int dataLen = dis.readInt();
        if (dataLen > 0) {
          byte[] data = new byte[dataLen];
          dis.read(data);
          Node node = nh.getNodeFromByteArray(data, this.encryptVersion);

          ContentValues cv = new ContentValues();
          cv.put("id", node.getId());
          cv.put("parent_id", node.getParentId());
          cv.put("name", node.getName());
          cv.put("date_created", node.getDateCreated().getTime());
          cv.put("date_modified", node.getDateModified().getTime());
          cv.put("type", node.getType().ordinal());
          cv.put("text_content", node.getTextContent());
          cv.put("encrypt_version", node.getEncryptVersion());

          try {
            db.insert("data", null, cv);
          } catch (Exception ex) {
          }
        }
      }

      db.execSQL("PRAGMA foreign_keys=ON;");
    } catch (IOException e1) {
      e1.printStackTrace();
      return RestoreResult.IO_ERROR;
    } finally {
      if (db != null) {
        db.close();
      }
      if (dis != null) {
        try {
          dis.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    return RestoreResult.OK;
  }

  @Override
  protected void onPreExecute() {
    bm.showDialog(BackupManagerActivity.DIALOG_RESTORE);
  }

  @Override
  protected void onPostExecute(RestoreResult result) {
    bm.dismissDialog(BackupManagerActivity.DIALOG_RESTORE);

    if (bm != null) {
      bm.onRestoreTaskCompleted(result);
    }
  }

  public void setActivity(BackupManagerActivity bm) {
    this.bm = bm;
  }
}