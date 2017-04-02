package com.notesandfolders.test;

import java.util.zip.*;
import java.io.*;
import java.util.Enumeration;
import android.util.Log;

public class ZipHelper {
	static public void unzip(File archive, File outputDir) {
		try {
			Log.d("control", "ZipHelper.unzip() - File: " + archive.getPath());
			ZipFile zipfile = new ZipFile(archive);
			for (Enumeration e = zipfile.entries(); e.hasMoreElements();) {
				ZipEntry entry = (ZipEntry) e.nextElement();
				unzipEntry(zipfile, entry, outputDir);
			}
		} catch (Exception e) {
			Log.d("control", "ZipHelper.unzip() - Error extracting file " + archive + ": " + e);
		}
	}

	static private void unzipEntry(ZipFile zipfile, ZipEntry entry, File outputDir)
			throws IOException {
		if (entry.isDirectory()) {
			createDirectory(new File(outputDir, entry.getName()));
			return;
		}

		File outputFile = new File(outputDir, entry.getName());
		if (!outputFile.getParentFile().exists()) {
			createDirectory(outputFile.getParentFile());
		}

		Log.d("control", "ZipHelper.unzipEntry() - Extracting: " + entry);
		BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
		BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(
				outputFile));

		try {
			copyStreamToStream(inputStream, outputStream);
		} catch (Exception e) {
			Log.d("control", "ZipHelper.unzipEntry() - Error: " + e);
		} finally {
			outputStream.close();
			inputStream.close();
		}
	}

	static private void createDirectory(File dir) {
		Log.d("control", "ZipHelper.createDir() - Creating directory: " + dir.getName());
		if (!dir.exists()) {
			if (!dir.mkdirs())
				throw new RuntimeException("Can't create directory " + dir);
		} else
			Log.d("control", "ZipHelper.createDir() - Exists directory: " + dir.getName());
	}

	public static void copyStreamToStream(InputStream input, OutputStream output)
			throws IOException {
		InputStream is = null;
		OutputStream os = null;
		int ch;

		try {
			if (input instanceof BufferedInputStream) {
				is = input;
			} else {
				is = new BufferedInputStream(input);
			}
			if (output instanceof BufferedOutputStream) {
				os = output;
			} else {
				os = new BufferedOutputStream(output);
			}

			while ((ch = is.read()) != -1) {
				os.write(ch);
			}
			os.flush();
		} finally {
			IOException exec1 = null;
			IOException exec2 = null;
			try {
				// because this close can throw exception we do next close in
				// finally statement
				if (os != null) {
					try {
						os.close();
					} catch (IOException exec) {
						exec1 = exec;
					}
				}
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException exec) {
						exec2 = exec;
					}
				}
			}
			if ((exec1 != null) && (exec2 != null)) {
				throw exec1;
			} else if (exec1 != null) {
				throw exec1;
			} else if (exec2 != null) {
				throw exec2;
			}
		}
	}

	public static void inputStreamToFile(InputStream in, File output) {
		try {
			FileOutputStream f = new FileOutputStream(output);

			byte[] buffer = new byte[1024];
			int len1 = 0;
			while ((len1 = in.read(buffer)) > 0) {
				f.write(buffer, 0, len1);
			}
			f.close();
		} catch (Exception e) {
			Log.d("Utils.downloadFile()", e.getMessage());
		}
	}
}