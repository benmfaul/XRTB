package com.xrtb.exchanges;

import java.io.InputStream;
import com.xrtb.pojo.BidRequest;

/**
 * A class to handle Epom ad exchange
 * @author Ben M. Faul
 *
 */

public class Epom extends BidRequest {

        public Epom() {
                super();
                parseSpecial();
        }
        
        /**
         * Make a Epom bid request using a String.
         * @param in String. The JSON bid request for Epom
         * @throws Exception on JSON errors.
         */
        public Epom(String  in) throws Exception  {
                super(in);
                parseSpecial();
    }

        /**
         * Make a Epom bid request using an input stream.
         * @param in InputStream. The contents of a HTTP post.
         * @throws Exception on JSON errors.
         */
        public Epom(InputStream in) throws Exception {
                super(in);
                parseSpecial();
        }
        
        /**
         * Process special Epom stuff, sets the exchange name.
         */
        @Override
        public boolean parseSpecial() {
                exchange = "epom";
                usesEncodedAdm = false;
                return true;
        }
}



        
