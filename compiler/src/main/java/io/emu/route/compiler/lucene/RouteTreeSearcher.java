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

public class RouteTreeSearcher {

	static void search(String file, String outPath) throws Exception {
		FSDirectory dir = FSDirectory.open(new File(outPath).toPath());
		DirectoryReader ireader = DirectoryReader.open(dir);
		BufferedReader bf = new BufferedReader(new FileReader(file));

		IndexSearcher isearcher = new IndexSearcher(ireader);
		String line = "";
		while ((line = bf.readLine()) != null) {

			String snode = line.split("#")[0].split(",")[0];
			String enode = line.split("#")[0].split(",")[1];
			Query query = new TermQuery(new Term("id", "n" + snode));
			ScoreDoc[] hits = isearcher.search(query, 1).scoreDocs;
			System.out.println(snode + "," + enode + "#" + line.split("#")[1]);
			for (int i = 0; i < hits.length; i++) {
				Document hitDoc = isearcher.doc(hits[i].doc);
				String info = hitDoc.get("info");
				int index = info.indexOf(";n" + enode + ",");
				if (index < 0) {
					System.out.println(snode + "->" + enode + " is null.");
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
					}
				}
				System.out.println(snode + "," + enode + "#"
						+ lnks.substring(1));
			}
		}
		ireader.close();
		dir.close();
		bf.close();

	}

	public static void main(String[] args) throws Exception {
		search("E:/Prj/OD/my-route-sample.csv", "E:/Prj/OD/mm-db-tree-3");
	}

}
