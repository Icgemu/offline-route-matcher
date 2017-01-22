
package io.emu.route.compiler.neo4j;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * TODO(这里用一句话描述这个类的作用)
 * 
 * @author Administrator
 * @version 2014年2月23日 上午12:45:37
 */
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
			// if(!t.exists()){
			try {
				bws[i] = new BufferedWriter(new FileWriter(t));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// }
		}

		// if(file[cnt-1].exists()){return file;}

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
				// file[i].delete();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return file;
	}
}
