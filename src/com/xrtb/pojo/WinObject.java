package com.xrtb.pojo;

public class WinObject {

	public static String getJson(String target) {
		int index = 0;
		String [] parts = target.split("/");
		if (parts.length > 11) 
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
		//// DO redis pull for BID ID
		//// DO redis pull for cost
		//// Do redis pull for ADM
		//// redis convert bid to win
		String bid = "";
		String cost = "";
		String adm = "";
		
		convertBidToWin(hash,cost,lat,lon,adId,pubId,image,forward,price,bid);
		return adm;
	}
	
	public static void convertBidToWin(String hash,String cost,String lat,
			String lon, String adId,String pubId,String image, 
			String forward,String price, String bid) {
		 
	}
}
