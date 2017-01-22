package io.emu.route.matcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;

public class ImportCSV {

	public static void importNode(String csvpath, String dbpath)
			throws IOException {
		GraphDatabaseService graphDb = new GraphDatabaseFactory()
				.newEmbeddedDatabase(new File(dbpath));
		BufferedReader br = null;
		String line = "";
		Transaction tx = graphDb.beginTx();
		try {
			Index<Node> nodeIndex = graphDb.index().forNodes("NodeIdIndex");
			long i = 0;
			br = new BufferedReader(new FileReader(csvpath));
			while ((line = br.readLine()) != null) {
				String[] s = line.split("@");
				String[] ss = s[1].split(",");
				++i;

				System.out.println(s[0] + ":" + i);
				Long nodeid = Long.parseLong(s[0]);
				Double x = Double.parseDouble(ss[0]);
				Double y = Double.parseDouble(ss[1]);

				Node node = graphDb.createNode();
				node.setProperty("id", nodeid);
				node.setProperty("x", x);
				node.setProperty("y", y);
				nodeIndex.add(node, "id", nodeid);

				node = nodeIndex.get("id", nodeid).getSingle();
				System.out.println(node.getProperty("id") + "_"
						+ node.getProperty("x") + "_" + node.getProperty("y"));

			}
			tx.success();
		} finally {

			tx.close();
			graphDb.shutdown();
			br.close();
		}
	}

	public static void importLink(String csvpath, String dbpath)
			throws IOException {
		GraphDatabaseService graphDb = new GraphDatabaseFactory()
				.newEmbeddedDatabase(new File(dbpath));
		BufferedReader br = null;
		String line = "";
		Transaction tx = graphDb.beginTx();
		try {
			Index<Node> nodeIndex = graphDb.index().forNodes("NodeIdIndex");
			Index<Relationship> relationshipIndex = graphDb.index()
					.forRelationships("LinkIdIndex");
			long i = 0;
			br = new BufferedReader(new FileReader(csvpath));
			while ((line = br.readLine()) != null) {
				String[] s = line.split("@");
				++i;
				// if(i>=600000 && i<800000)
				{
					System.out.println(s[0] + ":" + i);
					Long linkid = Long.parseLong(s[0]);
					String dr = s[1];
					Long snodeid = Long.parseLong(s[2]);
					Long enodeid = Long.parseLong(s[3]);
					int length = Integer.parseInt(s[4]);

					Node firstNode = nodeIndex.get("id", snodeid).getSingle();
					Node secondNode = nodeIndex.get("id", enodeid).getSingle();
					if (firstNode == null || secondNode == null)
						continue;

					Relationship direction = relationshipIndex
							.get("id", linkid).getSingle();
					if (direction == null) {
						direction = firstNode.createRelationshipTo(secondNode,
								Constant.Road.direction);
						direction.setProperty("length", length);
						direction.setProperty("id", linkid);
						relationshipIndex.add(direction, "id", linkid);
					}

					if (dr.equalsIgnoreCase("d")) {
						Long linkid2 = -linkid;
						Relationship direction2 = relationshipIndex.get("id",
								linkid2).getSingle();
						if (direction2 == null) {
							direction2 = secondNode.createRelationshipTo(
									firstNode, Constant.Road.direction);
							direction2.setProperty("length", length);
							direction2.setProperty("id", linkid2);
							relationshipIndex.add(direction2, "id", linkid2);
						}
					}

					Node node = nodeIndex.get("id", snodeid).getSingle();
					System.out.println(node.getProperty("id") + "_"
							+ node.getProperty("x") + "_"
							+ node.getProperty("y"));
					node = nodeIndex.get("id", enodeid).getSingle();
					System.out.println(node.getProperty("id") + "_"
							+ node.getProperty("x") + "_"
							+ node.getProperty("y"));
				}
			}
			tx.success();
		} finally {

			tx.close();
			graphDb.shutdown();
			br.close();
		}
	}

	public static void main(String[] args) throws IOException {
		// String tcsvpath=args[0];
		// String ncsvpath=args[0];
		// String dbpath=args[1];

		// String tcsvpath="D:/map/map/R.csv";
		// String ncsvpath="D:/map/map/N.csv";
		// String dbpath="D:/map/tmpNeo4jdata";
		//
		// importNode(ncsvpath,dbpath);
		// importLink(tcsvpath,dbpath);
	}
}
