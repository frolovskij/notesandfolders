package com.notesandfolders.test;

import com.notesandfolders.CheckList;
import com.notesandfolders.CheckListItem;
import com.notesandfolders.Serializer;

import android.test.AndroidTestCase;

public class CheckListTest extends AndroidTestCase {
	@Override
	protected void setUp() {
	}

	@Override
	protected void tearDown() {
	}

	public void testSerialization() {
		CheckListItem item1 = new CheckListItem("First", true);
		CheckListItem item2 = new CheckListItem("Second", false);
		CheckListItem item3 = new CheckListItem("Third", true);

		CheckList list = new CheckList();
		list.add(item1);
		list.add(item2);
		list.add(item3);

		String serialized = Serializer.serialize(list);

		CheckList list2 = (CheckList) Serializer.deserialize(serialized);
		assertEquals(3, list2.size());
		assertEquals("First", ((CheckListItem) list2.get(0)).getText());
		assertEquals(true, ((CheckListItem) list2.get(0)).isChecked());
		assertEquals("Second", ((CheckListItem) list2.get(1)).getText());
		assertEquals(false, ((CheckListItem) list2.get(1)).isChecked());
		assertEquals("Third", ((CheckListItem) list2.get(2)).getText());
		assertEquals(true, ((CheckListItem) list2.get(2)).isChecked());
	}

	public void testEquals() {
		CheckList list = new CheckList();
		list.add(new CheckListItem("First", true));
		list.add(new CheckListItem("Second", false));
		list.add(new CheckListItem("Third", true));

		CheckList list2 = new CheckList();
		list2.add(new CheckListItem("First", true));
		list2.add(new CheckListItem("Second", false));
		list2.add(new CheckListItem("Third", true));

		assertEquals(list, list2);
		assertEquals(list.hashCode(), list2.hashCode());

		list2.remove(0);
		assertFalse(list.equals(list2));
	}

}
