package com.notesandfolders;

import java.security.SecureRandom;

import net.sf.andhsli.hotspotlogin.SimpleCrypto;

public class KeyGenerator {
	public static String getRandomKey() {
		byte[] key = new byte[16];
		new SecureRandom().nextBytes(key);
		return SimpleCrypto.toHex(key);
	}
}
