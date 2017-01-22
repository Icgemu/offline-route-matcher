/*
 * Class Name:    Node.java
 * Description:   TODO(类的功能描述)
 * Version:       2014年6月20日 下午2:56:01
 * Author:        Administrator
 * Copyright 2010 Cennavi Corp, All Rights Reserved.
 */
package io.emu.route.matcher.compiler.lucene;

import java.util.HashMap;

/**
 * TODO(这里用一句话描述这个类的作用)
 * 
 * @author Administrator
 * @version 2014年6月20日 下午2:56:01
 */
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

	/**
	 * TODO(功能描述)
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
