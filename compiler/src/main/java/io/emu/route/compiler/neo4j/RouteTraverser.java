package io.emu.route.compiler.neo4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.InitialBranchState;
import org.neo4j.graphdb.traversal.PathEvaluator;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.graphdb.traversal.Uniqueness;

public class RouteTraverser implements Runnable {
	private GraphDatabaseService graphDb = null;
	LinkedBlockingQueue<MyPath> queue = new LinkedBlockingQueue<MyPath>();
	IndexManager index;
	File in;
	CountDownLatch latch;

	
	RouteTraverser(GraphDatabaseService graphDb,
			LinkedBlockingQueue<MyPath> queue, File in, CountDownLatch latch) {
		this.graphDb = graphDb;
		// this.index = index;
		this.queue = queue;

		this.in = in;
		this.latch = latch;
		// registerShutdownHook(graphDb);
	}

	@Override
	public void run() {

		BufferedReader bf;
		try {
			bf = new BufferedReader(new FileReader(in));
			String line = null;
			int cnt = 0;
			while ((line = bf.readLine()) != null) {
				cnt++;
				if (cnt % 10000 == 0) {
					System.out.println(in.getName() + "-line:" + cnt);
				}
				String[] info = line.split(":");
				// Node node =
				// graphDb.getNodeById(Long.parseLong(info[0].trim()));
				// String snode = (String) node.getProperty("nodeid");
				String snode = info[0];
				//
				IndexManager index = graphDb.index();
				Index<Node> nodeIndex = index.forNodes("nodeid");
				Node node = nodeIndex.get("nodeid", snode).getSingle();
				//
				Traverser my = graphDb
						.traversalDescription()
						// .depthFirst()
						.breadthFirst()
						.expand(new PathExpander<State>() {

							@Override
							public Iterable<Relationship> expand(Path path,
									BranchState<State> state) {
								// TODO Auto-generated method stub
								// System.out.println("call expand method.");
								Iterable<Relationship> b = null;
								int retry = 3;
								while (retry > 0) {
									try {
										b = path.endNode().getRelationships(
												Direction.OUTGOING);
										break;
									} catch (Exception e) {
										e.printStackTrace();
										System.out.println("err getRelationships=>"
												+ path.endNode());
										retry--;
									}
								}

								return b;
							}

							@Override
							public PathExpander<State> reverse() {
								// TODO Auto-generated method stub
								// System.out.println("call unsafe method.");
								return null;
							}

						},
								new InitialBranchState.State<State>(
										new State(), new State()))
						.evaluator(new PathEvaluator<State>() {

							@Override
							public Evaluation evaluate(
									org.neo4j.graphdb.Path path) {
								// TODO Auto-generated method stub
								System.out.println("call unsafe method.");
								return null;
							}

							@Override
							public Evaluation evaluate(
									org.neo4j.graphdb.Path path,
									BranchState<State> state) {
								// TODO Auto-generated method stub
								// System.out.println("call evaluate method.");
								// if(path.lastRelationship() == null&&){}
								if (path.startNode() == path.endNode()) {
									// System.out.println(state.getState().t+"=>"+"Start Node");
									return Evaluation.EXCLUDE_AND_CONTINUE;
								}
								// Relationship rel = path.lastRelationship();
								double t = 0.0;
								int cost = 0;
								for (Relationship rel : path.relationships()) {
									Float len = 0.0f;
									Float speedlimit = 0.0f;
									int retry = 3;

									while (retry > 0) {
										try {
											len = (Float) rel
													.getProperty("length");
											speedlimit = (Float) rel
													.getProperty("speedlimit");
											break;
										} catch (Exception e) {
											e.printStackTrace();
											System.out.println("err Node=>"
													+ rel);
											retry--;
										}
									}

									cost += len;
									t += len / (speedlimit * 1000.0 / (3600));
								}

								// state.getState().t = state.getState().t + t;
								// state.getState().cost = state.getState().cost
								// + len;

//								String mysnode = (String) path.startNode()
//										.getProperty("nodeid");
//								String myenode = (String) path.endNode()
//										.getProperty("nodeid");
								// String nodeStr = mysnode
								// +":"+myenode+":"+path.length()+"=>";
								if (t > 2 * 60) {

									// System.out.println(nodeStr+t+","+cost+"=>"+"INCLUDE_AND_PRUNE");
									// state.getState().t = state.getState().t -
									// t;
									// state.getState().cost =
									// state.getState().cost - len;
									return Evaluation.INCLUDE_AND_PRUNE;
								}
								// state.getState().t = state.getState().t + t;
								// state.getState().cost = state.getState().cost
								// + len;
								// System.out.println(nodeStr+t+","+cost+"=>"+"INCLUDE_AND_CONTINUE");
								return Evaluation.INCLUDE_AND_CONTINUE;
							}

						})

						.uniqueness(Uniqueness.NODE_GLOBAL).traverse(node);

				for (Path p : my) {
					String enode = (String) p.endNode().getProperty("nodeid");
					// String snode = (String)
					// p.startNode().getProperty("nodeid");
					// Node s = p.startNode();
					// Node e = p.endNode();
					MyPath myp = new MyPath(0, snode, enode);
					int cost = 0;

					WeightedPath shortest = RouteFinder.find(graphDb, snode,
							enode);
					Iterator<Relationship> it = shortest.relationships()
							.iterator();
					while (it.hasNext()) {
						Relationship rel = it.next();
						String mysnode = (String) rel.getStartNode()
								.getProperty("nodeid");
						String myenode = (String) rel.getEndNode().getProperty(
								"nodeid");
						String id = (String) rel.getProperty("linkid") + "";
						String width = Float.toString((Float) rel
								.getProperty("width"));
						float flength = (Float) rel.getProperty("length");
						int length = (int) flength;

						Float speedlimit = (Float) rel
								.getProperty("speedlimit");
						Integer direction = (Integer) rel
								.getProperty("direction");
						String kind = (String) rel.getProperty("roadtype");
						String roadclass = (String) rel
								.getProperty("roadclass");
						cost += length;

						LinkMB ll = new LinkMB(id, mysnode, myenode,
								direction.toString(), roadclass, kind, width,
								length, speedlimit.intValue());
						myp.addLink(ll);
					}
					myp.setCost((int) shortest.weight());
					queue.put(myp);
				}
				// prePath(snode, new ArrayList<LinkMB>(), queue);
			}

			bf.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.latch.countDown();
	}


	public static void main(String[] args) throws Exception {
		

		String in = "E:/Prj/OD/test/uN-G.csv";
		String out = "E:/Prj/OD/test/";
		int thread = 1;

		String db = "E:/Prj/OD/test/neo4j-db-G";

		HashMap<String, String> configuration = new HashMap<String, String>();
		configuration.put("use_memory_mapped_buffers", "true");
		configuration.put("grab_file_lock", "false");

		configuration.put("read_only", "true");
		configuration.put("cache_type", "none");

		final GraphDatabaseService graphDb = new GraphDatabaseFactory()
				.newEmbeddedDatabaseBuilder(new File(db))
				.setConfig(GraphDatabaseSettings.read_only, "true")
				.newGraphDatabase();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});

		// IndexManager index = graphDb.index();

		File[] files = Spliter.split(new File(in), thread);

		System.out.println(new Date().getTime());
		CountDownLatch wlatch = new CountDownLatch(thread);
		CountDownLatch rlatch = new CountDownLatch(thread);
		for (int i = 0; i < thread; i++) {
			// String dir = i%2==0?"E://":"F://";
			LinkedBlockingQueue<MyPath> queue = new LinkedBlockingQueue<MyPath>(
					1000);
			Thread writer = new Thread(new Writer(new File(out, "my-route-" + i
					+ ".csv"), queue, wlatch));
			writer.start();
			new Thread(new RouteTraverser(graphDb, queue, files[i], rlatch))
					.start();
		}

		rlatch.await();
		wlatch.await();

		System.out.println(new Date().getTime());
		
	}

}
