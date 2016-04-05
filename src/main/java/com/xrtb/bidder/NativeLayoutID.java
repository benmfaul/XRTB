package com.xrtb.bidder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple class to encode native ad ids in the embedded web server, RTBServer.java
 * @author Ben M. Faul
 *
 */
public class NativeLayoutID {

	/** The mime types hash */
	private static Map<Integer,String> ids = new HashMap<Integer, String>();
	static {
		add(1,"Content Wall");
		add(2,"App Wall");
		add(3,"News Feed");
		add(4,"Chat List");
		add(5,"Carousel");
		add(6,"Content Stream");

	}
	
	/**
	 * Add native ad type to hash
	 * @param a Integer. The native ad type
	 * @param b String. The mime type.
	 */
	static void add(Integer a, String b) {
		ids.put(a,b);
	}
	
	/**
	 * Given the file suffix, return the mime type.
	 * @param key String. The file suffix.
	 * @return String. The mime type, or null
	 */
	public static String substitute(Integer key) {
		return ids.get(key);
	}
}