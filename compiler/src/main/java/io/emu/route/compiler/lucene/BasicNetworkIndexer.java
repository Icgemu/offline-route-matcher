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

	public void index(File cell_file, File route_file, File out_dir) throws Exception {
		FSDirectory outDir = FSDirectory.open(out_dir.toPath());
		Analyzer analyzer = new StandardAnalyzer();

		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		// config.
		IndexWriter idxWriter = new IndexWriter(outDir, config);

		File[] files = new File[] { cell_file, route_file };
		long t1 = System.currentTimeMillis();
		for (File file : files) {
			BufferedReader bf = new BufferedReader(new FileReader(file));

			String line = null;
			while ((line = bf.readLine()) != null) {

				String id = line.split(":")[0];			
     			Document info = new Document();
				info.add(new StringField("id", id, Field.Store.NO));
				FieldType type = new FieldType();
				type.setIndexOptions(IndexOptions.NONE);
				type.setStored(true);
				type.setTokenized(false);
				info.add(new Field("info", line, type));
				
				idxWriter.addDocument(info);
			}
			bf.close();
		}
		idxWriter.forceMerge(1);
		idxWriter.close();

		long t2 = System.currentTimeMillis();
		System.out.println((t2 - t1) / 1000 + "s");
	}

	public static void main(String[] args) throws Exception {
		// 小网格数据
		File cell_file = new File("./C-G.csv");
		// R表数据
		File route_file = new File("./R-G.csv");
		// lucene 索引存放目录
		File out_dir = new File("./mm-basic");
		if(! out_dir.exists() ){
			out_dir.mkdir();
		}
		new BasicNetworkIndexer().index(cell_file, route_file, out_dir);

	}

}
