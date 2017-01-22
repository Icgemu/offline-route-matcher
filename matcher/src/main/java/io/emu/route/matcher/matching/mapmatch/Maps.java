package io.emu.route.matcher.matching.mapmatch;

import io.emu.route.matcher.matching.pojo.CandidatePath;
import io.emu.route.matcher.matching.pojo.CandidatePrj;
import io.emu.route.matcher.matching.pojo.GPS;
import io.emu.route.matcher.matching.pojo.Link;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import redis.clients.jedis.Jedis;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class Maps {
	private static Jedis jedis = new Jedis("172.16.52.6");
	private static Neo4JAstar astar =null;	
	static {
		  try {

			  GraphDatabaseService graphDb = new GraphDatabaseFactory()
				//.newEmbeddedDatabaseBuilder(new File("/run/shm/map"))
			    .newEmbeddedDatabaseBuilder(new File("D:/map/neo4jdata/map"))
				.newGraphDatabase();
			  astar = new Neo4JAstar(graphDb);
		  } catch (Throwable t) {
		    //log.error("Failure during static initialization", t);
		    throw t;
		  }
		}
	/**
	 * 设置方位角，根据前后点设置角度
	 * @param trace 原始从文件读取的GPS数据
	 */
	public static void setAzimuth(List<GPS> trace){
		GPS lastgps = trace.get(0);		
		double headerazm = Double.MAX_VALUE;
		GPS header = trace.get(0);
		for (int i = 0; i < trace.size(); i++) {
			GPS gps = trace.get(i);			
			//用两点连线的方位角提点GPS的角度
			if(i>0){
	    		//double azm = MapUtil.azimuth(lastgps.longtitude, lastgps.latitude, gps.longtitude, gps.latitude);
				double azm = 0.0;
	    		double dist = MapUtil.calPointDistance(header.longtitude, header.latitude, gps.longtitude, gps.latitude);
				//double dist = MapUtil.calPointDistance(lastgps.longtitude, lastgps.latitude, gps.longtitude, gps.latitude);
//				double spd = dist*3.6/((gps.getTime() - lastgps.getTime())/1000);
	    		//距离过小，车速小，GPS坐标偏移比较大，考虑用前面点的方位角
	    		if(dist < 25 ){
				//距离过小，车速小，GPS坐标偏移比较大，考虑用前面点的方位角
				//if(spd < 15 ){
	    			
	    			if(headerazm == Double.MAX_VALUE){
	    				azm = MapUtil.azimuth(lastgps.longtitude, lastgps.latitude, gps.longtitude, gps.latitude);
	    			}else{
	    				azm = headerazm;
	    			}
	    			
	    		}else{
	    			azm = MapUtil.azimuth(header.longtitude, header.latitude, gps.longtitude, gps.latitude);
	    			headerazm = azm;
		    		header = gps;
	    		}
//	    		if(i>1){
//	    			azm =  0.7 * azm + trace.get(i-2).getDirection()*0.3;
//	    		}
	    		lastgps.setDirection(azm);
	    		//最后一个点用前一个点的方位角近似
	    		if(i==(trace.size()-1)){gps.setDirection(azm);}
	    		lastgps = gps;
			}
		}
	}
	
	
	 
	public static void getCandidateLinks(GPS gps) {

		Point point = CellTools.createPoint(gps.getLongtitude(),gps.getLatitude());
		
		//GPS点网格的LINk
		
		//Jedis jedis = RedisUtil.getJedis();
		//String ping = jedis.ping();
		List<Link> links = CellTools.getLinks(gps.getLongtitude(), gps.getLatitude(), jedis);
		//RedisUtil.returnResource(jedis);
		
		List<CandidatePrj> candidates = new ArrayList<CandidatePrj>();

		if(links == null){//剔除找不到网格数据的
//			System.out.println(gps);
			gps.setCandidates(candidates);
			return;			
		}
		
//		if(gps.getTime() == 1438656847000l){
//			System.out.println("");
//		}
		
		for (Link link : links) {

//			if(link.getId().equals("34535212773")){
//				System.out.println("");
//			}
			
			LineString lineString = link.getLine();
			//投影点
			Coordinate cs = CellTools.closestPoint2LineString(point, lineString);
			// Point prjp = CellTools.createPoint(cs);

			//投影距离
			double prjDistance = CellTools.calPointDistance(point.getCoordinate() , cs);

			Coordinate coordinates[] = lineString.getCoordinates();
			int size = coordinates.length - 1;

			double distance, prjDistanceFormSNode = 0;
			double azimuth = 0;

			int choose = 0;
			for (int i = 0; i < size; i++) {//计算投影点到Link起点距离

				distance = CellTools.calPointDistance(coordinates[i] , coordinates[i + 1]);

				LineSegment lineSegment = new LineSegment(coordinates[i] , coordinates[i + 1]);
				Coordinate cur = lineSegment.closestPoint(cs);
				if (equal(cs, cur)) {
					distance = CellTools.calPointDistance(coordinates[i], cs);
					prjDistanceFormSNode = prjDistanceFormSNode + distance;
					choose = i;
					break;
				}
				choose = i;
				prjDistanceFormSNode = prjDistanceFormSNode + distance;
			}
			//投影点所在段的方位角
			azimuth = MapUtil.azimuth(coordinates[choose], coordinates[choose + 1]);

			double azimuthDelta = gps.getDirection() - azimuth;
			if (azimuthDelta > 180){
				azimuthDelta = 360 - azimuthDelta;
			}else if (azimuthDelta < -180){
				azimuthDelta = 360 + azimuthDelta;
			}
			//Link方向和GPS方位角的夹角(0~180度)
			azimuthDelta = Math.abs(azimuthDelta);
			
			Link finalLink = link;
			
			//考虑双向通行的Link的处理
			if(link.isDual() && azimuthDelta > 90){
				azimuthDelta = 180 - azimuthDelta;
				azimuth = (azimuth<0)?(azimuth+180):(azimuth-180);
				finalLink = new Link("-"+link.getId(),link.isDual(),
						link.getEnodeID(),
						link.getSnodeID(),
						link.getLength(),
						(LineString)link.getLine().reverse());
				prjDistanceFormSNode = link.getLength() - prjDistanceFormSNode;
				if(prjDistanceFormSNode<0){prjDistanceFormSNode=0.0;}
			}
			
			//夹角过大,投影距离过大，剔除
			//if (azimuthDelta > 90 || prjDistance > 100) {
			if (azimuthDelta > 90) {
				continue;
			}

			//weather projection point in Link start or end
			boolean isInBound = true;
			
			long len = Math.round(prjDistanceFormSNode);
			//判断投影点是否在
			if(len == 0l || ( link.getLength() - len ) <= 0){
				isInBound = false;
			}
			double cost = prjDistance;
			//加入候选Link
			candidates.add(new CandidatePrj(finalLink, gps, prjDistance,
					prjDistanceFormSNode, azimuth, azimuthDelta, cost, cs, isInBound));
		}

		
		int c = 5;
		if(candidates.size() > c){
			//按权值选前面几天Link
			adjust(gps, candidates, c);
		}

		gps.setCandidates(candidates);
		
		//return candidates;
	}
	
	/**
	 * 对GPS所有候选Link按权重排序
	 * 保留前面几条
	 * @param gps
	 * @param prjs
	 * @param c
	 */
	
	private static void adjust(GPS gps, List<CandidatePrj> prjs, int c) {
		double maxCost = -1.0, minCost = Double.MAX_VALUE;
		double maxAng = -1, minAng = 190;
		//找出投影距离和夹角的最大最小值
		for (CandidatePrj p : prjs) {
			if (p.getCost() > maxCost) {
				maxCost = p.getCost();
			}
			if (p.getCost() < minCost) {
				minCost = p.getCost();
			}

			if (p.getAzimuthDelta() > maxAng) {
				maxAng = p.getAzimuthDelta();
			}
			if (p.getAzimuthDelta() < minAng) {
				minAng = p.getAzimuthDelta();
			}
		}
		final double amin = minCost;
		final double aspan = maxCost - minCost;
		final double bmin = minAng;
		final double bspan = maxAng - minAng;
		// 最多保留c个候选

		Collections.sort(prjs, new Comparator<CandidatePrj>() {

			@Override
			public int compare(CandidatePrj o1, CandidatePrj o2) {
				//投影距离标准化到(0~1)
				double a1 = (o1.getCost() - amin) / aspan;
				//投影距离标准化到(0~1)
				double a2 = (o2.getCost() - amin) / aspan;
				//夹角标准化到(0~1)
				double c1 = (o1.getAzimuthDelta() - bmin) / bspan;
				//夹角标准化到(0~1)
				double c2 = (o2.getAzimuthDelta() - bmin) / bspan;

				//优先选取投影点在Link中间的Link
//				double dfs1 = o1.isInLink()?0.1:0.9;
//				double dfs2 = o2.isInLink()?0.1:0.9;
				//综合权重，权重小的优先选取
				//double k1 = 0.4 * a1 + 0.4 * c1 + 0.2 * dfs1;
				//double k2 = 0.4 * a2 + 0.4 * c2 + 0.2 * dfs2;
				double k1 = 0.7 * a1 + 0.3 * c1;
				double k2 = 0.7 * a2 + 0.3 * c2;
				//double k1 =  a1 * c1  * dfs1;
				//double k2 =  a2 * c2  * dfs2;
				
				if (k1 < k2)
					return -1;
				if (k1 > k2)
					return 1;
				return 0;
			}

		});
		//保留前面几条候选Link
		prjs.removeAll(prjs.subList(c, prjs.size()));
	}

	/**
	 * 大概判断坐标相等
	 * @param a
	 * @param b
	 * @return
	 */
	private static boolean equal(Coordinate a, Coordinate b) {
		return (Math.abs(a.x - b.x) < 0.0000001)
				&& (Math.abs(a.y - b.y) < 0.0000001);
	}
	
	/**
	 * 找出GPS点最终路径
	 * @param trace
	 * @return
	 * @throws Exception
	 */
	public static List<List<CandidatePath>> getRoute(List<GPS> trace) throws Exception{
		//设置方位角
		Maps.setAzimuth(trace);
		
		for(GPS gps: trace){
			//计算候选Link
			getCandidateLinks(gps);
		}
		
		
		List<List<CandidatePath>> paths = new ArrayList<List<CandidatePath>>();
		
		GPS lastGps = trace.get(0);
		List<List<CandidatePath>> finalPaths = new ArrayList<List<CandidatePath>>();
		for(int i=1;i<trace.size();i++){
			//deal with first GPS that has no matching Link.
			if(lastGps.getCandidates().size()<1 || lastGps.getCandidates() ==null){
				lastGps = trace.get(i);
				continue;
			}
			
			List<CandidatePrj> s = lastGps.getCandidates();
			GPS cur = trace.get(i);
			long deltaT = cur.getTime() -lastGps.getTime();
			if(cur.getCandidates().size()<1 || cur.getCandidates() ==null){
					lastGps = cur;
					continue;
			}
			
			List<CandidatePrj> e = cur.getCandidates();
			
//			if(lastGps.getTime() == 1440520325000l){
//				System.out.println("");
//			}
			
			lastGps = cur;
			
			
			//找出前后两点间候选路径
			List<CandidatePath> p = getPaths(s,e);
			
			//判断是否打断，如没有候选路径，没有候选link
			
			if((!checkBreak(paths,p)) &&  deltaT < 1000*60) {
				if(p.size()>0){paths.add(p);};
			}else{
				//找出目前所有点的最终匹配路径
				finalPaths.add(getFinalPathFor(paths));
				paths.clear();
				continue;
			}
		}
		
		finalPaths.add(getFinalPathFor(paths));
		
		return finalPaths;
	}
	
	/**
	 * 输出匹配结果到文件系统
	 * @param paths
	 * @param outfile
	 * @throws IOException
	 */
	public static void output(List<List<CandidatePath>> paths , String outfile) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outfile) ));
		
		for(List<CandidatePath> path: paths){
			
			for(CandidatePath p:path){
				String r = p.getSt().getGps().getTime()+","
			     + Math.round(p.getSt().getPrjDistanceFormSNode())+","
			     + p.getSt().getLink().getId()+","
			     + p.getEd().getGps().getTime()+","
			     + Math.round(p.getEd().getPrjDistanceFormSNode())+","
			     + p.getEd().getLink().getId()+","
			     + concatLink(p.getPath())
			     ;
				
				bw.write(r +"\n");
			}
			
		}
		bw.close();
	}
	
	
	/**
	 * 输出匹配结果到文件系统
	 * @param paths
	 * @param outfile
	 * @throws IOException
	 */
	public static List<String> output(List<List<CandidatePath>> paths ) throws IOException{
		//BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outfile) ));
		List<String> res = new ArrayList<String>();
		
		for(List<CandidatePath> path: paths){
			
			for(CandidatePath p:path){
				
				String r = p.getSt().getGps().getTime()+","
			     + p.getSt().getLink().getLength()+","
			     + Math.round(p.getSt().getPrjDistanceFormSNode())+","
			     + p.getSt().getLink().getId()+","
			     + p.getSt().getGps().getSoc()+","
			     + p.getEd().getGps().getTime()+","
			     + Math.round(p.getEd().getPrjDistanceFormSNode())+","
			     + p.getEd().getLink().getId()+","
			     + p.getEd().getGps().getSoc()+","
			     + concatLink(p.getPath())
			     ;
				
				//bw.write(r +"\n");
				res.add(r);
			}
			
		}
		return res;
	}
	/**
	 * 输出匹配结果到文件系统
	 * @param paths
	 * @param outfile
	 * @throws IOException
	 */
	public static List<String> output1(List<List<CandidatePath>> paths ) throws IOException{
		//BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outfile) ));
		List<String> res = new ArrayList<String>();
		
		for(List<CandidatePath> path: paths){
			
			for(CandidatePath p:path){
				long t = p.getEd().getGps().getTime() - p.getSt().getGps().getTime();
				
				long len = 0l;
				
				if(p.getSt().getLink().getId().equalsIgnoreCase(p.getEd().getLink().getId())){
					len = Math.round(p.getEd().getPrjDistanceFormSNode()) - Math.round(p.getSt().getPrjDistanceFormSNode());
				}else{
					if((p.getPath() == null) || (p.getPath().size()==0)){
						len = Math.round(p.getEd().getPrjDistanceFormSNode()) + 
								p.getSt().getLink().getLength() - 
								Math.round(p.getSt().getPrjDistanceFormSNode());
					}else{
						len = Math.round(p.getEd().getPrjDistanceFormSNode()) + 
								p.getSt().getLink().getLength() - 
								Math.round(p.getSt().getPrjDistanceFormSNode());
						for(Link lnk : p.getPath()){
							len += lnk.getLength();
						}
					}
				}
				len = Math.abs(len);//for u turn;
				long spd = len / t;
				float soc = Math.abs(p.getSt().getGps().getSoc() -p.getEd().getGps().getSoc());
				
				double delta = soc/t;
				long startT = p.getSt().getGps().getTime() - Math.round(p.getSt().getPrjDistanceFormSNode()/spd);
				
				double startSoc = (p.getSt().getGps().getTime() - startT)*delta + p.getSt().getGps().getSoc();
				long endT = p.getSt().getGps().getTime() + 
						Math.round((p.getSt().getLink().getLength()-p.getSt().getPrjDistanceFormSNode())/spd);
				
				double endSoc = p.getSt().getGps().getSoc() - (endT - p.getSt().getGps().getTime())*delta;
				
				res.add(p.getSt().getLink().getId()+ ","+ startT +","+endT+","+startSoc+","+endSoc);
				
				long baseT = endT;
				double baseSoc = endSoc;
				
				if(p.getPath() != null  && p.getPath().size() > 0){
					
					
					for(Link lnk : p.getPath()){
						String id = lnk.getId();
						long mybaseT = baseT + (lnk.getLength() / spd);
						double mybaseSoc = baseSoc + (lnk.getLength() / spd) * delta;
						
						res.add(id+","+baseT+","+mybaseT+","+baseSoc+","+mybaseSoc);
						baseT = mybaseT;
						baseSoc = mybaseSoc;
					}
					
					
				}
				
				if(!p.getSt().getLink().getId().equalsIgnoreCase(p.getEd().getLink().getId())){
					endT = baseT + (p.getEd().getLink().getLength()/spd);
					endSoc = baseSoc + (p.getEd().getLink().getLength()/spd) * delta;
					res.add(p.getEd().getLink().getId()+ ","+ baseT +","+endT+","+baseSoc+","+endSoc);
				}
			}
			
		}
		return res;
	}
	
	public static List<List<CandidatePath>> truncate(List<List<CandidatePath>> paths ) throws IOException{
		//BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outfile) ));
		List<List<CandidatePath>> truncatedPaths = new ArrayList<List<CandidatePath>>();
		if(paths.size() == 0) return truncatedPaths;
		
		for(List<CandidatePath> path: paths){
			List<CandidatePath> n = new ArrayList<CandidatePath>();
			if(path.size() == 0){continue;}
			n.add(path.get(0));
			for(int i =1 ; i<path.size(); i++){
				CandidatePath pre = n.get(n.size()-1);
				CandidatePath cur = path.get(i);
				
				if(pre.getSt().getGps().getSoc() == pre.getEd().getGps().getSoc()
						&& pre.getEd().getLink().getId().equalsIgnoreCase(cur.getSt().getLink().getId())){
					pre.setEd(cur.getEd());
					
					if((pre.getPath() == null)){
						pre.setPath(new ArrayList<Link>());
					}
					
					if(!cur.getSt().getLink().getId().equalsIgnoreCase(cur.getEd().getLink().getId())){
						if(!cur.getSt().getLink().getId().equalsIgnoreCase(pre.getSt().getLink().getId()))
							pre.getPath().add(cur.getSt().getLink());
					}
					
					if(cur.getPath()!=null){
						pre.getPath().addAll(cur.getPath());
					}
					
				    pre.setCost(pre.getCost()+cur.getCost());
				    pre.setPathLength(pre.getPathLength() + cur.getPathLength());
				}else{
					n.add(cur);
				}
			}
			
			truncatedPaths.add(n);			
		}
		return truncatedPaths;
	}
	
	public static String concatLink(List<Link> links){
		if(links == null || links.size()==0){return "0,0,,";}
		String r = links.size()+",";
		int sum = 0;
		String ids ="";
	    for(Link lnk :links){
	    	ids+=","+lnk.getId();
	    	sum += lnk.getLength();
	    }
		return r + sum + ids;
	}
	
	private static boolean checkBreak(List<List<CandidatePath>> paths,List<CandidatePath> cur){
		if(paths.size()<1){ return false;}
		if(cur.size()<1){
			return true;
		}
		List<CandidatePath> pre = paths.get(paths.size()-1);
		
		//判断前后两点的候选Link没有连接起来
		HashSet<String> en = new HashSet<String>();
		for(CandidatePath c:pre){
			en.add(c.getEd().getLink().getId());
		}
		HashSet<String> sn = new HashSet<String>();
		for(CandidatePath c:cur){
			String lid = c.getSt().getLink().getId();
			if(en.contains(lid)){
				sn.add(lid);
			}
		}
		
		return (sn.size()>0)?false:true;
	}
	
	private static List<CandidatePath> getFinalPathFor(List<List<CandidatePath>> paths){
		//Astar
		AStarPathFinder astar = new AStarPathFinder();
		//自定义拓扑
		PathMap map = new PathMap(paths);
		
		List<CandidatePath> finalPath = new ArrayList<CandidatePath>();
		List<String> finalNodes = new ArrayList<String>();
		double cost = Double.MAX_VALUE;
		int max = 0;
		
		//TODO(起终点去重)
		
		for(int i = 0; i<map.getStartNodes().size(); i++){
			for(int j = 0; j< map.getEndNodes().size(); j++){
				List<String> nodes = astar.find(map, map.getStartNodes().get(i), map.getEndNodes().get(j));
				if(astar.maxnode > max){max = astar.maxnode;}
				if(cost>astar.getCost()){
					cost = astar.getCost();
					finalNodes = nodes;
				}
			}
		}
		if(max < paths.size()){//从不连通处重新匹配
			List<List<CandidatePath>> pre = paths.subList(0, max);
			List<CandidatePath> part1 = getFinalPathFor(pre);
			List<List<CandidatePath>> tail = paths.subList(max, paths.size());
			List<CandidatePath> part2 = getFinalPathFor(tail);
			part1.addAll(part2)	;	
			return part1;
		}
		
		for(int i =1;i<finalNodes.size();i++){
			finalPath.add(map.getPath(finalNodes.get(i-1), finalNodes.get(i)));
		}
		
		return finalPath;
	}

	/**
	 * 调整候选路径的权重
	 * @param path
	 */
	private static void ajustWeightForPath(CandidatePath path){
		
		CandidatePrj st = path.getSt();
		CandidatePrj ed = path.getEd();
		String slinkId = st.getLink().getId();
		String elinkId = ed.getLink().getId();
		
		if (slinkId.equalsIgnoreCase( elinkId )) {
			path.setPathLength(ed.getPrjDistanceFormSNode() - st.getPrjDistanceFormSNode());			
		} else {
			path.setPathLength (path.getPathLength() + st.getLink().getLength() 
					- st.getPrjDistanceFormSNode() + ed.getPrjDistanceFormSNode());	

        //TUDO(omit U turn circumstance)
			
		}
		if(path.getPathLength() < 0) {
			path.setPathLength(1d);//不允许后退	
		}
	}
	
	/**
	 * 
	 * @param s
	 * @param e
	 * @return
	 */
	public static List<CandidatePath> getPaths(List<CandidatePrj> s, List<CandidatePrj> e){

		List<CandidatePath> paths = null;
		if (s != null && e != null && s.size() >0 && e.size() > 0) {
			try {
				paths = new ArrayList<CandidatePath>();
				HashMap<String,CandidatePath> id2cmp = new HashMap<String,CandidatePath>();
				for (int i = 0; i < s.size(); i++) {
					for (int j = 0; j < e.size(); j++) {
						//start and end link are the same!
						Link slink = s.get(i).getLink();
						Link elink = e.get(j).getLink();
						boolean sameLink = slink.getId().equalsIgnoreCase( elink.getId());
						boolean jointLink = slink.getEnodeID().longValue() == elink.getSnodeID().longValue();
						if ((!sameLink)&& (!jointLink)) {//前后两个点的候选Link不同并且没有相连，需要补全路径
							String mid = slink.getEnodeID() + "_" + elink.getSnodeID();
							
							if(!id2cmp.containsKey(mid)){
								//查找neo4j 用最短路径 补全路径
								WeightedPath wp = null;
								try{
									wp = astar.find(s.get(i).getLink().getEnodeID()+"",
											e.get(j).getLink().getSnodeID()+"");
								}catch(Exception exc){
									exc.printStackTrace();
								}
								if(wp == null){continue;}
								
								double w = wp.weight();
								List<Link> lnks = new ArrayList<Link>();
								for(Relationship r:wp.relationships()){
									String id = r.getProperty("id")+"";
									Integer len = (Integer)r.getProperty("length");
									Long sn = (Long)r.getStartNode().getProperty("id");
									Long en = (Long)r.getEndNode().getProperty("id");
									lnks.add(new Link(id,false,sn,en,len,null));
								}
								CandidatePath path = new CandidatePath(s.get(i),e.get(j),lnks,w,w);
								ajustWeightForPath(path);							
								id2cmp.put(mid, path);
								if(!filterPath(path)){
									paths.add(path);
								}
							}else{
								CandidatePath cache = id2cmp.get(mid);
								CandidatePath path = new CandidatePath(s.get(i),e.get(j),
										cache.getPath(),cache.getCost(),cache.getCost());
								ajustWeightForPath(path);							
								
								if(!filterPath(path)){
									paths.add(path);
								}
							}
							
						} else {//前后两个点的候选Link相同或者相连
							CandidatePath path = new CandidatePath(s.get(i),e.get(j), null, 0d,Double.MAX_VALUE);
							ajustWeightForPath(path);
							if(!filterPath(path)){
								paths.add(path);
							}
						}
					}
				}
				

				
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
		return paths;
	
	}
	
	/**
	 * 车速过滤
	 * @param p
	 * @return
	 */
	private static boolean filterPath(CandidatePath p){
		double len = p.getPathLength();
		long t = (p.getEd().getGps().getTime() - p.getSt().getGps().getTime())/1000;

		if(t==0){t=1;}
		double speed = (len / t) * 3.6;
		return (speed > 200) || (speed < 0 );
	}
	

}
