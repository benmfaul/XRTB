package com.xrtb.exchanges;

import java.io.InputStream;

import com.xrtb.pojo.BidRequest;

/**
 * A class to handle Cappture ad exchange
 * @author Ben M. Faul
 *
 */

public class Cappture extends BidRequest {

        public Cappture() {
                super();
                parseSpecial();
        }
        
        /**
         * Make a Epom bid request using a String.
         * @param in String. The JSON bid request for Epom
         * @throws Exception on JSON errors.
         */
        public Cappture(String  in) throws Exception  {
                super(in);
                parseSpecial();
    }

        /**
         * Make a Epom bid request using an input stream.
         * @param in InputStream. The contents of a HTTP post.
         * @throws Exception on JSON errors.
         */
        public Cappture(InputStream in) throws Exception {
                super(in);
                parseSpecial();
        }
        
        /**
         * Process special Epom stuff, sets the exchange name. Setss encoding.
         */
        @Override
        public boolean parseSpecial() {
                setExchange( "cappture" );
                usesEncodedAdm = false;
                return true;
        }
        
    	/**
    	 * Create a new Nexage object from this class instance.
    	 * @throws JsonProcessingException on parse errors.
    	 * @throws Exception on stream reading errors
    	 */
    	@Override
    	public Cappture copy(InputStream in) throws Exception  {
    		return new Cappture(in);
    	}
}