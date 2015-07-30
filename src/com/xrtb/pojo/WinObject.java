package com.xrtb.pojo;

import java.net.URLDecoder;

import java.util.Map;

import com.xrtb.bidder.Controller;

/**
 * TODO: This needs work, this is a performance pig
 * @author Ben M. Faul
 *
 */
public class WinObject {

	/** URL decoder used with digesting encoded url fields */
	static transient URLDecoder decoder = new URLDecoder();
	String hash,  cost, lat, lon,  adId, pubId, image,  forward, price;
	
	public WinObject() {
		
	}
	
	public WinObject(String hash,String cost,String lat,
			String lon, String adId,String pubId,String image, 
			String forward,String price) {
		this.hash = hash;
		this.cost = cost;
		this.lat = lat;
		this.lon = lon;
		this.adId = adId;
		this.pubId = pubId;
		this.image = image;
		this.forward = forward;
		this.price = price;
	}
	/**
	 * The worker method for converting a WIN http target into a win notification in the bidder.
	 * @param target String. The HTTP url that makes up the win notification from the exchange.
	 * @return String. The ADM field to be used by exchange serving up the data.
	 * @throws Exception on REDIS errors.
	 */
	public static String getJson(String target) throws Exception {	
		String image = null;
		String [] parts = target.split("http");
		String forward = "http:" + parts[1];
		if (parts.length > 2)
			image = "http:"+ parts[2];
		
		parts = parts[1].split("/");
		String pubId = parts[5];
		String price = parts[6];
		String lat = parts[7];
		String lon = parts[8];
		String adId = parts[9];
		String hash = parts[10];
		
		if (image != null)
			image = decoder.decode(image,"UTF-8");
		forward = decoder.decode(forward,"UTF-8");
		String cost = "";

		Map bid = Controller.getInstance().getBidData(hash);
		if (bid == null || bid.isEmpty()) {
			throw new Exception("No bid to convert to win: " + hash);
		}
		
		convertBidToWin(hash,cost,lat,lon,adId,pubId,image,forward,price);
		return (String)bid.get("ADM");
	}
	
	/**
	 * Pluck out the pieces from the win notification and create a win message.
	 * @param hash String. The object ID of the bid
	 * @param cost String. The cost of the bid.
	 * @param lat String. The latitude of the usre.
	 * @param lon String. The longitude of the user.
	 * @param adId String. The campaign ad id.
	 * @param pubId String. The publisher id.
	 * @param image String. The image served.
	 * @param forward String. The forwarding URL.
	 * @param price String. ??????????
	 * @throws Exception on REDIS errors (bid not found, can happen if bid times out.
	 * 
	 * TODO: Last 2 look redundant
	 */
	public static void convertBidToWin(String hash,String cost,String lat,
			String lon, String adId,String pubId,String image, 
			String forward,String price) throws Exception {
		 
		StringBuilder buf = new StringBuilder();
		// Remove the bid ID from the cache, we won...
		Controller.getInstance().deleteBidFromCache(hash);
		
		
		
		buf.append("{");
		
		buf.append("\"id\":"); buf.append("\"" + hash + "\"");
		buf.append(",\"cost\":"); buf.append("\"" + cost + "\"");
		buf.append(",\"lat\":"); buf.append("\"" + lat + "\"");
		buf.append(",\"lon\":"); buf.append("\"" + lon + "\"");
		buf.append(",\"adid\":"); buf.append("\"" + adId + "\"");
		buf.append(",\"pubId\":"); buf.append("\"" + pubId + "\"");
		buf.append(",\"image\":"); buf.append("\"" + image + "\"");
		buf.append(",\"forward\":"); buf.append("\"" + forward + "\"");
		buf.append(",\"price\":"); buf.append("\"" + price + "\"");
		
		buf.append("}");
		
		Controller.getInstance().sendWin(hash,cost,lat,
				lon,  adId, pubId, image, 
				 forward, price);
	}
}
