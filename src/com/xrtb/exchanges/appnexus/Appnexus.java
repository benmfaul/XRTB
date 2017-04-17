package com.xrtb.exchanges.appnexus;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.xrtb.bidder.Controller;
import com.xrtb.bidder.SelectedCreative;
import com.xrtb.common.Campaign;
import com.xrtb.common.Creative;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;
import com.xrtb.pojo.Impression;

/**
 * A class to handle Appnexus ad exchange
 * 
 * @author Ben M. Faul
 *
 */

public class Appnexus extends BidRequest {

	public static final int BID = 0;
	public static final int READY = 1;
	public static final int CLICK = 2;
	public static final int PIXEL = 3;
	public static final int DELIVERED = 4;

	// Type of endpoint, BID by default
	int endpoint = BID;
	// Alternate JSON value to return if not a bid request
	String altJson = "{}";
	// Alternate code to use if not a bid request.
	int altCode = 200;

	public static String seatId;
	
	public Appnexus() {
		super();
		parseSpecial();
	}

	public Appnexus(int type) {
		endpoint = type;
	}

	public Appnexus(int type, InputStream in) throws Exception {
		endpoint = type;
		switch (type) {
		case READY:
			doReady(in);
			break;
		case CLICK:
			doClick(in);
			break;
		case PIXEL:
			doPixel(in);
			break;
		case DELIVERED:
			doDelivered(in);
			break;
		}
	}

	/**
	 * Make a AppNexus bid request using a String.
	 * 
	 * @param in
	 *            String. The JSON bid request for smartyads
	 * @throws Exception
	 *             on JSON errors.
	 */
	public Appnexus(String in) throws Exception {
		super(in);
		parseSpecial();
	}

	/**
	 * Make a AppNexus bid request using an input stream.
	 * 
	 * @param in
	 *            InputStream. The contents of a HTTP post.
	 * @throws Exception
	 *             on JSON errors.
	 */
	public Appnexus(InputStream in) throws Exception {
		super(in);
		parseSpecial();
	}

	void doReady(InputStream in) throws Exception {
		// StringBuilder out = getData(in);
		// System.out.println("------- #READY# ----------\n" + out.toString() +
		// "-----------------------");
	}

	void doClick(InputStream in) throws Exception {
		// StringBuilder out = getData(in);
		// System.out.println("------- #CLICK# ----------\n" + out.toString() +
		// "-----------------------");
	}

	void doPixel(InputStream in) throws Exception {
		// StringBuilder out = getData(in);
		// System.out.println("------- #PIXEL# ----------\n" + out.toString() +
		// "-----------------------");
	}

	void doDelivered(InputStream in) throws Exception {
		// StringBuilder out = getData(in);
		// System.out.println("------- #DELIVERED# ----------\n" +
		// out.toString() + "-----------------------");
	}

	StringBuilder getData(InputStream inputStream) throws Exception {
		int bufferSize = 1024;
		char[] buffer = new char[bufferSize];
		StringBuilder out = new StringBuilder();
		Reader in = new InputStreamReader(inputStream, "UTF-8");
		for (;;) {
			int rsz = in.read(buffer, 0, buffer.length);
			if (rsz < 0)
				break;
			out.append(buffer, 0, rsz);
		}
		return out;
	}

	@Override
	public void incrementRequests() {
		if (endpoint == BID)
			super.incrementRequests();
	}

	/**
	 * Create a new Atomx Exchange object from this class instance.
	 * 
	 * @throws JsonProcessingException
	 *             on parse errors.
	 * @throws Exceptionsmartypants
	 *             on stream reading errors
	 */
	@Override
	public Appnexus copy(InputStream in) throws Exception {
		switch (endpoint) {
		case BID:
			Appnexus copy = new Appnexus(in);
			copy.usesEncodedAdm = usesEncodedAdm;
			return copy;
		case CLICK:
			return new Appnexus(CLICK, in);
		case PIXEL:
			return new Appnexus(PIXEL, in);
		case READY:
			return new Appnexus(READY, in);
		case DELIVERED:
			return new Appnexus(DELIVERED, in);
		}
		throw new Exception("Can't create a copy of this Appexus object");
	}

	/**
	 * This is not a bid request.
	 * 
	 * @return boolean Return true of this isn't a bid request.
	 */
	@Override
	public boolean notABidRequest() {
		if (endpoint == BID)
			return false;
		return true;
	}

	/**
	 * Override this method to return the code the non bid request return is
	 * supposed to be.
	 * 
	 * @return
	 */
	public int getNonBidReturnCode() {
		return altCode;
	}

	/**
	 * Override this method to return the data response the non bid request
	 * return is supposed to be.
	 * 
	 * @return
	 */
	public String getNonBidRespose() {
		return altJson;
	}

	/**
	 * Process special Atomx stuff, sets the exchange name.
	 */
	@Override
	public boolean parseSpecial() {
		setExchange("appnexus");
		usesEncodedAdm = false;
		return true;
	}

	@Override
	public BidResponse buildNewBidResponse(Impression imp, Campaign camp, Creative creat, double price, String dealId,
			int xtime) throws Exception {

		String adid = creat.extensions.get("appnexus_crid");

		if (adid != null)
			creat.alternateAdId = adid;

		AppnexusBidResponse response = new AppnexusBidResponse(this, imp, camp, creat, this.id, price, dealId, xtime);

		creat.alternateAdId = null;

		StringBuilder sb = response.getResponseBuffer();

		return (BidResponse) response;
	}

	@Override
	public BidResponse buildNewBidResponse(Impression imp, List<SelectedCreative> multi, int xtime) throws Exception {

		for (int i = 0; i < multi.size(); i++) {
			SelectedCreative x = multi.get(i);
			Creative c = x.getCreative();
			if (c.extensions == null || c.extensions.size() == 0)
				throw new Exception(
						x.getCampaign().adId + "/" + c.impid + " is missing required extensions for Appnexus SSP");

			String adid = c.extensions.get("appnexus_crid");

			if (adid == null)
				adid = "invalid:unassigned";

			c.alternateAdId = adid;
		}

		BidResponse response = new BidResponse(this, imp, multi, xtime);
		StringBuilder sb = response.getResponseBuffer();
		return response;
	}

	/**
	 * Makes sure the Appnexus keys are available on the creative
	 * @param creat Creative. The creative in question.
	 * @param errorString StringBuilder. The error handling string. Add your error here if not null.
	 * @returns boolean. Returns true if the Exchange and creative are compatible.
	 */
	@Override
	public boolean checkNonStandard(Creative c, StringBuilder sb) {
		if (c.extensions == null || c.extensions.get("appnexus_crid") == null) {
			if (sb != null) 
				sb.append("Creative is not Appnexus compatible");
			return false;
		}

		///////////////////////////
		//
		// Check for seat id
		//
		Object obj = interrogate("wseat");
		if (obj instanceof MissingNode) {
			if (sb != null) {
				sb.append("appnexus seat missing");
				return false;
			}
		}
		ArrayNode list = (ArrayNode) obj;
		boolean hasSeat = false;
		for (int i = 0; i < list.size(); i++) {
			JsonNode nx = list.get(i);
			if (nx.asText().equals(Appnexus.seatId)) {
				return true;
			}
		}
		if (sb != null)
			sb.append("Not our seat");
		return false;
	}
}