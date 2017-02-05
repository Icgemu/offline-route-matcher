package io.emu.route.compiler.lucene;

import java.util.HashMap;

public class Node {
	int idx;
	int parent = -1;
	int lev = 0;
	HashMap<String, Node> map = new HashMap<String, Node>();

	Node() {

	}

	boolean contains(String link) {
		return map.containsKey(link);
	}

	void add(String link) {
		map.put(link, new Node());
	}

	Node get(String link) {
		return map.get(link);
	}
}
