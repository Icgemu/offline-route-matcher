package io.emu.route.matcher.matching;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.neo4j.graphalgo.CommonEvaluators;
import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.EstimateEvaluator;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PathExpanders;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;


/** �������·��
 * @author Chunbai
 *
 */
public class STPathFinder {
	static CostEvaluator<Double> costEval = CommonEvaluators.doubleCostEvaluator("length");
	static EstimateEvaluator<Double> estimateEval = CommonEvaluators.geoEstimateEvaluator("y","x");
	PathFinder<WeightedPath> astar = new AStarPath(PathExpanders.forDirection(Direction.OUTGOING),costEval, estimateEval);
    GraphDatabaseService graphDb=null;
    
    public STPathFinder(String neo4jDbPath){
    	astar   = new AStarPath(PathExpanders.forDirection(Direction.OUTGOING),costEval, estimateEval);
    	graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(new File(neo4jDbPath));
    }
    
    public void find(long snodeID,long enodeID){
		Transaction tx = graphDb.beginTx();
		IndexManager index = graphDb.index();
		Index<Node> nodeIndex = index.forNodes( "NodeIdIndex" );
		
		Node sNode=nodeIndex.get("id", snodeID).getSingle();
		Node eNode=nodeIndex.get("id", enodeID).getSingle();			
		WeightedPath path=astar.findSinglePath( eNode, sNode );
	
		Iterator<Relationship> iterator=path.relationships().iterator();
		float length=0f;
		while(iterator.hasNext()){
			Relationship link=iterator.next();
			System.out.print(link.getProperty("id")+"->");
			length=length+(Integer)link.getProperty("length");			
		}
		System.out.println("");
		System.out.println(length);
	
		tx.success();
    }
    
    public long[] getRelationship(long id){
		Transaction tx = graphDb.beginTx();
		IndexManager index = graphDb.index();
		Index<Relationship> relationshipIndex = index.forRelationships("LinkIdIndex");
		Relationship rel =relationshipIndex.get("id", id).getSingle();
		long sn=rel.getStartNode().getId();
		long en=rel.getEndNode().getId();
		tx.success();
		return new long[]{sn,en};
    }
    
	public static void main(String args[]){
		STPathFinder pf=new STPathFinder("D:/map/map/neo4jdata/map");
		pf.find(34535101628l, 34534100013l);
		pf.find(34534100013l,34535101628l);
	}
}
