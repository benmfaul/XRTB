package com.xrtb.jmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.bidder.Publisher;

public class XPublisher extends Publisher {

	com.xrtb.jmq.Publisher p;

	public XPublisher(String address, String topic) throws Exception {
		super();
		if (address.startsWith("file://")) {
			int i = address.indexOf("file://");
			if (i > -1) {
				address = address.substring(7);
			}
			this.fileName = address;
			mapper = new ObjectMapper();
			sb = new StringBuilder();
		} else {
			p = new com.xrtb.jmq.Publisher(address, topic);
		}
		me = new Thread(this);
		me.start();
	}
	
	public XPublisher(String address) throws Exception {
		super();
		if (address.startsWith("file://")) {
			int i = address.indexOf("file://");
			if (i > -1) {
				address = address.substring(7);
			}
			this.fileName = address;
			mapper = new ObjectMapper();
			sb = new StringBuilder();
		} else {
			String [] parts = address.split("&");
			p = new com.xrtb.jmq.Publisher(parts[0], parts[1]);
		}
		me = new Thread(this);
		me.start();
	}

	@Override
	public void run() {
		if (p == null)
			runFileLogger();
		else
			runJmqLogger();
	}

	public void runJmqLogger() {
		String str = null;
		Object msg = null;
		while (true) {
			try {
				while((msg = queue.poll()) != null) {
					p.publish(msg);
				}
				Thread.sleep(1);
			} catch (Exception e) {
				e.printStackTrace();
				// return;
			}
		}
	}


}
