package io.emu.route.util;

import java.io.Serializable;
import java.util.ArrayList;

public class Cell implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;

	private ArrayList<String> linkIDsInCell;

	public Cell(String cellid, ArrayList<String> linkIDsInCell) {
		super();
		this.id = cellid;
		this.linkIDsInCell = linkIDsInCell;
	}

	public String getId() {
		return id;
	}

	public ArrayList<String> getLinkIDsInCell() {
		return linkIDsInCell;
	}

}
