package com.xrtb.bidder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple class to encode Image asset types in the embedded web server, RTBServer.java
 * @author Ben M. Faul
 *
 */
public class ImageAssetType {

	/** The mime types hash */
	private static Map<Integer,String> types = new HashMap<Integer, String>();
	static {
		add(1,"Icon");
		add(2,"Logo");
		add(3,"Main");
	}
	
	/**
	 * Add ad unit type id to hash
	 * @param a Integer. The image asset id id
	 * @param b String. The type.
	 */
	static void add(Integer a, String b) {
		types.put(a,b);
	}
	
	/**
	 * Given the ad unit id, return the type.
	 * @param key String. The id type.
	 * @return String. The image asset type
	 */
	public static String substitute(Integer key) {
		return types.get(key);
	}
}