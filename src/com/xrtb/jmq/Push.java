package com.xrtb.jmq;

import org.zeromq.ZMQ;

public class Push {

	public Push(String port, String message) throws Exception  {
		ZMQ.Context context = ZMQ.context(1);
		ZMQ.Socket sender = context.socket(ZMQ.PUSH);
		sender.connect("tcp://localhost:" + port);
		sender.send(message);
		sender.close();
		context.term();
	}
}