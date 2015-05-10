package com.xrtb.commands;

import com.xrtb.common.Configuration;

public class ClickLog {


	public String instance;
	public String payload;
	long time;
	
	public ClickLog() {
		
	}
	
	public ClickLog(String payload) {
		this.payload = payload;
		instance = Configuration.getInstance().instanceName;
		time = System.currentTimeMillis();
	}
}
