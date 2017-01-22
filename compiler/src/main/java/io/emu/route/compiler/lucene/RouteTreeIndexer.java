package io.emu.route.compiler.lucene;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
	static void index(String in, String out) throws Exception {
		BufferedWriter wt = new BufferedWriter(new FileWriter(new File(out)));
		File f = new File(in);
		File[] files = new File[] { f };
		long t1 = System.currentTimeMillis();
		for (File file : files) {
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
							writeFile(lastID, root, wt);
						}
					}
					lastID = id;
					root = new Node();
				}
			}
			if (!root.map.isEmpty()) {
				writeFile(lastID, root, wt);
			}
			bf.close();
		}
		wt.close();
		long t2 = System.currentTimeMillis();
		System.out.println((t2 - t1) / 1000 + "s");
	}

	static void writeFile(String id, Node root, BufferedWriter wt)
			throws IOException {
		AtomicInteger i = new AtomicInteger(-1);
		idx(root, i);
		List<String> str = new ArrayList<String>();
		build(root, str);
		System.out.println(id);
		Collections.sort(str, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				int rst = 0;
				try {
					int a1 = Integer.parseInt(o1.split(",")[4]);
					int a2 = Integer.parseInt(o2.split(",")[4]);

					if (a1 == a2)
						rst = 0;
					if (a1 > a2)
						rst = 1;
					rst = -1;
				} catch (Exception e) {

					System.out.println(o1);
					System.out.println(o2);
					System.out.println("-------");
				}
				return rst;
			}

		});
		String head = "";
		for (String l : str) {
			head += ";" + l.substring(0, l.lastIndexOf(","));
		}

		wt.write(id + "#" + head.substring(1) + "\n");

	}

	static void index2(String in, String out) throws Exception {
		String outPath = out;
		FSDirectory outDir = FSDirectory.open(new File(outPath).toPath());
		Analyzer analyzer = new StandardAnalyzer();

		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		config.setUseCompoundFile(false);
		IndexWriter iwriter = new IndexWriter(outDir, config);
		File f = new File(in);
		File[] files = new File[] { f };
		long t1 = System.currentTimeMillis();
		for (File file : files) {
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
							iwriter.addDocument(doc);
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
				iwriter.addDocument(doc);
			}
			bf.close();
		}
		iwriter.forceMerge(1);
		iwriter.close();

		long t2 = System.currentTimeMillis();
		System.out.println((t2 - t1) / 1000 + "s");
	}

	static void put(Node root, String links) {

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

	static void write(String id, Node root, Document doc) {
		AtomicInteger i = new AtomicInteger(-1);
		idx(root, i);
		List<String> str = new ArrayList<String>();
		build(root, str);

		Collections.sort(str, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				int rst = 0;
				try {
					int a1 = Integer.parseInt(o1.split(",")[4]);
					int a2 = Integer.parseInt(o2.split(",")[4]);

					if (a1 == a2)
						rst = 0;
					if (a1 > a2)
						rst = 1;
					rst = -1;
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
			System.exit(-1);
			e.printStackTrace();
		}
	}

	static void idx(Node root, AtomicInteger i) {
		root.idx = i.getAndAdd(1);
		for (String key : root.map.keySet()) {
			root.get(key).parent = root.idx;
			idx(root.get(key), i);
		}
	}

	static void build(Node root, List<String> head) {
		for (String key : root.map.keySet()) {
			head.add(key + "," + root.get(key).parent + "," + root.get(key).idx
					+ "," + root.get(key).lev);
		}
		for (String key : root.map.keySet()) {
			build(root.get(key), head);
		}
	}


	public static void main(String[] args) throws Exception {
		String in = "E:/Prj/OD/test/my-route-0.csv";
		String out = "E:/Prj/OD/test/mm-db-rt-tree";
		int type = 2;
		if (type == 1) {// 写到lucene
			index2(in, out);
		} else {// 写到文件
			index(in, out);
		}

	}

}
