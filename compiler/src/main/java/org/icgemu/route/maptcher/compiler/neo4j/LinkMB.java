/*
 * Class Name:    LinkMB.java
 * Description:   TODO(类的功能描述)
 * Version:       2014年2月23日 上午12:42:11
 * Author:        Administrator
 * Copyright 2010 Cennavi Corp, All Rights Reserved.
 */
package com.cennavi.compiler.neo4j;

/**
 * TODO(这里用一句话描述这个类的作用)
 * 
 * @author Administrator
 * @version 2014年2月23日 上午12:42:11
 */
public class LinkMB {

	String linkid;
	String snode;
	String enode;
	String direction;
	String roadclass;
	String kind;
	String width;
	int length;
	int speed;

	/**
	 * 构造函数
	 * 
	 * @param linkid
	 * @param snode
	 * @param enode
	 * @param direction
	 * @param roadclass
	 * @param kind
	 * @param width
	 * @param length
	 */
	public LinkMB(String linkid, String snode, String enode, String direction,
			String roadclass, String kind, String width, int length, int speed) {
		super();
		this.linkid = linkid;
		this.snode = snode;
		this.enode = enode;
		this.direction = direction;
		this.roadclass = roadclass;
		this.kind = kind;
		this.width = width;
		this.length = length;
		this.speed = speed;
	}

	public double getTime() {

		return length / (speed * 1000.0 / (3600));
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public String getLinkid() {
		return linkid;
	}

	public void setLinkid(String linkid) {
		this.linkid = linkid;
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

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getRoadclass() {
		return roadclass;
	}

	public void setRoadclass(String roadclass) {
		this.roadclass = roadclass;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	@Override
	public String toString() {
		return linkid + "," /**+ snode + "," + enode + ","*/ + direction + ","
				+ roadclass + "," + kind + "," + width + "," + length + ","
				+ speed;
	}

}
