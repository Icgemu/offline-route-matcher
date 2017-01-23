package io.emu.route.compiler.neo4j;

import io.emu.route.compiler.map.Link;

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
								if (path.startNode() == path.endNode()) {
									return Evaluation.EXCLUDE_AND_CONTINUE;
								}
								double t = 0.0;
								int cost = 0;
								for (Relationship rel : path.relationships()) {
									Integer len = 0;
									Integer speedlimit = 0;
									int retry = 3;

									while (retry > 0) {
										try {
											len = (Integer) rel.getProperty("length");
											speedlimit = (Integer) rel.getProperty("speedlimit");
											break;
										} catch (Exception e) {
											e.printStackTrace();
											System.out.println("err Node=>"+ rel);
											retry--;
										}
									}

									cost += len;
									t += len / (speedlimit * 1000.0 / (3600));
								}

								if (t > 2 * 60) {
									return Evaluation.INCLUDE_AND_PRUNE;
								}
								return Evaluation.INCLUDE_AND_CONTINUE;
							}

						})

						.uniqueness(Uniqueness.NODE_GLOBAL).traverse(node);

				for (Path p : my) {
					String enode = (String) p.endNode().getProperty("nodeid");
					MyPath myp = new MyPath(0, snode, enode);
					int cost = 0;

					WeightedPath shortest = RouteFinder.find(graphDb, snode,enode);
					Iterator<Relationship> it = shortest.relationships().iterator();
					while (it.hasNext()) {
						Relationship rel = it.next();
						String mysnode = (String) rel.getStartNode().getProperty("nodeid");
						String myenode = (String) rel.getEndNode().getProperty("nodeid");
						String id = (String) rel.getProperty("linkid") + "";
						Integer width = (Integer) rel.getProperty("width");
						Integer length = (Integer) rel.getProperty("length");
						Integer speedlimit = (Integer) rel.getProperty("speedlimit");
						Integer direction = (Integer) rel.getProperty("direction");
						String kind = (String) rel.getProperty("roadtype");
						String roadclass = (String) rel.getProperty("roadclass");
						cost += length;

						Link ll = new Link(id, mysnode, myenode, width, length, speedlimit,
								direction, roadclass, kind,null);
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

		String in = args[1];
		String out = args[2];
		String db = args[3];
		int thread = 1;
		
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
