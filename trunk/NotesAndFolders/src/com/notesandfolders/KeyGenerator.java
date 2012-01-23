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

import java.security.SecureRandom;

import net.sf.andhsli.hotspotlogin.SimpleCrypto;

public class KeyGenerator {
	public static String getRandomKey() {
		byte[] key = new byte[128];
		new SecureRandom().nextBytes(key);
		return SimpleCrypto.toHex(key);
	}
}
