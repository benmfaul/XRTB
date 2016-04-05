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
		
		for (int i=0;i<parts.length;i++) {
			if (parts[i].indexOf("=") > -1) {
				String [] items = parts[i].split("=");
				switch(items[0]) {
				case "lat":
					lat = Double.parseDouble(items[1]);
					break;
				case "lon":
					lon = Double.parseDouble(items[1]);
					break;
				case "price":
					price = Double.parseDouble(items[1]);
					break;
				case "bid_id":
					bid_id = items[1];
					break;
				case "ad_id":
					ad_id=items[1];
					break;
				case "creative_id":
					creative_id=items[1];
					break;
				case "exchange":
					exchange = items[1];
					break;
				}
			}
		}
		type = CLICK;
		instance = Configuration.getInstance().instanceName;
		time = System.currentTimeMillis();
	}
}
