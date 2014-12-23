package com.xrtb.pojo;

import java.util.Map;

import com.xrtb.bidder.Controller;

public class WinObject {

	public static String getJson(String target) throws Exception {
		int index = 0;
		String [] parts = target.split("/");
		if (parts.length > 12) 
			index = 1;
		
		String pubId = parts[3 + index];
		String price = parts[4 + index];
		String lat = parts[5 + index];
		String lon = parts[6 + index];
		String adId = parts[7 + index];
		String hash = parts[8 + index];
		String forward = parts[9 + index];
		String image = parts[10 + index];
		
		
		image = image.replaceAll("%3A",":");
		forward = image.replaceAll("%2F", "/");

		Map data = Controller.getInstance().getBidData(hash);
		if (data == null) {
			throw new Exception("{\"error\":\"can't find bid data for " + hash + "}");
		}
		String bid = "";
		String cost = "";
		String adm = "TBD";
		
		convertBidToWin(hash,cost,lat,lon,adId,pubId,image,forward,price,bid);
		return adm;
	}
	
	public static void convertBidToWin(String hash,String cost,String lat,
			String lon, String adId,String pubId,String image, 
			String forward,String price, String bid) {
		 
	}
}
