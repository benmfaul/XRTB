package com.xrtb.commands;


import com.xrtb.common.Configuration;

/**
 * A log record for when the user clicks on the ad.
 * @author Ben M. Faul
 *
 */
public class ClickLog extends PixelClickConvertLog {

	/**
	 * Default constructor
	 */
	public ClickLog() {
		super();
		type = CLICK;
	}
	
	/**
	 * Create a click log, the payload is the URI.
	 * @param payload String. The URI.
	 */
	public ClickLog(String payload) {
		this.payload = payload;
		String [] parts = payload.split("/");
		
		price = Double.parseDouble(parts[parts.length-3]);
		lat = Double.parseDouble(parts[parts.length-2]);
		lon = Double.parseDouble(parts[parts.length-1]);
		type = CLICK;
		instance = Configuration.getInstance().instanceName;
		time = System.currentTimeMillis();
	}
}
