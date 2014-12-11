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
	static Configuration config  = Configuration.getInstance();
	String id;
	ObjectMapper mapper = new ObjectMapper();
	JsonNode rootNode = null;
	
	public Creative creat;
	
	public double price;
	public double w;
	public double h;
	public double lat;
	public double lon;
	public String forwardUrl = "forwardUrlHere";
	public String imageUrl = "imageUrlHere";
	public String impid = "impIdHere";
	public String adid = "sdIdHere";
	public String campaignImpId = "campaignImpIdHere";
	public String seat;
	public Map adm;
	BidRequest br;
	Campaign camp;
	String oidStr;
	String exchange;
	
	StringBuffer snurl;
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
		
		impid = creat.impid;
		
		adm = camp.template;
		forwardUrl = creat.encodedFurl;
		imageUrl = creat.imageUrl;
		adid = camp.id;
		exchange = br.exchange;
		price = camp.price;
		
		macroSubs();
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
		
	   	sb = replace(sb,"{pixel_url}",config.pixelTrackingUrl);
		sb = replace(sb,"{redirect_url}", config.redirectUrl);
		sb = replace(sb,"{campaign_forward_url}", forwardUrl);

	   	sb = replace(sb,"{bid_id}",oidStr);
		sb = replace(sb,"{adid}", adid);
		sb = replace(sb,"{price}", ""+price);
		sb = replace(sb,"{creative_id}",campaignImpId);
		sb = replace(sb,"{pub}", exchange);
		return sb.toString();
	}
	
	StringBuffer replace(StringBuffer x, String what, String sub) {
		StringBuffer s = x;
		int start = x.indexOf(what);
		if (start != -1) {
			s = x.replace(start, start+what.length(), what);
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
		response.append("\"id\":");
		response.append(oidStr);              // backwards?
		response.append(",\"bidid\":");
		response.append(br.id);
		response.append("}");

	}
}
