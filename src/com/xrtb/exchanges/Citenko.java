package com.xrtb.exchanges;

import java.io.InputStream;

import com.xrtb.pojo.BidRequest;

/**
 * A class to handle a Citenko ad exchange
 * @author Ben M. Faul
 *
 */

public class Citenko extends BidRequest {

        public Citenko() {
                super();
                parseSpecial();
        }
        
        /**
         * Make a Citenko bid request using a String.
         * @param in String. The JSON bid request for Epom
         * @throws Exception on JSON errors.
         */
        public Citenko(String  in) throws Exception  {
                super(in);
                parseSpecial();
    }

        /**
         * Make a Citenko bid request using an input stream.
         * @param in InputStream. The contents of a HTTP post.
         * @throws Exception on JSON errors.
         */
        public Citenko(InputStream in) throws Exception {
                super(in);
                parseSpecial();
        }
        
        /**
         * Process special Citenko stuff, sets the exchange name. Setss encoding.
         */
        @Override
        public boolean parseSpecial() {
                setExchange(  "citenko" );
                usesEncodedAdm = false;
                return true;
        }
        
    	/**
    	 * Create a new Citenko object from this class instance.
    	 * @throws JsonProcessingException on parse errors.
    	 * @throws Exception on stream reading errors
    	 */
    	@Override
    	public Citenko copy(InputStream in) throws Exception  {
    		Citenko copy =  new Citenko(in);
    		copy.usesEncodedAdm = usesEncodedAdm;
    		return copy;
    	}
}



        
