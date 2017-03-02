package com.xrtb.exchanges;

import java.io.InputStream;

import com.xrtb.pojo.BidRequest;

/**
 * A class to handle Adventurefeeds ad exchange
 * @author Ben M. Faul
 *
 */

public class Adventurefeeds extends BidRequest {

        public Adventurefeeds() {
                super();
                parseSpecial();
        }
        
        /**
         * Make a Adventurefeeds bid request using a String.
         * @param in String. The JSON bid request for Epom
         * @throws Exception on JSON errors.
         */
        public Adventurefeeds(String  in) throws Exception  {
                super(in);
                parseSpecial();
    }

        /**
         * Make a Adventurefeeds bid request using an input stream.
         * @param in InputStream. The contents of a HTTP post.
         * @throws Exception on JSON errors.
         */
        public Adventurefeeds(InputStream in) throws Exception {
                super(in);
                parseSpecial();
        }
        
        /**
         * Process special Epom stuff, sets the exchange name. Setss encoding.
         */
        @Override
        public boolean parseSpecial() {
                setExchange( "adventurefeeds" );
                usesEncodedAdm = false;
                return true;
        }
        
    	/**
    	 * Create a new Adventurefeeds object from this class instance.
    	 * @throws JsonProcessingException on parse errors.
    	 * @throws Exception on stream reading errors
    	 */
    	@Override
    	public Adventurefeeds copy(InputStream in) throws Exception  {
    		return new Adventurefeeds(in);
    	}
}



        
