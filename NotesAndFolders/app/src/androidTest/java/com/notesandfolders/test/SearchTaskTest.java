package com.notesandfolders.test;

import java.util.List;

import com.notesandfolders.CheckList;
import com.notesandfolders.CheckListItem;
import com.notesandfolders.DbOpenHelper;
import com.notesandfolders.Node;
import com.notesandfolders.NodeHelper;
import com.notesandfolders.NodeType;
import com.notesandfolders.SearchParameters;
import com.notesandfolders.SearchTask;
import com.notesandfolders.Serializer;
import com.notesandfolders.Settings;

import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

public class SearchTaskTest extends AndroidTestCase {
	Settings s;
	NodeHelper nh;
	Node root;

	@Override
	protected void setUp() {
		DbOpenHelper dbOpenHelper = new DbOpenHelper(getContext());
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		dbOpenHelper.dropAllTables(db);
		dbOpenHelper.createAllTables(db);
		db.close();

		s = new Settings(getContext());

		nh = new NodeHelper(getContext(), "");
		root = nh.getRootFolder();

		// /
		// ├─A (folder)
		// │ ├─C (note: hello)
		// │ └─e (note: HELLO)
		// ├─b
		// │ └─G (checklist: H, i, j)

		Node folderA = nh.createFolder(root, "A");
		Node folderb = nh.createFolder(root, "b");

		Node noteC = nh.createNote(folderA, "C", "hello");
		Node noted = nh.createNote(folderb, "e", "HELLO");

		CheckList cl = new CheckList();
		cl.add(new CheckListItem("H", true));
		cl.add(new CheckListItem("i", true));
		cl.add(new CheckListItem("J", true));
		Node checkListG = nh.createCheckList(folderb, "G");
		nh.setTextContentById(checkListG.getId(), Serializer.serialize(cl));
	}

	@Override
	protected void tearDown() {
	}

	public void testSearchByNameCaseInsensitive() {
		SearchParameters params = new SearchParameters();
		params.setSearchInNames(true);
		params.setSearchInText(false);
		params.setCaseSensitive(false);
		params.setFolderId(0);
		params.setText("a");

		SearchTask st = new SearchTask(null, nh, params);
		List<Long> results = st.getSearchResults();

		// folder A

		assertEquals(1, results.size());
		Node found = nh.getNodeById(results.get(0));
		assertEquals("A", found.getName());
		assertEquals(NodeType.FOLDER, found.getType());
	}

	public void testSearchByTextCaseSensitive() {
		SearchParameters params = new SearchParameters();
		params.setSearchInNames(false);
		params.setSearchInText(true);
		params.setCaseSensitive(true);
		params.setFolderId(0);
		params.setText("HELLO");

		SearchTask st = new SearchTask(null, nh, params);
		List<Long> results = st.getSearchResults();

		// Notes C and e

		assertEquals(1, results.size());
		Node found1 = nh.getNodeById(results.get(0));
		assertEquals("e", found1.getName());
		assertEquals(NodeType.NOTE, found1.getType());
	}

	public void testSearchByTextCaseInsensitive() {
		SearchParameters params = new SearchParameters();
		params.setSearchInNames(false);
		params.setSearchInText(true);
		params.setCaseSensitive(false);
		params.setFolderId(0);
		params.setText("hello");

		SearchTask st = new SearchTask(null, nh, params);
		List<Long> results = st.getSearchResults();

		// Notes C and e

		assertEquals(2, results.size());
		Node found1 = nh.getNodeById(results.get(0));
		assertEquals("C", found1.getName());
		assertEquals(NodeType.NOTE, found1.getType());

		Node found2 = nh.getNodeById(results.get(1));
		assertEquals("e", found2.getName());
		assertEquals(NodeType.NOTE, found2.getType());
	}

	public void testSearchByTextInCheckLists() {
		SearchParameters params = new SearchParameters();
		params.setSearchInNames(false);
		params.setSearchInText(true);
		params.setCaseSensitive(false);
		params.setFolderId(0);
		params.setText("h");

		SearchTask st = new SearchTask(null, nh, params);
		List<Long> results = st.getSearchResults();

		// CheckList G is present in results

		assertEquals(3, results.size());
		Node found3 = nh.getNodeById(results.get(2));
		assertEquals("G", found3.getName());
		assertEquals(NodeType.CHECKLIST, found3.getType());
	}

}
