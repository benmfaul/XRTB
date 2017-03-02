package com.xrtb.exchanges;

import java.io.InputStream;

import com.xrtb.pojo.BidRequest;

/**
 * A class to handle Adprudence ad exchange
 * @author Ben M. Faul
 *
 */

public class Adprudence extends BidRequest {

        public Adprudence() {
                super();
                parseSpecial();
        }
        
        /**
         * Make a Adprudence bid request using a String.
         * @param in String. The JSON bid request for Epom
         * @throws Exception on JSON errors.
         */
        public Adprudence(String  in) throws Exception  {
                super(in);
                parseSpecial();
    }

        /**
         * Make a Epom bid request using an input stream.
         * @param in InputStream. The contents of a HTTP post.
         * @throws Exception on JSON errors.
         */
        public Adprudence(InputStream in) throws Exception {
                super(in);
                parseSpecial();
        }
        
        /**
         * Process special Epom stuff, sets the exchange name. Setss encoding.
         */
        @Override
        public boolean parseSpecial() {
                setExchange( "adprudence" );
                usesEncodedAdm = false;
                return true;
        }
        
    	/**
    	 * Create a new Adprudence object from this class instance.
    	 * @throws JsonProcessingException on parse errors.
    	 * @throws Exception on stream reading errors
    	 */
    	@Override
    	public Adprudence copy(InputStream in) throws Exception  {
    		return new Adprudence(in);
    	}
}



        
