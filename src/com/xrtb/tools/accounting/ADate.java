package com.xrtb.tools.accounting;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ADate {
	List<Hour> hours = new ArrayList();
	int name;
	
	long pixels;
	long clicks;
	long bids;
	long wins;
	BigDecimal winPrice = new BigDecimal(0);
	BigDecimal bidPrice = new BigDecimal(0);
	
	public ADate(int date) {
		name = date;
		for (int i=0;i<24;i++) {
			hours.add(new Hour(i));
		}
	}
	public void process(Record r) {
		int which = r.footprint.get(Record.HOUR);
		Hour h = hours.get(which);
		h.process(r);
	}
	
	public void print() {
	//	System.out.println("Date: " + name + "\n");
		
		//System.out.println("Hour       Pixels    Clicks   Bids     Wins    Bid Price    Win Price");
		for (int i=0; i<hours.size();i++) {
			Hour hour = hours.get(i);
			// hour.print();
			add(hour);
		}
		
		double bidP = bidPrice.doubleValue();
		double winP = winPrice.doubleValue();
		//String result = String.format("%4d  %8d %8d %8d %8d %12.4f %12.4f",name,pixels,clicks,bids,wins,bidP, winP);
		String result = String.format("%4d %16d %16d %16.4f %16.4f %16d %16d ",name,bids,wins,bidP, winP,pixels,clicks);
		System.out.println(result);
	}
	
	public void add(Hour h) {
		if (h.wins != 0) {
			System.out.print("");
		}
		pixels += h.pixels;
		clicks += h.clicks;
		bids += h.bids;
		wins += h.wins;
		winPrice = winPrice.add(h.winPrice);
		bidPrice = bidPrice.add(h.bidPrice);	
	}
}

