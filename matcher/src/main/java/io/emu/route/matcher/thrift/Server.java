//package io.emu.route.matcher.thrift;
//
//import java.io.IOException;
//import java.util.concurrent.TimeUnit;
//import org.apache.thrift.protocol.TCompactProtocol;
//import org.apache.thrift.protocol.TProtocolFactory;
//import org.apache.thrift.server.TServer;
//import org.apache.thrift.server.TThreadPoolServer;
//import org.apache.thrift.server.TThreadPoolServer.Args;
//import org.apache.thrift.transport.TFramedTransport;
//import org.apache.thrift.transport.TServerSocket;
//import org.apache.thrift.transport.TTransportException;
//import org.apache.thrift.transport.TTransportFactory;
//
///**
// * Thrfit Server端，处理来自Client端的请求.
// * 
// * @author Administrator
// *
// */
//public class Server {
//	/**
//	 * 主服务监听实例.
//	 */
//	private TServer ttss = null;
//
//	/**
//	 * 监听端口.
//	 */
//	private int serverPort = 17777;
//
//	/**
//	 * Neo4j的数据目录.
//	 */
//	// private String rtDb = "/run/shm/map";
//
//	/**
//	 * 使用默认的参数启动实例.
//	 * 
//	 * @throws IOException
//	 */
//	public Server() throws IOException {
//
//	}
//
//	/**
//	 * 使用指定的参数启动实例.
//	 * 
//	 * @param sNeo4jDbPath
//	 *            Neo4j的数据目录
//	 * @param sRedisCnnUrl
//	 *            连接Redis的主机和端口
//	 * @param iServerPort
//	 *            监听端口
//	 */
//	// public Server(String sNeo4jDbPath, String lucenceDb, int iServerPort) {
//	// super();
//	// this.serverPort = iServerPort;
//	// this.neo4jDbPath = sNeo4jDbPath;
//	// this.lucenceDb = lucenceDb;
//	// }
//
//	/**
//	 * 启动监听实例，开始接收请求.
//	 * 
//	 * @throws TTransportException
//	 *             连接异常
//	 * @throws IOException
//	 */
//	public void start() throws TTransportException, IOException {
//
//		RouteService.Processor<RouteEst> processor = new RouteService.Processor<RouteEst>(
//				new RouteEst());
//		TTransportFactory transportFactory = new TFramedTransport.Factory();
//		TProtocolFactory protocolFactory = new TCompactProtocol.Factory();
//		TServerSocket serverTransport = new TServerSocket(serverPort);
//
//		Args args = new Args(serverTransport);
//		args.processor(processor);
//		args.transportFactory(transportFactory);
//		args.protocolFactory(protocolFactory);
//		args.stopTimeoutUnit = TimeUnit.SECONDS;
//		args.stopTimeoutVal = 5;
//		args.maxWorkerThreads(5);
//		// args.minWorkerThreads(200);
//		// args.executorService(Executors.newCachedThreadPool());
//		ttss = new TThreadPoolServer(args);
//		ttss.serve();
//	}
//
//	/**
//	 * 停止服务.
//	 */
//	public void stop() {
//		ttss.stop();
//	}
//
//	/**
//	 * 程序入口.
//	 * 
//	 * @param args
//	 *            参数
//	 * @throws TTransportException
//	 *             连接异常
//	 * @throws IOException
//	 */
//	public static void main(String[] args) throws TTransportException,
//			IOException {
//		new Server().start();
//	}
//}
