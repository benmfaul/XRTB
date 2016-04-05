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
	
	public void print(boolean hourly, StringBuilder csv) {
		if (hourly)
			System.out.println("Date: " + name + "\nHour             Bids             Wins        Bid Price        Win Price           Pixels           Clicks");
		
		for (int i=0; i<hours.size();i++) {
			Hour hour = hours.get(i);
			
			if (hourly)
			   hour.print();
			
			add(hour);
		}
		
		double bidP = bidPrice.doubleValue();
		double winP = winPrice.doubleValue();
		
		bidP /= 1000;
		winP /= 1000;
		String result = String.format("%4d %16d %16d %16.4f %16.4f %16d %16d ",name,bids,wins,bidP, winP,pixels,clicks);
		
		if (hourly) {
			result = String.format("Tot: %16d %16d %16.4f %16.4f %16d %16d\n",bids,wins,bidP, winP,pixels,clicks);
		} else {
			if (csv != null) {
				csv.append(name); csv.append(",");
				csv.append(bids); csv.append(",");
				csv.append(wins); csv.append(",");
				csv.append(bidP); csv.append(",");
				csv.append(winP); csv.append(",");
				csv.append(pixels); csv.append(",");
				csv.append(clicks);
				csv.append("\n");
				
			}
		}
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

