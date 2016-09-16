package com.xrtb.jmq;

public interface SubscriberIF {

	public void close();
	public void shutdown();
	public void  subscribe(String topic);
}
