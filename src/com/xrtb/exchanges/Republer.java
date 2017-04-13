package com.xrtb.exchanges;

import java.io.InputStream;

import com.xrtb.pojo.BidRequest;

/**
 * A class to handle Republer ad exchange
 * @author Ben M. Faul
 *
 */

public class Republer extends BidRequest {

        public Republer() {
                super();
                parseSpecial();
        }
        
        /**
         * Make a Republer bid request using a String.
         * @param in String. The JSON bid request for Epom
         * @throws Exception on JSON errors.
         */
        public Republer(String  in) throws Exception  {
                super(in);
                parseSpecial();
    }

        /**
         * Make a Republer bid request using an input stream.
         * @param in InputStream. The contents of a HTTP post.
         * @throws Exception on JSON errors.
         */
        public Republer(InputStream in) throws Exception {
                super(in);
                parseSpecial();
        }
        
        /**
         * Process special Republer stuff, sets the exchange name. Setss encoding.
         */
        @Override
        public boolean parseSpecial() {
                setExchange( "republer" );
                usesEncodedAdm = false;
                return true;
        }
        
    	/**
    	 * Create a new Republer object from this class instance.
    	 * @throws JsonProcessingException on parse errors.
    	 * @throws Exception on stream reading errors
    	 */
    	@Override
    	public Republer copy(InputStream in) throws Exception  {
    		Republer copy = new Republer(in);
    		copy.usesEncodedAdm = usesEncodedAdm;
    		return copy;
    	}
}



        
