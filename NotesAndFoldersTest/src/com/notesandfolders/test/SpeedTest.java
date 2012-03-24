package com.notesandfolders.test;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import net.sf.andhsli.hotspotlogin.SimpleCrypto;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Debug;
import android.test.AndroidTestCase;
import android.test.InstrumentationTestCase;
import android.util.Log;

import com.notesandfolders.DbOpenHelper;
import com.notesandfolders.ImportHelper;
import com.notesandfolders.ImportHelper.ImportListener;
import com.notesandfolders.Node;
import com.notesandfolders.NodeHelper;
import com.notesandfolders.SearchParameters;
import com.notesandfolders.SearchTask;
import com.notesandfolders.Settings;
import com.notesandfolders.TempStorage;

public class SpeedTest extends InstrumentationTestCase {
	@Override
	protected void setUp() {
		DbOpenHelper dbOpenHelper = new DbOpenHelper(this.getInstrumentation().getTargetContext());
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		dbOpenHelper.dropAllTables(db);
		dbOpenHelper.createAllTables(db);
		db.close();

		prepareFilesToImport();
	}

	private void prepareFilesToImport() {
		File outputDir = getInstrumentation().getTargetContext().getFilesDir();
		File f = new File(outputDir, "txt.zip");
		InputStream fis = this.getInstrumentation().getContext().getResources()
				.openRawResource(R.raw.txt);
		ZipHelper.inputStreamToFile(fis, f);
		ZipHelper.unzip(f, outputDir);

		f.delete();
	}

	@Override
	protected void tearDown() {
	}

	public void notestPerformance() {
		long t = System.currentTimeMillis();
		NodeHelper nh = new NodeHelper(getInstrumentation().getTargetContext(), new TempStorage(
				getInstrumentation().getTargetContext()).getPassword());

		if (true) {

			// Debug.startMethodTracing("import");
			ImportHelper.doImport(getInstrumentation().getTargetContext().getFilesDir(),
					new ImportListener() {
						public void publishProgress(int processed, int nodesCount) {
						}

						public Context getContext() {
							return getInstrumentation().getTargetContext();
						}
					});
			// Debug.stopMethodTracing();

			// CPU#1 - Core 2 Duo E4600 @ 2.40GHz
			// CPU#2 - Pentium Dual Core CPU E2180 @ 2.00GHz

			// Importing:
			// r175: 34.4s @1
			// r173: 53s @2, 47s @ 1
			// r172: 61s @2
			// r169: 68s @2

			Log.i("SpeedTest", "Importing (ms): " + (System.currentTimeMillis() - t));
		}

		if (false) {
			t = System.currentTimeMillis();
			// Debug.startMethodTracing("listing");
			// /Imported at ...../files/
			List<Node> items = nh.getChildrenById(2);
			// Debug.stopMethodTracing();

			// Listing:
			// r175: 0.35s
			// r173: 12s
			Log.i("SpeedTest", "Listing (ms): " + (System.currentTimeMillis() - t) + ", count:"
					+ items.size());
		}

		if (false) {
			t = System.currentTimeMillis();
			// Debug.startMethodTracing("copying");
			nh.copy(1, 0);
			// Debug.stopMethodTracing();

			// Copying:
			// r175: 46.1s @1
			// r173: 72s @1
			Log.i("SpeedTest", "Copying (ms): " + (System.currentTimeMillis() - t));
		}

		t = System.currentTimeMillis();
		// Debug.startMethodTracing("searching");
		SearchParameters sp = new SearchParameters();
		sp.setText(" я ");
		sp.setCaseSensitive(false);
		sp.setSearchInNames(true);
		sp.setSearchInText(true);
		sp.setFolderId(0);

		SearchTask st = new SearchTask(null, nh, sp);
		List<Long> results = st.getSearchResults();

		// Debug.stopMethodTracing();

		// r176: 31s @1
		// r175: 60s @ 1
		Log.i("SpeedTest", "Searching (ms): " + (System.currentTimeMillis() - t) + ", found: "
				+ results.size());

	}
}