package com.xrtb.exchanges;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.jackson.JsonProcessingException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.xrtb.pojo.BidRequest;

/**
 * A Bid request for Nexage. Exchanges can introduce their own JSON into the bid request, so all the
 * special parsing is done in a class that extends the BidRequest. All standard bid requests are handled
 * in the BidRequest super class. The exchange only handles those values specific to the exchange.
 * <p>
 * The interrogate() method of the base class is used to retrieve the object values from the class.
 * @author Ben M. Faul
 *
 */
public class Nexage extends BidRequest {
	
	/**
	 * Make a default constructor, the bidder keeps a representative class instance for each
	 * exchange so it can use a Map to make new bid requests per the format of the bid request.
	 */
	public Nexage() {
		super();
		parseSpecial();
	}
	
	/**
	 * Constructs Nexage bid request from a file containoing JSON
	 * @param in. String - the File name containing the data.
	 * @throws JsonProcessingException on parse errors.
	 * @throws IOException on file reading errors.
	 */	
	public Nexage(String  in) throws Exception  {
		super(in);
		parseSpecial();
    }	
	
	/**
	 * Constructs Nexage bid request from JSON stream in jetty.
	 * @param in. InputStream - the JSON data coming from HTTP.
	 * @throws JsonProcessingException on parse errors.
	 * @throws IOException on file reading errors.
	 */
	public Nexage(InputStream in) throws Exception {
		super(in);
		parseSpecial();
	}
	
	/**
	 * Create a new Nexage object from this class instance.
	 * @throws JsonProcessingException on parse errors.
	 * @throws Exception on stream reading errors
	 */
	@Override
	public Nexage copy(InputStream in) throws Exception  {
		return new Nexage(in);
	}
	
	/**
	 * Process special Nexage stuff, sets the exchange name.
	 */
	@Override
	public boolean parseSpecial() {
		exchange = "nexage";
		return true;
	}
	
}
