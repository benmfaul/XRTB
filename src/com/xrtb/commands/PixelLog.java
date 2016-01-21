package com.xrtb.commands;

import com.xrtb.common.Configuration;

/**
 * A class for logging pixel loads. (ad loads in user web page)
 * @author Ben M. Faul.
 *
 */
public class PixelLog extends PixelClickConvertLog {

	/**
	 * Default constructor
	 */
	public PixelLog() {
		super();
		type = PIXEL;
	}
	
	/**
	 * Create a Click log, the payload is the URI.
	 * @param payload String. The URI.
	 */
	public PixelLog(String payload) {
		type = PIXEL;
		String [] parts = payload.split("/");
		price = Double.parseDouble(parts[parts.length-3]);
		lat = Double.parseDouble(parts[parts.length-2]);
		lon = Double.parseDouble(parts[parts.length-1]);
		this.payload = payload;
		instance = Configuration.getInstance().instanceName;
		time = System.currentTimeMillis();
	}
}
