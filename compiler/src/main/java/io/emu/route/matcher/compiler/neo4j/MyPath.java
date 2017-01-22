/*
 * Class Name:    Path.java
 * Description:   TODO(类的功能描述)
 * Version:       2014年2月23日 上午12:41:21
 * Author:        Administrator
 * Copyright 2010 Cennavi Corp, All Rights Reserved.
 */
package io.emu.route.matcher.compiler.neo4j;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO(这里用一句话描述这个类的作用)
 * 
 * @author Administrator
 * @version 2014年2月23日 上午12:41:21
 */
public class MyPath {

	int cost;
	String snode;
	String enode;
	List<LinkMB> links = new ArrayList<LinkMB>();

	/**
	 * 构造函数
	 * 
	 * @param cost
	 * @param snode
	 * @param enode
	 */
	public MyPath(int cost, String snode, String enode) {
		super();
		this.cost = cost;
		this.snode = snode;
		this.enode = enode;
	}

	public double getTime() {
		double t = 0.0;
		for (int i = 0; i < links.size(); i++) {
			t += links.get(i).getTime();
		}
		return t;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public String getSnode() {
		return snode;
	}

	public void setSnode(String snode) {
		this.snode = snode;
	}

	public String getEnode() {
		return enode;
	}

	public void setEnode(String enode) {
		this.enode = enode;
	}

	void addLink(LinkMB link) {
		this.links.add(link);
	}

	List<LinkMB> getLinks() {
		return this.links;
	}

	@Override
	public String toString() {
		String ls = "";
		for (int i = 0; i < links.size(); i++) {
			ls += ";" + links.get(i).getLinkid()+","+links.get(i).getLength();
		}
		return snode+","+enode + "#" +ls.substring(1) /**+ "\t" + cost + "\t" + links.size()*/;

	}
}
