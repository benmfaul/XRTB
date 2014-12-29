package com.xrtb.pojo;

import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.common.Creative;

/**
 * A class that handles RTB2 bid response.
 * @author Ben M. Faul
 */
public class BidResponse {
	/** The configuration used for generating this response */
	static Configuration config  = Configuration.getInstance();
	/** The object id of the corresponding bid request */
	String id;
	/** The JACKSON object mapper */
	ObjectMapper mapper = new ObjectMapper();
	/** The root node of the JSON object */
	JsonNode rootNode = null;
	
	/** The creative associated with this response */
	public Creative creat;
	
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
	/** The ADM maps */
	public Map adm;
	
	/** The bid request associated with this response */
	BidRequest br;
	
	/** The campaign used in this response */
	Campaign camp;
	
	public String oidStr;            // TODO: get this from the bid request object
	/** The exchange associated with this response */
	String exchange;
	
	/** The response nurl */
	StringBuffer snurl;
	/** The JSON of the response itself */
	StringBuffer response;
	
	/**
	 * Constructor for a bid response.
	 * @param br. BidRequest - the request this response is mated to.
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
		
		adm = camp.template;
		forwardUrl = creat.encodedFurl;
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
	 * Apply standard macro substitutions to the adm field.
	 */
	public String macroSubs() { 
		
		String str = (String)adm.get(exchange);	
		if (str == null)
			str = (String)adm.get("default");
		StringBuffer sb = new StringBuffer(str);
		
		sb = replace(sb,"{RTB_REDIRECT_URL}",config.redirectUrl);
		sb = replace(sb,"{RTB_CAMPAIGN_ADID}","???");                          // is this ad_id ?
		sb = replace(sb,"{RTB_PIXEL_URL}",config.pixelTrackingUrl);
				
		sb = replace(sb,"{campaign_forward_url}", creat.forwardurl);

		sb = replace(sb,"{campaign_ad_price}",""+price);
		sb = replace(sb,"{campaign_ad_width}",""+creat.w);			// todo replace with a canned string
		sb = replace(sb,"{campaign_ad_height}",""+creat.h);			// todo repplace with a canned string
		sb = replace(sb,"{creative_id}",creat.impid);
		sb = replace(sb,"{campaign_image_url}",creat.imageurl);
		sb = replace(sb,"{site_id}",br.siteId);
		
		sb = replaceAll(sb,"{pub}", exchange);
		sb = replaceAll(sb,"{bid_id}",oidStr);
		sb = replaceAll(sb,"{ad_id}", adid);
		admAsString = sb.toString();
		return admAsString;
	} 
	
	/**
	 * Replace a single instance of string.
	 * @param x
	 * @param what
	 * @param sub
	 * @return
	 */
	public static StringBuffer replace(StringBuffer x, String what, String sub) {
		StringBuffer s = x;
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
	 * @param x
	 * @param what
	 * @param sub
	 * @return
	 */
	public static StringBuffer replaceAll(StringBuffer x, String what, String sub) {
		StringBuffer s = x;
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
	
	@Override
	public String toString() {
		return response.toString();
	}
	
	/**
	 * Makes the RTB bid response's JSON response and URL.
	 */
	public void makeResponse() {
		StringBuffer  nurl = new StringBuffer();
		StringBuffer linkUrlX = new StringBuffer();
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
		
		snurl = new StringBuffer(config.winUrl);
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
		snurl.append(creat.encodedFurl);
		snurl.append("/");
		snurl.append(creat.encodedIurl);
		
		response = new StringBuffer("{\"seatbid\":[{\"seat\":\"");
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
		response.append(macroSubs());
		response.append("\"}]}],");
		response.append("\"id\":\"");
		response.append(oidStr);              // backwards?
		response.append("\",\"bidid\":\"");
		response.append(br.id);
		response.append("\"}");

	}
}
