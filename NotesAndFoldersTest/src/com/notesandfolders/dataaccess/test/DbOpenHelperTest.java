package com.notesandfolders.dataaccess.test;

import net.sf.andhsli.hotspotlogin.SimpleCrypto;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.notesandfolders.Settings;
import com.notesandfolders.dataaccess.DbOpenHelper;

public class DbOpenHelperTest extends AndroidTestCase {
	Settings s;

	@Override
	protected void setUp() {
		DbOpenHelper dbOpenHelper = new DbOpenHelper(getContext());
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		dbOpenHelper.dropAllTables(db);
		dbOpenHelper.createAllTables(db);
		db.close();

		s = new Settings(getContext());
	}

	@Override
	protected void tearDown() {
	}

	public void testDefaultPassword() {
		String passwordHash = s.getString(Settings.SETTINGS_PASSWORD_SHA1_HASH,
				"");
		assertTrue("da39a3ee5e6b4b0d3255bfef95601890afd80709"
				.equalsIgnoreCase(passwordHash));
	}

	public void testDecryptKey() {
		String defaultPassword = "";
		String encryptedKey = s.getString(Settings.SETTINGS_ENCRYPTED_KEY, "");

		assertFalse(encryptedKey.equals(""));

		try {
			String key = SimpleCrypto.decrypt(defaultPassword, encryptedKey);
			Log.i("testDecryptKey", key);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
