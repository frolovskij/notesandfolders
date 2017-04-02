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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import android.content.Context;

public class ImportHelper {

	private static long _id = 0;

	public static final int RESULT_OK = 0;
	public static final int RESULT_CANT_READ = -1;
	public static final int RESULT_NOT_EXISTS = -2;
	public static final int RESULT_NOT_TXT = -3;

	private static long nextId() {
		return _id++;
	}

	private static void setId(long id) {
		_id = id;
	}

	private static String getFileNameExtension(String fileName) {
		int dot = fileName.lastIndexOf('.');
		return (dot == -1) ? "" : fileName.substring(dot + 1);
	}

	public static String getFileContents(File theFile) {
		try {
			byte[] bytes = new byte[(int) theFile.length()];
			InputStream in;
			in = new FileInputStream(theFile);
			int m = 0, n = 0;
			while (m < bytes.length) {
				n = in.read(bytes, m, bytes.length - m);
				m += n;
			}
			in.close();
			return new String(bytes, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static Node getNodeFromFile(File file, long id, long parentId) {
		Node n = new Node();
		n.setId(id);
		n.setParentId(parentId);
		n.setName(file.getName());
		n.setDateModified(new Date(file.lastModified()));
		n.setDateCreated(n.getDateModified()); // can't know
		n.setType(file.isDirectory() ? NodeType.FOLDER : NodeType.NOTE);
		n.setTextContent(getFileContents(file));

		// remove *.txt extension
		if (!file.isDirectory() && getFileNameExtension(file.getName()).equalsIgnoreCase("txt")) {
			int nameLen = file.getName().length();
			n.setName(n.getName().substring(0, nameLen - 4)); // ".txt".length()
		}

		return n;
	}

	public static int canImport(File file) {
		if (!file.canRead()) {
			return RESULT_CANT_READ;
		}

		if (!file.exists()) {
			return RESULT_NOT_EXISTS;
		}

		if (!file.isDirectory() && !getFileNameExtension(file.getName()).equalsIgnoreCase("txt")) {
			return RESULT_NOT_TXT;
		}

		return RESULT_OK;
	}

	private static void processPath(File file, List<Node> list, long parentId) {
		if (canImport(file) != ImportHelper.RESULT_OK) {
			return;
		}

		// importing single file
		if (!file.isDirectory()) {
			list.add(getNodeFromFile(file, nextId(), parentId));
			return;
		}

		// create a folder where to import data
		Node root = getNodeFromFile(file, nextId(), parentId);
		list.add(root);

		File[] children = file.listFiles();

		for (File child : children) {
			if (canImport(child) == ImportHelper.RESULT_OK) {
				if (child.isDirectory()) {
					processPath(child, list, root.getId());
				} else {
					list.add(getNodeFromFile(child, nextId(), root.getId()));
				}
			}
		}
	}

	public static List<Node> getFiles(String path, long startId, long parentId) {
		setId(startId);
		ArrayList<Node> files = new ArrayList<Node>();
		processPath(new File(path), files, parentId);

		return files;
	}

	public interface ImportListener {
		Context getContext();

		void publishProgress(int processed, int nodesCount);
	}

	public static void doImport(File file, ImportListener listener) {
		if (listener == null) {
			return;
		}

		final NodeHelper nh = new NodeHelper(listener.getContext(), new TempStorage(
				listener.getContext()).getPassword());

		Node importRoot = nh.createFolder(nh.getRootFolder(), "Imported at "
				+ new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date()));

		final List<Node> nodes = ImportHelper.getFiles(file.getAbsolutePath(), nh.getLastId() + 1,
				importRoot.getId());

		// if there's nothing to import
		if (nodes.size() == 0) {
			nh.deleteNodeById(importRoot.getId());
		} else {

			int nodesCount = nodes.size();
			listener.publishProgress(0, nodesCount);

			for (int i = 0; i < nodesCount; i++) {
				Node n = nodes.get(i);

				nh.insertNode(n);
				listener.publishProgress(i + 1, nodesCount);
			}
		}
	}

}
