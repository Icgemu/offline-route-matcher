package io.emu.route.compiler.map;

import java.io.Serializable;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

/**
 * 基础node对象，如需扩展支持更多属性方法，请用装饰者模式扩展.
 * 
 * @author Ray
 * @version 2013-10-14 下午4:55:38
 */
public class Node implements Serializable {

	private static final long serialVersionUID = 1L;
	/**
	 * point.
	 */
	protected Point point;
	/**
	 * node id.
	 */
	protected String id;

	/**
	 * 构造函数.
	 * 
	 * @param id
	 *            nodeid
	 * @param lon
	 *            经度
	 * @param lat
	 *            维度
	 */
	public Node(String id, double lon, double lat) {
		init(id, new Coordinate(lon, lat));
	}

	/**
	 * 构造函数.
	 * 
	 * @param id
	 *            nodeid
	 * @param cs
	 *            Coordinate
	 */
	public Node(String id, Coordinate cs) {
		init(id, cs);
	}

	/**
	 * 构造函数.
	 * 
	 * @param id
	 *            nodeid
	 * @param point
	 *            Point
	 */
	public Node(String id, Point point) {
		this.point = point;
		this.id = id;
	}

	/**
	 * 初始化.
	 * 
	 * @param id
	 *            nodeid
	 * @param cs
	 *            Coordinate
	 */
	private void init(String id, Coordinate cs) {
		this.id = id;		
		this.point = MapUtil.getPoint(cs);
	}

	/**
	 * 得到精度.
	 * 
	 * @return double
	 */
	public double getLon() {
		return this.point.getX();
	}

	/**
	 * 得到维度.
	 * 
	 * @return double
	 */
	public double getLat() {
		return this.point.getY();
	}

	/**
	 * 得到id.
	 * 
	 * @return String
	 */
	public String getId() {
		return id;
	}

	/**
	 * 判断node是否相等.
	 * 
	 * @param node
	 *            Node
	 * @return boolean
	 */
	public boolean isEqual(Node node) {
		boolean flag = false;
		if (node.getId().equalsIgnoreCase(this.id)) {
			flag = true;
		}
		return flag;
	}
}
