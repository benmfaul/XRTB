package com.xrtb.exchanges;

import java.io.InputStream;

import com.xrtb.pojo.BidRequest;

/**
 * A class to handle Pubmatic ad exchange
 * @author Ben M. Faul
 *
 */

public class Pubmatic extends BidRequest {

        public Pubmatic() {
                super();
                parseSpecial();
        }
        
        /**
         * Make a Pubmatic bid request using a String.
         * @param in String. The JSON bid request for Epom
         * @throws Exception on JSON errors.
         */
        public Pubmatic(String  in) throws Exception  {
                super(in);
                parseSpecial();
    }
        
        /**
    	 * Debugging version of the constructor. Will dump if there is a problem
    	 * @param in InputStream. The JSON input
    	 * @param e String. The exchange name
    	 * @throws Exception will dump the error, and set the blackist flag.
    	 */
    	public Pubmatic(InputStream in, String e) throws Exception {
    		super(in,"pubmatic");
    	}


        /**
         * Make a Pubmatic bid request using an input stream.
         * @param in InputStream. The contents of a HTTP post.
         * @throws Exception on JSON errors.
         */
        public Pubmatic(InputStream in) throws Exception {
                super(in,"pubmatic");
                parseSpecial();
        }
        
        /**
         * Process special Pubmatic stuff, sets the exchange name. Setss encoding.
         */
        @Override
        public boolean parseSpecial() {
                setExchange( "pubmatic" );
                usesEncodedAdm = false;
                return true;
        }
        
    	/**
    	 * Create a new pubmatic object from this class instance.
    	 * @throws JsonProcessingException on parse errors.
    	 * @throws Exception on stream reading errors
    	 */
    	@Override
    	public Pubmatic copy(InputStream in) throws Exception  {
    		return new Pubmatic(in);
    	}
}



        
