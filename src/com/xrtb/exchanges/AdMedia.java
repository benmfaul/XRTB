package com.xrtb.exchanges;

import java.io.InputStream;

import com.xrtb.pojo.BidRequest;

/**
 * A class to handle AdMedia ad exchange
 * @author Ben M. Faul
 *
 */

public class AdMedia extends BidRequest {

        public AdMedia() {
                super();
                parseSpecial();
        }
        
        /**
         * Make a AdMedia bid request using a String.
         * @param in String. The JSON bid request for Epom
         * @throws Exception on JSON errors.
         */
        public AdMedia(String  in) throws Exception  {
                super(in);
                parseSpecial();
    }
        
    	/**
    	 * Debugging version of the constructor. Will dump if there is a problem
    	 * @param in InputStream. The JSON input
    	 * @param e String. The exchange name
    	 * @throws Exception will dump the error, and set the blackist flag.
    	 */
    	public AdMedia(InputStream in, String e) throws Exception {
    		super(in,"admedia");
    	}

        /**
         * Make a Admedia bid request using an input stream.
         * @param in InputStream. The contents of a HTTP post.
         * @throws Exception on JSON errors.
         */
        public AdMedia(InputStream in) throws Exception {
                super(in);
                parseSpecial();
        }
        
        /**
         * Process special AdMedia stuff, sets the exchange name. Setss encoding.
         */
        @Override
        public boolean parseSpecial() {
                setExchange( "admedia" );
                usesEncodedAdm = false;
                return true;
        }
        
    	/**
    	 * Create a new AdMedia object from this class instance.
    	 * @throws JsonProcessingException on parse errors.
    	 * @throws Exception on stream reading errors
    	 */
    	@Override
    	public AdMedia copy(InputStream in) throws Exception  {
    		AdMedia copy = new AdMedia(in);
    		copy.usesEncodedAdm = usesEncodedAdm;
    		return copy;
    	}
}



        
