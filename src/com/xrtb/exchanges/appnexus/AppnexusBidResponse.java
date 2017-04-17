package com.xrtb.exchanges.appnexus;

import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.common.Creative;
import com.xrtb.pojo.BidResponse;
import com.xrtb.pojo.Impression;

public class AppnexusBidResponse extends BidResponse {

	public AppnexusBidResponse() {
		
	}
	
	public AppnexusBidResponse(Appnexus br, Campaign camp, Creative creat, Impression imp) {
		
	}

	public AppnexusBidResponse(Appnexus br, Impression imp, Campaign camp, Creative creat, String id,
			double price, String dealId, int xtime) throws Exception {

		this.br = br;
		this.imp = imp;
		this.camp = camp;
		this.oidStr = br.id;
		this.creat = creat;
		this.xtime = xtime;
		this.price = Double.toString(price);
		this.dealId = dealId;

		impid = imp.getImpid();
		adid = camp.adId;
		crid = creat.impid;
		this.domain = br.siteDomain;

		forwardUrl = substitute(creat.getForwardUrl()); // creat.getEncodedForwardUrl();
		imageUrl = substitute(creat.imageurl);
		exchange = br.getExchange();

		if (!creat.isNative()) {
			if (imp.w != null) {
				width = imp.w.intValue();
				height = imp.h.intValue();
			}
		}

		utc = System.currentTimeMillis();
		makeResponse(price);
	}
	
	/**
	 * Makes the RTB bid response's JSON response and URL.
	 */
	@Override
	public void makeResponse(double price) throws Exception {
		
		/** Set the response type ****************/
		if (imp.nativead)
			this.adtype="native";
		else
		if (imp.video != null)
			this.adtype="video";
		else
			this.adtype="banner";
		/******************************************/
		
		/** The configuration used for generating this response */
		Configuration config = Configuration.getInstance();
		StringBuilder nurl = new StringBuilder();
		StringBuilder linkUrlX = new StringBuilder();
		linkUrlX.append(config.redirectUrl);
		linkUrlX.append("/");
		linkUrlX.append(oidStr.replaceAll("#", "%23"));
		linkUrlX.append("/?url=");

		// //////////////////////////////////////////////////////////////////

		if (br.lat != null)
			lat = br.lat.doubleValue();
		if (br.lon != null)
			lon = br.lon.doubleValue();
		seat = br.getExchange();

		snurl = new StringBuilder(config.winUrl);
		snurl.append("/");
		snurl.append(br.getExchange());
		snurl.append("/");
		snurl.append("${AUCTION_PRICE}"); // to get the win price back from the
											// Exchange....
		snurl.append("/");
		snurl.append(lat);
		snurl.append("/");
		snurl.append(lon);
		snurl.append("/");
		snurl.append(adid);
		snurl.append("/");
		snurl.append(creat.impid);
		snurl.append("/");
		snurl.append(oidStr.replaceAll("#", "%23"));

		response = new StringBuilder("{\"seatbid\":[{\"seat\":\"");
		response.append(Configuration.getInstance().seats.get(exchange));
		response.append("\",\"group\":0,");
				
		response.append("\"bid\":[{\"impid\":\"");
		response.append(impid);							// the impression id from the request
		response.append("\",\"id\":\"");
		response.append(br.id);						// the request bid id
		response.append("\"");

		/*
		 * if (camp.encodedIab != null) { response.append(",");
		 * response.append(camp.encodedIab); }
		 */

		if (creat.currency != null && creat.currency.length() != 0) { // fyber
																		// uses
																		// this,
																		// but
																		// is
																		// not
																		// standard.
			response.append(",");
			response.append("\"cur\":\"");
			response.append(creat.currency);
			response.append("\"");
		}

		response.append(",\"price\":");
		response.append(price);
		response.append(",\"adid\":\"");
		
		if (creat.alternateAdId == null)
			response.append(adid);
		else
			response.append(creat.alternateAdId);
		
		response.append("\",\"nurl\":\"");
		response.append(snurl);
		response.append("\",\"cid\":\"");
		response.append(adid);
		response.append("\",\"crid\":\"");
		response.append(creat.impid);
		if (dealId != null) {
			response.append("\",\"dealid\":\"");
			response.append(dealId);
		}
		response.append("\",\"iurl\":\"");
		response.append(imageUrl);
		response.append("\",\"adomain\": [\"");
		response.append(camp.adomain);
		response.append("\"]");
		// Does not use the ADM field.

		response.append("}]}],");
		response.append("\"id\":\"");
		response.append(oidStr); // backwards?
		response.append("\",\"bidid\":\"");
		response.append(br.id);
		response.append("\"}");

		this.cost = price; // pass this along so the bid response object
									// has a copy of the price
		macroSubs(response);
	}
	
	
}
