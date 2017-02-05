package io.emu.route.compiler.neo4j;

import io.emu.route.util.Link;
import io.emu.route.util.MapUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.index.RelationshipIndex;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;

public class DirectImport2Neo4J {

	private void saveNode(
			ArrayList<io.emu.route.compiler.map.Node> nodes, String neo4jDbPath) {

		HashMap<String, String> configuration = new HashMap<String, String>();
		// 数据库保存参数
		configuration.put("use_memory_mapped_buffers", "true");
		configuration.put("neostore.nodestore.db.mapped_memory", "64M");
		configuration
				.put("neostore.relationshipstore.db.mapped_memory", "256M");
		configuration.put("neostore.propertystore.db.mapped_memory", "64M");
		configuration.put("neostore.propertystore.db.strings.mapped_memory",
				"64M");
		configuration.put("neostore.propertystore.db.arrays.mapped_memory",
				"64M");
		configuration.put("node_cache_size", "64M");
		configuration.put("relationship_cache_size", "256M");
		GraphDatabaseService graphDb = new GraphDatabaseFactory()
				.newEmbeddedDatabaseBuilder(new File(neo4jDbPath))
				.setConfig(configuration)
				.newGraphDatabase();
		// new EmbeddedGraphDatabase(neo4jDbPath,
		// configuration);

		IndexManager index = graphDb.index();
		// 以nodeid作为索引
		Index<Node> nodeIndex = index.forNodes("nodeid");
		Transaction tx = graphDb.beginTx();
		try {
			for (int i = 0; i < nodes.size(); i++) {
				io.emu.route.compiler.map.Node cnNode = nodes.get(i);
				Node node = graphDb.createNode();
				node.setProperty("nodeid", cnNode.getId());
				node.setProperty("lat", cnNode.getLat());
				node.setProperty("lon", cnNode.getLon());
				nodeIndex.add(node, "nodeid", node.getProperty("nodeid"));
			}
			tx.success();
		} finally {
			tx.close();
		}
		graphDb.shutdown();
	}

	/**
	 * 将解析得到的node信息导入neo4j.
	 * 
	 * @param nodeCsvPath
	 *            node信息保存文件
	 * @param neo4jDbPath
	 *            neo4j 数据库路径
	 * @throws Exception
	 *             异常
	 */
	public  synchronized void importNode(String nodeCsvPath,
			String neo4jDbPath) throws Exception {
		ArrayList<io.emu.route.compiler.map.Node> nodes = new ArrayList<io.emu.route.compiler.map.Node>();
		// 用于去除重复的node
		// HashMap<String, String> nkv = new HashMap<String, String>();
		File file = new File(nodeCsvPath);
		BufferedReader bf = new BufferedReader(new FileReader(file));
		String line = null;
		while ((line = bf.readLine()) != null) {
			io.emu.route.compiler.map.Node cnNode = parseNodeLine(line);
			// 去除重复点
			// if (!nkv.containsKey(cnNode.getId())) {
			nodes.add(cnNode);
			// nkv.put(cnNode.getId(), cnNode.getId());
			// }
			if (nodes.size() % 10000 == 0) {
				saveNode(nodes, neo4jDbPath);
				nodes.clear();
			}
		}
		// 将Node写入neo4j
		saveNode(nodes, neo4jDbPath);

		bf.close();
	}
	
	public  io.emu.route.compiler.map.Node parseNodeLine(String line) throws Exception {
		String[] s = line.split(":");
		Long id = Long.parseLong(s[0]);
		Geometry geometry = MapUtil.parseWktString(s[1]);
		Point point = (Point) geometry;
		return new io.emu.route.compiler.map.Node(id + "", point);
	}

	/**
	 * 连接neo4j写入link.
	 * 
	 * @param links
	 *            link数据集合
	 * @param neo4jDbPath
	 *            neo4j数据库路径
	 */
	private  void saveLink(ArrayList<Link> links, String neo4jDbPath) {
		HashMap<String, String> configuration = new HashMap<String, String>();
		// 数据库链接参数
		configuration.put("use_memory_mapped_buffers", "true");
		configuration.put("neostore.nodestore.db.mapped_memory", "64M");
		configuration
				.put("neostore.relationshipstore.db.mapped_memory", "256M");
		configuration.put("neostore.propertystore.db.mapped_memory", "64M");
		configuration.put("neostore.propertystore.db.strings.mapped_memory",
				"64M");
		configuration.put("neostore.propertystore.db.arrays.mapped_memory",
				"64M");
		configuration.put("node_cache_size", "64M");
		configuration.put("relationship_cache_size", "256M");
		GraphDatabaseService graphDb = new GraphDatabaseFactory()
				.newEmbeddedDatabaseBuilder(new File(neo4jDbPath))
				.setConfig(configuration).newGraphDatabase();

		IndexManager index = graphDb.index();
		Index<Node> nodeIndex = index.forNodes("nodeid");
		// 用relation表示link，索引为linkid
		RelationshipIndex relationIndex = index.forRelationships("linkid");
		Transaction tx = graphDb.beginTx();
		try {
			for (int i = 0; i < links.size(); i++) {
				Link link = links.get(i);

				Node snode = nodeIndex.get("nodeid", link.getSnode())
						.getSingle();
				Node enode = nodeIndex.get("nodeid", link.getEnode())
						.getSingle();
				Relationship road = snode.createRelationshipTo(enode,
						RelationshipRoad.ROAD);
				road.setProperty("linkid", link.getId());
				road.setProperty("width", link.getWidth());
				road.setProperty("length", link.getLength());
				road.setProperty("speedlimit", link.getSpeedLimit());
				road.setProperty("direction", link.getDirection());
				road.setProperty("roadtype", link.getRoadType());
				road.setProperty("roadclass", link.getRoadClass());

				relationIndex.add(road, "linkid", road.getProperty("linkid"));
			}
			tx.success();

		} finally {
			tx.close();
		}
		graphDb.shutdown();
	}

	/**
	 * 将解析得到的link信息导入neo4j.
	 * 
	 * @param linkCsvPath
	 *            link写入文件路径
	 * @param neo4jDbPath
	 *            neo4j数据库路径
	 * @throws Exception
	 *             异常
	 */
	public  synchronized void importLink(String linkCsvPath,
			String neo4jDbPath) throws Exception {
		ArrayList<Link> links = new ArrayList<Link>();
		// 用以去除重复link
		// HashMap<String, String> rkv = new HashMap<String, String>();
		BufferedReader bf = new BufferedReader(new FileReader(new File(
				linkCsvPath)));
		String line = null;

		while ((line = bf.readLine()) != null) {
			Link link = parseLinkLine(line);
			// 去除重复link
			// if (!rkv.containsKey(link.getId())) {
			links.add(link);
			// rkv.put(link.getId(), link.getId());
			// }
			if (links.size() % 10000 == 0) {
				saveLink(links, neo4jDbPath);
				links.clear();
			}
		}
		// 将link写入neo4j
		saveLink(links, neo4jDbPath);
		bf.close();
	}
	
	public  Link parseLinkLine(String line) throws Exception {
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
			MultiLineString mln = (MultiLineString) geometry;
			ln = (LineString) mln.getGeometryN(0);
		}

		return new Link(id + "", snodeid + "", enodeid + "", width, length,
				speedLimit, orientation, roadclass, roadtype, ln);
	}

	public static void main(String[] args) throws Exception {

		String nCsv = args[1];
		String rCsv = args[2];
		String nodedb_dir = args[3];

		DirectImport2Neo4J doer = new DirectImport2Neo4J();
		doer.importNode(nCsv, nodedb_dir);
		doer.importLink(rCsv, nodedb_dir);

	}

}
