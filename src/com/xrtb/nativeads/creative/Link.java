package com.xrtb.nativeads.creative;

import java.util.ArrayList;
import java.util.List;

public class Link {
	public String url;
	public String fallback;
	public List<String> clicktrackers;
	
	public Link() {
		
	}
	
	public StringBuilder getStringBuilder() {
		StringBuilder buf = new StringBuilder();
		buf.append("{\"ur\":\"");
		buf.append(url);
		buf.append("\",\"fallback\":\"");
		buf.append(fallback);
		buf.append("\",\"clicktrackers\":[");
		for (int i=0;i<clicktrackers.size();i++) {
			buf.append("\"");
			buf.append(clicktrackers.get(i));
			buf.append("\"");
			if (i+1 != clicktrackers.size()) 
				buf.append(",");
		}
		buf.append("]}");
		return buf;
	}
}
