//package io.emu.route.matcher.thrift;
//
//import io.emu.route.matcher.mapmatch.MapsObject;
//import io.emu.route.matcher.pojo.GPS;
//import io.emu.route.matcher.thrift.RouteService.Iface;
//
//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ArrayBlockingQueue;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//
//import org.apache.thrift.TException;
//
//public class RouteEst implements Iface {
//
//	BufferedWriter bw;
//	MapsObject maps;
//
//	public RouteEst(String bwpath, String gra) throws IOException {
//		super();
//		this.bw = new BufferedWriter(new FileWriter(new File(bwpath)));
//		this.maps = new MapsObject(gra);
//	}
//
//	public RouteEst() throws IOException {
//	}
//
//	@Override
//	public List<String> getMatching(String gpslst) throws TException {
//		String[] test = gpslst.split("_");
//		// System.out.println(gpslst);
//		List<GPS> traces = new ArrayList<GPS>();
//		for (int i = 0; i < test.length; i++) {
//
//			String[] trace = test[i].split(":");
//			if (trace.length < 4) {
//				// System.out.println(gpslst);
//				// System.out.println("size-->"+test[i]);
//				continue;
//			}
//			traces.add(new GPS(Double.parseDouble(trace[1]), Double
//					.parseDouble(trace[2]), -1d, Long.parseLong(trace[0]),
//					Float.parseFloat(trace[3])));
//		}
//
//		// try {
//		// Main.writePointShp(traces, "D:/map/test-p1.shp");
//		// } catch (Exception e1) {
//		// // TODO Auto-generated catch block
//		// e1.printStackTrace();
//		// }
//		try {
//			return maps.output1(maps.truncate(maps.getRoute(traces)));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	/**
//	 * 输出匹配结果到文件系统
//	 * 
//	 * @param paths
//	 * @param outfile
//	 * @throws IOException
//	 */
//	public void output(List<String> paths, String vin) throws IOException {
//		if (paths == null) {
//			return;
//		}
//		for (String path : paths) {
//			bw.write(vin + "," + path + "\n");
//		}
//
//	}
//
//	public void close() throws IOException {
//		bw.close();
//	}
//
//	public static void test() throws IOException, TException {
//		String ln = "";
//		String[] sp = ln.split(",");
//		RouteEst rt = new RouteEst("a.csv", "D:/map/neo4jdata/map");
//		rt.output(rt.getMatching(sp[2]), sp[0]);
//	}
//
//	public static void main(String[] args) throws IOException, TException {
//		try {
//			File file = new File(args[0]);
//			// File file = new File("D:/map/test.csv");
//			BufferedReader rd = new BufferedReader(new FileReader(file));
//			String ln = null;
//
//			// RouteEst rt = new RouteEst(args[1]);
//			// RouteEst rt = new RouteEst("a.csv");
//			int threadCnt = Integer.parseInt(args[1]);
//			ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<String>(
//					10);
//
//			ExecutorService es = Executors.newFixedThreadPool(threadCnt);
//
//			for (int i = 0; i < threadCnt; i++) {
//				es.submit(new Worker(queue, new RouteEst("out-" + i + ".csv",
//						"/run/shm/map" + i)));
//			}
//			while ((ln = rd.readLine()) != null) {
//
//				queue.put(ln);
//
//			}
//
//			es.awaitTermination(60, TimeUnit.SECONDS);
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		// RouteEst.test();
//	}
//
//}
