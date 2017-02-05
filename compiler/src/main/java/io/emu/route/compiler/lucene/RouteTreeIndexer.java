package io.emu.route.compiler.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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

public class RouteTreeIndexer {

	public void index(String in, String out) throws Exception {
		String outPath = out;
		FSDirectory outDir = FSDirectory.open(new File(outPath).toPath());
		Analyzer analyzer = new StandardAnalyzer();

		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		config.setUseCompoundFile(false);
		IndexWriter idxWriter = new IndexWriter(outDir, config);
		File file = new File(in);
		long t1 = System.currentTimeMillis();
		BufferedReader bf = new BufferedReader(new FileReader(file));
		String line = null;
		String lastID = "";
		Node root = null;
		while ((line = bf.readLine()) != null) {
			String[] sps = line.split("#");
			String[] node = sps[0].split(",");
			String id = "n" + node[0];
			String tail = node[1];
			String info = sps[1] + ";n" + tail + ",0";
			if (id.equalsIgnoreCase(lastID)) {
				put(root, info);
			} else {
				if (!"".equalsIgnoreCase(lastID)) {
					if (!root.map.isEmpty()) {
						Document doc = new Document();
						write(lastID, root, doc);
						idxWriter.addDocument(doc);
					}
				}
				lastID = id;
				root = new Node();
				put(root, info);
			}
		}
		if (!root.map.isEmpty()) {
			Document doc = new Document();
			write(lastID, root, doc);
			idxWriter.addDocument(doc);
		}
		bf.close();
		idxWriter.forceMerge(1);
		idxWriter.close();

		long t2 = System.currentTimeMillis();
		System.out.println((t2 - t1) / 1000 + "s");
	}

	public void put(Node root, String links) {

		String[] ls = links.split(";");
		if (ls.length < 2) {
			return;
		}
		Node last = root;
		int lev = 0;
		for (String lnk : ls) {
			if (last.contains(lnk)) {
				last = last.get(lnk);
			} else {
				last.add(lnk);
				last = last.get(lnk);
			}
			last.lev = lev;
			lev++;
		}
	}

	public void write(String id, Node root, Document doc) {
		AtomicInteger i = new AtomicInteger(-1);
		setTreeIdx(root, i);
		List<String> str = new ArrayList<String>();
		serializeTreeToStr(root, str);

		Collections.sort(str, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				int rst = 0;
				try {
					int a1 = Integer.parseInt(o1.split(",")[4]);
					int a2 = Integer.parseInt(o2.split(",")[4]);

					rst = a1 - a2;
				} catch (Exception e) {
					System.out.println(o1);
					System.out.println(o2);
				}
				return rst;
			}

		});

		doc.add(new StringField("id", id, Field.Store.NO));
		FieldType type = new FieldType();
		type.setIndexOptions(IndexOptions.NONE);
		type.setStored(true);
		type.setTokenized(false);
		String head = "";
		for (String l : str) {
			head += ";" + l.substring(0, l.lastIndexOf(","));
		}
		try {
			doc.add(new Field("info", head.substring(1), type));
		} catch (Exception e) {
			//System.exit(-1);
			e.printStackTrace();
		}
	}

	public void setTreeIdx(Node root, AtomicInteger i) {
		root.idx = i.getAndAdd(1);
		for (String key : root.map.keySet()) {
			root.get(key).parent = root.idx;
			setTreeIdx(root.get(key), i);
		}
	}

	public void serializeTreeToStr(Node root, List<String> head) {
		for (String key : root.map.keySet()) {
			head.add(key + "," + root.get(key).parent + "," + root.get(key).idx
					+ "," + root.get(key).lev);
		}
		for (String key : root.map.keySet()) {
			serializeTreeToStr(root.get(key), head);
		}
	}

	public static void main(String[] args) throws Exception {
		String in = "./route.csv";
		String out = "./db-route";

		new RouteTreeIndexer().index(in, out);

	}

}
