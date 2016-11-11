package com.xrtb.tools.accounting;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
	public long pixels;
	public long bids;
	public long wins;
	public long clicks;
	public double bidPrice;
	public double winPrice;
	public String name;
	public String campaignName;
	public String accountName;
	public long time;
	
	public Slice slices;
	
	
	
	static DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm",
            Locale.US);
	static {
		format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
	}
	
	@JsonIgnore
	transient List<Integer> footprint = new ArrayList();
	
	public Record() {
		
	}
	
	public void process() {
		Date date = new Date(time);
		String result = format.format(date);
		String [] parts = result.split("-");
		footprint.add(Integer.parseInt(parts[0]));
		parts[1] = parts[1].replaceFirst("^0+(?!$)", "");
		footprint.add(Integer.parseInt(parts[1]));
		
		String []  nparts = parts[2].split("T");
		nparts[0] = nparts[0].replaceFirst("^0+(?!$)", "");
		footprint.add(Integer.parseInt(nparts[0]));
		
		nparts = nparts[1].split(":");
		nparts[0] = nparts[0].replaceFirst("^0+(?!$)", "");
		footprint.add(Integer.parseInt(nparts[0]));
		
	}
}
