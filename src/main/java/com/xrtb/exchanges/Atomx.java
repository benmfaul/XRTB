package com.xrtb.exchanges;

import java.io.InputStream;
import com.xrtb.pojo.BidRequest;

/**
 * A class to handle Atomx ad exchange
 * @author Ben M. Faul
 *
 */

public class Atomx extends BidRequest {
	
	public Atomx() {
		super();
		parseSpecial();
	}
	
	/**
	 * Make a Atomx bid request using a String.
	 * @param in String. The JSON bid request for smartyads
	 * @throws Exception on JSON errors.
	 */
	public Atomx(String  in) throws Exception  {
		super(in);
		parseSpecial();
    }	
	
	/**
	 * Make a Atomx bid request using an input stream.
	 * @param in InputStream. The contents of a HTTP post.
	 * @throws Exception on JSON errors.
	 */
	public Atomx(InputStream in) throws Exception {
		super(in);
		parseSpecial();
	}
	
	/**
	 * Create a new Atomx Exchange object from this class instance.
	 * @throws JsonProcessingException on parse errors.
	 * @throws Exceptionsmartypants on stream reading errors
	 */
	@Override
	public Atomx copy(InputStream in) throws Exception  {
		return new Atomx(in);
	}
	
	
	/**
	 * Process special Atomx stuff, sets the exchange name.
	 */
	@Override
	public boolean parseSpecial() {
		exchange = "atomx";
		return true;
	}
}