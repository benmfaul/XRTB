package com.xrtb.jmq;

import org.zeromq.ZMQ;

public class PushPull {

	public static void main(String... args) {
		PushPull p = new PushPull();

		p.pull();
		p.push();
	}

	public void push() {
		ZMQ.Context context = ZMQ.context(1);
		ZMQ.Socket sender = context.socket(ZMQ.PUSH);
		sender.connect("tcp://localhost:8086");
		sender.send("MESSAGE");
		sender.close();
		context.term();
	}

	public void pull() {
		Runnable redisupdater = () -> {
			ZMQ.Context context = ZMQ.context(1);
			ZMQ.Socket rcv = context.socket(ZMQ.PULL);
			rcv.bind("tcp://*:8086");
			rcv.setReceiveTimeOut(1000);
			String str = rcv.recvStr();
			System.out.println("Received: " + str);
			rcv.close();
			context.term();
		};
		Thread nthread = new Thread(redisupdater);
		nthread.start();
	}
}
