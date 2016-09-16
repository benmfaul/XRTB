package com.xrtb.jmq;

public interface EventIF {

	public void handleMessage(String id, String msg);
	public void shutdown();
}
