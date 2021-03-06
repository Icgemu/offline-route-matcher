package io.emu.route.util;

import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.datum.DefaultEllipsoid;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.operation.distance.DistanceOp;

public class MapUtil {
	/**
	 * 小网格边长，60m.
	 */
	public final static int DEFAULT_CELL_SIZE = 60;
	public final static GeometryFactory  GEO_FACTORY = new GeometryFactory(new PrecisionModel(),4326);

	/**
	 * 按经纬度坐标查找该点所在的二级网格.
	 * 
	 * @param lon
	 *            经度
	 * @param lat
	 *            维度
	 * @return int 网格号
	 */
	public static int findGrid(double lon, double lat) {
		int xx = (int) (lon - 60);
		int yy = (int) (lat * 60 / 40);
		int x = (int) ((lon - (int) lon) / 0.125);
		int y = (int) (((lat * 60 / 40 - yy)) / 0.125);
		return yy * 10000 + xx * 100 + y * 10 + x;

	}

	/**
	 * 按二级网格号获取二级网格的坐标范围.
	 * 
	 * @param gridNo
	 *            网格号
	 * @return Envelope
	 */
	public static Envelope getGridBound(int gridNo) {

		int yy = (int) (gridNo / 10000);
		int xx = (int) ((gridNo - yy * 10000) / 100);
		int y = (int) ((gridNo - yy * 10000 - xx * 100) / 10);
		int x = (int) ((gridNo - yy * 10000 - xx * 100 - y * 10));

		double miny = yy * 40.0 / 60 + y * 5.0 / 60;
		double maxy = yy * 40.0 / 60 + (y + 1) * 5.0 / 60;

		double minx = xx + 60 + x * 7.5 / 60;
		double maxx = xx + 60 + (x + 1) * 7.5 / 60;

		return new Envelope(minx, maxx, miny, maxy);
	}

	/**
	 * 按经纬度坐标查找点所在的二级网格和小网格.
	 * 
	 * @param lon
	 *            经度
	 * @param lat
	 *            维度
	 * @param cellSize
	 *            小网格边长
	 * @return String 小网格id
	 */
	public static String findCell(double lon, double lat, int cellSize) {
		int gridNo = findGrid(lon, lat);
		Envelope ev = getGridBound(gridNo);

		double minx = ev.getMinX();
		double miny = ev.getMinY();
		double maxx = ev.getMaxX();
		double maxy = ev.getMaxY();

		double xdelta = calPointDistance(minx, miny, maxx, miny);
		double ydelta = calPointDistance(minx, miny, minx, maxy);

		long xCellSize = Math.round(xdelta / cellSize);
		long yCellSize = Math.round(ydelta / cellSize);

		double spanx = maxx - minx;
		double spany = maxy - miny;

		double xCellDelta = spanx / xCellSize;
		double yCellDelta = spany / yCellSize;

		int col = (int) ((lon - minx) / xCellDelta);
		int row = (int) ((lat - miny) / yCellDelta);

		return gridNo + "_" + col + "_" + row;

	}

	/**
	 * 根据经纬度得到小网格id.
	 * 
	 * @param lon
	 *            经度
	 * @param lat
	 *            维度
	 * @return 小网格id
	 */
	public static String findCell(double lon, double lat) {
		return findCell(lon, lat, DEFAULT_CELL_SIZE);
	}

	/**
	 * 根据小网格id得到其范围.
	 * 
	 * @param cellID
	 *            小网格id
	 * @return Envelope
	 */
	public static Envelope getCellBound(String cellID) {
		return getCellBound(cellID, DEFAULT_CELL_SIZE);
	}

	/**
	 * 获取网格中每个小网格的范围.
	 * 
	 * @param cellID
	 *            小网格id
	 * @param cellSize
	 *            小网格边长
	 * @return Envelope
	 */
	public static Envelope getCellBound(String cellID, int cellSize) {

		String[] s = cellID.split("_");
		int gridNo = Integer.parseInt(s[0]);
		int row = Integer.parseInt(s[1]);
		int col = Integer.parseInt(s[2]);

		Envelope ev = getGridBound(gridNo);

		double minx = ev.getMinX();
		double miny = ev.getMinY();
		double maxx = ev.getMaxX();
		double maxy = ev.getMaxY();

		double deltax = calPointDistance(minx, miny, maxx, miny);
		double deltay = calPointDistance(minx, miny, minx, maxy);

		int xsize = (int) Math.floor(deltax / cellSize);
		long ysize = (int) Math.floor(deltay / cellSize);

		double spanx = maxx - minx;
		double spany = maxy - miny;

		double cxdelta = spanx / xsize;
		double cydelta = spany / ysize;

		double cminx = minx + cxdelta * row;
		double cminy = miny + cydelta * col;
		double cmaxx = minx + cxdelta * (row + 1);
		double cmaxy = miny + cydelta * (col + 1);

		Envelope env = new Envelope(cminx, cmaxx, cminy, cmaxy);
		return env;
	}

	/**
	 * 获取方位角 方位角范围为-180< azimuth <=180 求两点之间的方位角.
	 * 
	 * @param sPoint
	 *            start point
	 * @param ePoint
	 *            end point
	 * @return double 方向角
	 */
	public static double azimuth(Coordinate sPoint, Coordinate ePoint) {
		return azimuth(sPoint.x, sPoint.y, ePoint.x, ePoint.y);
	}

	/**
	 * 获取方位角 方位角范围为-180< azimuth <=180 求两点之间的方位角.
	 * 
	 * @param lon1
	 *            前点经度
	 * @param lat1
	 *            前点维度
	 * @param lon2
	 *            后点经度
	 * @param lat2
	 *            后点维度
	 * @return double
	 */
	public static double azimuth(double lon1, double lat1, double lon2,
			double lat2) {
		GeodeticCalculator azimuth = new GeodeticCalculator(
				DefaultEllipsoid.WGS84);
		azimuth.setStartingGeographicPoint(lon1, lat1);
		azimuth.setDestinationGeographicPoint(lon2, lat2);
		return azimuth.getAzimuth();
	}

	/**
	 * 两点之间的球面距离.
	 * 
	 * @param sp
	 *            Coordinate
	 * @param ep
	 *            Coordinate
	 * @return double 距离
	 */
	public static double calPointDistance(Coordinate sp, Coordinate ep) {
		return calPointDistance(sp.x, sp.y, ep.x, ep.y);
	}

	/**
	 * 两点之间的球面距离.
	 * 
	 * @param lng1
	 *            前点经度
	 * @param lat1
	 *            前点维度
	 * @param lng2
	 *            后点经度
	 * @param lat2
	 *            后点维度
	 * @return double
	 */
	public static double calPointDistance(double lng1, double lat1,
			double lng2, double lat2) {
		GeodeticCalculator az = new GeodeticCalculator(DefaultEllipsoid.WGS84);
		az.setStartingGeographicPoint(lng1, lat1);
		az.setDestinationGeographicPoint(lng2, lat2);
		return az.getOrthodromicDistance();
	}

	/**
	 * 点到线段的最近点.
	 * 
	 * @param p
	 *            Coordinate
	 * @param l
	 *            LineSegment
	 * @return Coordinate
	 */
	public static Coordinate closestPoint2LineSegment(Coordinate p,
			LineSegment l) {
		return l.closestPoint(p);
	}

	/**
	 * JTS计算的两个几何体的最近点,返回后面那几何体到最近点.
	 * 
	 * @param p
	 *            Geometry
	 * @param l
	 *            Geometry
	 * @return Coordinate
	 */
	public static Coordinate closestPoint(Geometry p, Geometry l) {
		Coordinate clsPoint = null;
		clsPoint = DistanceOp.nearestPoints(p, l)[1];
		return clsPoint;
	}

	/**
	 * JTS计算的点到折线的最近点.
	 * 
	 * @param p
	 *            Coordinate
	 * @param l
	 *            LineString
	 * @return Coordinate
	 */
	public static Coordinate closestPoint2LineString(Coordinate p, LineString l) {
		Coordinate clsPoint = null;		
		clsPoint = DistanceOp.nearestPoints( GEO_FACTORY.createPoint(p), l)[1];
		return clsPoint;
	}

	/**
	 * JTS距离计算的点到折线的最近点.
	 * 
	 * @param p
	 *            Coordinate
	 * @param ml
	 *            MultiLineString
	 * @return Coordinate
	 */
	public static Coordinate closestPoint2MultiLineString(Coordinate p,
			MultiLineString ml) {
		Coordinate clsPoint = null;
		clsPoint = DistanceOp.nearestPoints(GEO_FACTORY.createPoint(p), ml)[1];
		return clsPoint;
	}

	/**
	 * 解析数据，得到点序形状.
	 * 
	 * @param wktString
	 *            点序
	 * @return Geometry 形状
	 * @throws Exception
	 *             异常
	 */
	public static Geometry parseWktString(String wktString) throws Exception {
		int srid = 4326;
		GeometryFactory factory = new GeometryFactory(new PrecisionModel(),
				srid);
		WKTReader reader = new WKTReader(factory);
		Geometry geom = reader.read(wktString);
		return geom;
	}

	/**
	 * 生成point对象.
	 * 
	 * @param lon
	 *            经度
	 * @param lat
	 *            维度
	 * @return Point
	 */
	public static Point getPoint(double lon, double lat) {
		Coordinate p = new Coordinate(lon, lat);
		return GEO_FACTORY.createPoint(p);
	}

	/**
	 * 生成point对象.
	 * 
	 * @param p
	 *            Coordinate
	 * @return Point
	 */
	public static Point getPoint(Coordinate p) {
		return GEO_FACTORY.createPoint(p);
	}
}
