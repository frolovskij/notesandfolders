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

	public static void processPath(File file, List<Node> list, long parentId) {
		if (!file.exists()) {
			return;
		}

		if (!file.isDirectory()) {
			Node f = new Node();
			f.setId(nextId());
			f.setParentId(parentId);
			f.setName(file.getName());
			f.setDateModified(new Date(file.lastModified()));
			f.setDateCreated(f.getDateModified()); // can't know
			f.setType(NodeType.NOTE);

			list.add(f);
			return;
		}

		Node root = new Node();
		root.setId(nextId());
		root.setParentId(parentId);
		root.setName(file.getName());
		root.setDateModified(new Date(file.lastModified()));
		root.setDateCreated(root.getDateModified());
		root.setType(NodeType.FOLDER);

		list.add(root);

		File[] children = file.listFiles();

		for (File child : children) {
			if (child.isDirectory()) {
				processPath(child, list, root.getId());
			} else {
				if (getFileNameExtension(child.getName()).equalsIgnoreCase("txt")) {
					Node n = new Node();
					n.setId(nextId());
					n.setParentId(root.getId());
					n.setName(child.getName());
					n.setDateModified(new Date(child.lastModified()));
					n.setDateCreated(n.getDateModified());
					n.setType(NodeType.NOTE);

					StringBuffer sb = new StringBuffer();
					try {
						BufferedReader br = new BufferedReader(new InputStreamReader(
								new FileInputStream(child), "UTF-8"));
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

					n.setTextContent(sb.toString());

					list.add(n);
				}
			}
		}

		return;
	}

	// should pass the id of folder where the imported data would go
	public static List<Node> getFiles(String path, long startId, long parentId) {
		setId(startId);
		ArrayList<Node> files = new ArrayList<Node>();
		processPath(new File(path), files, parentId);
		return files;
	}
}
