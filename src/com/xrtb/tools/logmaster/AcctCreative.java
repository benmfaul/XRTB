package com.xrtb.tools.logmaster;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Acculumlates accounting bits by creatives.
 * @author Ben M. Faul
 *
 */
public class AcctCreative {

	public AtomicLong bids = new AtomicLong(0);
	public AtomicLong wins = new AtomicLong(0);
	public AtomicLong clicks = new AtomicLong(0);
	public AtomicLong pixels = new AtomicLong(0);

	public BigDecimal bidPrice = new BigDecimal(0);
	public BigDecimal winPrice = new BigDecimal(0);
	
	public String name;
	public String campaignName;
	public String accountName;
	
	public Slice slices = new Slice();
	
	public long time;
	
	public AcctCreative() {
		
	}
	
	public AcctCreative(String accountName, String campaignName, String name) {
		this.accountName = accountName;
		this.campaignName = campaignName;
		this.name = name;
	}
	
	public void clear() {
		bids = new AtomicLong(0);
		wins = new AtomicLong(0);
		clicks = new AtomicLong(0);
		pixels = new AtomicLong(0);
		bidPrice = new BigDecimal(0);
		winPrice = new BigDecimal(0);
		slices.clear();
	}
	
	@JsonIgnore
	public boolean isZero() {
		if (bids.longValue() == 0 && wins.longValue() == 0 && clicks.longValue() == 0 && pixels.longValue() == 0)
			return true;
		else
			return false;
	}
}
