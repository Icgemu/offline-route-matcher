package io.emu.route.compiler.neo4j;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Spliter {

	static File[] split(File in, int cnt) throws IOException {
		File par = in.getParentFile();

		if (cnt == 1) {
			return new File[] { in };
		}

		File[] file = new File[cnt];
		BufferedWriter[] bws = new BufferedWriter[cnt];
		for (int i = 0; i < cnt; i++) {
			File t = new File(par, "__test" + i + ".csv");
			file[i] = t;
			if (t.exists()) {
				t.delete();
			}
			try {
				bws[i] = new BufferedWriter(new FileWriter(t));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


		BufferedReader bf = new BufferedReader(new FileReader(in));

		String line = null;
		int c = 1;
		while ((line = bf.readLine()) != null) {
			bws[c % cnt].write(line + "\n");
			c++;
		}

		bf.close();

		for (int i = 0; i < cnt; i++) {
			try {
				bws[i].close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file;
	}
}
