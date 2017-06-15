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
					try {
						lat = Double.parseDouble(items[1]);
					} catch (Exception error) {
						lat = 0;
					}
					break;
				case "lon":
					try {
						lon = Double.parseDouble(items[1]);
					} catch (Exception error) {
						lon = 0;
					}
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
	
	/**
	 * Creates a click log using the payload provided and the instance name.
	 * @param payload String. The data to make the log with.
	 * @param instance String. The instance name to use.
	 */
	public ClickLog(String payload, String instance) {
		this.payload = payload;
		String [] parts = payload.split("/");
		
		lat = Double.parseDouble(parts[6].split("=")[1]);
		lon = Double.parseDouble(parts[7].split("=")[1]);
		price = Double.parseDouble(parts[5].split("=")[1]);
		//bid_id = parts[6];
		ad_id=parts[3];
		creative_id=parts[4];
		exchange = parts[2];
		type = CLICK;
		instance = this.instance;
		time = System.currentTimeMillis();
	}
}
