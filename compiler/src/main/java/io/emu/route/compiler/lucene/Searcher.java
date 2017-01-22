package io.emu.route.compiler.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;
public class Searcher {

	static void search(String file, String outPath) throws Exception {
		FSDirectory dir = FSDirectory.open(new File(outPath).toPath());
		DirectoryReader ireader = DirectoryReader.open(dir);
		BufferedReader bf = new BufferedReader(new FileReader(file));
		IndexSearcher isearcher = new IndexSearcher(ireader);
		String line = null;
		while ((line = bf.readLine()) != null) {
			String id = line.split("#")[0].split("_")[0];
			Query query = new TermQuery(new Term("id", id));
			ScoreDoc[] hits = isearcher.search(query, 1).scoreDocs;
			for (int i = 0; i < hits.length; i++) {
				Document hitDoc = isearcher.doc(hits[i].doc);
				System.out.print(id + "#");
				System.out.println(hitDoc.get("info"));

			}
		}
		ireader.close();
		dir.close();
		bf.close();

	}

	public static void main(String[] args) throws Exception {
		search("E:/Prj/OD/route-0.csv", "E:/Prj/OD/mm-db-tree");
	}

}
