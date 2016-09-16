package com.xrtb.jmq;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

/**
 * Subscriber. This CONNECTS to a publisher. It is S (many) -> P (single)
 * 
 * @author ben
 *
 */
public class Subscriber implements Runnable, SubscriberIF {

	Context context = JMQContext.getInstance();
	EventIF handler;
	Socket subscriber;
	Thread me;

	public static void main(String... args) {
		// Prepare our context and subscriber
	}

	public Subscriber(EventIF handler, String address) throws Exception {
		this.handler = handler;
		subscriber = context.socket(ZMQ.SUB);

		subscriber.connect(address);
		me = new Thread(this);
		me.start();
	}

	public void subscribe(String topic) {
		subscriber.subscribe(topic.getBytes());
	}

	@Override
	public void run() {
		while (me.isInterrupted()==false) {
			// Read envelope with address
			String address = subscriber.recvStr();
			// Read message contents
			String contents = subscriber.recvStr();
			handler.handleMessage(address, contents);
		}
	}

	public void shutdown() {
		subscriber.close();
	}
	
	public void close() {
		shutdown();
	}
}
