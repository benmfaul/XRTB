package com.xrtb.jmq;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

public class WebMQPublisher {
	Socket publisher = null;
	Context context = null;
	boolean running = false;
	
	public static void main(String [] args) throws Exception {
		WebMQPublisher p = new WebMQPublisher("5570","test","Hello world");
	}

	public WebMQPublisher(String port, String topic, String message) throws Exception {

		String binding = "tcp://*:" + port;
		context = ZMQ.context(1);
		publisher = context.socket(ZMQ.PUB);
		publisher.bind(binding);
		Thread.sleep(1000);
		publisher.setIdentity("B".getBytes());
		publisher.setLinger(5000);
		publisher.setHWM(0);

		publisher.sendMore(topic);
		boolean isSent = publisher.send(message);
		publisher.close();
		context.term();
	}
}