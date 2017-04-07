package com.xrtb.jmq;


import java.util.ArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class AbstractLogger implements Runnable {
	/** The log interval, once a minute. */
	public int LOG_INTERVAL = 60000;

	/** The queue that holds the objects to be logged */
	ConcurrentLinkedQueue<LogObject> queue = new ConcurrentLinkedQueue();
	/** My thread */
	Thread me;

	/**
	 * A map object to handle quick lookup of channel names to the list of
	 * objects for that channel
	 */
	Map mapper = new HashMap();

	/** A list of object sets. Each list is a different channel/file. */
	Set<List> setOfLists = new HashSet();

	public AbstractLogger(int interval) {
		LOG_INTERVAL = interval;
		me = new Thread(this);
		me.start();

	}

	/**
	 * Periodic processing of logs. Write the different channels to different
	 * files.
	 */
	public void run() {
		long time = System.currentTimeMillis() + LOG_INTERVAL;
		while (true) {
			if (System.currentTimeMillis() > time) {
				//System.out.println("---------- HAMMER TIME -------------------");

				time = System.currentTimeMillis() + LOG_INTERVAL;
				hammerTime();
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Write all the logging data
	 */
	public void hammerTime() {

		Set<Entry> entries = mapper.entrySet();
		for (Entry e : entries) {
			String name = (String) e.getKey();
			List<String> values = (List) e.getValue();
			System.out.println("-->" + name);

			execute(name, values);
			values.clear();
		}

		LogObject o = null;
		while((o = queue.poll()) != null) {
			List list = (List) mapper.get(o.name);
			if (list == null) {
				list = new ArrayList();
				mapper.put(o.name, list);
			}
			list.add(o.content);
		}
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Add a new object to the logger
	 * 
	 * @param offering
	 *            LogObject. The object to add to the logger queue
	 */
	public void offer(LogObject offering) {
		queue.offer(offering);
	}

	public abstract void execute(String name, List<String> values);

}
