/*
 * Class Name:    Indexer.java
 * Description:   TODO(类的功能描述)
 * Version:       2014年3月8日 上午11:25:50
 * Author:        Administrator
 * Copyright 2010 Cennavi Corp, All Rights Reserved.
 */
package io.emu.route.matcher.compiler.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

/**
 * 索引小网格数据
 * 
 * @author Administrator
 * @version 2014年3月8日 上午11:25:50
 */
public class CellRouteIndexer {

	static void index() throws Exception {
		// lucene 索引存放目录
		String outPath = "E:/Prj/OD/test/mm-db-cr";
		FSDirectory outDir = FSDirectory.open(new File(outPath).toPath());
		Analyzer analyzer = new StandardAnalyzer();

		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		// config.
		IndexWriter iwriter = new IndexWriter(outDir, config);
		// 小网格数据
		File f = new File("E:/Prj/OD/test/C-G.csv");
		// R表数据
		File f1 = new File("E:/Prj/OD/test/R-G.csv");

		File[] files = new File[] { f, f1 };
		long t1 = System.currentTimeMillis();
		for (File file : files) {
			BufferedReader bf = new BufferedReader(new FileReader(file));

			String line = null;

			// String lastID = "";
			// HashMap<String,Integer> map = new HashMap<String,Integer>();
			int i = 0;
			while ((line = bf.readLine()) != null) {

				String[] sps = line.split(":");

				String id = sps[0];
				// if(id.split("_")[0].equalsIgnoreCase(lastID)){
				// if(map.containsKey(id)){
				// i++;
				// continue;
				// }
				// map.put(id, 1);
				// }else{
				// map.clear();map.put(id, 1);
				// }
				Document doc = new Document();

				// if(types == 0){
				doc.add(new StringField("id", id, Field.Store.NO));
				FieldType type = new FieldType();
				type.setIndexOptions(IndexOptions.NONE);
				type.setStored(true);
				type.setTokenized(false);

				// doc.add(new Field("info",line,type));
				doc.add(new Field("info", line, type));
				// }else{
				// String lonlat = sps[sps.length -1];
				// String info =
				// sps[1]+","+sps[2]+","+sps[3]+","+sps[4]+","+sps[5]+","+sps[6]+","+sps[7]+","+sps[8];
				//
				// doc.add(new StringField("id",id,Field.Store.NO));
				//
				// FieldType type1 = new FieldType();
				// type1.setIndexed(false);
				// type1.setStored(true);
				// type1.setTokenized(false);
				//
				// FieldType type2 = new FieldType();
				// type2.setIndexed(false);
				// type2.setStored(true);
				// type2.setTokenized(false);
				//
				// doc.add(new Field("info",info,type1));
				//
				// doc.add(new Field("lonlat",lonlat,type2));
				// }
				iwriter.addDocument(doc);
			}
			//System.out.println("dupilicate:" + i);
			bf.close();
		}
		iwriter.forceMerge(1);
		iwriter.close();

		long t2 = System.currentTimeMillis();
		System.out.println((t2 - t1) / 1000 + "s");
	}

	/**
	 * TODO(功能描述)
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		// String in = args[0];
		// String out = args[1];
		// int type = Integer.parseInt(args[2]);
		index();
	}

}
