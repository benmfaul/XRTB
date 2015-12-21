package com.xrtb.tools.logmaster;

import java.util.HashSet;


public class AcctCampaign {

	public HashSet<AcctCreative> creatives = new HashSet();
	public int bids;
	public int wins;
	public int clicks;
	public int pixels;
	public double bidPrice;
	public double winPrice;
	
	public String  name;
	
	public AcctCampaign() {
		
	}
	
	public AcctCampaign(String name) {
		this.name = name;
	}
	
	public void clear() {
		wins = clicks = pixels = 0;
		bidPrice = winPrice = 0;
		for (AcctCreative a : creatives) {
			a.clear();
		}
	}
}
