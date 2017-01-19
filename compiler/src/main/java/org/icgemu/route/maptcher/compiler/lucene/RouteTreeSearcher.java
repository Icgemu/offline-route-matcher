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
public class RouteTreeSearcher {

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
		String line = "";
		while ((line = bf.readLine()) != null) {
			long t1 = System.currentTimeMillis();

			String snode = line.split("#")[0].split(",")[0];
			// String id = line;
			// String[] last = line.split("#")[1].split(",");
			String enode = line.split("#")[0].split(",")[1];
			// QueryParser parser = new QueryParser(Version.LUCENE_47, "id",
			// analyzer);
			Query query = new TermQuery(new Term("id", "n" + snode));
			ScoreDoc[] hits = isearcher.search(query, null, 1).scoreDocs;
			// assertEquals(1, hits.length);
			// Iterate through the results:
			System.out.println(snode + "," + enode + "#" + line.split("#")[1]);
			for (int i = 0; i < hits.length; i++) {
				Document hitDoc = isearcher.doc(hits[i].doc);
				// String ids = hitDoc.get("id");

				String info = hitDoc.get("info");

				int index = info.indexOf(";n" + enode + ",");

				if (index < 0) {
					System.out.println(snode + "->" + enode + " is null.");
					// lst.add(null);
					continue;
				}
				String head = info.substring(0, index);
				String end = info.substring(index + 1).split(";")[0];
				String[] infs = end.split(",");

				String parent = infs[2];

				String[] ins = head.split(";");
				String lnks = "";
				for (int j = ins.length - 1; j >= 0; j--) {
					String[] cur = ins[j].split(",");
					try {
						if (cur[3].equalsIgnoreCase(parent)) {
							lnks = ";" + cur[0] + "," + cur[1] + lnks;
							parent = cur[2];
							if (parent.equalsIgnoreCase("-1")) {
								break;
							}
						}
					} catch (Exception e1) {
						e1.printStackTrace();
						// System.out.println("");
					}
				}
				// lst.add(lnks.substring(1));
				System.out.println(snode + "," + enode + "#"
						+ lnks.substring(1));
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
		search("E:/Prj/OD/my-route-sample.csv", "E:/Prj/OD/mm-db-tree-3");
	}

}
