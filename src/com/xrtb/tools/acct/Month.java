package com.xrtb.tools.acct;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;

/**
 * Accumulator for the accountings by month.
 * @author Ben M. Faul
 *
 */
public class Month {
	List<ADate> dates = new ArrayList();
	int name;
	
	long requests;
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
		if (r.footprint.get(Record.DATE) >= dates.size()) {
			while(dates.size() <= r.footprint.get(Record.DATE)) {
				dates.add(new ADate(dates.size()));
			}
		}
		ADate date = dates.get(r.footprint.get(Record.DATE));
		date.process(r);
	}
	
	public void print(int year, List<Integer> days, Boolean hourly, StringBuilder csv) {
		System.out.println("\n\nMonth: " + name);
		if (!hourly) {
			if (bids == 0 && Process.nz) {
				
			}
			else
				System.out.println("Date             Requests       Bids             Wins        Bid Price        Win Price           Pixels           Clicks");
		}
		
		for (int i=1; i<dates.size();i++) {
			ADate date = dates.get(i);
			if (days != null) {
				if (days.contains(i)) {
					date.print(hourly,year,name,csv);
					add(date);
				}
			}
			else {
				date.print(hourly,year,name,csv);
				add(date);
			}
		}
		
		double bidP = bidPrice.doubleValue();
		double winP = winPrice.doubleValue();
		bidP /= 1000;
		winP /= 1000;
		
		if (hourly) {
			if (bids == 0 && Process.nz) {
				
			} else
				System.out.println("             Requests             Bids             Wins        Bid Price        Win Price           Pixels           Clicks");
		}
		
		if (Process.nz && bids == 0) {
			
		} else {
		String result = String.format("TOTAL%16d %16d %16d %16.4f %16.4f %16d %16d ",requests,bids,wins,bidP, winP,pixels,clicks);
		System.out.println(result);
		}

	}
	
	public void add(ADate h) {
		requests += h.requests;
		pixels += h.pixels;
		clicks += h.clicks;
		bids += h.bids;
		wins += h.wins;
		winPrice = winPrice.add(h.winPrice);
		bidPrice = bidPrice.add(h.bidPrice);	
	}
}