package com.xrtb.pojo;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.common.Creative;
import com.xrtb.common.URIEncoder;

/**
 * A class that handles RTB2 bid response. The BidResponse is built up using a
 * String buffer. At the close of the construction, macro substitutions are
 * applied and then it is converted to a string to be used in the HTTP response.
 * 
 * @author Ben M. Faul
 */
public class BidResponse {
	/** The configuration used for generating this response */
	transient static Configuration config = Configuration.getInstance();
	/** The object id of the corresponding bid request */
	String id;

	/** The creative associated with this response */
	transient public Creative creat;

	/** The response image width */
	public double width;
	/** The response image height */
	public double height;
	/** The latititude of the user */
	public double lat;
	/** The longitude of the user */
	public double lon;
	/** The ADM field as a string (banner ads */
	public String admAsString;
	/** The Native ADM */
	public String nativeAdm;
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

	public String oidStr; // TODO: get this from the bid request object
	/** The exchange associated with this response */
	String exchange;

	/** The response nurl */
	StringBuilder snurl;
	/** The JSON of the response itself */
	StringBuilder response;

	/**
	 * Constructor for a bid response.
	 * 
	 * @param br
	 *            . BidRequest - the request this response is mated to.
	 * @param creat
	 *            . Creative - the creative used for this response.
	 * @param camp
	 *            . Campaign - the campaign that will be used to form the
	 *            response.
	 * @param oidStr
	 *            . String - the unique id for this response.
	 */
	public BidResponse(BidRequest br, Campaign camp, Creative creat,
			String oidStr) {
		this.br = br;
		this.camp = camp;
		this.oidStr = oidStr;
		this.creat = creat;

		impid = creat.impid;

		forwardUrl = creat.getEncodedForwardUrl();
		imageUrl = creat.imageurl;
		adid = camp.adId;
		exchange = br.exchange;

		if (!creat.isNative()) {
			width = br.w;
			height = br.h;
		}

		makeResponse();
	}

	/**
	 * Empty constructor, useful for testing.
	 */
	public BidResponse() {

	}

	/**
	 * Return the StringBuilder of the template
	 * 
	 * @return The StringBuilder of the template
	 */
	public String getTemplate() {
		StringBuilder sb = null;
		if (exchange.equals("smaato")) {
			createSmaatoTemplate();
			sb = new StringBuilder(creat.smaatoTemplate);
		} else {
			Map adm = camp.template;
			Map x = (Map) adm.get("exchange");
			String str = null;
			if (x != null)
				str = (String) x.get(exchange);
			if (str == null)
				str = (String) adm.get("default");
			sb = new StringBuilder(str);
		}
		macroSubs(sb);
		admAsString = sb.toString();
		return URIEncoder.myUri(admAsString);

	}
	
	private void createSmaatoTemplate() {
		if (creat.smaatoTemplate == null) {
			if (creat.forwardurl.contains("<SCRIPT>")
					|| creat.forwardurl.contains("<script>")) {
				creat.smaatoTemplate = new StringBuilder(SmaatoTemplate.RICHMEDIA_TEMPLATE); 
			} else {
				creat.smaatoTemplate = new StringBuilder(SmaatoTemplate.IMAGEAD_TEMPLATE);
			}
			this.replaceAll(creat.smaatoTemplate, "__CLICKURL__", camp.SMAATOclickurl);
			this.replaceAll(creat.smaatoTemplate, "__IMAGEURL__", camp.SMAATOimageurl);
			this.replaceAll(creat.smaatoTemplate, "__TOOLTIP__", camp.SMAATOtooltip);
			this.replaceAll(creat.smaatoTemplate, "__ADDITIONALTEXT__", camp.SMAATOadditionaltext);
			this.replaceAll(creat.smaatoTemplate, "__PIXELURL__", camp.SMAATOpixelurl);
			this.replaceAll(creat.smaatoTemplate, "__CLICKURL__", camp.SMAATOclickurl);
			this.replaceAll(creat.smaatoTemplate, "__TEXT__", camp.SMAATOtext);
			this.replaceAll(creat.smaatoTemplate, "__JAVASCRIPT__", camp.SMAATOscript);
		}
	}

	/**
	 * Return the adm as a string. If video, use the encoded one in the
	 * creative, otherwise jusr return
	 * 
	 * @return String the adm to return to the exchange.
	 */
	public String getAdmAsString() {
		if (br.video != null)
			return creat.encodedAdm;
		if (br.nativePart != null)
			return nativeAdm;
		;
		return admAsString;
	}

	/**
	 * Apply standard macro substitutions to the adm field.
	 * 
	 * @param sb
	 *            StringBuilder. The adm field being substituted into.
	 */
	public void macroSubs(StringBuilder sb) {
		replaceAll(sb, "{redirect_url}", config.redirectUrl);
		replaceAll(sb, "{pixel_url}", config.pixelTrackingUrl);

		replaceAll(sb, "{campaign_forward_url}", creat.forwardurl);
		replaceAll(sb, "{campaign_ad_price}", creat.strPrice);
		replaceAll(sb, "{campaign_ad_width}", creat.strW);
		replaceAll(sb, "{campaign_ad_height}", creat.strH);
		replaceAll(sb, "{creative_id}", creat.impid);
		replaceAll(sb, "{campaign_image_url}", creat.imageurl);
		replaceAll(sb, "{site_id}", br.siteId);

		replaceAll(sb, "{pub}", exchange);
		replaceAll(sb, "{bid_id}", oidStr);
		replaceAll(sb, "{ad_id}", adid);
		replaceAll(sb, "%7Bpub%7D", exchange);
		replaceAll(sb, "%7Bbid_id%7D", oidStr);
		replaceAll(sb, "%7Bad_id%7D", adid);
		replaceAll(sb, "%7Bsite_id%7D", br.siteId);
		replaceAll(sb, "%7Bcreative_id%7D", creat.impid);
	}

	/**
	 * Replace a single instance of string.
	 * 
	 * @param x
	 *            StringBuilder. The buffer to do replacements in.
	 * @param what
	 *            String. The string we are looking to replace.
	 * @param sub
	 *            String. The string to use for the replacement.
	 */
	public static void replace(StringBuilder x, String what, String sub) {
		if (what == null || sub == null)
			return;

		int start = x.indexOf(what);
		if (start != -1) {
			x.replace(start, start + what.length(), sub);
		}
	}

	/**
	 * Replace All instances of a string.
	 * 
	 * @param x
	 *            StringBuilder. The buffer to do replacements in.
	 * @param what
	 *            String. The string we are looking to replace.
	 * @param sub
	 *            String. The string to use for the replacement.
	 */
	public static void replaceAll(StringBuilder x, String what, String sub) {
		if (what == null || sub == null)
			return;
		int start = x.indexOf(what);
		if (start != -1) {
			x.replace(start, start + what.length(), sub);
			replaceAll(x, what, sub);
		}
	}

	/**
	 * Returns the nurl for this response.
	 * 
	 * @return String. The nurl field formatted for use in the bid response.
	 */
	public String getNurl() {
		if (snurl == null)
			return null;
		return snurl.toString();
	}

	/**
	 * Return the JSON of this bid response.
	 * 
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
		StringBuilder nurl = new StringBuilder();
		StringBuilder linkUrlX = new StringBuilder();
		linkUrlX.append(config.redirectUrl);
		linkUrlX.append("/");
		linkUrlX.append(oidStr);
		linkUrlX.append("/?url=");

		// //////////////////////////////////////////////////////////////////

		if (br.lat != null)
			lat = br.lat.doubleValue();
		if (br.lon != null)
			lon = br.lon.doubleValue();
		seat = br.exchange;

		snurl = new StringBuilder(config.winUrl);
		snurl.append("/");
		snurl.append(br.exchange);
		snurl.append("/");
		snurl.append(creat.strPrice);
		snurl.append("/");
		snurl.append(lat);
		snurl.append("/");
		snurl.append(lon);
		snurl.append("/");
		snurl.append(adid);
		snurl.append("/");
		snurl.append(creat.impid);
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
		response.append("\"");

		if (camp.encodedIab != null) {
			response.append(",");
			response.append(camp.encodedIab);
		}

		if (creat.currency != null) { // fyber uses this, but is not standard.
			response.append(",");
			response.append("\"cur\":\"");
			response.append(creat.currency);
		}

		response.append(",\"price\":");
		response.append(creat.strPrice);
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
		response.append("\",\"adomain\": [\"");
		response.append(camp.adomain);

		response.append("\"],\"adm\":\"");
		if (this.creat.isVideo()) {
			response.append(this.creat.encodedAdm);
		} else if (this.creat.isNative()) {
			nativeAdm = this.creat.getEncodedNativeAdm(br);
			response.append(nativeAdm);
		} else {
			response.append(getTemplate());
		}

		response.append("\"}]}],");
		response.append("\"id\":\"");
		response.append(oidStr); // backwards?
		response.append("\",\"bidid\":\"");
		response.append(br.id);
		response.append("\"}");

		macroSubs(response);

	}
}
