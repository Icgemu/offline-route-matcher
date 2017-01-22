package io.emu.route.matcher.compiler.neo4j;

import io.emu.route.matcher.compiler.map.Link;
import io.emu.route.matcher.compiler.map.ParseUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URISyntaxException;
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

/**
 * noe4j工具类，实现将解析得到的node、link信息导入neo4j，建立路网.
 * 
 * @author Ray
 * @version 2013-10-14 下午3:22:32
 */
public class DirectImport2Neo4J {

	/**
	 * 连接neo4j写入node.
	 * @param nodes node数据集合
	 * @param neo4jDbPath neo4j数据库路径
	 */
	private static void saveNode(ArrayList<io.emu.route.matcher.compiler.map.Node> nodes,
			String neo4jDbPath) {

		HashMap<String, String> configuration = new HashMap<String, String>();
		//数据库保存参数
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
//		new EmbeddedGraphDatabase(neo4jDbPath,
//				configuration);

		IndexManager index = graphDb.index();
		// 以nodeid作为索引
		Index<Node> nodeIndex = index.forNodes("nodeid");
		Transaction tx = graphDb.beginTx();
		try {
			for (int i = 0; i < nodes.size(); i++) {
				io.emu.route.matcher.compiler.map.Node cnNode = nodes.get(i);
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
	 * @param nodeCsvPath node信息保存文件
	 * @param neo4jDbPath neo4j 数据库路径
	 * @throws Exception 异常
	 */
	public static synchronized void importNode(String nodeCsvPath,
			String neo4jDbPath) throws Exception {
		ArrayList<io.emu.route.matcher.compiler.map.Node> nodes = new ArrayList<io.emu.route.matcher.compiler.map.Node>();
		// 用于去除重复的node
		//HashMap<String, String> nkv = new HashMap<String, String>();
		File file = new File(nodeCsvPath);
		BufferedReader bf = new BufferedReader(new FileReader(file));
		String line = null;
		while ((line = bf.readLine()) != null) {
			io.emu.route.matcher.compiler.map.Node cnNode = ParseUtil.parseNodeLine(line);
			// 去除重复点
			//if (!nkv.containsKey(cnNode.getId())) {
				nodes.add(cnNode);
				//nkv.put(cnNode.getId(), cnNode.getId());
			//}
			if(nodes.size()%10000 == 0){
				saveNode(nodes, neo4jDbPath);
				nodes.clear();
			}
		}
		// 将Node写入neo4j
		saveNode(nodes, neo4jDbPath);
		;
		bf.close();
	}

	/**
	 * 连接neo4j写入link.
	 * @param links link数据集合
	 * @param neo4jDbPath neo4j数据库路径
	 */
	private static void saveLink(ArrayList<Link> links, String neo4jDbPath) {
		HashMap<String, String> configuration = new HashMap<String, String>();
		//数据库链接参数
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
				road.setProperty("speedlimit", link.getSpeedlimit());
				road.setProperty("direction", link.getDirection());
				road.setProperty("roadtype", link.getRoadtype());
				road.setProperty("roadclass", link.getRoadclass());

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
	 * @param linkCsvPath link写入文件路径
	 * @param neo4jDbPath neo4j数据库路径
	 * @throws Exception 异常
	 */
	public static synchronized void importLink(String linkCsvPath,
			String neo4jDbPath) throws Exception {
		ArrayList<Link> links = new ArrayList<Link>();
		// 用以去除重复link
		//HashMap<String, String> rkv = new HashMap<String, String>();
		BufferedReader bf = new BufferedReader(new FileReader(new File(
				linkCsvPath)));
		String line = null;

		while ((line = bf.readLine()) != null) {
			Link link = ParseUtil.parseLinkLine(line);
			// 去除重复link
			//if (!rkv.containsKey(link.getId())) {
				links.add(link);
				//rkv.put(link.getId(), link.getId());
			//}
			if(links.size()%10000 ==0){
				saveLink(links, neo4jDbPath);
				links.clear();
			}
		}
		// 将link写入neo4j
		saveLink(links, neo4jDbPath);
		bf.close();
	}

	/**
	 * 分省份保存后的csv文件合成一个大文件，调用import接口在neo4j中生成全国路网拓扑.
	 * 
	 * @param args 参数
	 * @throws URISyntaxException URISyntaxException
	 */
	public static void main(String[] args) throws URISyntaxException {

		/*****************************************************/

		try {
//			importNode("/home/hadoop/Downloads/csv1.3/N.csv",
//					"/home/hadoop/Downloads/csv1.3/neo4j-db");
			importNode("E:/Prj/OD/test/uN-G.csv",
			"E:/Prj/OD/test/neo4j-db-G/");
			importLink("E:/Prj/OD/test/R-G.csv",
					"E:/Prj/OD/test/neo4j-db-G/");
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		/*****************************************************/

		// GraphDatabaseService graphDb = new
		// EmbeddedGraphDatabase("D:/neo4j-db" );
		// IndexManager index = graphDb.index();
		// Index<Node> nodeIndex = index.forNodes( "nodeid" );
		// RelationshipIndex relationIndex = index.forRelationships( "linkid" );
		// Transaction tx = graphDb.beginTx();
		// try{
		// Node node = nodeIndex.get("nodeid", 58614410102l).getSingle();
		// System.out.println(node.getProperty("nodeid"));
		// tx.success();
		// }catch (Exception e) {
		// e.printStackTrace();
		// tx.failure();
		// }finally{
		// tx.finish();
		// }
		// graphDb.shutdown();
	}

}
