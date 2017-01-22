package io.emu.route.matcher.matching;

import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

/** ͨ�ñ��� 
 * @author Chunbai
 *
 */
public class Constant {
	
	/**����Ȩ��
	 * 
	 */
	public static CostEvaluator<Double> costEvaluator=new CostEvaluator<Double>(){
		double totalCost=0d;
		@Override
		public Double getCost(Relationship rel, Direction direct) {
			System.out.println(rel.getProperty("id")+"@"+rel.getProperty("length"));
			totalCost=totalCost+Double.parseDouble(rel.getProperty("length").toString());
			return totalCost;
		}

	};
	
//	EstimateEvaluator<Double> estimateEval = new EstimateEvaluator<Double>(){
//	   @Override
//	   public Double getCost( final Node node, final Node goal ){
//			 double dx = (Double) node.getProperty( "x" ) - (Double) goal.getProperty( "x" );
//			 double dy = (Double) node.getProperty( "y" ) - (Double) goal.getProperty( "y" );
//			 double result = Math.sqrt( Math.pow( dx, 2 ) + Math.pow( dy, 2 ) );
//			 return result;
//		}
//	};
	
	public static enum Road implements RelationshipType{
        direction
    }
}
