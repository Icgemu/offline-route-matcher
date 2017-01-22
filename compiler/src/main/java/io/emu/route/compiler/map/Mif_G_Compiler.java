package io.emu.route.compiler.map;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.text.cql2.CQL;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * MIF-G地图编译执行类，控制具体编译过程.
 * 
 * @author Ray
 * @version 2013-10-14 下午3:11:36
 */
public class Mif_G_Compiler {

	/**
	 * 读取shape文件.
	 * 
	 * @param shapePath
	 *            shapefile路径文件
	 * @return SimpleFeatureSource
	 * @throws Exception
	 *             读取异常
	 */
	public static SimpleFeatureSource read(String shapePath) throws Exception {
		GeometryFactory geometryFactory = new GeometryFactory();
		File file = new File(shapePath);
		Map<String, Object> connect = new HashMap<String, Object>();
		connect.put("url", file.toURI().toURL());
		DataStore dataStore = DataStoreFinder.getDataStore(connect);
		String[] typeNames = dataStore.getTypeNames();
		String typeName = typeNames[0];
		SimpleFeatureSource featureSource = dataStore
				.getFeatureSource(typeName);
		return featureSource;
	}

	/**
	 * 获取网格边界节点的替换ID的K V对.
	 * 
	 * @param inNodeShapePath
	 *            node文件路径
	 * @return 保存网格边界节点替换id对应表
	 * @throws Exception
	 *             异常
	 */
	public static HashMap<Long, Long> getReplaceNodeKV(String inNodeShapePath)
			throws Exception {

		// 处理临界点号，把在不同网格，坐标相同，编号不同的归一
		HashMap<Long, Long> ajNodeKV = new HashMap<Long, Long>();

		// 定义节点集合
		FeatureCollection ncollection = null;
		FeatureIterator nIterator = null;

		// 读取节点表，获取邻接点信息
		SimpleFeatureSource featureSource = read(inNodeShapePath);
		ncollection = featureSource.getFeatures();
		nIterator = ncollection.features();

		while (nIterator.hasNext()) {
			SimpleFeature feature = ( SimpleFeature ) nIterator.next();
			// 获取节点ID
			Long id = Long.parseLong(( String ) feature.getAttribute("ID"));
			// 获取邻接点ID
			Long aid = Long.parseLong(( String ) feature
					.getAttribute("Adjoin_NID"));
			if (id < aid) {
				// 取大值做ID
				ajNodeKV.put(id, aid);
			}
		}

		featureSource.getDataStore().dispose();
		return ajNodeKV;
	}

	/**
	 * 抽取节点信息，保存为N.csv文件（nodeOutCsvPath）.
	 * @param inNodeShapePath node输入文件路径
	 * @param nodeOutCsvPath node输出文件路径
	 * @param rpNodeKV 边界nodeid对应表
	 * @throws Exception 异常
	 */
	public static void processNode(String inNodeShapePath, 
			String nodeOutCsvPath, HashMap<Long, Long> rpNodeKV)
			throws Exception {

		// 输出文件
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
				nodeOutCsvPath)));
		// N 表的shapefile
		SimpleFeatureSource nodeFeatureSource = read(inNodeShapePath);

		// 节点集合
		FeatureCollection ncollection = null;
		FeatureIterator nIterator = null;

		ncollection = nodeFeatureSource.getFeatures();
		nIterator = ncollection.features();

		while (nIterator.hasNext()) {
			SimpleFeature feature = ( SimpleFeature ) nIterator.next();
			// 获取节点ID
			Long id = Long.parseLong(( String ) feature.getAttribute("ID"));
			String geometry = ( String ) feature.getDefaultGeometry().toString();
			// 如果为边界节点，统一id
			if (rpNodeKV.containsKey(id)) {
				id = rpNodeKV.get(id);
			}
			out.println(id + ":" + geometry);
		}
		out.close();
		nodeFeatureSource.getDataStore().dispose();
	}

	/**
	 * 抽取道路信息.
	 * @param inLinkShapePath link输入数据路径
	 * @param outLinkCsvPath link输出数据路径
	 * @param rpNodeKV 边界nodeid对应表
	 * @throws Exception 异常
	 */
	public static void processLink(String inLinkShapePath,
			String outLinkCsvPath, HashMap<Long, Long> rpNodeKV)
			throws Exception {
		// 根据代码设置道路宽度
		HashMap<String, String> widthKV = new HashMap<String, String>();
		widthKV.put("15", "3");
		widthKV.put("30", "7");
		widthKV.put("55", "11");
		widthKV.put("130", "15");
//		// 根据代码设置限速信息
//		HashMap<String, String> speedLimitKV = new HashMap<String, String>();
//		speedLimitKV.put("00", "100");
//		speedLimitKV.put("01", "80");
//		speedLimitKV.put("02", "60");
//		speedLimitKV.put("03", "60");
//		speedLimitKV.put("04", "40");
//		speedLimitKV.put("06", "30");
//		speedLimitKV.put("08", "30");
//		speedLimitKV.put("0a", "20");

		// 输出文件
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
				outLinkCsvPath)));
		// R 表的shapefile
		SimpleFeatureSource linkFeatureSource = read(inLinkShapePath);

		FeatureCollection linkcollection = linkFeatureSource.getFeatures();
		FeatureIterator linkIterator = linkcollection.features();

		while (linkIterator.hasNext()) {
			SimpleFeature feature = ( SimpleFeature ) linkIterator.next();
			String id = ( String ) feature.getAttribute("ID");
			String direction = ( String ) feature.getAttribute("Direction");
			String kind = ( String ) feature.getAttribute("Kind");
			String width = ( String ) feature.getAttribute("Width");
			Integer length = ( int ) (Float.parseFloat(( String ) feature
					.getAttribute("Length")) * 1000);
			String splmt = (String)feature.getAttribute("SpdLmtS2E");
			String  speed = "".equalsIgnoreCase(splmt)?(String)feature.getAttribute("SpdLmtE2S"):splmt;
			//feature.getAttribute("SpdLmtE2S");
			Integer SpdLmtS2E = ( int ) (Integer.parseInt(speed) /10);
			
			String geometry = ( String ) feature.getDefaultGeometry().toString();

//			// 去掉08道路等级,如需保留去掉此行
//			if ((kind.substring(0, 2)).equalsIgnoreCase("08")) {
//				continue;
//			}

			Long snode = Long.parseLong(( String ) feature
					.getAttribute("SnodeID"));
			Long enode = Long.parseLong(( String ) feature
					.getAttribute("EnodeID"));

			width = widthKV.get(width);

			//String speedlimit = speedLimitKV.get(kind.substring(0, 2));
			String speedlimit = SpdLmtS2E+"";
			String roadclass = "0x" + kind.substring(0, 2);
			String roadtype = "0x" + kind.substring(2, 4);
			String orientation = direction;

			// 处理邻接点拓扑
			if (rpNodeKV.containsKey(snode)) {
				snode = rpNodeKV.get(snode);
			}
			if (rpNodeKV.containsKey(enode)) {
				enode = rpNodeKV.get(enode);
			}

			// 方向为0或1表示双向路链，需要生成反向路链
			if (direction.equalsIgnoreCase("0")
					|| direction.equalsIgnoreCase("1")) {
				//orientation = "0";
				out.println(id + ":" + snode + ":" + enode + ":" + orientation
						+ ":" + roadclass + ":" + roadtype + ":" + width + ":"
						+ length + ":" + speedlimit + ":" + geometry);

				// 双向通行,计算反向ID
				//String rid = id.substring(0, 6) + "1" + id.substring(6, 11);
				String rid = "-"+id;
				//orientation = "1";
				Geometry geom = MapUtil.parseWktString(geometry);
				// 得到反向路链形状
				String reverseGeometry = geom.reverse().toString();
				out.println(rid + ":" + enode + ":" + snode + ":" + orientation
						+ ":" + roadclass + ":" + roadtype + ":" + width + ":"
						+ length + ":" + speedlimit + ":" + reverseGeometry);
			}
			// 方向为3表示画线方向与道路通行方向相反，需要反向路链形状
			if (direction.equalsIgnoreCase("3")) {
				Geometry geom = MapUtil.parseWktString(geometry);
				// 得到反向路链形状
				String reverseGeometry = geom.reverse().toString();
				//orientation = "1";
				out.println(id + ":" + enode + ":" + snode + ":" + orientation
						+ ":" + roadclass + ":" + roadtype + ":" + width + ":"
						+ length + ":" + speedlimit + ":" + reverseGeometry);
			}

			// 方向为2表示画线方向与道路通行方向相同
			if (direction.equalsIgnoreCase("2")) {
				//orientation = "0";
				out.println(id + ":" + snode + ":" + enode + ":" + orientation
						+ ":" + roadclass + ":" + roadtype + ":" + width + ":"
						+ length + ":" + speedlimit + ":" + geometry);
			}
		}
		out.close();
		linkFeatureSource.getDataStore().dispose();
	}

	/**
	 * 小网格化地图.
	 * @param inLinkShapePath link输入数据路径
	 * @param cellOutCsvPath 小网格输出文件路径
	 * @throws Exception 异常
	 */
	public static void tileGrid(String inLinkShapePath, String cellOutCsvPath)
			throws Exception {
		tileGrid(inLinkShapePath, cellOutCsvPath, MapUtil.DEFAULT_CELL_SIZE, 1);
	}

	/**
	 * 按网格切割地图，生成各小网格包含的路链id集合.
	 * @param inLinkShapePath link输入数据路径
	 * @param cellOutCsvPath 小网格输出路径
	 * @param csize 小网格边长
	 * @param cellType 小网格类型，1表示只保存路链id，2表示保存路链信息
	 * @throws Exception 异常
	 */
	public static void tileGrid(String inLinkShapePath, String cellOutCsvPath,
			int csize, int cellType) throws Exception {
		// 保存网格号
		HashMap<String, Integer> gridMap = new HashMap<String, Integer>();
		// 输出文件
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
				cellOutCsvPath)));
		// R表的shapefile
		SimpleFeatureSource linkFeatureSource = read(inLinkShapePath);

		// 循环获取网格号集合
		FeatureCollection collection = linkFeatureSource.getFeatures();
		FeatureIterator iterator = collection.features();
		while (iterator.hasNext()) {
			SimpleFeature feature = ( SimpleFeature ) iterator.next();
			String gridID = ( String ) feature.getAttribute("MapID");
			if (!gridMap.containsKey(gridID)) {
				gridMap.put(gridID, Integer.parseInt(gridID));
			}
		}

		// 按网格集合循环选择link
		Iterator itor = gridMap.entrySet().iterator();
		while (itor.hasNext()) {
			Map.Entry<String, Integer> entry = ( Map.Entry<String, Integer> ) itor
					.next();
			int gridNo = entry.getValue();
			// 生成小网格信息
			HashMap<String, StringBuffer> cellKV = tileCell(gridNo,
					linkFeatureSource, csize, cellType);

			Iterator iteratorCkv = cellKV.entrySet().iterator();
			while (iteratorCkv.hasNext()) {
				Map.Entry<String, StringBuffer> e = ( Map.Entry<String, StringBuffer> ) iteratorCkv
						.next();
				out.println(e.getKey() + ":" + e.getValue());
			}
		}
		out.close();
		linkFeatureSource.getDataStore().dispose();
	}

	/**
	 * 生成小网格包含的路链集合.
	 * @param gridNo 网格号           
	 * @param linkFeatureSource link的shape  
	 * @param cellSize 小网格边长
	 * @param cellType 小网格类型，1表示只保存路链id，2表示保存路链信息        
	 * @throws Exception 异常
	 * @return HashMap<String, StringBuffer> 小网格信息
	 */
	private static HashMap<String, StringBuffer> tileCell(Integer gridNo,
			SimpleFeatureSource linkFeatureSource, int cellSize, int cellType)
			throws Exception {

		HashMap<String, StringBuffer> outCellKV = new HashMap<String, StringBuffer>();
		// 保存小网格范围
		HashMap<String, Polygon> cellPolygonKV = new HashMap<String, Polygon>();

		Envelope ev = MapUtil.getGridBound(gridNo);
		double minx = ev.getMinX();
		double miny = ev.getMinY();
		double maxx = ev.getMaxX();
		double maxy = ev.getMaxY();

		double deltax = MapUtil.calPointDistance(minx, miny, maxx, miny);
		double deltay = MapUtil.calPointDistance(minx, miny, minx, maxy);

		int xsize = ( int ) Math.floor(deltax / cellSize);
		long ysize = ( int ) Math.floor(deltay / cellSize);

		double spanx = maxx - minx;
		double spany = maxy - miny;

		double cxdelta = spanx / xsize;
		double cydelta = spany / ysize;

		// 生成小网格范围
		setCellPolygonKV(xsize, ysize, minx, cxdelta, cydelta, miny, cellPolygonKV, gridNo);

		// 参与计算的范围
		double bminx = minx - 0.0002;
		double bmaxx = maxx + 0.0002;
		double bminy = miny - 0.0002;
		double bmaxy = maxy + 0.0002;

		// 得到大网格覆盖范围包含路链信息
		FeatureCollection collection = linkFeatureSource.getFeatures(CQL
				.toFilter("BBOX(the_geom," + bminx + "," + bminy + "," + bmaxx
						+ "," + bmaxy + ")"));
		FeatureIterator iterator = collection.features();

		while (iterator.hasNext()) {
			SimpleFeature feature = ( SimpleFeature ) iterator.next();

			String kind = ( String ) feature.getAttribute("Kind");

//			if ((kind.substring(0, 2)).equalsIgnoreCase("08")) {
//				// 去掉08道路等级,如需保留去掉此行
//				continue;
//			}

			// 获取路链坐标
			Geometry geom = ( Geometry ) feature.getDefaultGeometry();

			Iterator cellPolygonIterator = cellPolygonKV.entrySet().iterator();
			while (cellPolygonIterator.hasNext()) {

				Map.Entry<String, Polygon> entry = ( Map.Entry<String, Polygon> ) cellPolygonIterator
						.next();
				String id = entry.getKey();
				Polygon polygon = entry.getValue();

				// 判断link是否与小网格范围相交
				if (geom.intersects(polygon)) {
					Geometry gs = geom.intersection(polygon);
					if (outCellKV.containsKey(id)) {
						StringBuffer sb = outCellKV.get(id);
						String linkid = ( String ) feature.getAttribute("ID");
						String direction = ( String ) feature
								.getAttribute("Direction");

						//保存路链到cell中
						saveLink2Cell(cellType, linkid, sb, direction, gs);

					} else {
						StringBuffer sb = new StringBuffer();
						String linkid = ( String ) feature.getAttribute("ID");
						String direction = ( String ) feature
								.getAttribute("Direction");

						//保存路链到cell中
						saveLink2Cell(cellType, linkid, sb, direction, gs);
						outCellKV.put(id, sb);
					}
				}
			}
		}
		return outCellKV;
	}

	/** 
	 * 保存路链到cell中.
	 * @param cellType 小网格类型
	 * @param linkid linkid
	 * @param sb 路链id集合
	 * @param direction 方向
	 * @param gs 点序
	 */
	private static void saveLink2Cell(int cellType, String linkid, StringBuffer sb, String direction, Geometry gs) {
		// TODO Auto-generated method stub
		// cellType表示两种保存方式，1表示只保存linkid，2表示保存link信息
		if (cellType == 1) {
			// cell1
			sb.append(",").append(linkid);
			if (direction.equalsIgnoreCase("0")
					|| direction.equalsIgnoreCase("1")) {
//				sb.append(",").append(
//						linkid.substring(0, 6) + "1"
//								+ linkid.substring(6, 11));
				sb.append(",").append("-"+linkid);
			}
		} else if (cellType == 2) {
			// cell2
			sb.append(":").append(linkid).append("_")
					.append(gs.toString());
			if (direction.equalsIgnoreCase("0")
					|| direction.equalsIgnoreCase("1")) {
				sb.append(":")
//						.append(linkid.substring(0, 6) + "1"
//								+ linkid.substring(6, 11))
				.append("-"+linkid)
						.append("_")
						.append(gs.reverse().toString());
			}
		}
	}

	/** 
	 * 生成小网格范围.
	 * @param xsize xsize
	 * @param ysize ysize
	 * @param minx minx
	 * @param cxdelta cxdelta
	 * @param cydelta cydelta
	 * @param miny miny
	 * @param cellPolygonKV cellPolygonKV
	 * @param gridNo gridNo
	 */
	private static void setCellPolygonKV(int xsize, long ysize, double minx, 
			double cxdelta, double cydelta, double miny, HashMap<String, Polygon> cellPolygonKV, Integer gridNo) {
		// TODO Auto-generated method stub
		for (int m = 0; m < xsize; m++) {
			for (int n = 0; n < ysize; n++) {
				double cminx = minx + cxdelta * m - cxdelta*60.0/MapUtil.DEFAULT_CELL_SIZE;
				double cminy = miny + cydelta * n - cydelta*60.0/MapUtil.DEFAULT_CELL_SIZE;
				double cmaxx = minx + cxdelta * (m + 1) + cxdelta*60.0/MapUtil.DEFAULT_CELL_SIZE;
				double cmaxy = miny + cydelta * (n + 1) + cydelta*60.0/MapUtil.DEFAULT_CELL_SIZE;
				Coordinate[] points = new Coordinate[] {
						new Coordinate(cminx, cminy),
						new Coordinate(cmaxx, cminy),
						new Coordinate(cmaxx, cmaxy),
						new Coordinate(cminx, cmaxy),
						new Coordinate(cminx, cminy), };

				LinearRing linearRing = new LinearRing(points,
						new PrecisionModel(), 4326);
				Polygon polygon = new Polygon(linearRing, new PrecisionModel(),
						4326);
				cellPolygonKV.put(gridNo + "_" + m + "_" + n, polygon);
				// System.out.println(gridNo+"_"+m+"_"+n+" : "+polygon);
			}
		}
	}

	/**
	 * 静态方法，控制地图编译流程.
	 * 
	 * @param inNodeShapePath
	 *            N表路径
	 * @param inLinkShapePath
	 *            R表路径
	 * @param nodeOutCsvPath
	 *            N输出路径
	 * @param outLinkCsvPath
	 *            R输出路径
	 * @param neo4jDbPath
	 *            拓扑输出路径
	 * @param cellOutCsvPath
	 *            小网格输出路径
	 */
	public static void processMap(String inNodeShapePath,
			String inLinkShapePath, String nodeOutCsvPath,
			String outLinkCsvPath, String cellOutCsvPath) {
		try {
			// 解析node
			HashMap<Long, Long> kv = getReplaceNodeKV(inNodeShapePath);
			processNode(inNodeShapePath, nodeOutCsvPath, kv);
			// 解析link
			processLink(inLinkShapePath, outLinkCsvPath, kv);
			// 生成cell
			tileGrid(inLinkShapePath, cellOutCsvPath);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 单省份编译入口.
	 * TODO(功能描述)
	 * @param args 参数
	 */
	public static void main(String[] args) {
		try {
			// HashMap<Long,Long>
			// kv=getReplaceNodeKV("/home/hadoop/Downloads/shp/N.shp");
			// processNode("/home/hadoop/Downloads/shp/N.shp","/home/hadoop/Downloads/shp/N.csv",kv);
			// processLink("/home/hadoop/Downloads/shp/R.shp","/home/hadoop/Downloads/shp/R.csv",kv);
			// processRoadTopology("/home/hadoop/Downloads/shp/R.csv","/home/hadoop/Downloads/shp/T.csv");
			// tileGrid("/home/hadoop/Downloads/shp/R.shp","/home/hadoop/Downloads/shp/C.csv");

			processMap(
					"F:/beijing/level2/beijing/road/Nbeijing.shp",
					"F:/beijing/level2/beijing/road/Rbeijing.shp",
					"E:/Prj/OD/test/N-G.csv",
					"E:/Prj/OD/test/R-G.csv",
					"E:/Prj/OD/test/C-60-G.csv");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
