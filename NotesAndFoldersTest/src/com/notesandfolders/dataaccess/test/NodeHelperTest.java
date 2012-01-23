package com.notesandfolders.dataaccess.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.notesandfolders.NodeType;
import com.notesandfolders.Node;
import com.notesandfolders.dataaccess.DbOpenHelper;
import com.notesandfolders.dataaccess.NodeHelper;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

public class NodeHelperTest extends AndroidTestCase {
	NodeHelper fh;
	Node root;

	@Override
	protected void setUp() {
		DbOpenHelper dbOpenHelper = new DbOpenHelper(getContext());
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		dbOpenHelper.dropAllTables(db);
		dbOpenHelper.createAllTables(db);
		db.close();

		fh = new NodeHelper(getContext());
		root = fh.getRootFolder();
	}

	@Override
	protected void tearDown() {
	}

	public void testGetRootFolder() {
		assertNotNull(root);
		assertEquals("ROOT", root.getName());
		assertEquals(0, root.getId());
		assertEquals(1, fh.getNodesCount());
	}

	public void testCreateFolder() {
		Node folder = fh.createFolder(root, "test");
		Node sameFolder = fh.getNodeById(folder.getId());

		assertEquals(2, fh.getNodesCount());

		assertEquals("test", sameFolder.getName());
		assertEquals(NodeType.FOLDER, sameFolder.getType());
		assertEquals(root.getId(), sameFolder.getParentId());

		assertEquals(folder.getName(), sameFolder.getName());
		assertEquals(folder.getId(), sameFolder.getId());
		assertEquals(folder.getParentId(), sameFolder.getParentId());
		assertEquals(folder.getDateCreated(), sameFolder.getDateCreated());
		assertEquals(folder.getDateModified(), sameFolder.getDateModified());
	}

	public void testCreateFoldersTree() {
		Node f1 = fh.createFolder(root, "1");
		Node f2 = fh.createFolder(root, "2");
		Node f1_1 = fh.createFolder(f1, "1.1");
		Node f2_1 = fh.createFolder(f2, "2.1");

		assertEquals(5, fh.getNodesCount());
	}

	public void testGetChildrenIdsById() {
		Node f1 = fh.createFolder(root, "1");
		Node f2 = fh.createFolder(root, "2");
		Node f3 = fh.createFolder(root, "3");
		Node f4 = fh.createFolder(root, "4");

		ArrayList<Long> expected = new ArrayList<Long>();
		expected.add(f1.getId());
		expected.add(f2.getId());
		expected.add(f3.getId());
		expected.add(f4.getId());

		List<Long> ids = fh.getChildrenIdsById(root.getId());

		assertEquals(expected.size(), ids.size());

		if (ids.size() == expected.size()) {
			for (int i = 0; i < ids.size(); i++) {
				assertEquals(expected.get(i), ids.get(i));
			}
		}
	}

	public void testDeleteSingleFileById() {
		Node folder = fh.createFolder(root, "test");
		assertEquals(2, fh.getNodesCount());

		fh.deleteNodeById(folder.getId());

		Node sameFolder = fh.getNodeById(folder.getId());
		assertNull(sameFolder);

		assertEquals(1, fh.getNodesCount());
	}

	public void testDeleteTree() {
		Node f1 = fh.createFolder(root, "1");
		Node f2 = fh.createFolder(f1, "2");
		Node f3 = fh.createFolder(f2, "3");
		Node f4 = fh.createFolder(f3, "4");
		Node f5 = fh.createFolder(f4, "4");

		fh.deleteNodeById(f1.getId());
		assertEquals(1, fh.getNodesCount());
	}

	public void testGetNotExistingFile() {
		Node noSuchFile = fh.getNodeById(123);
		assertNull(noSuchFile);
	}

	public void testEqualsTo() {
		Node f1 = fh.createFolder(root, "1");
		Node f2 = fh.getNodeById(f1.getId());
		Node f3 = fh.createFolder(root, "2");

		assertTrue(f1.equalsTo(f1));
		assertTrue(f1.equalsTo(f2));
		assertFalse(f1.equalsTo(f3));
	}

	public void testGetChildrenById() {
		Node f1 = fh.createFolder(root, "1");
		Node f2 = fh.createFolder(root, "2");
		Node f3 = fh.createFolder(root, "3");
		Node f4 = fh.createFolder(root, "4");

		List<Node> expected = new ArrayList<Node>();
		expected.add(f1);
		expected.add(f2);
		expected.add(f3);
		expected.add(f4);

		List<Node> children = fh.getChildrenById(root.getId());

		assertEquals(expected.size(), children.size());

		if (expected.size() == children.size()) {
			for (int i = 0; i < expected.size(); i++) {
				assertTrue(expected.get(i).equalsTo(children.get(i)));
			}
		}
	}
}
