package com.xrtb.exchanges.google;

import java.io.IOException;
import java.io.InputStream;

import com.google.openrtb.OpenRtb.BidRequest;


public class OpenRTB extends GoogleBidRequest {
	public static final String GOOGLE = "google";
	
	/**
	 * Make a default constructor, the bidder keeps a representative class instance for each
	 * exchange so it can use a Map to make new bid requests per the format of the bid request.
	 */
	public OpenRTB() {
		super();
		parseSpecial();
	}
	
	
	/**
	 * Constructs OpenRTB google bid request from JSON stream in jetty.
	 * @param in. InputStream - the JSON data coming from HTTP.
	 * @throws JsonProcessingException on parse errors.
	 * @throws IOException on file reading errors.
	 */
	public OpenRTB(InputStream in) throws Exception {
		super(in);
		parseSpecial();
	}
	
	/**
	 * Create a new Adx object from this class instance.
	 * @throws JsonProcessingException on parse errors.
	 * @throws Exception on stream reading errors
	 */
	@Override
	public OpenRTB copy(InputStream in) throws Exception  {
		return new OpenRTB(in);
	}
	
	/**
	 * Process special Nexage stuff, sets the exchange name.
	 */
	@Override
	public boolean parseSpecial() {
		setExchange(OpenRTB.GOOGLE);
        usesEncodedAdm = false;
		return true;
	}
	
	/**
	 * Return the internal protobuf
	 */
	public BidRequest getInternal() {
		return getInternal();
	}
}

