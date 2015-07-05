package com.xrtb.bidder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MimeTypes {

	static Map<String,String> mimes = new HashMap();
	static List<String> types = new ArrayList();
	static {
		add("gif","image/gif");
		add("png","image/png");
		add("gif","image/gif");
		add("jpg","image/jpg");
		add("js","text/javascript;charset=utf-8");
		add("css","text/css");
		add("mp4","video/mp4");
		add("ogv","video/ogg");
	}
	
	static void add(String a, String b) {
		mimes.put(a,b);
	}
	
	public static String substitute(String key) {
		return mimes.get(key);
	}
}
