package com.xrtb.exchanges;

import java.io.InputStream;

import com.xrtb.pojo.BidRequest;

/**
 * A class to handle Generic ad exchange. This is unencoded type SSP.
 * @author Ben M. Faul
 *
 */

public class Generic extends BidRequest {

        public Generic() {
                super();
                parseSpecial();
        }
        
        /**
         * Make a Generic bid request using a String. Note, the exchange is not set yet,
         * make sure you set the exchange before using this object.
         * Also, the usesEncodedAdm flag needs to be set to false before you create the
         * response from this object, if the SSP does not use encoded ADM fields.
         * @param in String. The JSON bid request for Generic SSP.
         * @throws Exception on JSON errors.
         */
        public Generic(String  in) throws Exception  {
                super(in);
                parseSpecial();
        }
        
    	/**
    	 * Debugging version of the constructor. Will dump if there is a problem
    	 * @param in InputStream. The JSON input
    	 * @param e String. The exchange name
    	 * @throws Exception will dump the error, and set the blackist flag.
    	 */
    	public Generic(InputStream in, String e) throws Exception {
    		super(in,e);
    	}

        /**
         * Make a Generic bid request using an input stream.
         * @param in InputStream. The contents of a HTTP post.
         * @throws Exception on JSON errors.
         */
        public Generic(InputStream in) throws Exception {
                super(in);
                parseSpecial();
        }
        
        /**
         * Process special AdMedia stuff, sets the exchange name. Setss encoding.
         */
        @Override
        public boolean parseSpecial() {
                return true;
        }
        
    	/**
    	 * Create a new AdMedia object from this class instance.
    	 * @throws JsonProcessingException on parse errors.
    	 * @throws Exception on stream reading errors
    	 */
    	@Override
    	public Generic copy(InputStream in) throws Exception  {
    		Generic copy = new Generic(in);
    		copy.setExchange(getExchange());
    		copy.usesEncodedAdm = usesEncodedAdm;
    		return copy;
    	}
}



        
