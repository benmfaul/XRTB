package com.xrtb.exchanges.adx;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public class DoubleClick extends AdxBidRequest {

	public static void main(String [] args) throws Exception {
		Decrypter.testWinningPrice();
	}
	
	/**
	 * Make a default constructor, the bidder keeps a representative class instance for each
	 * exchange so it can use a Map to make new bid requests per the format of the bid request.
	 */
	public DoubleClick() {
		super();
		parseSpecial();
	}
	
	/**
	 * Constructs Nexage bid request from a file containoing JSON
	 * @param in. String - the File name containing the data.
	 * @throws JsonProcessingException on parse errors.
	 * @throws IOException on file reading errors.
	 */	
	public DoubleClick(String  in) throws Exception  {
		super(in);
		parseSpecial();
    }	
	
	/**
	 * Constructs Nexage bid request from JSON stream in jetty.
	 * @param in. InputStream - the JSON data coming from HTTP.
	 * @throws JsonProcessingException on parse errors.
	 * @throws IOException on file reading errors.
	 */
	public DoubleClick(InputStream in) throws Exception {
		super(in);
		parseSpecial();
	}
	
	/**
	 * Create a new Nexage object from this class instance.
	 * @throws JsonProcessingException on parse errors.
	 * @throws Exception on stream reading errors
	 */
	@Override
	public DoubleClick copy(InputStream in) throws Exception  {
		return new DoubleClick(in);
	}
	
	/**
	 * Process special Nexage stuff, sets the exchange name.
	 */
	@Override
	public boolean parseSpecial() {
		exchange = AdxBidRequest.ADX;
        usesEncodedAdm = false;
		return true;
	}
	
	@Override
	public void handleConfigExtensions(Map m) throws Exception {
		List<String> ekey = (List<String>)m.get("e_key");
		List<String> ikey = (List<String>)m.get("i_key");
		
		if (ekey == null)
			throw new Exception("Configuration of double click requires an ekey");
		
		if (ikey == null)
			throw new Exception("Configuration of double click requires an ikey");
		
		byte[] bytes = new byte[ekey.size()];
		for (int i=0; i<ekey.size(); i++) {
			String x = ekey.get(i);
			x = x.replaceAll("0x", "");
			bytes[i] = (byte)Integer.parseInt(x,16);
		}
		AdxWinObject.encryptionKeyBytes = bytes;
		
		bytes = new byte[ekey.size()];
		for (int i=0; i<ikey.size(); i++) {
			String x = ikey.get(i);
			x = x.replaceAll("0x", "");
			bytes[i] = (byte)Integer.parseInt(x,16);
		}
		AdxWinObject.integrityKeyBytes = bytes;
	}
}
