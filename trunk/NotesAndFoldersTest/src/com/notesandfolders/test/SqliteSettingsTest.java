package com.notesandfolders.test;

import com.notesandfolders.SqliteSettings;
import com.notesandfolders.dataaccess.DbOpenHelper;

import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

public class SqliteSettingsTest extends AndroidTestCase {
	SqliteSettings settings;

	@Override
	protected void setUp() {
		DbOpenHelper dbOpenHelper = new DbOpenHelper(getContext());
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		dbOpenHelper.dropAllTables(db);
		dbOpenHelper.createAllTables(db);
		db.close();

		settings = new SqliteSettings(getContext());
	}

	@Override
	protected void tearDown() {
	}

	public void testGetNotExistingString() {
		assertEquals("default", settings.getString("this_key_does_not_exist", "default"));
	}

	public void testGetNotExistingInt() {
		assertEquals(123, settings.getInt("this_key_does_not_exist", 123));
	}

	public void testGetNotExistingDouble() {
		assertEquals(3.14, settings.getDouble("this_key_does_not_exist", 3.14));
	}

	public void testGetNotExistingBoolean() {
		assertEquals(true, settings.getBoolean("this_key_does_not_exist", true));
	}

	public void testGetExistingString() {
		settings.setString("new_key", "value");
		assertEquals("value", settings.getString("new_key", "default"));
	}

	public void testGetExistingInt() {
		settings.setInt("new_key", 1);
		assertEquals(1, settings.getInt("new_key", 2));
	}

	public void testGetExistingDouble() {
		settings.setDouble("new_key", 1.0);
		assertEquals(1.0, settings.getDouble("new_key", 2.0));
	}

	public void testGetExistingBoolean() {
		settings.setBoolean("new_key", false);
		assertEquals(false, settings.getBoolean("new_key", true));
	}

	public void testGetStringAsInt() {
		settings.setString("key", "value");
		assertEquals("value", settings.getString("key", "wrong_value"));
		// assertEquals(5, settings.getInt("key", 5)); // this behavior is undefined
	}

	public void testReSet() {
		settings.setString("key", "value");
		settings.setString("key", "new_value");
		assertEquals("new_value", settings.getString("key", "value"));
	}

	public void testReSetWithAnotherDataType() {
		settings.setString("key", "value");
		settings.setInt("key", 1);
		assertEquals(1, settings.getInt("key", 2));
	}

}
