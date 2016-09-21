package com.xrtb.tools.logmaster;

/**
 * Acculumlates accounting bits by creatives.
 * @author Ben M. Faul
 *
 */
public class AcctCreative {

	public int bids;
	public int wins;
	public int clicks;
	public int pixels;
	public double bidPrice;
	public double winPrice;
	
	public String name;
	public String campaignName;
	public String accountName;
	
	public long time;
	
	public AcctCreative() {
		
	}
	
	public AcctCreative(String accountName, String campaignName, String name) {
		this.accountName = accountName;
		this.campaignName = campaignName;
		this.name = name;
	}
	
	public void clear() {
		bids = wins = clicks = pixels = 0;
		bidPrice = winPrice = 0;
	}
}
