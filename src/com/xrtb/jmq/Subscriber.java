package com.xrtb.jmq;


import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

/**
 * Subscriber. This CONNECTS to a publisher. It is S (many) -> P (single)
 * 
 * @author ben
 *
 */
public class Subscriber implements Runnable, SubscriberIF, ConsumerRebalanceListener {

	Context context = JMQContext.getInstance();
	EventIF handler;
	Socket subscriber;
	Thread me;

	KafkaConsumer<String, String> consumer;
	String topic;

	static int kount = 0;

	public static void main(String... args) {
		// Prepare our context and subscriber
	}

	/**
	 * A subscriber using Kafka
	 * @param handler EventIF, the classes that will get the messages.
	 * @param props Properties. The kafka properties
	 * @throws Exception on Errors setting up with Kafka
	 */
	public Subscriber(EventIF handler, Properties props, String topic) throws Exception {
		this.handler = handler;
		consumer = new KafkaConsumer<>(props);
		consumer.subscribe(Collections.singletonList(topic), this);
		this.topic = topic;

		me = new Thread(this);
		me.start();
	}

	public Subscriber(EventIF handler, String address) throws Exception {
		this.handler = handler;

		if (address.startsWith("kafka")==false) {
			subscriber = context.socket(ZMQ.SUB);
			subscriber.connect(address);
			subscriber.setHWM(100000);
		} else {
			String topic = null;
			if (address.contains("groupid")==false) {
                address += "&groupid=a";
            }
			KafkaConfig c = new KafkaConfig(address);
			consumer = new KafkaConsumer<>(c.getProperties());
			consumer.subscribe(Collections.singletonList(c.getTopic()), this);

			this.topic = c.topic;
		}
		me = new Thread(this);
		me.start();
	}

	public void subscribe(String topic) {
		if (consumer != null) {
		//	consumer.subscribe(Collections.singletonList(topic), this);
		} else
			subscriber.subscribe(topic.getBytes());
	}

	@Override
	public void run() {

		if (consumer != null) {
			while(true) {
				ConsumerRecords<String, String> records = consumer.poll(1000);
				for (ConsumerRecord<String,String> record : records) {
					handler.handleMessage(topic,record.value());
				}

				if (consumer == null)
					return;

				consumer.commitSync();
			}
		}

		while (me.isInterrupted()==false) {
			// Read envelope with address
			String address = subscriber.recvStr();
			// Read message contents
			String contents = subscriber.recvStr();
			handler.handleMessage(address, contents);
		}
	}

	public void shutdown() {
		if (subscriber != null)
			subscriber.close();
		if (consumer != null) {
			consumer.close();
			consumer = null;
		}


	}
	
	public void close() {
		shutdown();
	}

	@Override
	public void onPartitionsRevoked(Collection<TopicPartition> collection) {

	}

	@Override
	public void onPartitionsAssigned(Collection<TopicPartition> collection) {

	}
}
