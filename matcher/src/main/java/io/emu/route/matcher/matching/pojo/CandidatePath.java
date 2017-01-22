package io.emu.route.matcher.matching.pojo;

import java.util.List;

public class CandidatePath {
	
	private CandidatePrj st;
	private CandidatePrj ed;
	
	private List<Link> path;
	private double pathLength;
	private double cost;
	
	
	public CandidatePath(CandidatePrj st, CandidatePrj ed, List<Link> path,
			double pathLength, double cost) {
		super();
		this.st = st;
		this.ed = ed;
		this.path = path;
		this.pathLength = pathLength;
		this.cost = cost;
	}
	public CandidatePrj getSt() {
		return st;
	}
	public void setSt(CandidatePrj st) {
		this.st = st;
	}
	public CandidatePrj getEd() {
		return ed;
	}
	public void setEd(CandidatePrj ed) {
		this.ed = ed;
	}
	public List<Link> getPath() {
		return path;
	}
	public void setPath(List<Link> path) {
		this.path = path;
	}
	public double getPathLength() {
		return pathLength;
	}
	public void setPathLength(double pathLength) {
		this.pathLength = pathLength;
	}
	public double getCost() {
		return cost;
	}
	public void setCost(double cost) {
		this.cost = cost;
	}
		
}
