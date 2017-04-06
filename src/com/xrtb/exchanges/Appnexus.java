package com.xrtb.exchanges;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.xrtb.pojo.BidRequest;

/**
 * A class to handle Appnexus ad exchange
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
	
	public Appnexus() {
		super();
		parseSpecial();
	}
	
	public Appnexus(int type) {
		endpoint = type;
	}
	
	public Appnexus(int type, InputStream in) throws Exception {
		endpoint = type;
		switch(type) {
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
	 * @param in String. The JSON bid request for smartyads
	 * @throws Exception on JSON errors.
	 */
	public Appnexus(String  in) throws Exception  {
		super(in);
		parseSpecial();
    }	
	
	/**
	 * Make a AppNexus bid request using an input stream.
	 * @param in InputStream. The contents of a HTTP post.
	 * @throws Exception on JSON errors.
	 */
	public Appnexus(InputStream in) throws Exception {
		super(in);
		parseSpecial();
	}
	
	void doReady(InputStream in) throws Exception {
		//StringBuilder out = getData(in);
		//System.out.println("------- #READY# ----------\n" + out.toString() + "-----------------------");
	}
	
	void doClick(InputStream in) throws Exception {
		//StringBuilder out = getData(in);
		//System.out.println("------- #CLICK# ----------\n" + out.toString() + "-----------------------");
	}
	
	void doPixel(InputStream in) throws Exception {
		//StringBuilder out = getData(in);
		//System.out.println("------- #PIXEL# ----------\n" + out.toString() + "-----------------------");
	}
	
	void doDelivered(InputStream in) throws Exception {
		//StringBuilder out = getData(in);
		//System.out.println("------- #DELIVERED# ----------\n" + out.toString() + "-----------------------");
	}
	
	StringBuilder getData(InputStream inputStream) throws Exception {
		int bufferSize = 1024;
    	char[] buffer = new char[bufferSize];
    	StringBuilder out = new StringBuilder();
    	Reader in = new InputStreamReader(inputStream, "UTF-8");
    	for (; ; ) {
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
	 * @throws JsonProcessingException on parse errors.
	 * @throws Exceptionsmartypants on stream reading errors
	 */
	@Override
	public Appnexus copy(InputStream in) throws Exception  {
		switch(endpoint) {
		case BID:
			return new Appnexus(in);
		case CLICK:
			return new Appnexus(CLICK,in);
		case PIXEL:
			return new Appnexus(PIXEL,in);
		case READY:
			return new Appnexus(READY,in);
		case DELIVERED:
			return new Appnexus(DELIVERED,in);
		}
		throw new Exception("Can't create a copy of this Appexus object");
	}
	
	/**
	 * This is not a bid request.
	 * @return boolean Return true of this isn't a bid request.
	 */
	@Override
	public boolean notABidRequest() {
		if (endpoint == BID)
			return false;
		return true;
	}
	
	/**
	 * Override this method to return the code the non bid request return is supposed to be.
	 * @return
	 */
	public int getNonBidReturnCode() {
		return altCode;
	}
	
	/**
	 * Override this method to return the data response the non bid request return is supposed to be.
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
		setExchange( "appnexus" );
		usesEncodedAdm = false;
		return true;
	}
}