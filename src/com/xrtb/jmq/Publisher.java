package com.xrtb.jmq;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

public class Publisher {

	Socket publisher = null;
	Context context = JMQContext.getInstance();
	boolean running = false;
	String topicName = null;

	public static void main(String[] args) throws Exception {

		// Publisher s = new Publisher("tcp://*:5570", "test");
		Publisher s = new Publisher("tcp://*:5570", "test");
		for (int i = 0; i < 100; i++) {
			s.publish("Hello");
		}
		s.shutdown();
	}

	public Publisher(String binding, String topicName) throws Exception {

		context = ZMQ.context(1);
		publisher = context.socket(ZMQ.PUB);
		publisher.bind(binding);

		Thread.sleep(100);
		//System.out.println("Starting Publisher..");
		publisher.setIdentity("B".getBytes());
		publisher.setLinger(5000);
		publisher.setHWM(0);

		this.topicName = topicName;
	}

	public void publish(Object message) throws Exception { 
		publisher.sendMore(topicName);
		String msg = Tools.serialize(message);
		publisher.send(msg);
	}

	public void publishAsync(Object message) throws Exception {
		Runnable u = () -> {
			publisher.sendMore(topicName);
			String msg = Tools.serialize(message);
			publisher.send(msg);
		};
		Thread nthread = new Thread(u);
		nthread.start();
	}

	public void shutdown() {
		publisher.close();
	}
}