package com.xrtb.jmq;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

public class RTopic implements EventIF {
	
	Socket publisher = null;
	SubscriberIF subscriber = null;
	Context context = null;
	boolean running = false;
	String topicName = null;
	public Map<String,MessageListener> m = new HashMap();
	
	public RTopic(String address) throws Exception {
		context = ZMQ.context(1);
		if (address.contains("&")) {
			String [] parts = address.split("&");
			subscriber = new Subscriber(this,parts[0]);
			subscriber.subscribe(parts[1]);
		} else
			subscriber = new Subscriber(this,address);
	}
	/**
	 * A Topic handler with no publishing (just subscribes)
	 * @param binding String.  The binding for the 
	 * @param addresses List. My TCP address and topics for listening.
	 * @throws Exception
	 */
	public RTopic(List<String> addresses) throws Exception {
		
		context = ZMQ.context(1);	
		subscriber = new MSubscriber(this,addresses);
	}
	
	/**
	 * A Topic handler that subscribes but has a publisher too.
	 * @param paddress String. My TCP address for publishing.
	 * @param addresses List. My TCP address and topics for listening.
	 * @throws Exception
	 */
	public RTopic(String phandler, List<String> addresses) throws Exception {
		this(addresses);
		
		publisher = context.socket(ZMQ.PUB);
		publisher.bind(phandler);

		publisher.setIdentity("B".getBytes());
		publisher.setLinger(5000);
		publisher.setHWM(0);
	}
	
	public void subscribe(String str) {
		subscriber.subscribe(str);
	}
	
	public void close() {
		shutdown();
	}
	
	public void shutdown() {
		if (subscriber != null) {
	        subscriber.close ();
	        subscriber = null;
		}
		if (publisher != null) {
			publisher.close();
			publisher = null;
		}
        context.term ();
        subscriber.shutdown();
	}
	
	public void publishAsync(String topic, Object message) {
		
	}
	
	public void publish(String topic, Object message) {

	}
	
	public void addListener(MessageListener<?> z) {
		Type type = z.getClass().getGenericInterfaces()[0];
		String theType = null;

		if (type instanceof ParameterizedType) {
		    Type actualType = ((ParameterizedType) type).getActualTypeArguments()[0];
		    theType = actualType.getTypeName();
		}
		m.put(theType,z);

	}
	
	/**
	 * Handle messages from msubscriber
	 */

	@Override
	public void handleMessage(String id, String msg) {
		Object [] x = Tools.deSerialize(msg);
		String name = (String)x[0];
		Object o = m.get(name);
		if (o != null) {
			MessageListener z = (MessageListener)o;
			z.onMessage(id, x[1]);
		} else {
			//MessageListener z = m.get("com.xrtb.commands.BasicCommand");
			Set set = m.keySet();
			Iterator<String> it = set.iterator();
			name = it.next();
			MessageListener z = m.get(name);
			z.onMessage(id, x[1]);
		}
	}
}
