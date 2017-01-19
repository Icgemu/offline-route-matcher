package org.icgemu.route.matcher.matching.mapmatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.neo4j.graphalgo.CommonEvaluators;
import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.EstimateEvaluator;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.kernel.Traversal;

import com.iq.AStarPath;


public class Neo4JAstar {
	static CostEvaluator<Double> costEval = CommonEvaluators.doubleCostEvaluator("length");
	static EstimateEvaluator<Double> estimateEval = CommonEvaluators.geoEstimateEvaluator("x","y");
//	static PathFinder<WeightedPath> astar = GraphAlgoFactory.aStar(Traversal.expanderForAllTypes(),costEval, estimateEval);
	static PathFinder<WeightedPath> astar = new AStarPath(Traversal.expanderForAllTypes(Direction.OUTGOING),costEval, estimateEval);
	
	
	static GraphDatabaseService graphDb=null;
	static String graphDbPath=null;
	static HashMap<String,String> configuration = null;
	
	public Neo4JAstar(GraphDatabaseService graphDb){
		this.graphDb=graphDb;	
		//init();
	}
	
	public static WeightedPath find(GraphDatabaseService graphDb,String snode,String enode){
		Transaction tx = graphDb.beginTx();
		WeightedPath path =null;
		try{
		IndexManager index = graphDb.index();
		Index<Node> nodeIndex = index.forNodes( "NodeIdIndex" );
		
			
			Node sNode=nodeIndex.get("id", snode).getSingle();
			Node eNode=nodeIndex.get("id", enode).getSingle();
			
			path = astar.findSinglePath( sNode, eNode );
		}catch (Exception e) {
			tx.failure();
			e.printStackTrace();
		}finally{
			tx.success();
		}
		return path;
	}
	
	public static List<WeightedPath> find(GraphDatabaseService graphDb,List<String> sNodes,List<String> eNodes){
		IndexManager index = graphDb.index();
		Index<Node> nodeIndex = index.forNodes( "NodeIdIndex" );
		List<WeightedPath> paths=new ArrayList<WeightedPath>();
		
		for(int i=0;i<sNodes.size();i++){
			WeightedPath path =null;
			try{
				Node sNode=nodeIndex.get("id", sNodes.get(i)).getSingle();
				Node eNode=nodeIndex.get("id", eNodes.get(i)).getSingle();				
				path = astar.findSinglePath( sNode, eNode );	
			}catch (Exception e) {
				//e.printStackTrace();
			}
			paths.add(path);			
		}
		
		return paths;
	}
	
	
	public  WeightedPath find(String snode,String enode){
		return find(graphDb,snode,enode);
	}
	
	public static List<WeightedPath> find(List<String> sNodes,List<String> eNodes){
		return find(graphDb,sNodes,eNodes);
	}
	
}
