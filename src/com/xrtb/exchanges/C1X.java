package com.xrtb.exchanges;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.xrtb.pojo.BidRequest;
import com.xrtb.tools.IsoTwo2Iso3;
import com.xrtb.tools.LookingGlass;

/**
 * A class to handle C1X ad exchange
 * @author Ben M. Faul
 *
 */

public class C1X extends BidRequest {

		// Reference to symbol that
		private static final IsoTwo2Iso3 isoMap = (IsoTwo2Iso3)LookingGlass.symbols.get("@ISO2-3");
		private static Map<String,TextNode> cache = new HashMap<String,TextNode>();
        public C1X() {
                super();
                parseSpecial();
        }
        
        /**
         * Make a C1X bid request using a String.
         * @param in String. The JSON bid request for Epom
         * @throws Exception on JSON errors.
         */
        public C1X(String  in) throws Exception  {
                super(in);
                parseSpecial();
        }

        /**
         * Make a C1X bid request using an input stream.
         * @param in InputStream. The contents of a HTTP post.
         * @throws Exception on JSON errors.
         */
        public C1X(InputStream in) throws Exception {
                super(in);
                parseSpecial();
        }
        
        /**
         * Process special C1X stuff, sets the exchange name. Sets encoding.
         */
        @Override
        public boolean parseSpecial() {
                setExchange( "c1x" );
                usesEncodedAdm = false;
                
                // C1X uses ISO2 country codes, we can't digest that with our campaign processor, so we
                // will convert it for them and patch the bid request.
                // Use a cache of country codes to keep from creating a lot of objects to be later garbage collected.
                Object o = this.database.get("device.geo.country");
                if (o instanceof MissingNode) {
                	return true;
                }
                
                TextNode country = (TextNode)o;
                TextNode test = null;
                if (country != null) {
                	test = cache.get(country.asText());
                	if (test == null) {
                		String iso3 = isoMap.query(country.asText());
                		test = new TextNode(iso3);
                	}
                	if (test != country)
                		database.put("device.geo.country", test);
                }
                return true;
        }
        
    	/**
    	 * Create a new c1x object from this class instance.
    	 * @throws JsonProcessingException on parse errors.
    	 * @throws Exception on stream reading errors
    	 */
    	@Override
    	public C1X copy(InputStream in) throws Exception  {
    		return new C1X(in);
    	}
}



        
