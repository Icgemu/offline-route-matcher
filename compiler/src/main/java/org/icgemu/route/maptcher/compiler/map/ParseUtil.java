package org.icgemu.route.maptcher.compiler.map;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;

/**
 * 数据解析工具类，实现解析csv中的node、line信息，生成相应对象.
 * 
 * @author Ray
 * @version 2013-10-14 下午3:48:58
 */
public class ParseUtil {

	/**
	 * 解析link数据，生成Node对象.
	 * 
	 * @param line 数据
	 * @return Link
	 * @throws Exception 异常
	 */
	public static Link parseLinkLine(String line) throws Exception {
		String[] s = line.split(":");

		Long id = Long.parseLong(s[0]);

		Long snodeid = Long.parseLong(s[1]);
		Long enodeid = Long.parseLong(s[2]);

		int orientation = Integer.parseInt(s[3]);
		String roadclass = s[4];
		String roadtype = s[5];

		int width = Integer.parseInt(s[6]);
		int length = Integer.parseInt(s[7]);
		int speedLimit = Integer.parseInt(s[8]);

		Geometry geometry = MapUtil.parseWktString(s[9]);
		LineString ln = null;

		if (geometry.getGeometryType().equalsIgnoreCase("MultiLineString")) {
			MultiLineString mln = ( MultiLineString ) geometry;
			ln = ( LineString ) mln.getGeometryN(0);
		}

		return new Link(id + "", snodeid + "", enodeid + "", orientation,
				roadclass, roadtype, width, length, speedLimit, ln);
	}

	/**
	 * 解析node数据，生成Node对象.
	 * 
	 * @param line 数据
	 * @return Node
	 * @throws Exception 异常
	 */
	public static Node parseNodeLine(String line) throws Exception {
		String[] s = line.split(":");
		Long id = Long.parseLong(s[0]);
		Geometry geometry = MapUtil.parseWktString(s[1]);
		Point point = ( Point ) geometry;
		return new Node(id + "", point);
	}

	/**
	 * 解析cell数据，生成Cell对象.
	 * 
	 * @param line 数据
	 * @return Cell
	 * @throws Exception 异常
	 */
	public static Cell parseCellLine(String line) throws Exception {
		String[] s = line.split(":");
		String cellid = s[0];
		String[] sids = s[1].split(",");
		ArrayList<String> cellIDs = new ArrayList<String>();

		for (int i = 0; i < sids.length; i++) {
			cellIDs.add(sids[i]);
		}

		return new Cell(cellid, cellIDs);
	}

}
