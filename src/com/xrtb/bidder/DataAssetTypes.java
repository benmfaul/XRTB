package com.xrtb.bidder;

import java.util.HashMap;

/**
 * A list of common asset element types of native advertising.
 * @author Ben M. Faul
 *
 */

public class DataAssetTypes {

	private static HashMap<Integer,String> types = new HashMap();
	static {
		add(1,"sponsored");
		add(2,"desc");
		add(3,"rating");
		add(4,"likes");
		add(5,"downloads");
		add(6,"price");
		add(7,"saleprice");
		add(8,"phone");
		add(9,"address");
		add(10,"desc2");
		add(11,"displayurl");
		add(12,"ctatext");
	}
	
	/**
	 * Add data asset id to hash
	 * @param a Integer. The data asset id
	 * @param b String. The type.
	 */
	static void add(Integer a, String b) {
		types.put(a,b);
	}
	
	/**
	 * Given the data asset id, return the description.
	 * @param key String. The file suffix.
	 * @return String. The mime type, or null
	 */
	public static String substitute(Integer key) {
		return types.get(key);
	}
}
