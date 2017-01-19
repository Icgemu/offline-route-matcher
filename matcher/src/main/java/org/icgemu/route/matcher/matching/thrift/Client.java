package org.icgemu.route.matcher.matching.thrift;

import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
/**
 * 连接Thrift的客户端.
 * 
 * @author Rong
 * 
 */
public final class Client {
    /**
     * 缓存建立的客户端连接.
     */
	private static TTransport transport= null ;

    /**
     * 不能实例化.
     */
    private Client() {
    }

    /**
     * 新建一个到Server 端的连接.
     * @return Client对象
     */
    public static synchronized  RouteService.Client openClient(String host,int port) {
        //TTransport transport = null;
        RouteService.Client client = null;
        try {
            //String[] s = CNConfig.getThriftClient().split(":");
            //String host = s[0];
            //int port = Integer.parseInt(s[1]);
            transport = new TSocket(host, port);

            TFramedTransport protocol = new TFramedTransport(transport);
            TProtocol p = new TCompactProtocol(protocol);
            client = new RouteService.Client(p);
            transport.open();

        } catch (TTransportException e) {
            e.printStackTrace();
        }
        //maps.put(client.hashCode(), transport);
        return client;
    }

    /**
     * 关闭到Server的连接.
     * @param client OpenClient返回的对象
     */
    public static synchronized  void closeClient() {
    	transport.close();
    }
	
}
