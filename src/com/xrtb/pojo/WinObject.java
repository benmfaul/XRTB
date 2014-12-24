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
	static URLDecoder decoder = new URLDecoder();
	/** The controller object of this bid engine. */
	static Controller control = Controller.getInstance();
	
	/**
	 * The worker method for converting a WIN http target into a win notification in the bidder.
	 * @param target String. The HTTP url that makes up the win notification from the exchange.
	 * @return String. The ADM field to be used by exchange serving up the data.
	 * @throws Exception. Throws exceptions on JSON errors.
	 */
	public static String getJson(String target) throws Exception {
		int index = 0;
		
		
		
		String [] parts = target.split("http:");
		String forward = "http:" + parts[1];
		String image = "http:"+ parts[2];
		
		parts = parts[0].split("/");
		String pubId = parts[3];
		String price = parts[4];
		String lat = parts[5];
		String lon = parts[6];
		String adId = parts[7];
		String hash = parts[8];
		
		image = decoder.decode(image);
		forward = decoder.decode(forward);
		Map data = Controller.getInstance().getBidData(hash);
		if (data == null) {
			throw new Exception("{\"error\":\"can't find bid data for " + hash + "}");
		}
		String cost = "";

		String adm = "TBD";
		
		convertBidToWin(hash,cost,lat,lon,adId,pubId,image,forward,price);
		return adm;
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
	 * @param price String. ??????????/
	 * @param bid String. ????????????????
	 * 
	 * TODO: Last 2 look redundant
	 */
	public static void convertBidToWin(String hash,String cost,String lat,
			String lon, String adId,String pubId,String image, 
			String forward,String price) {
		 
		StringBuffer buf = new StringBuffer();
		// Remove the bid ID from the cache, we won...
		control.deleteBidFromCache(hash);
		
		buf.append("{");
		
		buf.append("\"id\":"); buf.append("\"" + hash + "\"");
		buf.append(",\"cost\":"); buf.append("\"" + cost + "\"");
		buf.append(",\"lat\":"); buf.append("\"" + lat + "\"");
		buf.append(",\"lon\":"); buf.append("\"" + lon + "\"");
		buf.append(",\"adid\":"); buf.append("\"" + adId + "\"");
		buf.append(",\"pubId\":"); buf.append("\"" + pubId + "\"");
		buf.append(",\"image\":"); buf.append("\"" + image + "\"");
		buf.append(",\"forrward\":"); buf.append("\"" + forward + "\"");
		buf.append(",\"price\":"); buf.append("\"" + price + "\"");
		
		buf.append("}");
		
		control.sendWin(buf.toString());
	}
}
