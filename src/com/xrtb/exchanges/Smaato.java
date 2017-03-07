package com.xrtb.exchanges;

import java.io.IOException;

import java.io.InputStream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.xrtb.pojo.BidRequest;

/**
 * A Bid request for Smaato. Exchanges can introduce their own JSON into the bid request, so all the
 * special parsing is done in a class that extends the BidRequest. All standard bid requests are handled
 * in the BidRequest super class. The exchange only handles those values specific to the exchange.
 * <p>
 * The interrogate() method of the base class is used to retrieve the object values from the class.
 * @author Ben M. Faul
 *
 */
public class Smaato extends BidRequest {
	
	/**
	 * Make a default constructor, the bidder keeps a representative class instance for each
	 * exchange so it can use a Map to make new bid requests per the format of the bid request.
	 */
	public Smaato() {
		super();
		parseSpecial();
	}
	
	/**
	 * Constructs Smaato bid request from a file containing JSON
	 * @param in. String - the File name containing the data.
	 * @throws JsonProcessingException on parse errors.
	 * @throws IOException on file reading errors.
	 */	
	public Smaato(String  in) throws Exception  {
		super(in);
		parseSpecial();
    }	
	
	/**
	 * Constructs Smaato bid request from JSON stream in jetty.
	 * @param in. InputStream - the JSON data coming from HTTP.
	 * @throws JsonProcessingException on parse errors.
	 * @throws IOException on file reading errors.
	 */
	public Smaato(InputStream in) throws Exception {
		super(in);
		parseSpecial();
	}
	
	/**
	 * Create a new Smaato object from this class instance.
	 * @throws JsonProcessingException on parse errors.
	 * @throws Exception on stream reading errors
	 */
	@Override
	public Smaato copy(InputStream in) throws Exception  {
		return new Smaato(in);
	}
	
	/**
	 * Process special Smaato stuff, sets the exchange name.
	 */
	@Override
	public boolean parseSpecial() {
		setExchange( "smaato" );
		return true;
	}
	
}
