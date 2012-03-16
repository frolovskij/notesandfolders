package com.notesandfolders.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.notesandfolders.DbOpenHelper;
import com.notesandfolders.NaturalOrderNodesComparator;
import com.notesandfolders.NodeHelper;
import com.notesandfolders.NodeType;
import com.notesandfolders.Node;
import com.notesandfolders.Settings;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

public class NodeHelperTest extends AndroidTestCase {
	Settings s;
	NodeHelper fh;
	Node root;

	@Override
	protected void setUp() {
		DbOpenHelper dbOpenHelper = new DbOpenHelper(getContext());
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		dbOpenHelper.dropAllTables(db);
		dbOpenHelper.createAllTables(db);
		db.close();

		s = new Settings(getContext());

		fh = new NodeHelper(getContext(), "");
		root = fh.getRootFolder();
	}

	@Override
	protected void tearDown() {
	}

	public void testGetRootFolder() {
		assertNotNull(root);
		assertEquals("", root.getName());
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
		fh.createFolder(f1, "1.1");
		fh.createFolder(f2, "2.1");

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
		fh.createFolder(f4, "4");

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
		Collections.sort(children, new NaturalOrderNodesComparator());

		assertEquals(expected.size(), children.size());

		if (expected.size() == children.size()) {
			for (int i = 0; i < expected.size(); i++) {
				assertTrue(expected.get(i).equalsTo(children.get(i)));
			}
		}
	}

	public void testCreateNodeTextContent() {
		String expected = "ololo";
		Node f1 = fh.createNote(root, "test", expected);

		String tc = fh.getTextContentById(f1.getId());
		assertEquals(expected, tc);
	}

	public void testGetFullPathById() {
		Node f1 = fh.createFolder(root, "1");
		Node f2 = fh.createFolder(f1, "2");
		Node f3 = fh.createFolder(f2, "3");
		Node f4 = fh.createFolder(f3, "4");

		assertEquals("", fh.getFullPathById(-29384974));
		assertEquals("/", fh.getFullPathById(root.getId()));
		assertEquals("/1/", fh.getFullPathById(f1.getId()));
		assertEquals("/1/2/3/4/", fh.getFullPathById(f4.getId()));

		Node n1 = fh.createNote(root, "test.txt", "");
		Node n2 = fh.createNote(f1, "test2.txt", "");
		Node n3 = fh.createNote(f2, "test3.txt", "");

		assertEquals("/test.txt", fh.getFullPathById(n1.getId()));
		assertEquals("/1/test2.txt", fh.getFullPathById(n2.getId()));
		assertEquals("/1/2/test3.txt", fh.getFullPathById(n3.getId()));
	}

	public void testMoveToExistingFolder() {
		Node f1 = fh.createFolder(root, "1");
		Node f2 = fh.createFolder(root, "2");

		Node n = fh.createNote(f1, "test", "");
		fh.move(n.getId(), f2.getId());

		Node sameN = fh.getNodeById(n.getId());
		assertEquals(f2.getId(), sameN.getParentId());
	}

	public void testMoveToNotExisting() {
		Node n = fh.createNote(root, "test", "");
		fh.move(n.getId(), 123123);

		Node sameN = fh.getNodeById(n.getId());
		assertEquals(root.getId(), sameN.getParentId());
	}

	public void testMoveToNote() {
		Node n1 = fh.createNote(root, "test", "");
		Node n2 = fh.createNote(root, "test2", "");
		fh.move(n1.getId(), n2.getId());

		Node sameN1 = fh.getNodeById(n1.getId());
		assertEquals(root.getId(), sameN1.getParentId());
	}

	public void testCloneNodeById() {
		Node node = fh.createNote(root, "test", "text");
		long cloneId = fh.cloneNodeById(node.getId());
		Node clone = fh.getNodeById(cloneId);

		assertEquals(node.getName(), clone.getName());
		assertEquals(node.getParentId(), clone.getParentId());
		assertEquals(node.getType(), clone.getType());
		assertEquals(node.getDateCreated(), clone.getDateCreated());
		assertTrue(node.getId() != clone.getId());
		assertTrue(clone.getDateModified().getTime() >= node.getDateModified().getTime());

		assertEquals(fh.getTextContentById(node.getId()), fh.getTextContentById(clone.getId()));
	}

	public void testCopySingleNode() {
		Node f1 = fh.createFolder(root, "1");
		Node f2 = fh.createFolder(root, "2");

		Node node = fh.createNote(f1, "test", "text");
		fh.copy(node.getId(), f2.getId());

		List<Long> childrenIds = fh.getChildrenIdsById(f2.getId());
		assertEquals(1, childrenIds.size());
	}

	public void testCopyTree() {
		Node f1 = fh.createFolder(root, "1");
		Node f2 = fh.createFolder(root, "2");

		Node node = fh.createNote(f1, "test", "text");
		fh.copy(f1.getId(), f2.getId());

		List<Long> childrenIds = fh.getChildrenIdsById(f2.getId());
		assertEquals(1, childrenIds.size());
	}

	public void testGetParentsListById() {
		Node f1 = fh.createFolder(root, "1");
		Node f2 = fh.createFolder(f1, "2");
		Node f3 = fh.createFolder(f2, "3");
		Node n = fh.createNote(f3, "test.txt", "test");

		List<Long> parents = fh.getParentsListById(n.getId());

		assertEquals(4, parents.size());
		assertTrue(parents.contains(root.getId()));
		assertTrue(parents.contains(f1.getId()));
		assertTrue(parents.contains(f2.getId()));
		assertTrue(parents.contains(f3.getId()));
	}

	public void testCopyIntoItself() {
		Node f1 = fh.createFolder(root, "1");

		long nodesCount = fh.getNodesCount();
		fh.copy(f1.getId(), f1.getId());

		assertEquals(nodesCount, fh.getNodesCount());
	}

	public void testCopyIntoItself2() {
		Node f1 = fh.createFolder(root, "1");
		Node f2 = fh.createFolder(f1, "2");
		Node f3 = fh.createFolder(f2, "3");
		Node n = fh.createNote(f3, "test.txt", "test");

		long nodesCount = fh.getNodesCount();

		// trying to copy 1 to 3
		fh.copy(f1.getId(), f3.getId());

		assertEquals(nodesCount, fh.getNodesCount());
	}

	public void testMoveIntoItself() {
		Node f1 = fh.createFolder(root, "1");

		fh.move(f1.getId(), f1.getId());

		assertEquals(root.getId(), f1.getParentId());
	}

	public void testMoveIntoItself2() {
		Node f1 = fh.createFolder(root, "1");
		Node f2 = fh.createFolder(f1, "2");
		Node f3 = fh.createFolder(f2, "3");
		Node n = fh.createNote(f3, "test.txt", "test");

		// trying to move 1 to 3
		fh.move(f1.getId(), f3.getId());

		assertEquals(root.getId(), f1.getParentId());
	}
}
