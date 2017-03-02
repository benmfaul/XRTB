package com.xrtb.exchanges;

import java.io.InputStream;

import com.xrtb.pojo.BidRequest;

/**
 * A class to handle Epom ad exchange
 * @author Ben M. Faul
 *
 */

public class Epomx extends BidRequest {

        public Epomx() {
                super();
                parseSpecial();
        }
        
        /**
         * Make a Epom bid request using a String.
         * @param in String. The JSON bid request for Epom
         * @throws Exception on JSON errors.
         */
        public Epomx(String  in) throws Exception  {
                super(in);
                parseSpecial();
    }

        /**
         * Make a Epom bid request using an input stream.
         * @param in InputStream. The contents of a HTTP post.
         * @throws Exception on JSON errors.
         */
        public Epomx(InputStream in) throws Exception {
                super(in);
                parseSpecial();
        }
        
        /**
         * Process special Epom stuff, sets the exchange name. Setss encoding.
         */
        @Override
        public boolean parseSpecial() {
                setExchange( "epomx" );
                usesEncodedAdm = false;
                return true;
        }
        
    	/**
    	 * Create a new Nexage object from this class instance.
    	 * @throws JsonProcessingException on parse errors.
    	 * @throws Exception on stream reading errors
    	 */
    	@Override
    	public Epomx copy(InputStream in) throws Exception  {
    		return new Epomx(in);
    	}
}



        
