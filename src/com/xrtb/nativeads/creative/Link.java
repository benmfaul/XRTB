package com.xrtb.nativeads.creative;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A class that defines the Link asset of a native ad bid request.
 * @author Ben M. Faul
 *
 */

public class Link {
	/** The url  as defined in the RTB Native Ad spec */
	public String url;
	/** The fallback url as defined in the RTB Native Ad spec */
	public String fallback;
	/** The click tracker urls as defined in the RTB Native Ad spec */
	public List<String> clicktrackers;
	
	/**
	 * The empty constructor.
	 */
	public Link() {
		
	}
	
	/**
	 * Creates the bid response native ad component of the asset
	 * @return StringBuilder. The value of the asset as a string.
	 */
	@JsonIgnore
	public StringBuilder getStringBuilder() {
		StringBuilder buf = new StringBuilder();
		buf.append("{\"url\":\"");
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
