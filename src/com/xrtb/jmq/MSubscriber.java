package com.xrtb.jmq;

import org.zeromq.ZMQ.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that implements an event Loop. It sets up mulltiple point-point
 * workers on separate channels. Then, it also publishes whatever it receives
 * from those point-point workers to its own publisher port.
 * 
 * @author ben
 *
 */

public class MSubscriber implements Runnable, EventIF, SubscriberIF {

	Thread me = null;
	Context context = null;
	volatile List<Subscriber> workers = new ArrayList<Subscriber>();
	public EventIF handler = null;
	String topic;

	public static String logDir;

    /**
     * Subscribe to subscriber, could be kafka or 0mq instance.
     * @param handler EventIF: The event handler when a messaage is received.
     * @param address String. The kafka configuration strung.
     * @param topic String. The topic subscribed to.
     * @throws Exception on network errors.
     */
	public MSubscriber(EventIF handler, String address, String topic) throws Exception {

		this.topic = topic;
		this.handler = handler;
		if (address.contains("kafka://")==false) {
			Subscriber w = new Subscriber(this,address);
			w.subscribe(topic);
			workers.add(w);
			return;
		} else {
			KafkaConfig c = new KafkaConfig(address);
			Subscriber w = new Subscriber(this, c.getProperties(), c.getTopic());
			workers.add(w);
		}

		me = new Thread(this);
		me.start();

	}

    /**
     * Add a list of addresses to subscribe from.
     * @param handler EventIF. The handler for any messages received.
     * @param addresses List. A list of hosts that we will initially connect to.
     * @throws Exception on 0MQ errors.
     */
	public MSubscriber(EventIF handler, List<String> addresses) throws Exception {
		for (String address : addresses) {
			if (address.startsWith("kafka")==false) {
				String[] parts = address.split("&");
				if (parts.length != 2) {
					throw new Exception("Malformed address, format example: tcp://*:556&topic1,topic2,topic3");
				}
				String addr = parts[0];
				String topics[] = parts[1].split(",");
				if (topics.length == 0) {
					throw new Exception("Malformed address, missing topics, format example: tcp://*:556&logs,bids,junk");
				}

				Subscriber w = new Subscriber(this, addr);
				for (String topic : topics) {
					w.subscribe(topic);
					this.topic = topic;
				}
				workers.add(w);
			} else {
				Subscriber w = new Subscriber(this,address);
				workers.add(w);
			}
		}

		this.handler = handler;
		
		me = new Thread(this);
		me.start();

	}
	
	public void subscribe(String topic) {
		for (Subscriber w : workers) {
			w.subscribe(topic);
		}
	}

	@Override
	public void run() {
		try {
			while (!me.isInterrupted()) {
				Thread.sleep(1);
			}

		} catch (Exception e) {
			// System.out.println("Interrupt!");
			e.printStackTrace();
		}
	}

	public void handleMessage(String key, String message) {
		if (key.equals(topic)==false)
			return;

		if (message != null && message.contains("rtb.jmq.Ping\"}"))
			return;

		if (handler != null)
			handler.handleMessage(key, message);
	}

	public void close() {
		shutdown();
	}

	public void shutdown() {

		for (Subscriber w : workers) {
			w.shutdown();
		}

		me.interrupt();
		if (handler != null)
			handler.shutdown();
	}
}