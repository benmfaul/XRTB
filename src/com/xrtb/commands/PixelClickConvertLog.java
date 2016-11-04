package com.xrtb.commands;

import com.xrtb.common.Configuration;

/**
 * Base class for logging pixel loads, clicks and conversions.
 * @author Ben M. Faul
 *
 */
public class PixelClickConvertLog  {
	public String instance;
	public String payload;
	public double lat;
	public double lon;
	public double price;
	public long time;
	public int type;
	public String ad_id;
	public String creative_id;
	public String bid_id;
	public String exchange;
	public static final int PIXEL = 0;
	public static final int CLICK = 1;
	public static final int CONVERT = 2;
	
	public static void main(String [] args) {
		PixelClickConvertLog x = new PixelClickConvertLog();
		x.create("//pixel/citenko/4/3/25c40279-dd90-4caa-afc9-d0474705e0d1/0.0425/32.83/-83.65");
		
	}
	public PixelClickConvertLog() {
		
	}
	
	public void create(String data) {
	
		if (data.contains("redirect")) {
			doClick(data);

			return;
		}
		String [] parts = data.split("/");
		payload = data;
		exchange = parts[3];
		ad_id = parts[4];
		creative_id = parts[5];
		bid_id = parts[6];
		try {
			price = Double.parseDouble(parts[7]);
			lat = Double.parseDouble(parts[8]);
			lon = Double.parseDouble(parts[9]);
		} catch (Exception error) {
			
		}
		type = PIXEL;
		time = System.currentTimeMillis();
	
	}
	
	void doClick(String payload) {
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
		time = System.currentTimeMillis();
	}
}
