package com.xrtb.exchanges;

import java.io.InputStream;

import com.xrtb.pojo.BidRequest;

public class Fyber extends BidRequest {
	
	public Fyber() {
		super();
		parseSpecial();
	}
	
	public Fyber(String  in) throws Exception  {
		super(in);
		parseSpecial();
    }	
	
	public Fyber(InputStream in) throws Exception {
		super(in);
		parseSpecial();
	}
	
	/**
	 * Create a new Private Exchange object from this class instance.
	 * @throws JsonProcessingException on parse errors.
	 * @throws Exception on stream reading errors
	 */
	@Override
	public Privatex copy(InputStream in) throws Exception  {
		return new Privatex(in);
	}
	
	
	/**
	 * Process special Nexage stuff, sets the exchange name.
	 */
	@Override
	public boolean parseSpecial() {
		exchange = "fyber";
		return true;
	}
}
