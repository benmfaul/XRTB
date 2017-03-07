package com.xrtb.exchanges;

import java.io.InputStream;

import com.xrtb.pojo.BidRequest;

/**
 * A class to handle a Taggify ad exchange
 * @author Ben M. Faul
 *
 */

public class Taggify extends BidRequest {

        public Taggify() {
                super();
                parseSpecial();
        }
        
        /**
         * Make a Taggify bid request using a String.
         * @param in String. The JSON bid request for Epom
         * @throws Exception on JSON errors.
         */
        public Taggify(String  in) throws Exception  {
                super(in);
                parseSpecial();
    }

        /**
         * Make a Taggify bid request using an input stream.
         * @param in InputStream. The contents of a HTTP post.
         * @throws Exception on JSON errors.
         */
        public Taggify(InputStream in) throws Exception {
                super(in);
                parseSpecial();
        }
        
        /**
         * Process special Taggify stuff, sets the exchange name. Setss encoding.
         */
        @Override
        public boolean parseSpecial() {
                setExchange( "taggify" );
                usesEncodedAdm = false;
                return true;
        }
        
    	/**
    	 * Create a new Taggify object from this class instance.
    	 * @throws JsonProcessingException on parse errors.
    	 * @throws Exception on stream reading errors
    	 */
    	@Override
    	public Taggify copy(InputStream in) throws Exception  {
    		return new Taggify(in);
    	}
}



        
