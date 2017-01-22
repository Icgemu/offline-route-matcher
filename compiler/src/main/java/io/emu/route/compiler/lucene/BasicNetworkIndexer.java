package io.emu.route.compiler.lucene;

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

public class BasicNetworkIndexer {

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
			while ((line = bf.readLine()) != null) {

				String[] sps = line.split(":");

				String id = sps[0];
     			Document doc = new Document();

				doc.add(new StringField("id", id, Field.Store.NO));
				FieldType type = new FieldType();
				type.setIndexOptions(IndexOptions.NONE);
				type.setStored(true);
				type.setTokenized(false);
				doc.add(new Field("info", line, type));
				
				iwriter.addDocument(doc);
			}
			bf.close();
		}
		iwriter.forceMerge(1);
		iwriter.close();

		long t2 = System.currentTimeMillis();
		System.out.println((t2 - t1) / 1000 + "s");
	}

	public static void main(String[] args) throws Exception {
		
		index();
	}

}
