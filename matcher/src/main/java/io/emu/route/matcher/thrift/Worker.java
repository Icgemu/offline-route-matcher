package io.emu.route.matcher.thrift;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.thrift.TException;

public class Worker implements Runnable {

	BlockingQueue<String> queue;
	RouteEst rst;

	public Worker(BlockingQueue<String> q, RouteEst rst) {
		this.queue = q;
		this.rst = rst;
	}

	@Override
	public void run() {
		String ln = null;
		try {
			ln = this.queue.poll(10, TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while ((ln) != null) {
			String[] sp = ln.split(",");
			try {
				this.rst.output(this.rst.getMatching(sp[2]), sp[0]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				ln = this.queue.poll(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			this.rst.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
