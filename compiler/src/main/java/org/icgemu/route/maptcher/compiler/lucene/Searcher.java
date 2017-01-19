/*
 * Class Name:    Searcher.java
 * Description:   TODO(类的功能描述)
 * Version:       2014年3月8日 下午12:36:00
 * Author:        Administrator
 * Copyright 2010 Cennavi Corp, All Rights Reserved.
 */
package org.icgemu.route.maptcher.compiler.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * TODO(这里用一句话描述这个类的作用)
 * 
 * @author Administrator
 * @version 2014年3月8日 下午12:36:00
 */
public class Searcher {

	static void search(String file, String outPath) throws Exception {
		FSDirectory dir = FSDirectory.open(new File(outPath).toPath());

		DirectoryReader ireader = DirectoryReader.open(dir);

		BufferedReader bf = new BufferedReader(new FileReader(file));

		Analyzer analyzer = new StandardAnalyzer();

		// IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47,
		// analyzer);
		// config.
		// IndexWriter iwriter = new IndexWriter(dir, config);
		IndexSearcher isearcher = new IndexSearcher(ireader);
		String line = null;
		while ((line = bf.readLine()) != null) {
			long t1 = System.currentTimeMillis();
			String id = line.split("#")[0].split("_")[0];
			// QueryParser parser = new QueryParser(Version.LUCENE_47, "id",
			// analyzer);
			Query query = new TermQuery(new Term("id", id));
			ScoreDoc[] hits = isearcher.search(query, null, 1).scoreDocs;
			// assertEquals(1, hits.length);
			// Iterate through the results:
			for (int i = 0; i < hits.length; i++) {
				Document hitDoc = isearcher.doc(hits[i].doc);
				// String ids = hitDoc.get("id");
				System.out.print(id + "#");
				System.out.println(hitDoc.get("info"));

				// if(!line.equalsIgnoreCase(hitDoc.get("info"))){
				// System.out.println(ids);
				// }
			}
			long t2 = System.currentTimeMillis();
			// System.out.println(t2-t1);
		}
		ireader.close();
		dir.close();
		// iwriter.forceMerge(1);
		// iwriter.close();
		bf.close();

	}

	/**
	 * TODO(功能描述)
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		search("E:/Prj/OD/route-0.csv", "E:/Prj/OD/mm-db-tree");
	}

}
