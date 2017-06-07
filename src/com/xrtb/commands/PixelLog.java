package com.xrtb.commands;

import com.xrtb.bidder.RTBServer;
import com.xrtb.common.Configuration;
import com.xrtb.exchanges.adx.AdxWinObject;
import com.xrtb.exchanges.google.GoogleWinObject;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.WinObject;

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
						
					}
					break;
				case "lon":
					try {
						lon = Double.parseDouble(items[1]);
					} catch (Exception error) {
						
					}
					break;
				case "price":
					try {
						price = Double.parseDouble(items[1]);
					} catch (Exception error) {
						price = 0;
						String ctext = items[1].trim();
						if (exchange.equals("google") || exchange.equals("adx")) {
							try {
								if (exchange.equals("google"))
									price = GoogleWinObject.decrypt(ctext, System.currentTimeMillis());
								else 
									price = AdxWinObject.decrypt(ctext, System.currentTimeMillis());
							} catch (Exception e) {
								
							}
						}
					}
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
		type = PIXEL;
		instance = Configuration.getInstance().instanceName;
		time = System.currentTimeMillis();
		instance = Configuration.getInstance().instanceName;
		
		/**
		 * Huge hack. C1X SSP does not do win url's you have to piggy back the the win from the pixel
		 */
		if (BidRequest.usesPiggyBackedWins(exchange)) {
			try {
				StringBuilder sb =  new StringBuilder();
				sb.append("https://fake:notreal/rtb/win/");
				sb.append(exchange);
				sb.append("/");
				sb.append(price);
				sb.append("/");
				sb.append(lat);
				sb.append("/");
				sb.append(lon);
				sb.append("/");
				sb.append(ad_id);
				sb.append("/");
				sb.append(creative_id);
				sb.append("/");
				sb.append(bid_id);
				WinObject.getJson(sb.toString());
				RTBServer.win++;
			} catch (Exception error) {
				error.printStackTrace();
			}
		}
	}
	
	/**
	 * Create a pixel log from the payload and the bidder instance name.
	 * @param payload String. The data to convert.
	 * @param instance String. The instance name.
	 */
	public PixelLog(String payload, String instance) {
		type = PIXEL;
		this.payload = payload;
		String [] parts = payload.split("/");
		lat = Double.parseDouble(parts[8]);
		lon = Double.parseDouble(parts[9]);
		price = Double.parseDouble(parts[7]);
		bid_id = parts[6];
		ad_id=parts[4];
		creative_id=parts[5];
		exchange = parts[3];
		type = PIXEL;
		time = System.currentTimeMillis();
		this.instance = instance;
	}
}
