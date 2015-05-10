package com.xrtb.commands;

import com.xrtb.common.Configuration;

public class LogMessage {

	public int sev;
	public String field;
	public String message;
	public String source;
	long time;
	
	public LogMessage() {
		
	}
	
	public LogMessage(int sev, String field, String message) {
		this.sev = sev;
		this.field = field;
		this.message = message;
		source = Configuration.getInstance().instanceName;
		time = System.currentTimeMillis();
	}
}
