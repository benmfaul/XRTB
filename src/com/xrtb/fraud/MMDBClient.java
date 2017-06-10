package com.xrtb.fraud;

import java.io.File;


import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.IspResponse;

/**
 * A Singleton class that implements the Max Mind bot detection class.
 * @author Ben M. Faul
 *
 */
public enum MMDBClient implements FraudIF {

	INSTANCE;
	
	static DatabaseReader reader;
	
	/** Forensiq round trip time */
	public volatile static AtomicLong forensiqXtime = new AtomicLong(0);
	/** forensiq count */
	public volatile static AtomicLong forensiqCount = new AtomicLong(0);
	/** Should we bid on an error */
	public boolean bidOnError = false;
	/** The database file */
	public static String file;
	
	/** The Set of things to key on to mark as possible bot */
	static Set<String> watchlist = new HashSet();
	
	/** The set of things to filter on */
	static List<String> filterList = new ArrayList();
	
	/** The object mapper for converting the return from forensiq */
	@JsonIgnore
	transient ObjectMapper mapper = new ObjectMapper();
	
	public static void main(String [] args) throws Exception {
		MMDBClient q = MMDBClient.build();
		q.bid("","","","","","");
	}
	
	/**
	 * Default constructor
	 */
	public static MMDBClient build() throws Exception {
		setup();
		return 	INSTANCE;
	}
	
	/**
	 * Create a MMDB bot detector with the fiven file.
	 * @param file String. The name of the file.
	 * @return MMDBClient. The object that detects bots.
	 * @throws Exception on I/O errors.
	 */
	public static MMDBClient build(String file) throws Exception {
		MMDBClient.file = file;
		setup();
		return INSTANCE;
	}
	
	/**
	 * Common setup.
	 * @throws Exception on I/O errors.
	 */
	public static void setup() throws Exception {
		File f = new File(file);
		if (!f.exists())
			throw new Exception("No such file: " + file);
		reader = new DatabaseReader.Builder(f).build();
	}
	
	/**
	 * Initialize the watch list from the configuration file.
	 * @param theList List. The list of entities to block.
	 */
	public static void setWatchlist(List<String> theList) {
		for (String s : theList) {
			watchlist.add(s.trim().toLowerCase());
		}
	}
	
	/**
	 * Initialize the filter list from the configuration file.
	 * @param theList theList. The list of entries to filter out.
	 */
	public static void setFilterList(List<String> theList) {
		for (String s : theList) {
			filterList.add(s.trim().toLowerCase());
		}	
	}
	
	/**
	 * Should I bid, or not?
	 * @param rt String. The type, usually "display".
	 * @param ip String. The IP address of the user.
	 * @param url String. The URL of the publisher.
	 * @param ua String. The user agent.
	 * @param seller String. The seller's domain.
	 * @param crid String. The creative id
	 * @return boolean. If it returns true, good to bid. Or, false if it fails the confidence test.
	 * @throws Exception on missing required fields - seller and IP.
	 */
	public FraudLog bid(String rt, String ip, String url, String ua, String seller, String crid) throws Exception {
		if (reader == null) {
			return null;
		}
		
		if (seller == null)
			seller = "na";
		if (ip == null) {
			if (bidOnError)
				return null;
			FraudLog m = new FraudLog();
			m.source = "MMDB";
			m.ip = "UNKNOWN";
			m.url = url;
			m.ua = ua;
			m.seller = seller;
			m.risk = 1;
			m.organization = "UNKNOWN";
			m.xtime = 1;
			return m;
		}
		
		try {

			long xtime = System.currentTimeMillis();
			forensiqCount.incrementAndGet();
			InetAddress ip4 = InetAddress.getByName(ip);
			IspResponse r = reader.isp(ip4);
			String test = r.getOrganization().toLowerCase();
			
			// System.out.println("--- FRAUD ----->" + test + ", size = " + watchlist.size());
			xtime = System.currentTimeMillis() - xtime;
			forensiqXtime.addAndGet(xtime);
			if (watchlist.contains(test)) {
					FraudLog m = new FraudLog();
					m.source = "MMDB";
					m.ip = ip;
					m.url = url;
					m.ua = ua;
					m.seller = seller;
					m.risk = 1;
					m.organization = test;
					m.xtime = xtime;
					return m;
				}
			for (int i=0;i<filterList.size();i++) {
				String s = filterList.get(i);
				if (test.contains(s)) {
					FraudLog m = new FraudLog();
					m.source = "MMDB";
					m.ip = ip;
					m.url = url;
					m.ua = ua;
					m.seller = seller;
					m.risk = 1;
					m.organization = test;
					m.xtime = xtime;
					return m;
				}
			} 
			
		} catch (Exception e) {
			FraudLog m = new FraudLog();
			m.source = "MMDB";
			m.ip = ip;
			m.url = url;
			m.ua = ua;
			m.seller = seller;
			m.risk = 1;
			m.organization = "NOT IN DATABASE";
			m.xtime = 1;
			return m;
		} finally {

		}
		return null;
	}
	
	/**
	 * Should we bid if there is an error?
	 * @return boolean. Returns true if you should bid even if there was I/O error on the maxmind file.
	 */
	public boolean bidOnError() {
		return bidOnError;
	}
	
}
