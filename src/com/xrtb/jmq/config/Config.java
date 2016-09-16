package com.xrtb.jmq.config;

import java.util.List;

public class Config {
	public int port;
	public String password;
	public List<String> addresses;
	public String myAddress = "tcp://*:5563";
	
	public Config() {
		
	}

}
