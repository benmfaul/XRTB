package com.xrtb.tools.accounting;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Month {
	List<ADate> dates = new ArrayList();
	int name;
	
	long pixels;
	long clicks;
	long bids;
	long wins;
	BigDecimal winPrice = new BigDecimal(0);
	BigDecimal bidPrice = new BigDecimal(0);
	
	public Month(int month) {
		name = month;
		for (int i=0;i<31;i++) {
			dates.add(new ADate(i));
		}
	}
	
	public void process(Record r) {
		ADate date = dates.get(r.footprint.get(Record.DATE));
		date.process(r);
	}
	
	public void print(List<Integer> days, Boolean hourly, StringBuilder csv) {
		System.out.println("\n\nMonth: " + name);
		if (!hourly) {
			System.out.println("Date             Bids             Wins        Bid Price        Win Price           Pixels           Clicks");
		}
		
		for (int i=1; i<dates.size();i++) {
			ADate date = dates.get(i);
			if (days != null) {
				if (days.contains(i)) {
					if (csv != null) {
						csv.append(name);
						csv.append(",");
					}
					date.print(hourly,csv);
					add(date);
				}
			}
			else {
				if (csv != null) {
					csv.append(name);
					csv.append(",");
				}
				date.print(hourly,csv);
				add(date);
			}
		}
		
		double bidP = bidPrice.doubleValue();
		double winP = winPrice.doubleValue();
		bidP /= 1000;
		winP /= 1000;
		
		if (hourly) {
			System.out.println("                 Bids             Wins        Bid Price        Win Price           Pixels           Clicks");
		}
		String result = String.format("TOTAL%16d %16d %16.4f %16.4f %16d %16d ",bids,wins,bidP, winP,pixels,clicks);
		System.out.println(result);
	}
	
	public void add(ADate h) {
		pixels += h.pixels;
		clicks += h.clicks;
		bids += h.bids;
		wins += h.wins;
		winPrice = winPrice.add(h.winPrice);
		bidPrice = bidPrice.add(h.bidPrice);	
	}
}