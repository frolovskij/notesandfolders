package com.notesandfolders.test;

import java.io.File;
import java.io.InputStream;

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
import com.notesandfolders.NodeHelper;
import com.notesandfolders.Settings;

public class SpeedTest extends InstrumentationTestCase {
	@Override
	protected void setUp() {
		DbOpenHelper dbOpenHelper = new DbOpenHelper(this.getInstrumentation().getTargetContext());
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		dbOpenHelper.dropAllTables(db);
		dbOpenHelper.createAllTables(db);
		db.close();
	}

	@Override
	protected void tearDown() {
	}

	public void testUnzip() {
		try {
			// output
			File outputDir = getInstrumentation().getTargetContext().getFilesDir();

			long t = System.currentTimeMillis();

			// zip
			File f = new File(outputDir, "txt.zip");
			InputStream fis = this.getInstrumentation().getContext().getResources()
					.openRawResource(R.raw.txt);
			ZipHelper.inputStreamToFile(fis, f);
			ZipHelper.unzip(f, outputDir);

			f.delete();

			Log.i("SpeedTest", "Unzipping (ms): " + (System.currentTimeMillis() - t));
			t = System.currentTimeMillis();

			Debug.startMethodTracing("import");
			ImportHelper.doImport(outputDir, new ImportListener() {
				public void publishProgress(int processed, int nodesCount) {
				}

				public Context getContext() {
					return getInstrumentation().getTargetContext();
				}
			});
			Debug.stopMethodTracing();

			Log.i("SpeedTest", "Importing (ms): " + (System.currentTimeMillis() - t));
			t = System.currentTimeMillis();

		} catch (Exception e) {
			Log.d("control", "unzip error: " + e);
		}

	}
}
