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

import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import net.sf.andhsli.hotspotlogin.SimpleCrypto;

import com.notesandfolders.R;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class NodeHelper {
	Context context;
	String key = null;

	public static final int RESULT_OK = 0;
	public static final int RESULT_BAD_PARAMS = -9;
	public static final int RESULT_CANT_PASTE_TO_OWN_SUBFOLDER = -10;
	public static final int RESULT_CANT_PASTE_TO_ITSELF = -11;

	public NodeHelper(Context context, String password) {
		this.context = context;

		// decrypting key with password
		try {
			Settings s = new Settings(context);
			String encryptedKey = s.getString(Settings.SETTINGS_ENCRYPTED_KEY,
					"");
			this.key = SimpleCrypto.decrypt(password, encryptedKey);
		} catch (Exception e) {
			Log.i("NodeHelper", "Can't decrypt key with password '" + password
					+ "'");
		}
	}

	private Node createNode(Node parent, String name, String textContent,
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

	public String getFullPathById(long id) {
		StringBuffer path = new StringBuffer();

		Node current = getNodeById(id);
		if (current == null) {
			return "";
		}

		path.append(current.getName());
		if (current.getType() == NodeType.FOLDER) {
			path.append("/");
		}

		if (current.getParentId() != -1) {
			path.insert(0, getFullPathById(current.getParentId()));
		}

		return path.toString();
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

		Node here = getNodeById(id);
		if (here == null) {
			return children;
		}

		// adds ".." pseudo-directory to the listing
		if (here.getParentId() != -1) {
			Node parent = getNodeById(here.getParentId());
			if (parent != null) {
				parent.setName("..");
				children.add(parent);
			}
		}

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

	public Node createNote(Node parent, String name, String textContent) {
		return createNode(parent, name, textContent, NodeType.NOTE);
	}

	public Node createCheckList(Node parent, String name) {
		return createNode(parent, name, new CheckList().serialize(),
				NodeType.CHECKLIST);
	}

	public Node getRootFolder() {
		return getNodeById(0);
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
					"update data set text_content = ?, date_modified = ? where id = ?",
					new String[] { SimpleCrypto.encrypt(key, textContent),
							Long.toString(new Date().getTime()),
							Long.toString(id) });
		} catch (Exception ex) {
			Log.i("setTextContentById", ex.toString());
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}

	public void renameNodeById(long id, String newName) {
		// name policies go here
		DbOpenHelper dbOpenHelper = new DbOpenHelper(context);
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

		try {
			db.execSQL(
					"update data set name = ?, date_modified = ? where id = ?",
					new String[] { newName,
							Long.toString(new Date().getTime()),
							Long.toString(id) });
		} catch (Exception ex) {
			Log.i("renameNodeById", ex.toString());
		} finally {
			if (db != null) {
				db.close();
			}
		}
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

		try {
			db.insert("data", null, cv);
		} catch (Exception ex) {
		} finally {
			if (db != null) {
				db.close();
			}
		}

		setTextContentById(node.getId(), node.getTextContent());
	}

	/**
	 * Returns a list of parents' ids over a node
	 * 
	 * For example, if a full path for node is \some\dir\node.txt and some has
	 * id 1 and dir has id 2 then the parents list for node.txt should be {0, 1,
	 * 2} (0 for root folder).
	 * 
	 * @param id
	 *            id of the node which parents list should be returned
	 * @return list of parents' ids
	 */
	public List<Long> getParentsListById(long id) {
		List<Long> parents = new ArrayList<Long>();

		long currentId = id;

		while (true) {
			Node node = getNodeById(currentId);
			if (node == null) {
				break;
			}
			currentId = node.getParentId();

			// parent of root has id -1, don't add it
			if (currentId == -1) {
				break;
			}

			parents.add(currentId);
		}

		return parents;
	}

	/**
	 * Moves node to another folder
	 * 
	 * @param id
	 *            id of node to move
	 * @param newParentId
	 *            id of the folder that is a new parent
	 * @return Result code
	 */
	public int move(long id, long newParentId) {
		Node node = getNodeById(id);
		Node newParent = getNodeById(newParentId);

		if (node == null || newParent == null
				|| node.getParentId() == newParentId
				|| newParent.getType() != NodeType.FOLDER) {
			return RESULT_BAD_PARAMS;
		}

		// Prevents copying into itself
		if (id == newParentId) {
			return RESULT_CANT_PASTE_TO_ITSELF;
		}

		/**
		 * Prevent copying a node into it's children E.g., if we have \1\2\3\4
		 * tree then 2 won't be copied into 4
		 */
		// this check makes sense only for folders
		if (node.getType() == NodeType.FOLDER) {
			List<Long> parents = getParentsListById(newParentId);
			if (parents.contains(id)) {
				return RESULT_CANT_PASTE_TO_OWN_SUBFOLDER;
			}
		}

		DbOpenHelper dbOpenHelper = new DbOpenHelper(context);
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

		try {
			db.execSQL(
					"update data set parent_id = ?, date_modified = ? where id = ?",
					new String[] { Long.toString(newParentId),
							Long.toString(new Date().getTime()),
							Long.toString(id) });
		} catch (Exception ex) {
			Log.i("move", ex.toString());
		} finally {
			if (db != null) {
				db.close();
			}
		}

		return RESULT_OK;
	}

	/**
	 * Clones a single node and puts it in the same directory
	 * 
	 * @param id
	 *            id of the node to clone
	 * 
	 * @return id of the cloned node
	 */
	public long cloneNodeById(long id) {
		DbOpenHelper dbOpenHelper = new DbOpenHelper(context);
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

		Cursor c = null;
		try {
			c = db.rawQuery(
					"select name, parent_id, date_created, date_modified, type, text_content from data where id = ?",
					new String[] { Long.toString(id) });

			if (c.moveToFirst()) {
				ContentValues cv = new ContentValues();
				cv.put("name", c.getString(0));
				cv.put("parent_id", c.getLong(1));
				cv.put("date_created", c.getLong(2));
				cv.put("date_modified", new Date().getTime());
				cv.put("type", c.getInt(4));
				cv.put("text_content", c.getString(5));

				long cloneId = db.insertOrThrow("data", null, cv);

				return cloneId;

			} else {
				Log.i("clone", "Cursor is empty");
			}
		} catch (Exception ex) {
			Log.i("clone", ex.toString());
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

	/**
	 * Makes a copy of node and its children
	 * 
	 * @param id
	 *            id of the node to copy
	 * @param newParentId
	 *            id of the folder where the copy should go
	 * @return Result code
	 */
	public int copy(long id, long newParentId) {
		Node node = getNodeById(id);
		Node newParent = getNodeById(newParentId);

		if (node == null || newParent == null) {
			return RESULT_BAD_PARAMS;
		}

		// Prevents copying into itself
		if (id == newParentId) {
			return RESULT_CANT_PASTE_TO_ITSELF;
		}

		// If it would be proven to be slow then all copying would go into
		// copy0() without this check

		// this check makes sense only for folders
		if (node.getType() == NodeType.FOLDER) {
			List<Long> parents = getParentsListById(newParentId);
			if (parents.contains(id)) {
				return RESULT_CANT_PASTE_TO_OWN_SUBFOLDER;
			}
		}

		long cloneId = cloneNodeById(id);

		// rename if copy goes into the same folder
		if (newParentId == node.getParentId()) {
			renameNodeById(
					cloneId,
					node.getName()
							+ context.getText(R.string.filename_copy_suffix));
		}

		move(cloneId, newParentId);

		if (node.getType() == NodeType.FOLDER) {
			List<Long> children = getChildrenIdsById(id);
			for (Long childId : children) {
				copy(childId, cloneId);
			}
		}

		return RESULT_OK;
	}

}
