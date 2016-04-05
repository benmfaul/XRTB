package com.xrtb.bidder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple class to encode ad unit ids in the embedded web server, RTBServer.java
 * @author Ben M. Faul
 *
 */
public class AdUnitID {

	/** The mime types hash */
	private static Map<Integer,String> adids = new HashMap<Integer, String>();
	static {
		add(1,"Paid Search Units");
		add(2,"Recommendation Widgets");
		add(3,"Promoted Listings");
		add(4,"In-Ad (IAB Standard) with Native Element Units");
		add(5,"Custom /”Can’t Be Contained”");
	}
	
	/**
	 * Add ad unit id to hash
	 * @param a Integer. The Ad unit id
	 * @param b String. The type.
	 */
	static void add(Integer a, String b) {
		adids.put(a,b);
	}
	
	/**
	 * Given the ad unit id descri, return the description.
	 * @param key String. The file suffix.
	 * @return String. The mime type, or null
	 */
	public static String substitute(Integer key) {
		return adids.get(key);
	}
}