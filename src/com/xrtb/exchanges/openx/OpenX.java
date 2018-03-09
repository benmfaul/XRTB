package com.xrtb.exchanges.openx;

import com.xrtb.pojo.BidRequest;

import java.io.InputStream;
import java.util.Map;

/**
 * A class to handle Openx ad exchange. Not much different, except, openx uses encrypted pricing, and micros and no winurl
 * @author Ben M. Faul
 *
 */

public class OpenX extends BidRequest {

    public static final String OPENX = "openx";
        public OpenX() {
                super();
                parseSpecial();
        }

        /**
         * Make a C1X bid request using a String.
         * @param in String. The JSON bid request for Epom
         * @throws Exception on JSON errors.
         */
        public OpenX(String  in) throws Exception  {
                super(in);
                parseSpecial();
        }

        /**
         * Make a C1X bid request using an input stream.
         * @param in InputStream. The contents of a HTTP post.
         * @throws Exception on JSON errors.
         */
        public OpenX(InputStream in) throws Exception {
                super(in);
                parseSpecial();
        }

        /**
         * Create a c1x bid request from a string builder buffer
         * @param in StringBuilder. The text.
         * @throws Exception on parsing errors.
         */
        public OpenX(StringBuilder in) throws Exception {
			super(in);
            multibid = BidRequest.usesMultibids(OPENX);
			parseSpecial();
		}

		/**
         * Process special C1X stuff, sets the exchange name. Sets encoding.
         */
        @Override
        public boolean parseSpecial() {
                setExchange( OPENX );
                usesEncodedAdm = false;
                BidRequest.setUsesPiggyBackWins(OPENX);       // Note, override whatever the config says openx must to piggybacks.
                return true;
        }
        
    	/**
    	 * Create a new c1x object from this class instance.
    	 * @throws Exception on stream reading errors
    	 */
    	@Override
    	public OpenX copy(InputStream in) throws Exception  {
    		OpenX copy = new OpenX(in);
    		copy.usesEncodedAdm = usesEncodedAdm;
            copy.multibid = BidRequest.usesMultibids(OPENX);
            return copy;
    	}

    /**
     * The configuration requires an e_key and an i_key. Unlike google, Openx uses hex chars instead of
     * web safe base 64 encoded keys.
     */
    @Override
    public void handleConfigExtensions(Map extension)  {
        String ekey = (String) extension.get("e_key");
        String ikey = (String) extension.get("i_key");
        try {
            OpenXWinObject.setKeys(ekey, ikey);
        } catch (Exception error) {
            logger.error("Error setting OpenX key: {}. {}. reason: {}",ekey,ikey,error.toString());
        }
    }
}



        
