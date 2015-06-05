package com.xrtb.db;

import java.util.ArrayList;
import java.util.List;

import com.xrtb.common.Campaign;

public class User  {

	public String name;
	public String directory;
	public long origin;
	public long lastAccess;
	public ArrayList<Campaign> campaigns = new ArrayList();
	
	public User() {
		
	}
	
	public User(String name) {
		this.name = name;
		lastAccess = origin = System.currentTimeMillis();
	}
}
