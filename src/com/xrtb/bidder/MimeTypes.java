package com.xrtb.bidder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A simple class to encode mime types in the embedded web server, RTBServer.java
 * @author Ben M. Faul
 *
 */
public class MimeTypes {

	static final String TEXT_JS = "text/javascript";
	/** The mime types hash */
	static Map<String,String> mimes = new HashMap<String, String>();
	static {
		add("gif","image/gif");
		add("png","image/png");
		add("gif","image/gif");
		add("jpg","image/jpg");
		add("js",TEXT_JS);
		add("css","text/css");
		add("mp4","video/mp4");
		add("ogv","video/ogg");
		add("xml","application/xml");
		add("svg","image/svg+xml");
	}
	
	/**
	 * Add suffix/mine-type to hash
	 * @param a String. The file suffix
	 * @param b String. The mime type.
	 */
	static void add(String a, String b) {
		mimes.put(a,b);
	}
	
	/**
	 * Given the file suffix, return the mime type.
	 * @param key String. The file suffix.
	 * @return String. The mime type, or null
	 */
	public static String substitute(String key) {
		return mimes.get(key);
	}
	
	/**
	 * Given a string of a creative, return the mime type
	 * @param str String. The creative string
	 * @return String. The mime type if known, or null.
	 */
	public static String determineType(String str) {
		str = str.toLowerCase();
		Set<Entry<String, String>> entrySet = mimes.entrySet();
		if (str.contains("<script")) {
			return TEXT_JS;
		}
		for (Entry<String,String> e : entrySet) {
			String test = "." + e.getKey();
			
			if (str.contains(test))
				return e.getValue();
		}
		return null;
	}
}
