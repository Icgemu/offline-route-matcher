package org.icgemu.route.matcher.matching.mapmatch;

import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;



public class AStarPathFinder extends AStar<String>{
	private PathMap map;
	private String from;	//source	
	private String to; //target	

	@Override
	protected Double g(String from, String to) {		
		return map.getCost(from, to);
	}

	@Override
	protected List<String> generateSuccessors(String node) {		
		return map.getAdjNodes(node);
	}

	@Override
	protected Double h(String from, String to) {
		return map.getCost(from, to);
	}
	/**
	 * Expand a path.
	 *
	 * @param path The path to expand.
	 */
	@Override
	protected void expand(Path path){
			String p = path.getPoint();
			Double min = mindists.get(path.getPoint());

			/*
			 * If a better path passing for this point already exists then
			 * don't expand it.
			 */
			if(min == null || min.doubleValue() > path.f.doubleValue())
					mindists.put(path.getPoint(), path.f);
			else
					return;

			List<String> successors = generateSuccessors(p);
			if(successors!=null){//如果是孤点，死胡同，无法展开
				for(String t : successors){
						Path newPath = new Path(path);
						newPath.setPoint(t);
						f(newPath, path.getPoint(), t);
						paths.offer(newPath);
				}
			}else{
				paths.remove(path);
			}
			expandedCounter++;
	}
	@Override
	protected boolean isGoal(String node) {		
		return node.equalsIgnoreCase(to);
	}
	
	public List<String> find(PathMap map,String sid, String eid){
		paths = new PriorityQueue<Path>();
		mindists = new HashMap<String, Double>();
		expandedCounter = 0;
		lastCost = 0.0;
		
		this.map = map;	
		this.from = sid;
		this.to = eid;
		return compute(this.from);
	}
}
