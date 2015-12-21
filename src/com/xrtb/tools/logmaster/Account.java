package com.xrtb.tools.logmaster;

import java.util.HashSet;

public class Account {

	public HashSet<AcctCampaign> campaigns = new HashSet();
	public int bids;
	public int wins;
	public int clicks;
	public int pixels;
	public double bidPrice;
	public double winPrice;
	
	public String name;
	
	public Account() {
		
	}
	
	public Account(String name) {
		this.name = name;
	}
	
	public void clear() {
		wins = clicks = pixels = 0;
		bidPrice = winPrice = 0;
		for (AcctCampaign a : campaigns) {
			a.clear();
		}
	}
	
}
