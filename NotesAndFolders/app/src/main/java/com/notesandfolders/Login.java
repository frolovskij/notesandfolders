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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.sf.andhsli.hotspotlogin.SimpleCrypto;

public class Login {
  Settings settings;

  public Login(Settings settings) {
    this.settings = settings;
  }

  public static String getSha1Digest(String text) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      return SimpleCrypto.toHex(md.digest(text.getBytes("UTF-8")));
    } catch (NoSuchAlgorithmException e) {
      // Won't happen as we use SHA-1
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      // Won't happen as we use UTF-8
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return null;
  }

  // todo It would be better to have a random salt which is generated every time a new password is set
  public static String PASSWORD_SALT = "YW5hbCBzZXg=";

  public static boolean isPasswordValid(Settings settings, String password) {
    String passwordSha1Hash = settings.getPasswordSha1Hash();
    return getSha1Digest(password).equals(passwordSha1Hash) ||
        getSha1Digest(password + PASSWORD_SALT).equals(passwordSha1Hash);
  }

  public boolean isEmptyPassword() {
    String passwordSha1Hash = settings.getPasswordSha1Hash();
    return passwordSha1Hash.equals(Settings.EMPTY_PASSWORD_SHA1_HASH);
  }

}
