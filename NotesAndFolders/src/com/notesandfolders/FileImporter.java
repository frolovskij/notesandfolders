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
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class FileImporter {

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

	private static String getFileContents(File f) {
		StringBuffer sb = new StringBuffer();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(f), "UTF-8"));
			try {
				String s;
				while ((s = br.readLine()) != null) {
					sb.append(s);
					sb.append(String.format("%n"));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return sb.toString();

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

		return n;
	}

	public static int canImport(File file) {
		if (!file.canRead()) {
			return RESULT_CANT_READ;
		}

		if (!file.exists()) {
			return RESULT_NOT_EXISTS;
		}

		if (!file.isDirectory()
				&& !getFileNameExtension(file.getName())
						.equalsIgnoreCase("txt")) {
			return RESULT_NOT_TXT;
		}

		return RESULT_OK;
	}

	private static void processPath(File file, List<Node> list, long parentId) {
		if (canImport(file) != FileImporter.RESULT_OK) {
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
			if (canImport(child) == FileImporter.RESULT_OK) {
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

}
