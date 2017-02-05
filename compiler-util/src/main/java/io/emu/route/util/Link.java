package io.emu.route.util;

import java.io.Serializable;

import com.vividsolutions.jts.geom.LineString;

public class Link implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String id;
	
	private String snode;

	private String enode;
	
	private int width;
	
	private int length;
	
	private int speedLimit;

	private int direction;
	
	private String roadClass;
	
	private String roadType;
	
	private LineString geometry;
	
	public Link() {
	}

	public Link(String id, String snode, String enode, int width,
			int length, int speedLimit, int direction, String roadClass,
			String roadType, LineString geometry) {
		super();
		this.id = id;
		this.snode = snode;
		this.enode = enode;
		this.width = width;
		this.length = length;
		this.speedLimit = speedLimit;
		this.direction = direction;
		this.roadClass = roadClass;
		this.roadType = roadType;
		this.geometry = geometry;
	}

	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
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



	public int getWidth() {
		return width;
	}



	public void setWidth(int width) {
		this.width = width;
	}



	public int getLength() {
		return length;
	}



	public void setLength(int length) {
		this.length = length;
	}



	public int getSpeedLimit() {
		return speedLimit;
	}



	public void setSpeedLimit(int speedLimit) {
		this.speedLimit = speedLimit;
	}



	public int getDirection() {
		return direction;
	}



	public void setDirection(int direction) {
		this.direction = direction;
	}



	public String getRoadClass() {
		return roadClass;
	}



	public void setRoadClass(String roadClass) {
		this.roadClass = roadClass;
	}



	public String getRoadType() {
		return roadType;
	}



	public void setRoadType(String roadType) {
		this.roadType = roadType;
	}



	public LineString getGeometry() {
		return geometry;
	}



	public void setGeometry(LineString geometry) {
		this.geometry = geometry;
	}



	public boolean isEqual(Link link) {
		return link.getId() == id;
	}
	
	public double getTime() {

		return length / (speedLimit * 1000.0 / (3600));
	}
}
