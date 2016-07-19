package com.xrtb.bidder;

import java.io.File;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.redisson.RedissonClient;
import org.redisson.core.RTopic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.tools.logmaster.AppendToFile;
import com.xrtb.tools.logmaster.FileLogger;
import com.xrtb.tools.logmaster.Spark;

/**
 * A publisher for REDIS based messages, sharable by multiple threads.
 * 
 * @author Ben M. Faul
 *
 */
public class Publisher implements Runnable {
	/** The objects thread */
	Thread me;
	/** The JEDIS connection used */
	String channel;
	/** The topic of messages */
	RTopic logger;
	/** The queue of messages */
	ConcurrentLinkedQueue queue = new ConcurrentLinkedQueue();

	/** Filename, if not using redis */
	String fileName;
	StringBuilder sb;
	ObjectMapper mapper;

	/**
	 * Constructor for base class.
	 * 
	 * @param conn
	 *            Jedis. The REDIS connection.
	 * @param channel
	 *            String. The topic name to publish on.
	 * @throws Exception. Throws
	 *             exceptions on REDIS errors
	 */
	public Publisher(RedissonClient redisson, String channel) throws Exception {
		this.channel = channel;
		logger = redisson.getTopic(channel);
		me = new Thread(this);
		me.start();

	}

	public Publisher(String fileName) throws Exception {
		int i = fileName.indexOf("file://");
		if (i > -1) {
			fileName = fileName.substring(7);
		}
		this.fileName = fileName;
		mapper = new ObjectMapper();
		sb = new StringBuilder();
		me = new Thread(this);
		me.start();
	}

	/**
	 * Return the publishing channel
	 * 
	 * @return RTopic. The RTopic channel.
	 */
	public RTopic getChannel() {
		return logger;
	}

	@Override
	public void run() {
		if (logger == null)
			runFileLogger();
		else
			runRedisLogger();
	}

	void runFileLogger() {
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
				error.printStackTrace();
				return;
			}
		}
	}

	void runRedisLogger() {
		String str = null;
		Object msg = null;
		while (true) {
			try {
				if ((msg = queue.poll()) != null) {
					// System.out.println("message");
					logger.publishAsync(msg);
				}
				Thread.sleep(1);
			} catch (Exception e) {
				e.printStackTrace();
				// return;
			}
		}
	}

	/**
	 * Out of band write, like when you absolutely have to send a notice now
	 * (Like a shutdown notice)
	 * 
	 * @param Object
	 */
	public void writeFast(Object msg) {
		logger.publishAsync(msg);
	}

	/**
	 * Add a message to the messages queue.
	 * 
	 * @param s
	 *            . String. JSON formatted message.
	 */
	public void add(Object s) {
		if (fileName != null) {
			synchronized (sb) {
				sb.append(s);
				sb.append("\n");
			}
		} else
			queue.add(s);
	}
}