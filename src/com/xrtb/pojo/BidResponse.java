package com.xrtb.pojo;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.common.Creative;

/**
 * A class that handles RTB2 bid response. The BidResponse is built up using a String buffer. At the close of the
 * construction, macro substitutions are applied and then it is converted to a string to be used in the HTTP response.
 * @author Ben M. Faul
 */
public class BidResponse {
	/** The configuration used for generating this response */
	transient static Configuration config  = Configuration.getInstance();
	/** The object id of the corresponding bid request */
	String id;

	/** The creative associated with this response */
	transient public Creative creat;
	
	/** The response price */
	public double price;
	/** The response image width */
	public double width;
	/** The response image height */
	public double height;
	/** The latititude of the user */
	public double lat;
	/** The longitude of the user */
	public double lon;
	/** The ADM field as a string */
	public String admAsString;
	/** The forward url used by this response */
	public String forwardUrl = "forwardUrlHere";
	/** The image url used in this response */
	public String imageUrl = "imageUrlHere";
	/** The creative impression id used in this response */
	public String impid = "impIdHere";
	/** The advertisers id used in this response */
	public String adid = "sdIdHere";
	/** The seat id of this response */
	public String seat;
	
	/** The bid request associated with this response */
	transient BidRequest br;
	
	/** The campaign used in this response */
	transient Campaign camp;
	
	
	
	
	public String oidStr;            // TODO: get this from the bid request object
	/** The exchange associated with this response */
	String exchange;
	
	/** The response nurl */
	StringBuilder snurl;
	/** The JSON of the response itself */
	StringBuilder response;
	
	/**
	 * Constructor for a bid response.
	 * @param br. BidRequest - the request this response is mated to.
	 * @param creat. Creative - the creative used for this response.
	 * @param camp. Campaign - the campaign that will be used to form the response.
	 * @param oidStr. String - the unique id for this response.
	 */
	public BidResponse(BidRequest br, Campaign camp, Creative creat, String oidStr) {
		this.br = br;
		this.camp = camp;
		this.oidStr = oidStr;
		this.creat = creat;
		width = br.w;
		height = br.h;
		
		impid = creat.impid;
		
		forwardUrl = creat.getEncodedForwardUrl();
		imageUrl = creat.imageurl;
		adid = camp.adId;
		exchange = br.exchange;
		price = camp.price;
		
		makeResponse();
	}

	
	/**
	 * Empty constructor, useful for testing.
	 */
	public BidResponse() {
		
	}
	
	/**
	 * Return the StringBuilder of the template
	 * @return The StringBuilder of the template
	 */
	public StringBuilder getTemplate() { 
		
		Map adm = camp.template;
		Map x = (Map)adm.get("exchange");	
		String str = (String)x.get(exchange); 
		if (str == null)
			str = (String)adm.get("default");
		StringBuilder sb = new StringBuilder(str);
		macroSubs(sb);
		admAsString = sb.toString();
		return sb;

	} 
	
	/**
	 * Return the adm as a string. If video, use the encoded one in the creative, otherwise jusr return
	 * @return String the adm to return to the exchange.
	 */
	public String getAdmAsString() {
		if (creat.encodedAdm != null)
			return creat.encodedAdm;
		else
			return admAsString;
	}
	
	/**
	 * Apply standard macro substitutions to the adm field.
	 * @param sb StringBuilder. The adm field being substituted into.
	 */
	public void macroSubs(StringBuilder sb) {
		sb = replace(sb,"{RTB_REDIRECT_URL}",config.redirectUrl);
		sb = replace(sb,"{RTB_CAMPAIGN_ADID}",camp.adId);                         
		sb = replace(sb,"{RTB_PIXEL_URL}",config.pixelTrackingUrl);
				
		sb = replace(sb,"{campaign_forward_url}", creat.forwardurl);

		sb = replace(sb,"{campaign_ad_price}",""+price);
		sb = replace(sb,"{campaign_ad_width}",""+creat.w);			// TODO replace with a canned string
		sb = replace(sb,"{campaign_ad_height}",""+creat.h);			// TODO repplace with a canned string
		sb = replace(sb,"{creative_id}",creat.impid);
		sb = replace(sb,"{campaign_image_url}",creat.imageurl);
		sb = replace(sb,"{site_id}",br.siteId);

		sb = replaceAll(sb,"{pub}", exchange);
		sb = replaceAll(sb,"{bid_id}",oidStr);
		sb = replaceAll(sb,"{ad_id}", adid);
		sb = replaceAll(sb,"{site_id}",br.siteId);
		/* Watch out for encoded stuff */
		sb = replaceAll(sb,"%7Bpub%7D", exchange);
		sb = replaceAll(sb,"%7Bbid_id%7D",oidStr);
		sb = replaceAll(sb,"%7Bad_id%7D",adid);
		sb = replaceAll(sb,"%7Bsite_id%7D",br.siteId);
	}
	
	/**
	 * Replace a single instance of string.
	 * @param x StringBuilder. The buffer to do replacements in.
	 * @param what String. The string we are looking to replace.
	 * @param sub String. The string to use for the replacement.
	 * @return the same string buffer passed as the first param.
	 */
	public static StringBuilder replace(StringBuilder x, String what, String sub) {
		StringBuilder s = x;
		if (what == null || sub == null)
			return x;
	
		int start = x.indexOf(what);
		if (start != -1) {
			s = x.replace(start, start+what.length(), sub);
		}
		return s;
	}
	
	/**
	 * Replace All instances of a string.
	 * @param x StringBuilder. The buffer to do replacements in.
	 * @param what String. The string we are looking to replace.
	 * @param sub String. The string to use for the replacement.
	 * @return the same string buffer passed as the first param.
	 */
	public static StringBuilder replaceAll(StringBuilder x, String what, String sub) {
		StringBuilder s = x;
		if (what == null || sub == null)
			return x;
		
		while(true) {
			int start = x.indexOf(what);
			if (start != -1) {
				s = x.replace(start, start+what.length(), sub);
			} else
				break;
		}
		return s;
	}
	/**
	 * Returns the nurl for this response.
	 * @return String. The nurl field formatted for use in the bid response.
	 */
	public String getNurl() {
		if (snurl == null)
			return null;
		return snurl.toString();
	}
	
	/**
	 * Return the JSON of this bid response.
	 * @return String. The JSON to send back to the exchange.
	 */
	public String prettyPrint() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		if (response == null)
			return null;
		String str = response.toString();
		System.out.println(str);
		Map m = gson.fromJson(str, Map.class);
		return gson.toJson(m);
	}
	
	/**
	 * Convert the response to a string.
	 */
	@Override
	public String toString() {
		return response.toString();
	}
	
	/**
	 * Makes the RTB bid response's JSON response and URL.
	 */
	public void makeResponse() {
		StringBuilder  nurl = new StringBuilder();
		StringBuilder linkUrlX = new StringBuilder();
		linkUrlX.append(config.redirectUrl);
		linkUrlX.append("/");
		linkUrlX.append(oidStr);
		linkUrlX.append("/?url=");
	
		////////////////////////////////////////////////////////////////////
	
		if (br.lat != null)
			lat = br.lat.doubleValue(); 
		if (br.lon != null)
			lon = br.lon.doubleValue();
		seat = br.exchange;
		
		snurl = new StringBuilder(config.winUrl);
		snurl.append("/");
		snurl.append(br.exchange);
		snurl.append("/");
		snurl.append(""+ camp.price);
		snurl.append("/");
		snurl.append(lat);
		snurl.append("/");
		snurl.append(lon);
		snurl.append("/");
		snurl.append(adid);
		snurl.append("/");
		snurl.append(oidStr);
		snurl.append("/");	
		snurl.append(creat.getEncodedForwardUrl());
		snurl.append("/");		
		snurl.append(creat.getEncodedIUrl());
		
		response = new StringBuilder("{\"seatbid\":[{\"seat\":\"");
		response.append(Configuration.getInstance().seats.get(exchange));
		response.append("\",");
		response.append("\"bid\":[{\"impid\":\"");
		response.append(impid);
		response.append("\",\"id\":\"");
		response.append(br.id);
		response.append("\",\"price\":");
		response.append(price);
		response.append(",\"adid\":\"");
		response.append(adid);
		response.append("\",\"nurl\":\"");
		response.append(snurl);
		response.append("\",\"cid\":\"");
		response.append(adid);
		response.append("\",\"crid\":\"");
		response.append(creat.impid);
		response.append("\",\"iurl\":\"");
		response.append(imageUrl);
		response.append("\",\"adomain\":\"");
		response.append(camp.adomain);
		
		response.append("\",\"adm\":\"");
		if (this.creat.isVideo()) {
			response.append(this.creat.encodedAdm);
		} else {
			response.append(getTemplate());
		}
		
		response.append("\"}]}],");
		response.append("\"id\":\"");
		response.append(oidStr);              // backwards?
		response.append("\",\"bidid\":\"");
		response.append(br.id);
		response.append("\"}");
		
		macroSubs(response);

	}
}
