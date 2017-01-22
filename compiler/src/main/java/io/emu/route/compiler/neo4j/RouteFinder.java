package io.emu.route.compiler.neo4j;
import java.util.HashMap;
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
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;

public class RouteFinder {
	static CostEvaluator<Double> costEval = CommonEvaluators.doubleCostEvaluator("length");
	static EstimateEvaluator<Double> estimateEval = CommonEvaluators.geoEstimateEvaluator("lat","lon");
	static PathFinder<WeightedPath> astar = GraphAlgoFactory.aStar(PathExpanders.forDirection(Direction.OUTGOING),costEval, estimateEval);
//	static PathFinder<WeightedPath> astar = new CnNeo4JAStar(Traversal.expanderForAllTypes(Direction.OUTGOING),costEval, estimateEval);
	
	
	static GraphDatabaseService graphDb=null;
	static String graphDbPath=null;
	static HashMap<String,String> configuration = null;
	
	public static WeightedPath find(GraphDatabaseService graphDb,String snode,String enode){
		IndexManager index = graphDb.index();
		Index<Node> nodeIndex = index.forNodes( "nodeid" );
		WeightedPath path =null;
		try{	
			Node sNode=nodeIndex.get("nodeid", snode).getSingle();
			Node eNode=nodeIndex.get("nodeid", enode).getSingle();			
			path = astar.findSinglePath( sNode, eNode );
		}catch (Exception e) {
			e.printStackTrace();
		}
		return path;
	}
}
