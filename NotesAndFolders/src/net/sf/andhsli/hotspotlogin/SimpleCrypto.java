package net.sf.andhsli.hotspotlogin;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Usage:
 * 
 * <pre>
 * String crypto = SimpleCrypto.encrypt(masterpassword, cleartext)
 * ...
 * String cleartext = SimpleCrypto.decrypt(masterpassword, crypto)
 * </pre>
 * 
 * @author ferenc.hechler
 * 
 *         Modified by Frolovskij Aleksej
 */
public class SimpleCrypto {

	public static String encrypt(String seed, String cleartext) throws Exception {
		byte[] rawKey = getRawKey(seed.getBytes());
		byte[] result = encrypt(rawKey, cleartext.getBytes());
		return toHex(result);
	}

	public static String decrypt(String seed, String encrypted) throws Exception {
		byte[] rawKey = getRawKey(seed.getBytes());
		byte[] enc = toByte(encrypted);
		byte[] result = decrypt(rawKey, enc);
		return new String(result);
	}

	private static byte[] getRawKey(byte[] seed) throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		sr.setSeed(seed);
		kgen.init(128, sr); // 192 and 256 bits may not be available
		SecretKey skey = kgen.generateKey();
		byte[] raw = skey.getEncoded();
		return raw;
	}

	private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		byte[] encrypted = cipher.doFinal(clear);
		return encrypted;
	}

	private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		byte[] decrypted = cipher.doFinal(encrypted);
		return decrypted;
	}

	public static String toHex(String txt) {
		return toHex(txt.getBytes());
	}

	public static String fromHex(String hex) {
		return new String(toByte(hex));
	}

	// True govnokod, but it's 12 times faster than original code
	public static byte[] toByte(String hexString) {
		int len = hexString.length() / 2;
		byte[] result = new byte[len];
		char[] s = hexString.toCharArray();
		for (int i = 0; i < len; i++) {
			byte h = 0;
			switch (s[i * 2]) {
			case '0':
				h = 0;
				break;
			case '1':
				h = 1;
				break;
			case '2':
				h = 2;
				break;
			case '3':
				h = 3;
				break;
			case '4':
				h = 4;
				break;
			case '5':
				h = 5;
				break;
			case '6':
				h = 6;
				break;
			case '7':
				h = 7;
				break;
			case '8':
				h = 8;
				break;
			case '9':
				h = 9;
				break;
			case 'A':
				h = 10;
				break;
			case 'B':
				h = 11;
				break;
			case 'C':
				h = 12;
				break;
			case 'D':
				h = 13;
				break;
			case 'E':
				h = 14;
				break;
			case 'F':
				h = 15;
				break;
			}

			byte l = 0;
			switch (s[i * 2 + 1]) {
			case '0':
				l = 0;
				break;
			case '1':
				l = 1;
				break;
			case '2':
				l = 2;
				break;
			case '3':
				l = 3;
				break;
			case '4':
				l = 4;
				break;
			case '5':
				l = 5;
				break;
			case '6':
				l = 6;
				break;
			case '7':
				l = 7;
				break;
			case '8':
				l = 8;
				break;
			case '9':
				l = 9;
				break;
			case 'A':
				l = 10;
				break;
			case 'B':
				l = 11;
				break;
			case 'C':
				l = 12;
				break;
			case 'D':
				l = 13;
				break;
			case 'E':
				l = 14;
				break;
			case 'F':
				l = 15;
				break;
			}

			result[i] = (byte) (h * 16 + l);
		}
		return result;
	}

	private final static char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B',
			'C', 'D', 'E', 'F' };

	public static String toHex(byte[] buf) {
		if (buf == null)
			return "";
		char[] result = new char[2 * buf.length];
		for (int i = 0; i < buf.length; i++) {
			byte b = buf[i];
			result[i * 2] = HEX[(b >> 4) & 0x0f];
			result[i * 2 + 1] = HEX[b & 0x0f];
		}
		return new String(result);
	}
}