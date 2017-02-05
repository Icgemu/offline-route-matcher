package io.emu.route.compiler.neo4j;

import io.emu.route.util.Link;
import java.util.ArrayList;
import java.util.List;

public class MyPath {

	int cost;
	String snode;
	String enode;
	List<Link> links = new ArrayList<Link>();

	
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

	void addLink(Link link) {
		this.links.add(link);
	}

	List<Link> getLinks() {
		return this.links;
	}

	@Override
	public String toString() {
		String ls = "";
		for (int i = 0; i < links.size(); i++) {
			ls += ";" + links.get(i).getId() + ","
					+ links.get(i).getLength();
		}
		return snode + "," + enode + "#" + ls.substring(1)/**
		 * + "\t" + cost + "\t"
		 * + links.size()
		 */
		;

	}
}
