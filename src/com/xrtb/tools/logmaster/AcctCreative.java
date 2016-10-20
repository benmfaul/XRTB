package com.xrtb.tools.logmaster;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
	public BigDecimal bidPrice = new BigDecimal(0);
	public BigDecimal winPrice = new BigDecimal(0);
	
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
		bidPrice = new BigDecimal(0);
		winPrice = new BigDecimal(0);
	}
	
	@JsonIgnore
	public boolean isZero() {
		if (bids == 0 && wins == 0 && clicks == 0 && pixels == 0)
			return true;
		else
			return false;
	}
}
