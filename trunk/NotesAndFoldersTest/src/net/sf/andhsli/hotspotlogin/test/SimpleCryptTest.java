package net.sf.andhsli.hotspotlogin.test;

import net.sf.andhsli.hotspotlogin.SimpleCrypto;
import com.notesandfolders.KeyGenerator;
import android.test.AndroidTestCase;

public class SimpleCryptTest extends AndroidTestCase {
	@Override
	protected void setUp() {
	}

	@Override
	protected void tearDown() {
	}

	public void testToHex() {
		String hex = SimpleCrypto.toHex(new byte[] { 0, 15, 127, -128, -1 });
		assertEquals("000F7F80FF", hex);
	}

	public void testFromHex() {
		String s = SimpleCrypto.fromHex("6A617661");
		assertEquals("java", s);
	}

	public void testEncryptDecrypt() {
		String key = KeyGenerator.getRandomKey();

		String plainText = "Plain text";
		String encrypted = "";
		try {
			encrypted = SimpleCrypto.encrypt(key, plainText);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String decrypted = "";
		try {
			decrypted = SimpleCrypto.decrypt(key, encrypted);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertEquals(plainText, decrypted);
	}

}