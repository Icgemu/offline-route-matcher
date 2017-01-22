package io.emu.route.matcher.pojo;

import com.vividsolutions.jts.geom.Coordinate;

public class CandidatePrj {

	private Link link;
	private GPS gps;
	private Coordinate prjPoint;

	public Coordinate getPrjPoint() {
		return prjPoint;
	}

	public void setPrjPoint(Coordinate prjPoint) {
		this.prjPoint = prjPoint;
	}

	private double prjDistance; // required
	private double prjDistanceFormSNode; // required
	private double azimuth; // required
	private double azimuthDelta; // required
	private double cost; // required

	private boolean inLink;

	public boolean isInLink() {
		return inLink;
	}

	public void setInLink(boolean inLink) {
		this.inLink = inLink;
	}

	public CandidatePrj(Link link, GPS gps, double prjDistance,
			double prjDistanceFormSNode, double azimuth, double azimuthDelta,
			double cost, Coordinate prjPoint, boolean inLink) {
		super();
		this.link = link;
		this.gps = gps;
		this.prjDistance = prjDistance;
		this.prjDistanceFormSNode = prjDistanceFormSNode;
		this.azimuth = azimuth;
		this.azimuthDelta = azimuthDelta;
		this.cost = cost;
		this.prjPoint = prjPoint;

		this.inLink = inLink;
	}

	public Link getLink() {
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
	}

	public GPS getGps() {
		return gps;
	}

	public void setGps(GPS gps) {
		this.gps = gps;
	}

	public double getPrjDistance() {
		return prjDistance;
	}

	public void setPrjDistance(double prjDistance) {
		this.prjDistance = prjDistance;
	}

	public double getPrjDistanceFormSNode() {
		return prjDistanceFormSNode;
	}

	public void setPrjDistanceFormSNode(double prjDistanceFormSNode) {
		this.prjDistanceFormSNode = prjDistanceFormSNode;
	}

	public double getAzimuth() {
		return azimuth;
	}

	public void setAzimuth(double azimuth) {
		this.azimuth = azimuth;
	}

	public double getAzimuthDelta() {
		return azimuthDelta;
	}

	public void setAzimuthDelta(double azimuthDelta) {
		this.azimuthDelta = azimuthDelta;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

}
