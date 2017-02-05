package io.emu.route.compiler.neo4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Writer implements Runnable {

	File outFile = null;

	LinkedBlockingQueue<MyPath> queue = null;
	CountDownLatch latch;

	Writer(File out, LinkedBlockingQueue<MyPath> queue, CountDownLatch latch) {
		outFile = out;
		this.queue = queue;
		this.latch = latch;
	}

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
				}
				System.out.println("Done");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.latch.countDown();
	}

}
