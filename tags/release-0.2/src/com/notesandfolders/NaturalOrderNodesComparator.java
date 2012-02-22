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

import java.util.Comparator;

/**
 * This comparator is used to sort Nodes in natural order, i.e.: [a], [b], [z],
 * a, b, z, where [x] is a folder and x is not a folder.
 */
public class NaturalOrderNodesComparator implements Comparator<Node> {
	public int compare(Node first, Node second) {
		if (first == null || second == null) {
			return 0;
		}

		if (first.getType() == second.getType()) {
			return first.getName().compareTo(second.getName());
		} else {
			if (first.getType() == NodeType.FOLDER) {
				return -1;
			} else {
				return 1;
			}
		}
	}
}
