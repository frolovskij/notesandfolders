package com.notesandfolders.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.notesandfolders.DbOpenHelper;
import com.notesandfolders.NaturalOrderNodesComparator;
import com.notesandfolders.Node;
import com.notesandfolders.NodeHelper;
import com.notesandfolders.Settings;

import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

public class NaturalOrderNodesComparatorTest extends AndroidTestCase {
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
		nh = new NodeHelper(getContext(), "");
		root = nh.getRootFolder();
	}

	@Override
	protected void tearDown() {
	}

	public void testOrder() {
		List<Node> list = new ArrayList<Node>();
		list.add(nh.createFolder(root, "x"));
		list.add(nh.createFolder(root, "Z"));
		list.add(nh.createNote(root, "A", ""));
		list.add(nh.createNote(root, "c", ""));
		list.add(nh.createCheckList(root, "b"));
		list.add(nh.createCheckList(root, "D"));

		Collections.sort(list, new NaturalOrderNodesComparator());

		StringBuilder sb = new StringBuilder();
		for (Node n : list) {
			sb.append(n.getName());
		}

		assertEquals("ZxADbc", sb.toString());
	}

}
