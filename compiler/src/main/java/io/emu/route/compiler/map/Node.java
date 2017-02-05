package io.emu.route.compiler.map;

import io.emu.route.util.MapUtil;

import java.io.Serializable;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

public class Node implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Point point;
	
	private String id;

	public Node(String id, double lon, double lat) {
		init(id, new Coordinate(lon, lat));
	}

	
	public Node(String id, Coordinate cs) {
		init(id, cs);
	}

	public Node(String id, Point point) {
		this.point = point;
		this.id = id;
	}

	
	private void init(String id, Coordinate cs) {
		this.id = id;		
		this.point = MapUtil.getPoint(cs);
	}

	
	public double getLon() {
		return this.point.getX();
	}

	
	public double getLat() {
		return this.point.getY();
	}

	
	public String getId() {
		return id;
	}

	
	public boolean isEqual(Node node) {
		boolean flag = false;
		if (node.getId().equalsIgnoreCase(this.id)) {
			flag = true;
		}
		return flag;
	}
}
