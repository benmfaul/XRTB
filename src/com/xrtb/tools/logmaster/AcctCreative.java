package com.xrtb.tools.logmaster;

public class AcctCreative {

	public int bids;
	public int wins;
	public int clicks;
	public int pixels;
	public double bidPrice;
	public double winPrice;
	
	public String name;
	
	public AcctCreative() {
		
	}
	
	public AcctCreative(String name) {
		this.name = name;
	}
	
	public void clear() {
		wins = clicks = pixels = 0;
		bidPrice = winPrice = 0;
	}
}
