package com.xrtb.tools.acct;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;
import com.xrtb.pojo.WinObject;
import com.xrtb.tools.logmaster.Slice;

/**
 * Implements the Spark accounting record
 * @author Ben M. Faul
 *
 */
public class Record {
	public static int YEAR = 0;
	public static int MONTH = 1;
	public static int DATE = 2;
	public static int HOUR = 3;
	public LongAdder requests = new LongAdder();
	public LongAdder pixels = new LongAdder();
	public LongAdder bids = new LongAdder();
	public LongAdder wins = new LongAdder();
	public LongAdder clicks = new LongAdder();
	public DoubleAdder bidPrice = new DoubleAdder();
	public DoubleAdder winPrice = new DoubleAdder();
	public String name;
	public String campaignName;
	public String accountName;
	public long time;
	
	
	static DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm",
            Locale.US);
	public static Map<String, Record> records = new ConcurrentHashMap();
	static {
		format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
	}
	
	String key;
	
	@JsonIgnore
	transient List<Integer> footprint = new ArrayList();
	
	public Record(Object x) {
		if (x instanceof Map) {
			Map r = (Map)x;
			r = (Map)r.get("ext");
			time = (long)r.get("timestamp");
			
		} else
		if (x instanceof BidResponse) {
			BidResponse r = (BidResponse)x;
			time = r.utc;
		} else
		if (x instanceof WinObject) {
			WinObject r = (WinObject) x;
			time = r.utc;
		}
		
		process();
	}
	
	public static synchronized Record getInstance(Object x) {
		long time = 0;
		long wins = 0;
		long bids = 0;
		long requests = 0;
		double bidPrice = 0;
		double winCost = 0;
		if (x instanceof Map) {
			Map r = (Map)x;
			r = (Map)r.get("ext");
			time = (long)r.get("timestamp");
			requests++;			
		} else
		if (x instanceof BidResponse) {
			BidResponse r = (BidResponse)x;
			time = r.utc;
			bids++;
			bidPrice = r.cost;
		} else
		if (x instanceof WinObject) {
			WinObject r = (WinObject) x;
			time = r.utc;
			wins++;
			winCost = Double.parseDouble(r.price);
		}
		Date date = new Date(time);
		String key = format.format(date);
		Record r = records.get(key);
		if (r == null) {
			r = new Record(x);
			records.put(key, r);
		}
		
		r.bidPrice.add(bidPrice);
		r.winPrice.add(winCost);
		r.bids.add(bids);
		r.wins.add(wins);
		r.requests.add(requests);
		
		return r;
	}
	
	
	public void process() {
		Date date = new Date(time);
		key = format.format(date);
		
		String [] parts = key.split("-");
		
		// Year
		footprint.add(Integer.parseInt(parts[0]));
		parts[1] = parts[1].replaceFirst("^0+(?!$)", "");	
		// Month
		footprint.add(Integer.parseInt(parts[1]));
		
		String []  nparts = parts[2].split("T");
		nparts[0] = nparts[0].replaceFirst("^0+(?!$)", "");
		// Day
		footprint.add(Integer.parseInt(nparts[0]));
		
		nparts = nparts[1].split(":");
		nparts[0] = nparts[0].replaceFirst("^0+(?!$)", "");
		// Hour
		footprint.add(Integer.parseInt(nparts[0]));
		
		
		
	}
	
	public String getKey() {
		return key;
	}
	
	public String toString() {
		return key + " = " + bids;
	}
}
