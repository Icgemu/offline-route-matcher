package org.icgemu.route.matcher.matching.pojo;

import java.util.List;

public class GPS {
	
	public double longtitude; // required
	public double latitude; // required
	public double direction; // required
	public long time; // required
	public double speed; // required
	public double engspd;
	public int keyst;
	public float soc;
	
	List<CandidatePrj> candidates;
	
	public List<CandidatePrj> getCandidates() {
		return candidates;
	}
	public void setCandidates(List<CandidatePrj> candidates) {
		this.candidates = candidates;
	}
	public GPS(double longtitude, double latitude, double direction, long time,
			double speed, double engspd, int keyst) {
		super();
		this.longtitude = longtitude;
		this.latitude = latitude;
		this.direction = direction;
		this.time = time;
		this.speed = speed;
		this.engspd = engspd;
		this.keyst = keyst;
	}
	
	public GPS(double longtitude, double latitude, double direction, long time,float soc) {
		super();
		this.longtitude = longtitude;
		this.latitude = latitude;
		this.direction = direction;
		this.time = time;
		this.soc = soc;
	}
	public double getLongtitude() {
		return longtitude;
	}
	public void setLongtitude(double longtitude) {
		this.longtitude = longtitude;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getDirection() {
		return direction;
	}
	public void setDirection(double direction) {
		this.direction = direction;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public double getSpeed() {
		return speed;
	}
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	public double getEngspd() {
		return engspd;
	}
	public void setEngspd(double engspd) {
		this.engspd = engspd;
	}
	public int getKeyst() {
		return keyst;
	}
	public void setKeyst(int keyst) {
		this.keyst = keyst;
	}
	public float getSoc() {
		return soc;
	}
	public void setSoc(float soc) {
		this.soc = soc;
	}
	@Override
	public String toString() {
		return "GPS [longtitude=" + longtitude + ", latitude=" + latitude
				+ ", time=" + time + "]";
	}	

}
