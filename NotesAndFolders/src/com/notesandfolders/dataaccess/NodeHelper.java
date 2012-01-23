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

package com.notesandfolders.dataaccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import net.sf.andhsli.hotspotlogin.SimpleCrypto;
import com.notesandfolders.Node;
import com.notesandfolders.NodeType;
import com.notesandfolders.Settings;
import com.notesandfolders.SqliteSettings;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class NodeHelper {
	Context context;
	String key = null;

	public NodeHelper(Context context, String password) {
		this.context = context;

		// decrypting key with password
		try {
			Settings s = new SqliteSettings(context);
			String encryptedKey = s.getString(Settings.SETTINGS_ENCRYPTED_KEY,
					"");
			this.key = SimpleCrypto.decrypt(password, encryptedKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Node createNode(Node parent, String name, String textContent,
			NodeType type) {
		if (parent == null) {
			Log.i("createNode", "parent is null");
			return null;
		}

		if (name == null || name.trim().equals("")) {
			Log.i("createNode", "name is null or empty");
			return null;
		}

		if (parent.getType() != NodeType.FOLDER) {
			Log.i("createNode", "parent is not a directory");
			return null;
		}

		Node f = new Node();
		f.setName(name);
		f.setParentId(parent.getId());
		f.setDateCreated(new Date());
		f.setDateModified(f.getDateCreated());
		f.setType(type);
		f.setTextContent(textContent);

		DbOpenHelper dbOpenHelper = new DbOpenHelper(context);
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

		try {
			ContentValues cv = new ContentValues();
			cv.put("name", f.getName());
			cv.put("parent_id", f.getParentId());
			cv.put("date_created", f.getDateCreated().getTime());
			cv.put("date_modified", f.getDateModified().getTime());
			cv.put("type", f.getType().getType());

			long id = db.insertOrThrow("data", null, cv);
			if (id != -1) {
				f.setId(id);
			} else {
				Log.i("createNode", "inserted id is -1, result is null");
				f = null;
			}

			if (f.getType() != NodeType.FOLDER && f.getTextContent() != null
					&& (!f.getTextContent().trim().equals(""))) {
				setTextContentById(id, f.getTextContent());
			}
		} catch (SQLException ex) {
			// if not created for some reason
			Log.i("createNode", "SQLException happened, result is null");
			f = null;
		} finally {
			if (db != null) {
				db.close();
			}
		}

		return f;
	}

	public Node getNodeById(long id) {
		DbOpenHelper dbOpenHelper = new DbOpenHelper(context);
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

		Node f = null;

		Cursor c = null;
		try {
			c = db.rawQuery(
					"select name, parent_id, date_created, date_modified, type from data where id = ?",
					new String[] { Long.toString(id) });

			if (c.moveToFirst()) {
				f = new Node();

				f.setId(id);
				f.setName(c.getString(0));
				f.setParentId(c.getLong(1));
				f.setDateCreated(new Date(c.getLong(2)));
				f.setDateModified(new Date(c.getLong(3)));
				switch (c.getInt(4)) {
				case 0:
					f.setType(NodeType.FOLDER);
					break;
				case 1:
					f.setType(NodeType.NOTE);
					break;
				case 2:
					f.setType(NodeType.CHECKLIST);
					break;
				}
			} else {
				Log.i("getNodeById", "Cursor is empty");
			}
		} catch (Exception ex) {
			Log.i("getNodeById", ex.toString());
		} finally {
			if (c != null) {
				c.close();
			}
			if (db != null) {
				db.close();
			}
		}

		return f;
	}

	public List<Long> getChildrenIdsById(long id) {
		List<Long> childrenIds = new ArrayList<Long>();

		DbOpenHelper dbOpenHelper = new DbOpenHelper(context);
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

		Cursor c = null;
		try {
			c = db.rawQuery("select id from data where parent_id = ?",
					new String[] { Long.toString(id) });

			for (boolean hasItem = c.moveToFirst(); hasItem; hasItem = c
					.moveToNext()) {
				childrenIds.add(c.getLong(0));
			}
		} catch (Exception ex) {
			Log.i("getChildrenIds", ex.toString());
		} finally {
			if (c != null) {
				c.close();
			}
			if (db != null) {
				db.close();
			}
		}

		return childrenIds;
	}

	public List<Node> getChildrenById(long id) {
		List<Node> children = new ArrayList<Node>();

		for (long childId : getChildrenIdsById(id)) {
			Node child = getNodeById(childId);
			if (child != null) {
				children.add(child);
			}
		}

		return children;
	}

	public void deleteNodeById(long id) {
		if (id == 0) {
			Log.i("deleteNodeById", "Won't delete root folder");
			return;
		}

		DbOpenHelper dbOpenHelper = new DbOpenHelper(context);
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

		try {
			/**
			 * Android 2.1 (sqlite version < 3.6.19) doesn't support ON CASCADE
			 * DELETE. Thus we have to delete the children manually with
			 * recursion :)
			 */
			for (long childId : getChildrenIdsById(id)) {
				deleteNodeById(childId);
			}

			db.execSQL("delete from data where id = ?", new Long[] { id });
		} catch (Exception ex) {
			Log.i("deleteNodeById", ex.toString());
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}

	public long getNodesCount() {
		DbOpenHelper dbOpenHelper = new DbOpenHelper(context);
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

		Cursor c = null;
		try {
			c = db.rawQuery("select count(id) from data", null);

			if (c.moveToFirst()) {
				return c.getLong(0);
			}
		} catch (Exception ex) {
			Log.i("getNodesCount", ex.toString());
			return -1;
		} finally {
			if (c != null) {
				c.close();
			}
			if (db != null) {
				db.close();
			}
		}

		return -1;
	}

	public long getLastId() {
		DbOpenHelper dbOpenHelper = new DbOpenHelper(context);
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

		Cursor c = null;
		try {
			c = db.rawQuery("select max(id) from data", null);

			if (c.moveToFirst()) {
				return c.getLong(0);
			}
		} catch (Exception ex) {
			Log.i("getLastId", ex.toString());
			return -1;
		} finally {
			if (c != null) {
				c.close();
			}
			if (db != null) {
				db.close();
			}
		}

		return -1;
	}

	public Node createFolder(Node parent, String name) {
		return createNode(parent, name, null, NodeType.FOLDER);
	}

	public Node getRootFolder() {
		return getNodeById(0);
	}

	public void insertNode(Node node) {
		if (node == null) {
			return;
		}

		DbOpenHelper dbOpenHelper = new DbOpenHelper(context);
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

		ContentValues cv = new ContentValues();
		cv.put("id", node.getId());
		cv.put("parent_id", node.getParentId());
		cv.put("name", node.getName());
		cv.put("date_created", node.getDateCreated().getTime());
		cv.put("date_modified", node.getDateModified().getTime());
		cv.put("type", node.getType().getType());
		cv.put("text_content", node.getTextContent());

		try {
			db.insert("data", null, cv);
		} catch (Exception ex) {
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}

	public String getTextContentById(long id) {
		DbOpenHelper dbOpenHelper = new DbOpenHelper(context);
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

		String textContent = "";
		Cursor c = null;
		try {
			c = db.rawQuery("select text_content from data where id = ?",
					new String[] { Long.toString(id) });

			if (c.moveToFirst()) {
				textContent = SimpleCrypto.decrypt(key, c.getString(0));
			} else {
				Log.i("getTextContentById", "Cursor is empty");
			}
		} catch (Exception ex) {
			Log.i("getTextContentById", ex.toString());
		} finally {
			if (c != null) {
				c.close();
			}
			if (db != null) {
				db.close();
			}
		}

		return textContent;
	}

	public void setTextContentById(long id, String textContent) {
		if (textContent == null) {
			textContent = "";
		}

		DbOpenHelper dbOpenHelper = new DbOpenHelper(context);
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

		try {
			db.execSQL(
					"update data set text_content = ? where id = ?",
					new String[] { SimpleCrypto.encrypt(key, textContent),
							Long.toString(id) });
		} catch (Exception ex) {
			Log.i("setTextContentById", ex.toString());
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}
}
