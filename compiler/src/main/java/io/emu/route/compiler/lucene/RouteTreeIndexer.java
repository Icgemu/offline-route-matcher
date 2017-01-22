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
import java.util.HashMap;
import java.util.Iterator;
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
import org.apache.lucene.util.Version;

/**
 * 读取预编译的路径，处理成树结构，写入文件或lucene
 * 
 * @author Administrator
 * @version 2014年3月8日 上午11:25:50
 */
public class RouteTreeIndexer {
	static void index(String in, String out) throws Exception {
		// BufferedWriter wt = new BufferedWriter(new FileWriter(new
		// File("E:/Prj/OD/pre-rt.csv")));
		BufferedWriter wt = new BufferedWriter(new FileWriter(new File(out)));
		// File f = new File("E:/Prj/OD/my-route-0.csv");
		File f = new File(in);
		// File f1 = new File("E:/Prj/OD/R-G.csv");
		File[] files = new File[] { f };
		long t1 = System.currentTimeMillis();
		for (File file : files) {
			BufferedReader bf = new BufferedReader(new FileReader(file));
			String line = null;
			String lastID = "";
			// String prefix = "";
			Node root = null;
			// int i = 0;
			while ((line = bf.readLine()) != null) {
				// System.out.println(line);
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
							// Document doc = new Document();
							// write(lastID,root,doc);
							writeFile(lastID, root, wt);
							// iwriter.addDocument(doc);
						}
					}
					// System.out.print(lastID+"->");
					lastID = id;
					// System.out.println(lastID);
					root = new Node();
				}
			}
			if (!root.map.isEmpty()) {
				// Document doc = new Document();
				// write(lastID,root,doc);
				writeFile(lastID, root, wt);
				// iwriter.addDocument(doc);
			}
			// System.out.println("dupilicate:"+i);
			bf.close();
		}
		// iwriter.forceMerge(1);
		// iwriter.close();
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
				// TODO Auto-generated method stub
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
		// String outPath = "E:/Prj/OD/mm-db-tree-2";
		String outPath = out;
		FSDirectory outDir = FSDirectory.open(new File(outPath).toPath());
		Analyzer analyzer = new StandardAnalyzer();

		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		config.setUseCompoundFile(false);
		// config.
		IndexWriter iwriter = new IndexWriter(outDir, config);
		// File f = new File("E:/Prj/OD/my-route-0.csv");
		File f = new File(in);
		// File f1 = new File("E:/Prj/OD/R-G.csv");
		File[] files = new File[] { f };
		long t1 = System.currentTimeMillis();
		for (File file : files) {
			BufferedReader bf = new BufferedReader(new FileReader(file));
			String line = null;
			String lastID = "";
			// String prefix = "";
			Node root = null;
			// int i = 0;
			while ((line = bf.readLine()) != null) {
				// System.out.println(line);
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
					// System.out.print(lastID+"->");
					lastID = id;
					// System.out.println(lastID);
					root = new Node();
					put(root, info);
				}
			}
			if (!root.map.isEmpty()) {
				Document doc = new Document();
				write(lastID, root, doc);
				iwriter.addDocument(doc);
			}
			// System.out.println("dupilicate:"+i);
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
				// TODO Auto-generated method stub
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
		// doc.add(new Field("info",line,type));
		try {
			doc.add(new Field("info", head.substring(1), type));
		} catch (Exception e) {
			System.exit(-1);
			e.printStackTrace();
		}
	}

	static void idx(Node root, AtomicInteger i) {
		// int i = -1;
		root.idx = i.getAndAdd(1);
		for (String key : root.map.keySet()) {
			root.get(key).parent = root.idx;
			idx(root.get(key), i);
		}
	}

	static void build(Node root, List<String> head) {
		// int i = -1;
		for (String key : root.map.keySet()) {
			head.add(key + "," + root.get(key).parent + "," + root.get(key).idx
					+ "," + root.get(key).lev);
		}
		for (String key : root.map.keySet()) {
			build(root.get(key), head);
		}
		// return head;
	}

	/**
	 * TODO(功能描述)
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String in = "E:/Prj/OD/test/my-route-0.csv";
		String out = "E:/Prj/OD/test/mm-db-rt-tree";
		int type = 2;
		// String in = args[0];
		// String out = args[1];
		// int type = Integer.parseInt(args[2]);
		if (type == 1) {// 写到lucene
			index2(in, out);
		} else {// 写到文件
			index(in, out);
		}

	}

}
