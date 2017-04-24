package com.xrtb.exchanges;

import java.io.InputStream;

import com.xrtb.pojo.BidRequest;

/**
 * A class to handle Gotham ad exchange
 * @author Ben M. Faul
 *
 */

public class Gotham extends BidRequest {

        public Gotham() {
                super();
                parseSpecial();
        }
        
        /**
         * Make a Gotham bid request using a String.
         * @param in String. The JSON bid request for Epom
         * @throws Exception on JSON errors.
         */
        public Gotham(String  in) throws Exception  {
                super(in);
                parseSpecial();
    }

        /**
         * Make a Gotham bid request using an input stream.
         * @param in InputStream. The contents of a HTTP post.
         * @throws Exception on JSON errors.
         */
        public Gotham(InputStream in) throws Exception {
                super(in);
                parseSpecial();
        }
        
        /**
         * Process special Gotham stuff, sets the exchange name. Setss encoding.
         */
        @Override
        public boolean parseSpecial() {
                setExchange( "gotham" );
                usesEncodedAdm = false;
                return true;
        }
        
    	/**
    	 * Create a new Gotham object from this class instance.
    	 * @throws JsonProcessingException on parse errors.
    	 * @throws Exception on stream reading errors
    	 */
    	@Override
    	public Gotham copy(InputStream in) throws Exception  {
    		Gotham copy = new Gotham(in);
    		copy.usesEncodedAdm = usesEncodedAdm;
    		return copy;
    	}
}



        
