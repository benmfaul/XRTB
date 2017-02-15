package com.xrtb.pojo;

import java.net.URLDecoder;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.bidder.Controller;
import com.xrtb.bidder.RTBServer;
import com.xrtb.exchanges.adx.AdxBidRequest;
import com.xrtb.exchanges.adx.AdxWinObject;

/**
 * TODO: This needs work, this is a performance pig
 * 
 * @author Ben M. Faul
 *
 */
public class WinObject {

	/** URL decoder used with digesting encoded url fields */
	transient static ObjectMapper mapper = new ObjectMapper();
	static transient URLDecoder decoder = new URLDecoder();

	public String hash, cost, lat, lon, adId, pubId, image, forward, price, cridId, siteId, adm;
	
	/** The region field, may be added by crosstalk, but if not using crosstalk, will be null */
	public String region;
	/** The time the record was written */
	public long utc;

	public WinObject() {

	}

	public WinObject(String hash, String cost, String lat, String lon, String adId, String crid, String siteid, String pubId,
			String image, String forward, String price, String adm) {
		this.hash = hash;
		this.cost = cost;
		this.lat = lat;
		this.lon = lon;
		this.adId = adId;
		this.cridId = crid;
		this.siteId = siteid;
		this.pubId = pubId;
		this.image = image;
		this.forward = forward;
		this.price = price;
		if (adm == null)
			this.adm = "";
		else
			this.adm = adm;
		this.utc = System.currentTimeMillis();
	}

	/**
	 * The worker method for converting a WIN http target into a win
	 * notification in the bidder.
	 * 
	 * @param target
	 *            String. The HTTP url that makes up the win notification from
	 *            the exchange.
	 * @return String. The ADM field to be used by exchange serving up the data.
	 * @throws Exception
	 *             on REDIS errors.
	 */
	@JsonIgnore
	public static String getJson(String target) throws Exception {
		String image = null;
		String[] parts = target.split("http");
		String forward = "http:" + parts[1];
		if (parts.length > 2)
			image = "http:" + parts[2];

		parts = parts[1].split("/");
		String pubId = parts[5];
		String price = parts[6];
		String lat = parts[7];
		String lon = parts[8];
		String adId = parts[9];
		String cridId = parts[10];
		String siteId = parts[11];
        String hash = parts[12];

		if (image != null)
			image = decoder.decode(image, "UTF-8");
		forward = decoder.decode(forward, "UTF-8");
		String cost = "";

		/*
		 * This is synthetic, because in reality, adx has no win notification,
		 * this is a fake pixel fire that does the work
		 */
		if (pubId.equals(AdxBidRequest.ADX)) {
			Long value = AdxWinObject.decrypt(price, System.currentTimeMillis());
			Double dv = new Double(value);
			dv /= 1000000;
			convertBidToWin(hash, cost, lat, lon, adId, cridId, siteId, pubId, image, forward, dv.toString(), pubId);
			BidRequest.incrementWins(pubId);
			return "";
		}

		Map bid = Controller.getInstance().getBidData(hash);
		
		// if (bid == null || bid.isEmpty()) {
		// throw new Exception("No bid to convert to win: " + hash);
		// }
		String adm = null;
		try {
			adm = (String) bid.get("ADM");
			cost = (String) bid.get("PRICE");
		} catch (Exception error) {
			// System.out.println("-----------> " + bid);
		}

		// If the adm can't be retrieved, go ahead and convert it to win so that
		// the accounting works. just return ""
		convertBidToWin(hash, cost, lat, lon, adId, cridId, siteId, pubId, image, forward, price, adm);
		BidRequest.incrementWins(pubId);

		if (adm == null) {
			return "";
		}
		return adm;
	}

	/**
	 * Fast write this to a JSON String.
	 * 
	 * @return String. The json representation of this object.
	 */
	public String toString() {
		try {
			return mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Pluck out the pieces from the win notification and create a win message.
	 * 
	 * @param hash
	 *            String. The object ID of the bid
	 * @param cost
	 *            String. The cost of the bid.
	 * @param lat
	 *            String. The latitude of the usre.
	 * @param lon
	 *            String. The longitude of the user.
	 * @param adId
	 *            String. The campaign ad id.
	 * @param pubId
	 *            String. The publisher id.
	 * @param image
	 *            String. The image served.
	 * @param forward
	 *            String. The forwarding URL.
	 * @param price
	 *            String. ??????????
	 * @param adm
	 *            String. The adm that was returned.
	 * @throws Exception
	 *             on REDIS errors (bid not found, can happen if bid times out.
	 * 
	 *             TODO: Last 2 look redundant
	 */
	public static void convertBidToWin(String hash, String cost, String lat, String lon, String adId, String cridId, String siteId,
			String pubId, String image, String forward, String price, String adm) throws Exception {

		Controller.getInstance().deleteBidFromCache(hash);
		Controller.getInstance().sendWin(hash, cost, lat, lon, adId, cridId, siteId, pubId, image, forward, price, adm);

		try {
			RTBServer.adspend += Double.parseDouble(price);
		} catch (Exception error) {
			Controller.getInstance().sendLog(1, "WinObject:convertBidToWin",
				"Error: exchange " + pubId + " did not pass a proper {AUCTION_PRICE} substitution on the WIN, win price is undeterimed: " + price);
		}
	}
}
