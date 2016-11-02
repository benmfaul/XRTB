package com.xrtb.exchanges;

import java.io.InputStream;

import com.xrtb.pojo.BidRequest;

/**
 * A class to handle Kadam ad exchange
 * @author Ben M. Faul
 *
 */

public class Kadam extends BidRequest {

        public Kadam() {
                super();
                parseSpecial();
        }
        
        /**
         * Make a Kadam bid request using a String.
         * @param in String. The JSON bid request for Epom
         * @throws Exception on JSON errors.
         */
        public Kadam(String  in) throws Exception  {
                super(in);
                parseSpecial();
    }

        /**
         * Make a Kadam bid request using an input stream.
         * @param in InputStream. The contents of a HTTP post.
         * @throws Exception on JSON errors.
         */
        public Kadam(InputStream in) throws Exception {
                super(in);
                parseSpecial();
        }
        
        /**
         * Process special Kadam stuff, sets the exchange name. Setss encoding.
         */
        @Override
        public boolean parseSpecial() {
                exchange = "kadam";
                usesEncodedAdm = false;
                return true;
        }
        
    	/**
    	 * Create a new Kadam object from this class instance.
    	 * @throws JsonProcessingException on parse errors.
    	 * @throws Exception on stream reading errors
    	 */
    	@Override
    	public Kadam copy(InputStream in) throws Exception  {
    		return new Kadam(in);
    	}
}



        
