package io.emu.route.matcher.pojo;

import com.vividsolutions.jts.geom.LineString;

public class Link {

	private String id;

	private boolean isDual;

	private Long snodeID;

	private Long enodeID;

	private Integer length;

	private LineString line;

	public Link() {
	}

	// public Link(String id, LineString line) {
	// super();
	// this.id = id;
	// this.line = line;
	// }

	public Link(String id, boolean isDual, Long snodeID, Long enodeID,
			Integer length, LineString line) {
		super();
		this.id = id;
		this.isDual = isDual;
		this.snodeID = snodeID;
		this.enodeID = enodeID;
		this.length = length;
		this.line = line;
	}

	public String getId() {
		return id;
	}

	public LineString getLine() {
		return line;
	}

	public Long getSnodeID() {
		return snodeID;
	}

	public Long getEnodeID() {
		return enodeID;
	}

	public Integer getLength() {
		return length;
	}

	public boolean isDual() {
		return isDual;
	}

}
