package com.xrtb.jmq;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Publisher {

	Socket publisher = null;
	Context context = JMQContext.getInstance();
	boolean running = false;
	String topicName = null;
	
	ObjectMapper mapper = new ObjectMapper();

	public static void main(String[] args) throws Exception {

		// Publisher s = new Publisher("tcp://*:5570", "test");
		Publisher s = new Publisher("tcp://*:5570", "test");
		for (int i = 0; i < 100; i++) {
			s.publish("Hello");
		}
		s.shutdown();
	}

	public Publisher(String binding, String topicName) throws Exception {

		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		context = ZMQ.context(1);
		publisher = context.socket(ZMQ.PUB);
		if (binding.contains("*"))
			publisher.bind(binding);
		else
			publisher.connect(binding);

		Thread.sleep(100);
		//System.out.println("Starting Publisher..");
		publisher.setIdentity("B".getBytes());
		publisher.setLinger(5000);
		publisher.setHWM(0);

		this.topicName = topicName;
	}

	public void publish(Object message)  {
		publisher.sendMore(topicName);
		String msg = Tools.serialize(mapper, message);
		if (msg != null)
			publisher.send(msg);
		else
			System.err.println("No publish:" + message);
	}

	public void publishAsync(Object message)  {
		Runnable u = () -> {
			publisher.sendMore(topicName);
			String msg = Tools.serialize(mapper,message);
			publisher.send(msg);
		};
		Thread nthread = new Thread(u);
		nthread.start();
	}

	public void shutdown() {
		publisher.close();
	}
}