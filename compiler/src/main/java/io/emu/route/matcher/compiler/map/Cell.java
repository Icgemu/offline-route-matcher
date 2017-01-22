package io.emu.route.matcher.compiler.map;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 小网格 60*60m,只存储和小网格相交的路链ID，不存储和小网格交叉端的几何形体.
 * 
 * @author Mark
 * @version 2013-10-14 下午4:55:48
 */
public class Cell implements Serializable {
    /**
	 * 小网格ID号，二次网格ID+”_“+行号+”_“+列号.
	 */
	private String cellid;

	/**
	 * 二次网格内的路链ID.
	 */
	private ArrayList<String> cellLinkIDs;

	/**
	 * 构造函数.
	 * @param cellid cellid
	 * @param cellLinkIDs cell包括的linkid集合
	 */
	public Cell(String cellid, ArrayList<String> cellLinkIDs) {
		super();
		this.cellid = cellid;
		this.cellLinkIDs = cellLinkIDs;
	}

	/**
	 * 得到cellid.
	 * @return cellid
	 */
	public String getCellid() {
		return cellid;
	}

	/**
	 * 得到cell包含路链集合.
	 * @return 路链id集合
	 */
	public ArrayList<String> getCellLinkIDs() {
		return cellLinkIDs;
	}

}
