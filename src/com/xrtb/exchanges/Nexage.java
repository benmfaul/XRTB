package com.xrtb.exchanges;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.codehaus.jackson.JsonProcessingException;

import com.xrtb.pojo.BidRequest;

/**
 * A Bid request for Mobclix.
 * @author Ben M. Faul
 *
 */
public class Nexage extends BidRequest {
	
	/**
	 * Constructs Nexage bid request from a file containoing JSON
	 * @param in. String - the File name containing the data.
	 * @throws JsonProcessingException. Throws if bad JSON.
	 * @throws IOException. Throws if I/O error on the JSON stream.
	 */	
	public Nexage(String  in) throws JsonProcessingException, IOException {
		super(in);
		parseSpecial();
    }	
	
	/**
	 * Constructs Nexage bid request from JSON stream in jetty.
	 * @param in. InputStream - the JSON data coming from HTTP.
	 * @throws JsonProcessingException. Throws if bad JSON.
	 * @throws IOException. Throws if I/O error on the JSON stream.
	 */
	public Nexage(InputStream in) throws JsonProcessingException,
			IOException {
		super(in);
		parseSpecial();
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
