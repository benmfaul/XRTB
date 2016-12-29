package com.xrtb.tools.acct;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;

import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;
import com.xrtb.pojo.WinObject;
import com.xrtb.tools.acct.Hour;
import com.xrtb.tools.acct.Record;

/**
 * Accumulator of accountings by date.
 * 
 * @author Ben M. Faul
 *
 */
public class ADate {
	List<Hour> hours = new ArrayList();
	int name;

	long requests;
	long pixels;
	long clicks;
	long bids;
	long wins;
	BigDecimal winPrice = new BigDecimal(0);
	BigDecimal bidPrice = new BigDecimal(0);

	public ADate(int date) {
		name = date;
		for (int i = 0; i < 24; i++) {
			hours.add(new Hour(i));
		}
	}

	public void process(Record r) {
		int which = r.footprint.get(Record.HOUR);
		Hour h = hours.get(which);
		h.process(r);
	}

	public void print(boolean hourly, int year, int month, StringBuilder csv) {
				
		if (hourly)
			System.out.println("Date: " + name
					+ "\nHour         Requests             Bids             Wins        Bid Price        Win Price           Pixels           Clicks");

		for (int i = 0; i < hours.size(); i++) {
			Hour hour = hours.get(i);

			if (hourly)
				hour.print(year, month, name, csv);

			add(hour);
		}

		double bidP = bidPrice.doubleValue();
		double winP = winPrice.doubleValue();

		 bidP /= 1000;
		 winP /= 1000;

		 String result = null;
		 if (bids == 0 && Process.nz) {
			 
		 }
		 else {
			 result = String.format("%4d %16d %16d %16d %16.4f %16.4f %16d %16d ", name, requests, bids, wins, bidP, winP, pixels,
				clicks);
		 }

		if (hourly) {
			result = String.format("Tot: %16d %16d %16d %16.4f %16.4f %16d %16d\n", requests, bids, wins, bidP, winP, pixels, clicks);
		} else {
			if (csv != null) {
				if (!hourly) {
					String bidPrice = "0";
					if (bidP != 0)
						bidPrice = String.format("%.4f", bidP);
					String winPrice = "0";
					if (winP != 0)
						winPrice = winPrice = String.format("%4f", winP);

					if (Process.nz && bids == 0) {

					} else {
						csv.append(year);
						csv.append(",");
						csv.append(month);
						csv.append(",");
						csv.append(name);
						csv.append(",");
						csv.append(requests);
						csv.append(",");
						csv.append(bids);
						csv.append(",");
						csv.append(wins);
						csv.append(",");
						csv.append(bidPrice);
						csv.append(",");
						csv.append(winPrice);
						csv.append(",");
						csv.append(pixels);
						csv.append(",");
						csv.append(clicks);
						csv.append("\n");
					}
				}
			}
		}
		if (bids == 0 && Process.nz) {
			
		} else
			System.out.println(result);
	}

	public void add(Hour h) {

		requests += h.requests;
		pixels += h.pixels;
		clicks += h.clicks;
		bids += h.bids;
		wins += h.wins;
		winPrice = winPrice.add(h.winPrice);
		bidPrice = bidPrice.add(h.bidPrice);
	}
}
