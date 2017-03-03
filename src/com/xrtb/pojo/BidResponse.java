package com.xrtb.pojo;

import java.util.List;


import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.bidder.SelectedCreative;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.common.Creative;
import com.xrtb.common.URIEncoder;
import com.xrtb.tools.DbTools;
import com.xrtb.tools.MacroProcessing;

/**
 * A class that handles RTB2 bid response. The BidResponse is built up using a
 * String buffer. At the close of the construction, macro substitutions are
 * applied and then it is converted to a string to be used in the HTTP response.
 * 
 * 
 * @author Ben M. Faul
 */
public class BidResponse {
	/** The object id of the corresponding bid request */
	String id;

	/** The creative associated with this response */
	transient public Creative creat;

	/** The response image width */
	public int width;
	/** The response image height */
	public int height;
	/** The latititude of the user */
	public double lat;
	/** The longitude of the user */
	public double lon;
	/** The ADM field as a string (banner ads */
	public transient String admAsString;
	/** The Native ADM */
	public String nativeAdm;
	/** The forward url used by this response */
	public String forwardUrl = "forwardUrlHere";
	/** The image url used in this response */
	public String imageUrl;
	/** The creative impression id used in this response */
	public String impid;
	/** The advertisers id used in this response */
	public String adid;
	/** The seat id of this response */
	public String seat;
	/** The creative id */
	public String crid;
	/** The domain of where the bid was directed */
	public String domain;
	/** The time it took to build the request in milliseconds (campaign processor time) */
	public int xtime;
	/** The region field, may be added by crosstalk, but if not using crosstalk, will be null */
	public String region;
	/** The dealid, if any */
	public String dealId;
	/** The price as a string */
	transient String price;

	/** The bid request associated with this response */
	public transient BidRequest br;

	/** The campaign used in this response */
	public transient Campaign camp;

	public String oidStr; // TODO: get this from the bid request object
	/** The exchange associated with this response */
	public String exchange;

	/** Will be set by the macro sub phase */
	public double cost;
	
	/** The time of the bid response */
	public long utc;

	/** The response nurl */
	transient StringBuilder snurl;
	/** The JSON of the response itself */
	transient StringBuilder response;

	transient public String capSpec;
	
	/** type of ad, video, banner, native. Was 'type', elastic search doesn;t like that */
	public String adtype;
	
	/** adx protobuf */
	public String protobuf;            // Will be null except for Adx

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
			String oidStr, double price, String dealId, int xtime) throws Exception {
		this.br = br;
		this.camp = camp;
		this.oidStr = oidStr;
		this.creat = creat;
		this.xtime = xtime;
		this.price = Double.toString(price);
		this.dealId = dealId;

		impid = br.impid;
		adid = camp.adId;
		crid = creat.impid;
		this.domain = br.siteDomain;

		forwardUrl = substitute(creat.getForwardUrl()); // creat.getEncodedForwardUrl();
		imageUrl = substitute(creat.imageurl);
		exchange = br.getExchange();

		if (!creat.isNative()) {
			if (br.w != null) {
				width = br.w.intValue();
				height = br.h.intValue();
			}
		}

		utc = System.currentTimeMillis();
		makeResponse();

	}
	
	/**
	 * Bid response object for multiple bids per request support. 
	 * @param br BidRequest used 
	 * @param multi
	 * @param oidStr
	 * @throws Exception
	 */
	public BidResponse(BidRequest br, List<SelectedCreative> multi, int xtime) throws Exception {
		this.br = br;
		this.exchange = br.getExchange();
		this.xtime = xtime;
		this.oidStr = br.id;
		this.impid = br.impid;
		/** Set the response type ****************/
		if (br.nativead)
			this.adtype="native";
		else
		if (br.video != null)
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
		linkUrlX.append(oidStr);
		linkUrlX.append("/?url=");

		// //////////////////////////////////////////////////////////////////

		if (br.lat != null)
			lat = br.lat.doubleValue();
		if (br.lon != null)
			lon = br.lon.doubleValue();
		seat = br.getExchange();
		
		/**
		 * Create the stub for the nurl, thus
		 */
		StringBuilder xnurl = new StringBuilder(config.winUrl);
		xnurl.append("/");
		xnurl.append(br.getExchange());
		xnurl.append("/");
		xnurl.append("${AUCTION_PRICE}"); // to get the win price back from the
											// Exchange....
		xnurl.append("/");
		xnurl.append(lat);
		xnurl.append("/");
		xnurl.append(lon);
		xnurl.append("/");
		xnurl.append(adid);
		xnurl.append("/");

		response = new StringBuilder("{\"seatbid\":[{\"seat\":\"");
		response.append(Configuration.getInstance().seats.get(exchange));
		response.append("\",");
		
		response.append("\"bid\":[");
			
		for (int i=0; i<multi.size();i++) {
			
			SelectedCreative x = multi.get(i);
			this.camp = x.getCampaign();
			this.creat = x.getCreative();
			this.price = Double.toString(x.price);
			this.dealId = x.dealId;
				
			snurl = new StringBuilder(xnurl);
			snurl.append(creat.impid);
			snurl.append("/");
			snurl.append(oidStr);	
			
			makeMultiResponse();
			if (i+1 < multi.size()) {
				response.append(",");
			}
		}
		
		response.append("],");
		response.append("\"id\":\"");
		response.append(oidStr); // backwards?
		response.append("\",\"bidid\":\"");
		response.append(br.id);
		response.append("\"}]}");

		this.cost = creat.price; // pass this along so the bid response object // has a copy of the price
		macroSubs(response);
	}
	
	public void makeMultiResponse() throws Exception  {
		response.append("{\"impid\":\"");
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
		response.append(adid);
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

		response.append("\"],\"adm\":\"");
		if (this.creat.isVideo()) {
			response.append(this.creat.encodedAdm);
			this.forwardUrl = this.creat.encodedAdm;   // not part of protocol, but stuff here for logging purposes
		} else if (this.creat.isNative()) {
			nativeAdm = this.creat.getEncodedNativeAdm(br);
			response.append(nativeAdm);
		} else {
			response.append(getTemplate());
		}

		response.append("\"}");
	}

	private String substitute(String str) throws Exception {
		if (str == null)
			return null;

		StringBuilder sb = new StringBuilder(str);
		MacroProcessing.replace(creat.macros, br, creat, adid, sb);

		return sb.toString();
	}

	/**
	 * Empty constructor, useful for testing.
	 */
	public BidResponse() {
		utc = System.currentTimeMillis();
	}

	/**
	 * Return the StringBuilder of the template
	 * 
	 * @return The StringBuilder of the template
	 */
	@JsonIgnore
	public String getTemplate() throws Exception {
		StringBuilder sb = null;

		/* Test if you are completely overriding the template */
		if (creat.adm_override) {
			sb = new StringBuilder(creat.forwardurl);
			macroSubs(sb);
			MacroProcessing.replace(creat.macros, br, creat, adid, sb);
			if (exchange.equals("smaato")) {
				xmlEscape(sb);
				xmlEscapeEncoded(sb);
			}
			admAsString = sb.toString();
			return admAsString;
		}

		if (exchange.equals("smaato")) {
			createSmaatoTemplate();
			sb = new StringBuilder(creat.smaatoTemplate);
			macroSubs(sb);
			MacroProcessing.replace(creat.macros, br, creat, adid, sb);
			xmlEscape(sb);
			xmlEscapeEncoded(sb);
			admAsString = sb.toString();
			return admAsString; // DO NOT URI ENCODE THIS, IT WILL SCREW UP THE
								// SMAATO XML!
		} else {

			String str = Configuration.getInstance().masterTemplate.get(exchange);
			if (str == null)
				throw new Exception("No configured template for: " + exchange);
			sb = new StringBuilder(str);

			macroSubs(sb);
			MacroProcessing.replace(creat.macros, br, creat, adid, sb);

			if (br.usesEncodedAdm == false) {
				admAsString = sb.toString();
				return sb.toString();
			} else {
				xmlEscape(sb);
				xmlEscapeEncoded(sb);
				admAsString = sb.toString();
				return URIEncoder.myUri(admAsString);
			}
		}

	}

	/**
	 * While we can't uuencode the adm for smaato (pesky XML tags, we have to
	 * change & to &amp;
	 * 
	 * @param sb
	 *            StringBuilder. The string to escape the &.
	 */
	private void xmlEscape(StringBuilder sb) {
		int i = 0;
		while (i < sb.length()) {
			i = sb.indexOf("&", i);
			if (i == -1)
				return;
			if (!(sb.charAt(i + 1) == 'a' && sb.charAt(i + 2) == 'm'
					&& sb.charAt(i + 3) == 'p' && sb.charAt(i + 4) == ';')) {

				sb.insert(i + 1, "amp;");
			}
			i += 4;
		}
	}

	private void xmlEscapeEncoded(StringBuilder sb) {
		int i = 0;
		while (i < sb.length()) {
			i = sb.indexOf("%26", i);
			if (i == -1)
				return;
			if (!(sb.charAt(i + 3) == 'a' && sb.charAt(i + 4) == 'm'
					&& sb.charAt(i + 5) == 'p' && sb.charAt(i + 6) == ';')) {

				sb.insert(i + 3, "amp;");
			}
			i += 7;
		}
	}

	/**
	 * Creates a template for the smaato exchange, which has an XML format for
	 * the ADM
	 */
	private void createSmaatoTemplate() {
		if (creat.smaatoTemplate == null) {
			if (creat.forwardurl.contains("<SCRIPT")
					|| creat.forwardurl.contains("<script")) {
				creat.smaatoTemplate = new StringBuilder(
						SmaatoTemplate.RICHMEDIA_TEMPLATE);
			} else {
				creat.smaatoTemplate = new StringBuilder(
						SmaatoTemplate.IMAGEAD_TEMPLATE);
			}

			System.out.println(new String(creat.smaatoTemplate));
			Configuration config = Configuration.getInstance();
			replaceAll(creat.smaatoTemplate, "__IMAGEURL__",
					config.SMAATOimageurl);
			replaceAll(creat.smaatoTemplate, "__TOOLTIP__",
					config.SMAATOtooltip);
			replaceAll(creat.smaatoTemplate, "__ADDITIONALTEXT__",
					config.SMAATOadditionaltext);
			replaceAll(creat.smaatoTemplate, "__PIXELURL__",
					config.SMAATOpixelurl);
			replaceAll(creat.smaatoTemplate, "__CLICKURL__",
					config.SMAATOclickurl);
			replaceAll(creat.smaatoTemplate, "__TEXT__", config.SMAATOtext);
			replaceAll(creat.smaatoTemplate, "__JAVASCRIPT__",
					config.SMAATOscript);
		}
	}

	/**
	 * Return the adm as a string. If video, use the encoded one in the
	 * creative, otherwise jusr return
	 * 
	 * @return String the adm to return to the exchange.
	 */
	@JsonIgnore
	public String getAdmAsString() {
		if (br.video != null) {
			return creat.encodedAdm;
		}
		if (br.nativePart != null)
			return nativeAdm;

		return admAsString;
	}

	/**
	 * Apply standard macro substitutions to the adm field.
	 * 
	 * @param sb
	 *            StringBuilder. The adm field being substituted into.
	 */
	public void macroSubs(StringBuilder sb) {
		String lat = "0.0";
		String lon = "0.0";
		if (br.lat != null && br.lat != 0.0 && br.lon != null && br.lon != 0) {
			lat = br.lat.toString();
			lon = br.lon.toString();
		}
		/** The configuration used for generating this response */
		Configuration config = Configuration.getInstance();
		replaceAll(sb, "{redirect_url}", config.redirectUrl);
		replaceAll(sb, "{pixel_url}", config.pixelTrackingUrl);
		replaceAll(sb, "{creative_forward_url}", creat.forwardurl);

		try {
			MacroProcessing.replace(creat.macros, br, creat, adid, sb);
			MacroProcessing.replace(Configuration.getInstance().macros, br, creat, adid, sb);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}
	
	public StringBuilder getResponseBuffer() {
		return response;
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
		if (response == null)
			return null;
		try {
		String str = response.toString();
		Map m =DbTools. mapper.readValue(str, Map.class);
		return DbTools.mapper.writer().withDefaultPrettyPrinter().writeValueAsString(m);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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
	public void makeResponse() throws Exception {
		
		/** Set the response type ****************/
		if (br.nativead)
			this.adtype="native";
		else
		if (br.video != null)
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
		linkUrlX.append(oidStr);
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
		snurl.append(oidStr);

		response = new StringBuilder("{\"seatbid\":[{\"seat\":\"");
		response.append(Configuration.getInstance().seats.get(exchange));
		response.append("\",");
		
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
		response.append(adid);
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

		response.append("\"],\"adm\":\"");
		if (this.creat.isVideo()) {
			response.append(this.creat.encodedAdm);
			this.forwardUrl = this.creat.encodedAdm;   // not part of protocol, but stuff here for logging purposes
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

		this.cost = creat.price; // pass this along so the bid response object
									// has a copy of the price
		macroSubs(response);
	}
	
	/**
	 * Instantiate a bid response from a JSON object
	 * @param content - String. The JSON object.
	 * @return BidResponse. The returned bid response.
	 * @throws Exception on JSON errors.
	 */
	public static BidResponse instantiate (String content) throws Exception  {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		BidResponse response = mapper.readValue(content, BidResponse.class);
		return response;
	}
	

	/**
	 * Output the bid response.
	 * @param response HttpServletResponse
	 * @throws Exception on I/O errors.
	 */
	public void writeTo(HttpServletResponse res) throws Exception {
		res.getOutputStream().write(response.toString().getBytes());
	}
	
	public void writeTo(HttpServletResponse res, String json) throws Exception {
		res.getOutputStream().write(json.getBytes());
	}
	
	/**
	 * Return whether this is a no bid. For openRTB it always returns false because we won't make this object when
	 * http response code is 204. Adx always returns 200.
	 * @return
	 */
	public boolean isNoBid() {
		return false;
	}
}
