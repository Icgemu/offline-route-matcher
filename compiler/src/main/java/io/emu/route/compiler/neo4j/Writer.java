
package io.emu.route.compiler.neo4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * TODO(这里用一句话描述这个类的作用)
 * 
 * @author Administrator
 * @version 2014年2月23日 上午12:43:00
 */
public class Writer implements Runnable {

	File outFile = null;

	LinkedBlockingQueue<MyPath> queue = null;
	CountDownLatch latch;

	Writer(File out, LinkedBlockingQueue<MyPath> queue, CountDownLatch latch) {
		outFile = out;
		this.queue = queue;
		this.latch = latch;
	}

	/*
	 * TODO(功能描述)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(outFile));
			MyPath p;
			try {
				while ((p = queue.poll(30, TimeUnit.SECONDS)) != null
						&& p != null) {
					bw.write(p + "\n");
					//System.out.println(p);
				}
				System.out.println("Done");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.latch.countDown();
		// TODO Auto-generated method stub

	}

}
