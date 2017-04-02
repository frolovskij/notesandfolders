package com.notesandfolders.test;

import com.notesandfolders.Login;
import android.test.AndroidTestCase;

public class LoginTest extends AndroidTestCase {

	public void testEmptyPasswordSha1Hash() {
		assertTrue("da39a3ee5e6b4b0d3255bfef95601890afd80709"
				.equalsIgnoreCase(Login.getSha1Digest("")));
	}

}
