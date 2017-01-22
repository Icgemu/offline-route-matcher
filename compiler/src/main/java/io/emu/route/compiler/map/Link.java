package io.emu.route.compiler.map;

import java.io.Serializable;

import com.vividsolutions.jts.geom.LineString;

/**
 * 基础link对象，如需扩展支持更多属性方法，请用装饰者模式扩展.
 * 
 * @author Ray
 * @version 2013-10-14 下午4:55:22
 */
public class Link implements Serializable {

	/**
	 * link id.
	 */
	protected String id;
	/**
	 * link start node.
	 */
	protected String snode;
	/**
	 * link end node.
	 */
	protected String enode;
	/**
	 * 宽度，单位:米.
	 */
	protected float width;
	/**
	 * 长度，单位:米.
	 */
	protected float length;
	/**
	 * 速度，单位:米/每秒.
	 */
	protected float speedlimit;
	/**
	 * 方向.
	 */
	protected int direction;
	/**
	 * 道路等级R表Kind前两位.
	 */
	protected String roadclass;
	/**
	 * 道路属性R表Kind后两位.
	 */
	protected String roadtype;
	/**
	 * 形状.
	 */
	protected LineString geometry;

	/**
	 * 构造函数.
	 * @param id linkid
	 * @param snode start node
	 * @param enode end node
	 * @param direction direction
	 * @param roadclass 道路等级R表Kind前两位
	 * @param roadtype 道路属性R表Kind后两位
	 * @param width 宽度
	 * @param length 长度
	 * @param speedlimit 限速
	 * @param geometry 形状
	 */
	public Link(String id, String snode, String enode, int direction,
			String roadclass, String roadtype, float width, float length,
			float speedlimit, LineString geometry) {
		this.id = id;
		this.snode = snode;
		this.enode = enode;
		this.width = width;
		this.length = length;
		this.speedlimit = speedlimit;
		this.direction = direction;
		this.roadclass = roadclass;
		this.roadtype = roadtype;
		this.geometry = geometry;
	}

	/**
	 * 得到linkid.
	 * @return linkid
	 */
	public String getId() {
		return id;
	}

	/**
	 * 得到start node.
	 * @return String nodeid
	 */
	public String getSnode() {
		return snode;
	}

	/**
	 * 得到end node id.
	 * @return String nodeid
	 */
	public String getEnode() {
		return enode;
	}

	/**
	 * width.
	 * @return float width
	 */
	public float getWidth() {
		return width;
	}

	/**
	 * length.
	 * @return float length
	 */
	public float getLength() {
		return length;
	}

	/**
	 * 限速.
	 * @return float 限速
	 */
	public float getSpeedlimit() {
		return speedlimit;
	}

	/**
	 * direction.
	 * @return int
	 */
	
	public int getDirection() {
		return direction;
	}

	/**
	 * 得到道路类型.
	 * @return String 0x
	 */
	public String getRoadtype() {
		return roadtype;
	}

	/**
	 * 得到道路等级.
	 * @return String 0x
	 */
	public String getRoadclass() {
		return roadclass;
	}

	/**
	 * 得到路链形状.
	 * @return LineString
	 */
	public LineString getGeometry() {
		return geometry;
	}

	/**
	 * 判断路链是否相等.
	 * @param link 路链
	 * @return boolean
	 */
	public boolean isEqual(Link link) {
		return link.getId() == id;
	}
}
