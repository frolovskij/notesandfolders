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

import net.sf.andhsli.hotspotlogin.SimpleCrypto;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Settings {
  public static final String SETTINGS_PASSWORD_SHA1_HASH = "password_sha1_hash";
  public static final String SETTINGS_ENCRYPTED_KEY = "encrypted_key";
  /**
   * 0 means that key was encrypted using the raw key made with SecureRandom
   * (which is bad).
   * 1 means that key was encrypted with the key made with PBKDF
   */
  public static final String SETTINGS_KEY_ENCRYPT_VERSION = "key_encrypt_version";
  public static final String EMPTY_PASSWORD_SHA1_HASH = "DA39A3EE5E6B4B0D3255BFEF95601890AFD80709";
  public static final String EMPTY_PASSWORD = "";

  Context context;

  public Settings(Context context) {
    this.context = context;
  }

  public String getPasswordSha1Hash() {
    return getString(Settings.SETTINGS_PASSWORD_SHA1_HASH,
        EMPTY_PASSWORD_SHA1_HASH);
  }
  

  public String getEncryptedKey() {
    return getString(Settings.SETTINGS_ENCRYPTED_KEY, "");
  }

  /**
   * Sets the new password and re-encrypts the key with it
   * 
   * @param newPassword
   * @param oldPassword
   */
  public boolean setPassword(String newPassword, String oldPassword) {
    if (newPassword == null) {
      newPassword = "";
    }

    boolean result = true;

    try {
      String currentPassword = oldPassword;
      String encryptedOldKeyHex = getEncryptedKey();

      int keyEncryptVersion = getInt(Settings.SETTINGS_KEY_ENCRYPT_VERSION, 0);

      String oldKey = SimpleCrypto.decrypt(currentPassword, encryptedOldKeyHex, keyEncryptVersion);
      String encryptedNewKeyHex = SimpleCrypto.encrypt(newPassword, oldKey);

      setString(Settings.SETTINGS_PASSWORD_SHA1_HASH, Login.getSha1Digest(newPassword + Login.PASSWORD_SALT));
      setString(Settings.SETTINGS_ENCRYPTED_KEY, encryptedNewKeyHex);
      setInt(Settings.SETTINGS_KEY_ENCRYPT_VERSION, 1);

    } catch (Exception e) {
      // TODO: if something goes wrong here - need to get back the old
      // password
      // and key

      result = false;

      e.printStackTrace();
    }

    return result;
  }

  private Object getData(String name, Object defaultValue) {
    DbOpenHelper dbOpenHelper = new DbOpenHelper(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

    Object result = defaultValue;

    Cursor c = null;
    try {
      c = db.rawQuery("select value from settings where name = ?",
          new String[] { name });
      if (c.getCount() != 0) {
        c.moveToFirst();

        if (defaultValue instanceof String) {
          result = c.getString(0);
        } else if (defaultValue instanceof Integer) {
          result = c.getInt(0);
        } else if (defaultValue instanceof Double) {
          result = c.getDouble(0);
        }
      }
    } catch (Exception ex) {
      // Can't be sure if exception would happen if we're reading, for
      // example, string data as getInt(0), because it's implementation
      // specific

      // if exception is thrown because of data type is being other then
      // expected then result is defaultValue
    } finally {
      if (c != null) {
        c.close();
      }
      if (db != null) {
        db.close();
      }
    }

    return result;
  }

  private void setData(String name, Object value) {
    DbOpenHelper dbOpenHelper = new DbOpenHelper(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

    try {
      db.execSQL(
          "INSERT OR REPLACE INTO 'settings' ('name', 'value') VALUES (?, ?)",
          new String[] { name, value.toString() });
    } finally {
      db.close();
    }
  }

  public String getString(String name, String defaultValue) {
    return (String) getData(name, defaultValue);
  }

  public void setString(String name, String value) {
    setData(name, value);
  }

  public int getInt(String name, int defaultValue) {
    return (Integer) getData(name, defaultValue);
  }

  public void setInt(String name, int value) {
    setData(name, value);
  }

  public double getDouble(String name, double defaultValue) {
    return (Double) getData(name, defaultValue);
  }

  public void setDouble(String name, double value) {
    setData(name, value);
  }

  public boolean getBoolean(String name, boolean defaultValue) {
    return getInt(name, defaultValue ? 1 : 0) == 1 ? true : false;
  }

  public void setBoolean(String name, boolean value) {
    setData(name, value);
  }

}
