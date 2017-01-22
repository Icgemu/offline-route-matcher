package io.emu.route.matcher;

//package org.icgemu.route.matcher.matching;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.apache.commons.lang.ArrayUtils;
//import org.apache.hadoop.hive.ql.udf.generic.GenericUDTF;
//import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
//import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
//import org.apache.hadoop.hive.ql.metadata.HiveException;
//import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
//import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
//import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
//import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
//import com.iq.thrift.Client;
//import com.iq.thrift.RouteService;
//
//public class MatchingUDTF extends GenericUDTF{
//
//	RouteService.Client client = null;
//    @Override
//    public void close() throws HiveException {
//        // TODO Auto-generated method stub   
//    	Client.closeClient();    
//    }
//
//    @Override
//    public StructObjectInspector initialize(ObjectInspector[] args)
//            throws UDFArgumentException {
//        if (args.length != 2) {
//            throw new UDFArgumentLengthException("MatchingUDTF takes only two argument");
//        }
//        if (args[0].getCategory() != ObjectInspector.Category.PRIMITIVE) {
//            throw new UDFArgumentException("MatchingUDTF takes string as a first parameter");
//        }
//        if (args[1].getCategory() != ObjectInspector.Category.PRIMITIVE) {
//            throw new UDFArgumentException("MatchingUDTF takes string as a second parameter");
//        }
//
//        ArrayList<String> fieldNames = new ArrayList<String>();
//        ArrayList<ObjectInspector> fieldOIs = new ArrayList<ObjectInspector>();
//        fieldNames.add("vin");
//        fieldNames.add("t1");
//        fieldNames.add("slen");
//        fieldNames.add("slinkid");
//        
//        fieldNames.add("t2");
//        fieldNames.add("elen");
//        fieldNames.add("elinkid");
//        
//        fieldNames.add("cnt");
//        fieldNames.add("len");
//        fieldNames.add("ids");
//        
//        fieldOIs.add(PrimitiveObjectInspectorFactory.javaStringObjectInspector);
//        
//        fieldOIs.add(PrimitiveObjectInspectorFactory.javaLongObjectInspector);
//        fieldOIs.add(PrimitiveObjectInspectorFactory.javaLongObjectInspector);
//        fieldOIs.add(PrimitiveObjectInspectorFactory.javaStringObjectInspector);
//        
//        
//        fieldOIs.add(PrimitiveObjectInspectorFactory.javaLongObjectInspector);
//        fieldOIs.add(PrimitiveObjectInspectorFactory.javaLongObjectInspector);
//        fieldOIs.add(PrimitiveObjectInspectorFactory.javaStringObjectInspector);
//        
//        
//
//        fieldOIs.add(PrimitiveObjectInspectorFactory.javaIntObjectInspector);
//        fieldOIs.add(PrimitiveObjectInspectorFactory.javaIntObjectInspector);
//        fieldOIs.add(PrimitiveObjectInspectorFactory.javaStringObjectInspector);
//        client = Client.openClient("localhost", 17777);
//        return ObjectInspectorFactory.getStandardStructObjectInspector(fieldNames,fieldOIs);
//    }
//
//    @Override
//    public void process(Object[] args) throws HiveException {
//    	String vin = args[0].toString();
//    	String input = args[1].toString();
//
//        
//        try {
//        	List<String> paths = client.getMatching(input);
//			
//			for(String path: paths){
//				
//				//System.out.println(path);
//				Object[] result = new Object[10];
//				result[0] = vin;
//				String[] res1 = path.split(",");
//				result[1] = Long.valueOf(res1[0]).longValue();
//				result[2] = Long.valueOf(res1[1]).longValue();;
//				result[3] = res1[2];
//				result[4] = Long.valueOf(res1[3]).longValue();
//				result[5] = Long.valueOf(res1[4]).longValue();
//				result[6] = res1[5];
//				result[7] = Integer.valueOf(res1[6]).intValue();
//				result[8] = Integer.valueOf(res1[7]).intValue();
//				
//				String ids = "";
//				for(int i=8 ;i<res1.length ;i++){
//					ids +=","+res1[i];
//				}
//				if(ids.startsWith(",")){ids = ids.substring(1);}
//				result[9] = ids;
//				//result = ArrayUtils.addAll(result, res1);
//				forward(result);
//				
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    }
//    
//    
//    
//    
//    
//    
//    public static void main(String[] args){
//    	System.out.println("loaded!");
//    	
//    	MatchingUDTF m = new MatchingUDTF();
//    	
//    	System.out.println(m+"loaded!");
//    }
// }