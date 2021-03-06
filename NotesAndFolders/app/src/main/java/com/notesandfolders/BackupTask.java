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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import net.sf.andhsli.hotspotlogin.SimpleCrypto;

import android.os.AsyncTask;
import android.os.Environment;

public class BackupTask extends
    AsyncTask<Void, Integer, BackupTask.BackupResult> {

  public enum BackupResult {
    OK, CANT_CREATE_OUTPUT_DIRECTORY, CANT_CREATE_OUTPUT_FILE, FILE_ALREADY_EXISTS, CANT_WRITE_TO_OUTPUT_FILE, IO_ERROR
  };

  private BackupManagerActivity bm;
  private NodeHelper nh;
  private File outputDir;

  public BackupTask(BackupManagerActivity bm, NodeHelper nh, File outputDir) {
    this.bm = bm;
    this.nh = nh;
    this.outputDir = outputDir;
  }

  // PrintWriter, strings - 5056k
  // DataOutputStream(BufferedOutputStream(FileOutputStream))), string - 5057k
  // DataOutputStream(BufferedOutputStream(FileOutputStream))), byte[] - 3794
  // DataOutputStream(GZIPOutputStream(BufferedOutputStream(FileOutputStream)))),
  // byte[] -
  @Override
  protected BackupResult doInBackground(Void... arg0) {
    if (!outputDir.exists()) {
      if (outputDir.mkdirs() == false) {
        return BackupResult.CANT_CREATE_OUTPUT_DIRECTORY;
      }
    }

    File backupFile = new File(outputDir, String.format("%d.nf2",
        new Date().getTime()));
    if (backupFile.exists()) {
      return BackupResult.FILE_ALREADY_EXISTS;
    }

    try {
      if (backupFile.createNewFile() == false) {
        return BackupResult.CANT_CREATE_OUTPUT_FILE;
      }
    } catch (IOException ex) {
      ex.printStackTrace();
      return BackupResult.CANT_CREATE_OUTPUT_FILE;
    }

    if (backupFile.canWrite() == false) {
      return BackupResult.CANT_WRITE_TO_OUTPUT_FILE;
    }

    Settings s = new Settings(bm);
    final String passwordSha1 = s.getPasswordSha1Hash();

    DataOutputStream dos = null;
    try {
      dos = new DataOutputStream(new BufferedOutputStream(
          new FileOutputStream(backupFile)));

      dos.writeUTF(passwordSha1);

      String encryptedKey = s.getEncryptedKey();
      int keyEncryptVersion = s.getInt(Settings.SETTINGS_KEY_ENCRYPT_VERSION, 0);
      if (keyEncryptVersion == 0) {
        String password = new TempStorage(bm).getPassword();
        String oldKey = SimpleCrypto.decrypt(password, encryptedKey, keyEncryptVersion);
        encryptedKey = SimpleCrypto.encrypt(password, oldKey);
      }
      dos.writeUTF(encryptedKey);

      int count = 0;
      for (long id : nh.getAllIds()) {
        byte[] nodeData = nh.getNodeAsByteArray(id);
        if (nodeData != null) {
          dos.writeInt(nodeData.length);
          dos.write(nodeData);
        }

        publishProgress(count++ + 1);
      }
    } catch (IOException e1) {
      e1.printStackTrace();
      return BackupResult.IO_ERROR;
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      if (dos != null) {
        try {
          dos.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    return BackupResult.OK;
  }

  protected void onProgressUpdate(Integer... progress) {
    if (progress.length > 0) {
      bm.getBackupDialog().setProgress(progress[0]);
    }
  }

  @Override
  protected void onPreExecute() {
    bm.showDialog(BackupManagerActivity.DIALOG_BACKUP);
  }

  @Override
  protected void onPostExecute(BackupResult result) {
    bm.dismissDialog(BackupManagerActivity.DIALOG_BACKUP);

    if (bm != null) {
      bm.onBackupTaskCompleted(result);
    }
  }

  public void setActivity(BackupManagerActivity bm) {
    this.bm = bm;
  }
}