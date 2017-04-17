package com.xrtb.jmq;

import java.util.ArrayList;

import java.util.List;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

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
	List<Subscriber> workers = new ArrayList<Subscriber>();
	public EventIF handler = null;

	public static String logDir;

	public static void main(String... args) throws Exception {
		List<String> addresses = new ArrayList();
		String myAddress = "tcp://*:5563";
		MSubscriber pool = null;
		MSubscriber.logDir = null; // "logs";
		AbstractLogger logger = null;
		int interval = 60000;
		EventIF handler = null;

		int i = 0;
		if (args.length == 0) {
			addresses.add("tcp://*:5570&test,bids,requests,logs,wins,click,pixel");
			addresses.add("tcp://*:5571&test,bids,requests,logs,wins,click,pixel");
			addresses.add("tcp://*:5572&test,bids,requests,logs,wins,click,pixel");
			addresses.add("tcp://*:5573&test,bids,requests,logs,wins,click,pixel");
			addresses.add("tcp://*:5574&test,bids,requests,logs,wins,click,pixel");
			addresses.add("tcp://*:5575&test,bids,requests,logs,wins,click,pixel");
			addresses.add("tcp://*:5576&test,bids,requests,logs,wins,click,pixel");
			addresses.add("tcp://*:5577&test,bids,requests,logs,wins,click,pixel");
		}
		while (i < args.length) {
			switch (args[i]) {
			case "-h":
				System.out.println("-h                This message");
				System.out.println("-i <number>       Number of seconds to queue files to be written (see -d)");
				System.out.println(
						"-p <pool-entry>   Pool entry, example: -p tcp://*:5570&bids,requests,logs (No spaces please)");
				System.out
						.println("-a <my-address>   My publisher address, example: -a tcp://*5563 (the default value");
				System.out.println("-d <directory>    The directory to log (don't specify if no logs desired)");
				return;
			case "-p":
				addresses.add(args[i + 1]);
				i += 2;
				break;
			case "-a":
				myAddress = args[i + 1];
				i += 2;
				break;
			case "-d":
				MSubscriber.logDir = args[i + 1];
				i += 2;
				break;
			case "-i":
				interval = Integer.parseInt(args[i + 1]);
				interval *= 1000;
				i += 2;
				break;
			default:
				System.err.println("Huh: " + args[i]);
				return;
			}
		}

		pool = new MSubscriber(null,addresses);
		if (handler == null)
			pool.handler = new TestHandler();

	}

	public MSubscriber(EventIF handler, List<String> addresses) throws Exception {
		for (String address : addresses) {

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
			}

			workers.add(w);
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
				Thread.sleep(10000000);
			}

		} catch (Exception e) {
			// System.out.println("Interrupt!");
			e.printStackTrace();
		}
	}

	public void handleMessage(String key, String message) {
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

class TestHandler implements EventIF {

	@Override
	public void handleMessage(String id, String msg) {
		System.out.println("Message: " + id + ", " + msg);

	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}

}