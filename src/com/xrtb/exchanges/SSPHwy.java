package com.xrtb.exchanges;

import java.io.InputStream;

import com.xrtb.pojo.BidRequest;

/**
 * A class to handle Mobile Ad Exchange SSPHwy ad exchange
 * @author Ben M. Faul
 *
 */

public class SSPHwy extends BidRequest {

        public SSPHwy() {
                super();
                parseSpecial();
        }
        
        /**
         * Make a SSPHwy bid request using a String.
         * @param in String. The JSON bid request for Epom
         * @throws Exception on JSON errors.
         */
        public SSPHwy(String  in) throws Exception  {
                super(in);
                parseSpecial();
    }

        /**
         * Make a SSPHwy bid request using an input stream.
         * @param in InputStream. The contents of a HTTP post.
         * @throws Exception on JSON errors.
         */
        public SSPHwy(InputStream in) throws Exception {
                super(in);
                parseSpecial();
        }
        
        /**
         * Process special Gotham stuff, sets the exchange name. Setss encoding.
         */
        @Override
        public boolean parseSpecial() {
                setExchange( "ssphwy" );
                usesEncodedAdm = false;
                return true;
        }
        
    	/**
    	 * Create a new SSPHwy object from this class instance.
    	 * @throws JsonProcessingException on parse errors.
    	 * @throws Exception on stream reading errors
    	 */
    	@Override
    	public SSPHwy copy(InputStream in) throws Exception  {
    		SSPHwy copy = new SSPHwy(in);
    		copy.usesEncodedAdm = usesEncodedAdm;
    		return copy;
    		
    	}
}



        
