package com.xrtb.bidder;

import java.io.File;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.tools.logmaster.AppendToFile;

/**
 * A publisher for REDIS based messages, sharable by multiple threads.
 * 
 * @author Ben M. Faul
 *
 */
public class ZPublisher implements Runnable {
	/** The objects thread */
	protected Thread me;
	/** The JEDIS connection used */
	String channel;
	/** The topic of messages */
	com.xrtb.jmq.Publisher logger;
	/** The queue of messages */
	protected ConcurrentLinkedQueue queue = new ConcurrentLinkedQueue();

	/** Filename, if not using redis */
	protected String fileName;
	protected StringBuilder sb;
	protected ObjectMapper mapper;
	protected boolean errored = false;

	public ZPublisher() {
		
	}
	
	public ZPublisher(String address, String topic) throws Exception {
		logger = new com.xrtb.jmq.Publisher(address, topic);

		me = new Thread(this);
		me.start();
	}
	
	public ZPublisher(String address) throws Exception {
		if (address.startsWith("file://")) {
			int i = address.indexOf("file://");
			if (i > -1) {
				address = address.substring(7);
			}
			this.fileName = address;
			mapper = new ObjectMapper();
			sb = new StringBuilder();
		} else {
			String [] parts = address.split("&");
			logger = new com.xrtb.jmq.Publisher(parts[0], parts[1]);
		}
		me = new Thread(this);
		me.start();
	}

	public void runFileLogger() {
		Object obj = null;

		while (true) {
			try {
				Thread.sleep(60000);

				synchronized (sb) {
					if (sb.length() != 0) {
						AppendToFile.item(fileName, sb);
						sb.setLength(0);
					}
				}
			} catch (Exception error) {
				errored = true;
				try {
					Controller.getInstance().sendLog(1, "Publisher:"+fileName,"Publisher log error on " + fileName + ", error = " + error.toString());
				} catch (Exception e) {
				}
				error.printStackTrace();
				sb.setLength(0);
			}
		}
	}

	public void run() {
		if (logger == null)
			runFileLogger();
		else
			runJmqLogger();
	}

	public void runJmqLogger() {
		String str = null;
		Object msg = null;
		while (true) {
			try {
				while((msg = queue.poll()) != null) {
					logger.publish(msg);
				}
				Thread.sleep(1);
			} catch (Exception e) {
				e.printStackTrace();
				// return;
			}
		}
	}

	/**
	 * Add a message to the messages queue.
	 * 
	 * @param s
	 *            . String. JSON formatted message.
	 */
	public void add(Object s) {
		if (fileName != null) {
			if (errored)
				return;
			
			synchronized (sb) {
				sb.append(s);
				sb.append("\n");
			}
		} else
			queue.add(s);
	}
}