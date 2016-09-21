package com.xrtb.tools.accounting;

import java.math.BigDecimal;

/**
 * Accumulator for accountings by Hour
 * @author Ben M. Faul
 *
 */
public class Hour {
	long pixels;
	long clicks;
	long bids;
	long wins;
	BigDecimal winPrice = new BigDecimal(0);
	BigDecimal bidPrice = new BigDecimal(0);
	
	int name;
	
	public Hour(int hour) {
		name = hour;
	}
	
	public void process(Record r) {
		if (r.pixels != 0) {
			System.out.print("");
		}
		pixels += r.pixels;
		clicks += r.clicks;
		bids += r.bids;
		wins += r.wins;
		winPrice = winPrice.add(new BigDecimal(r.winPrice));
		bidPrice = bidPrice.add(new BigDecimal(r.bidPrice));	
	}
	
	public void print() {
		double bidP = bidPrice.doubleValue();
		double winP = winPrice.doubleValue();
		bidP /= 1000;
		winP /= 1000;
		//String result = String.format("%2d    %8d %8d %8d %8d %12.4f %12.4f",name,pixels,clicks,bids,wins,bidP, winP);
		String result = String.format("%4d %16d %16d %16.4f %16.4f %16d %16d ",name,bids,wins,bidP, winP,pixels,clicks);
		System.out.println(result);
		
	}
}
