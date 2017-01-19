package org.icgemu.route.matcher.matching.mapmatch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.neo4j.graphdb.Relationship;

import redis.clients.jedis.Jedis;

import com.iq.STPathFinder;
import com.iq.pojo.Link;
import com.iq.pojo.PointBasic;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;

public class ProjectionMapping {
	private static SimpleDateFormat f=new SimpleDateFormat("yyyyMMddhhmmss");
	
	private static double anlge(double lon1,double lat1,double lon2,double lat2){		
		return Angle.toDegrees((Angle.angle(new Coordinate(lon1,lat1), new Coordinate(lon2,lat2))));
	}
	
	
	public static ArrayList<PointBasic> preMatch(String inpath) throws NumberFormatException, IOException {		
		BufferedReader br = new BufferedReader(new FileReader(inpath));		
		ArrayList<PointBasic> pts=new ArrayList<PointBasic>();
		
		String line = ""; 
		int c=0;
    	while ((line = br.readLine()) != null) {
    		String[] s = line.split("\t");    	
    		String vin=s[0];
    		String sdate=s[1];
    		String tdae=s[2];
    		String bcm_keyst=s[3];
    		String bcs_vehspd=s[4];if(bcs_vehspd.equalsIgnoreCase("NULL"))bcs_vehspd="0";
    		String ems_engspd=s[5];
    		String lon84=s[6];
    		String lat84=s[7];
    		String lon02=s[8];
    		String lat02=s[9];
    		String gps_heading=s[10];  
    		
        	long t=Long.parseLong(tdae);	
    		if(lon02.equalsIgnoreCase("NULL") || lat02.equalsIgnoreCase("NULL"))continue;

			double lon=Double.parseDouble(lon02);
			double lat=Double.parseDouble(lat02);
			
			if(lon>115.0 || lon<112.5 || lat>24.00000002 || lat<22.0)continue;
			
			if(c==0){
				
				PointBasic p=new PointBasic();
				p.setVin(vin);
				p.setTime(new Date(t));
				p.setLon(lon);
				p.setLat(lat);
				p.setSpd(Float.parseFloat(bcs_vehspd));
				
				p.setDist(0);
				p.setDangle(0);
				p.setDtime(0);
				pts.add(p);
			}else{
				PointBasic lastp =pts.get(pts.size()-1);
				int ds  = (int)Math.round(CellTools.distance(lastp.getLon(), lastp.getLat(), lon, lat));
				int    dt  = (int)((t-lastp.getTime().getTime())/1000);
				int    da  = (int)anlge(lastp.getLon(), lastp.getLat(),lon,lat);
				
				PointBasic p=new PointBasic();
				p.setVin(vin);
				p.setTime(new Date(t));
				p.setLon(lon);
				p.setLat(lat);
				p.setSpd(Float.parseFloat(bcs_vehspd));
				
				p.setDist(ds);
				p.setDangle(da);
				p.setDtime(dt);
				pts.add(p);
						
			}
			c++;			
    	}
    	return pts;
	}
	

	public static void main(String[] args) throws NumberFormatException, IOException {

		System.out.println(anlge(0,0,45,-45));
		ArrayList<PointBasic> pts=preMatch("D:/tmp/LMGGN1S59F1000316.csv");
		
		
		STPathFinder pf=new STPathFinder("D:/map/map/neo4jdata/map");
		Jedis jedis = new Jedis("172.16.52.6");
		BufferedReader br = new BufferedReader(new FileReader("D:/tmp/SLMGGN1S59F1000316.csv"));
		PrintWriter pr=new PrintWriter(new File("D:/tmp/MLMGGN1S59F1000316.csv"));
		
		String line = ""; 
		double lastlon=0,lastlat=0;
		long lasttime=0;
		int c=0;
    	while ((line = br.readLine()) != null) {
    		String[] s = line.split(",");    	
    		String vin=s[0];
    		String sdate=s[1];
    		String speed=s[2];
    		double lon=Double.parseDouble(s[3]);
    		double lat=Double.parseDouble(s[4]);
    		float deltaDistance =Float.parseFloat(s[5]);
    		int deltaTime=Integer.parseInt(s[6]);   		
    		
    		if(deltaTime>3600)break;
    		
    		if(speed.equalsIgnoreCase("NULL") )continue;
			
			Point p=CellTools.createPoint(lon,lat);
			List<Link> links=CellTools.getLinks(lon,lat,jedis);
			if(links==null ||links.size()==0){
				//output no road
				continue;
			}		
			
			System.out.println("-----------------"+sdate+"-----------------");
//			pr.println("-----------------"+sdate+"-----------------");
			TreeMap<Double,String> ds=new TreeMap<Double,String>();
			for(int i=0;i<links.size();i++){
				Link l=links.get(i);
				Coordinate clsp=CellTools.closestPoint2LineString(p, l.getLine());
				double dist=Math.round(CellTools.distance(clsp.x, clsp.y, p.getX(), p.getY()));
				long rel[]=pf.getRelationship(Long.parseLong(l.getId()));
				
				ds.put(dist,(l.getId()+","+clsp.x+","+clsp.y+","+dist+","+rel[0]+","+rel[1]+","+deltaDistance));
				System.out.println((l.getId()+","+clsp.x+","+clsp.y+","+dist+","+rel[0]+","+rel[1]+","+deltaDistance));
				pr.println((sdate+","+l.getId()+","+clsp.x+","+clsp.y+","+dist+","+rel[0]+","+rel[1]+","+deltaDistance));
			}
//			System.out.println(ds.firstEntry().getValue());
//			pr.println(ds.firstEntry().getValue());
    	}
    	pr.close();
		br.close();		
		jedis.close();
	}

}
