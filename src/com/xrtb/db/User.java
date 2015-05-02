package com.xrtb.db;

import java.util.ArrayList;
import java.util.List;

import com.xrtb.common.Campaign;

public class User {

	public String name;
	public List<Campaign> campaigns;
	public long origin;
	public long lastAccess;
	
	public User(String name) {
		campaigns = new ArrayList();
		this.name = name;
		lastAccess = origin = System.currentTimeMillis();
	}
}
