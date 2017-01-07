package com.xrtb.bidder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.tools.DbTools;
import com.xrtb.tools.logmaster.AppendToFile;

/**
 * A publisher for Aerospike based messages, sharable by multiple threads.
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
	/** The timestamp part of the name */
	String tailstamp;
	/** Logger time, how many minuutes before you clip the log */
	protected int time;
	/** count down time */
	protected long countdown;
	/** Strinbuilder for file ops */
	protected StringBuilder sb;
	/** Object to JSON formatter */
	protected ObjectMapper mapper;
	/** Set if error occurs */
	protected boolean errored = false;
	/** Logging formatter yyyy-mm-dd-hh:ss part. */
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm");

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
				String [] parts = address.split("&");
				if (parts.length > 1) {
					address = parts[0];
					String [] x = parts[1].split("=");
					time = Integer.parseInt(x[1]);
					time *= 60000;
					setTime();
				}
			}
			this.fileName = address;
			mapper = new ObjectMapper();
			sb = new StringBuilder();
		} else {
			String[] parts = address.split("&");
			logger = new com.xrtb.jmq.Publisher(parts[0], parts[1]);
		}
		me = new Thread(this);
		me.start();
	}
	
	void setTime() {
		countdown = System.currentTimeMillis() + time;
	}

	public void runFileLogger() {
		Object obj = null;

		String thisFile = this.fileName;
		
		if (countdown != 0) {
			tailstamp = "-" + sdf.format(new Date());
			thisFile += tailstamp;
		}
		else
			tailstamp = "";
		
		while (true) {
			try {
				Thread.sleep(1);

				synchronized (sb) {
					if (sb.length() != 0) {
						try {
							AppendToFile.item(thisFile, sb);
						} catch (Exception error) {
							error.printStackTrace();
						}
						sb.setLength(0);
						sb.trimToSize();
					}
					
					if (countdown != 0 && System.currentTimeMillis() > countdown) {
						tailstamp = "-" + sdf.format(new Date());
						thisFile = this.fileName + tailstamp;
						setTime();
					}
				}
			} catch (Exception error) {
				errored = true;
				try {
					Controller.getInstance().sendLog(1, "Publisher:" + fileName,
							"Publisher log error on " + fileName + ", error = " + error.toString());
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
				while ((msg = queue.poll()) != null) {
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

			String contents = null;
			try {
				contents = DbTools.mapper.writer().writeValueAsString(s);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			synchronized (sb) {
				sb.append(contents);
				sb.append("\n");
			}
		} else
			queue.add(s);
	}
}