package com.xrtb.tools.accounting;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements the accumulator for accounting by year.
 * @author Ben M. Faul
 *
 */
public class Year {
	List<Month> months = new ArrayList();    // warning 1 is the first, 0 is unused
	int year;
	
	long pixels;
	long clicks;
	long bids;
	long wins;
	BigDecimal winPrice = new BigDecimal(0);
	BigDecimal bidPrice = new BigDecimal(0);
	
	public Year(int year) {
		this.year = year;
		for (int i=0;i<13;i++) {
			months.add(new Month(i));
		}
	}
	
	public void process(Record r) {
		int which = r.footprint.get(Record.MONTH);
		Month m = months.get(which);
		m.process(r);
	}
	
	public void print(Integer sm, Integer em, List<Integer>days, boolean hourly, StringBuilder csv) {
		System.out.println(year);
		for (int i=sm; i<= em;i++) {
			Month month = months.get(i);
			month.print(days,hourly, csv);
			add(month);
		}
		
		double bidP = bidPrice.doubleValue();
		double winP = winPrice.doubleValue();
		bidP /= 1000;
		winP /= 1000;
		
		if (days == null) {
			System.out.println("\n\nYEAR TOTALS");
			System.out.println("Year             Bids             Wins        Bid Price        Win Price           Pixels           Clicks");
			String result = String.format("%4d %16d %16d %16.4f %16.4f %16d %16d ",year,bids,wins,bidP, winP,pixels,clicks);
			System.out.println(result);
		}
	}
	
	public void add(Month h) {
		pixels += h.pixels;
		clicks += h.clicks;
		bids += h.bids;
		wins += h.wins;
		winPrice = winPrice.add(h.winPrice);
		bidPrice = bidPrice.add(h.bidPrice);	
	}
}
