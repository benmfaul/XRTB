package com.xrtb.tools.logmaster;

import java.util.HashSet;

public class Account {

	public HashSet<AcctCampaign> campaigns = new HashSet();
	public int bids;
	public int wins;
	public double bidPrice;
	public double winPrice;
	
	public String name;
	
	public Account() {
		
	}
	
	public Account(String name) {
		this.name = name;
	}
	
}
