package org.icgemu.route.matcher.matching.mapmatch;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.icgemu.route.matcher.matching.pojo.Link;
import redis.clients.jedis.Jedis;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.operation.distance.DistanceOp;


/**С���񹤾�
 * @author Chunbai
 *
 */
public class CellTools {
	
	private static GeometryFactory geometryFactory=new GeometryFactory(new PrecisionModel(), 4326);
	private static LonLat2Cell l2c=new LonLat2Cell();
	
	/**������
	 * @param s
	 * @return
	 * @throws ParseException
	 */
	public static LineString createLineByWKT(String s) throws ParseException{  
        WKTReader reader = new WKTReader( geometryFactory );  
        LineString line = (LineString) reader.read(s);  
        return line;  
    }
	
	 /**������
	 * @param lon
	 * @param lat
	 * @return
	 */
	public static Point createPoint(double lon,double lat){  
	        Coordinate coord = new Coordinate(lon, lat);  
	        Point point = geometryFactory.createPoint( coord );  
	        return point;  
	 }
	
	public static Point createPoint(Coordinate coord){  
        //Coordinate coord = new Coordinate(lon, lat);  
        Point point = geometryFactory.createPoint( coord );  
        return point;  
    }
	 
	/**Ѱ�ҵ㵽�ߵ������
	 * @param p
	 * @param l
	 * @return
	 */
	public  static synchronized Coordinate closestPoint2LineString(Point p,LineString l) {
			Coordinate clsPoint=null;
			clsPoint=DistanceOp.closestPoints(p, l)[1];
			return clsPoint;
	}
		
	 
	/**���л� Link
	 * @param link
	 * @return
	 */
	public static byte[] serializeProtoStuffProductsList(Link link) {
        if(link == null) return null;

        Schema<Link> schema = RuntimeSchema.getSchema(Link.class);
        LinkedBuffer buffer = LinkedBuffer.allocate(4096);
        byte[] protostuff = null;
        try {
            protostuff = ProtostuffIOUtil.toByteArray(link, schema, buffer);
        } finally {
           buffer.clear();
        }        

        return protostuff;
    }
	
	/**�����л�Link
	 * @param bytes
	 * @return
	 */
	public static Link deserializeProtoStuffLink(byte[] bytes) {
        if(bytes == null)return null;        
       
        Schema<Link> schema = RuntimeSchema.getSchema(Link.class);         
        Link link = new Link();
        ProtostuffIOUtil.mergeFrom(bytes, link, schema);        
       
        return link;
    }
	
	
	
	 /**ͨ���������ȡLink
	 * @param lon
	 * @param lat
	 * @param jedis
	 * @return
	 */
	public static List<Link> getLinks(double lon,double lat,Jedis jedis){
		 String cellid=l2c.lonlat2cell(lon, lat);
		 byte[] b=jedis.get(cellid.getBytes());
		 if(b==null)return null;
		 
		 //System.out.print(cellid+"@");
		 
		 Set<byte[]> set=jedis.smembers(b);
		 List<Link> links = new ArrayList<Link> ();
		 
		 Iterator<byte[]> iterator=set.iterator();
		 while(iterator.hasNext()){
			 byte[] lid=iterator.next();
			 b=jedis.get(lid);
			 if(b!=null){
				 Link link=deserializeProtoStuffLink(b);
				 links.add(link);
//				 System.out.print(link.getId()+",");
			 }
		 }		
//		 System.out.println("");	 
		 return links;
	 }
	

	/**��Linkд��redis
	 * @param rPath
	 * @param host
	 * @throws Exception
	 */
	public static void r2Redis(String rPath,String host) throws Exception{
		Jedis jedis = new Jedis(host);
		BufferedReader br = new BufferedReader(new FileReader(rPath));
		String line = ""; 
		int count=0;
    	while ((line = br.readLine()) != null) {
    		String[] s = line.split("@");
    		String id=s[0];    		   		
            String direction=s[1];
            boolean isDual=true;
            if(direction.equals("s")){
            	isDual=false;
            }
            String snode=s[2];
            String enode=s[3];
	        String length=s[4];        		 
    		LineString linestring=createLineByWKT(s[5]);
    		
    		byte[] b=serializeProtoStuffProductsList(new Link(id, isDual, Long.parseLong(snode), Long.parseLong(enode),
    				Integer.parseInt(length), linestring));  	
    		jedis.set(id.getBytes(), b);
    		System.out.println(id+"-"+(++count));
    	}
    	jedis.close();
	}
	
	/**cellд��redis
	 * @param cellIndexPath
	 * @param cellDataPath
	 * @param host
	 * @throws Exception
	 */
	public static void cell2Redis(String cellIndexPath,String cellDataPath,String host) throws Exception{
		Jedis jedis = new Jedis(host);
		BufferedReader br = new BufferedReader(new FileReader(cellIndexPath));
		String line = ""; 
		int count=0;
    	while ((line = br.readLine()) != null) {
    		++count;
	    	String[] s = line.split("@");
	    	String cellid=s[0];
	    	String index=s[1];
	    	jedis.set(cellid.getBytes(), index.getBytes());
	    	System.out.println(cellid+"-"+(count));
    	}    	
    	
		br = new BufferedReader(new FileReader(cellDataPath));
		line = ""; 
		count=0;
    	while ((line = br.readLine()) != null) {
    		++count;
	    	String[] s = line.split("@");
	    	String indexid=s[0];
	    	String[] linkids=s[1].split(",");
	    	for(int i=0;i<linkids.length;i++){
	    		jedis.sadd(indexid.getBytes(),linkids[i].getBytes());  
	    	}
	    	System.out.println(indexid+"-"+(count));
    	}
    	jedis.close();
	}
	
	/**
	 * @param id
	 * @param host
	 * @return
	 * @throws Exception
	 */
	public static Link getLink(String id,String host) throws Exception{
		Jedis jedis = new Jedis(host);
		byte[] b=jedis.get(id.getBytes());
		Link l=deserializeProtoStuffLink(b);
		jedis.close();
		return l;
	}
	
	/**
	 * @param id
	 * @param host
	 * @return
	 * @throws Exception
	 */
	public static Link getLink(String id,Jedis jedis) throws Exception{
		//Jedis jedis = new Jedis(host);
		byte[] b=jedis.get(id.getBytes());
		Link l=deserializeProtoStuffLink(b);
		jedis.close();
		return l;
	}
	
	public static double distance(double lng1, double lat1, double lng2,  double lat2){
		return Tools.distance(lng1,lat1, lng2, lat2);
	}
	
	public static double calPointDistance(Coordinate sp, Coordinate ep) {
		return distance(sp.x, sp.y, ep.x, ep.y);
	}
	public static void main(String[] args) throws Exception {
		
		
		
//		String host=args[0];
//		String rcsv=args[1];
//		String cindex=args[2];
//		String cdata=args[3];
//		
		String host="172.16.52.6";
//		String rcsv="D:/map/map/r.csv";
//		cindex="D:/map/map/cellIndex.csv";
//		cdata="D:/map/map/cellData.csv";
//		
//		r2Redis(rcsv,host);
//		cell2Redis(cindex,cdata,host);
		
		Jedis jedis = new Jedis(host);
		List<Link> ls=getLinks(113.32207,23.207277,jedis);
		System.out.println(ls.size());
	}
}
