package com.xrtb.jmq;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;

public enum JMQContext {
	INSTANCE;
	
	static Context context;
	
	public static Context getInstance() {
		if (context == null)
			context = ZMQ.context(1);
		return context;
	}
	
	public static void term() {
		if (context == null)
			return;
		context.term();
	}
}
