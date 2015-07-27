package com.xrtb.exchanges;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.jackson.JsonProcessingException;

import com.xrtb.pojo.BidRequest;

/**
 * A Bid request for Mobclix. Exchanges can introduce their own JSON into the bid request, so all the
 * special parsing is done in a class that extends the BidRequest. All standard bid requests are handled
 * in the BidRequest super class. The exchange only handles those values specific to the exchange.
 * <p>
 * The interrogate() method of the base class is used to retrieve the object values from the class.
 * @author Ben M. Faul
 *
 */
public class Mobclix extends BidRequest {
	
	/**
	 * Constructs Mobclix bid request from a file containoing JSON
	 * @param in. String - the File name containing the data.
	 * @throws JsonProcessingException on parse errors.
	 * @throws Exception on file reading errors
	 */	
	public Mobclix(String  in) throws Exception {
		super(in);

    }
	/**
	 * Constructs Mobclix bid request from JSON stream in jetty.
	 * @param in. InputStream - the JSON data coming from HTTP.
	 * @throws JsonProcessingException on parse errors.
	 * @throws Exception on stream reading errors.
	 */
	public Mobclix(InputStream in) throws Exception {
		super(in);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Process special mobclix stuff, sets the exchange name.
	 */
	@Override
	public boolean parseSpecial() {
		this.exchange = "mobclix";
		return true;
	}
}
