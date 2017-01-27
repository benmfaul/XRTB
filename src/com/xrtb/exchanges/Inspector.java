package com.xrtb.exchanges;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.xrtb.bidder.CampaignSelector;
import com.xrtb.common.Campaign;
import com.xrtb.common.Creative;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;
import com.xrtb.tools.DbTools;
import com.xrtb.tools.logmaster.AppendToFile;

/**
 * A class to handle arbitrary ad exchange info and write it to file
 * @author Ben M. Faul
 *
 */

public class Inspector extends BidRequest {

		static String fileName = null;
		static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm");
        public Inspector() {
                super();
                parseSpecial();
        }
        
        /**
         * Make bid request using a String.
         * @param in String. The JSON bid request for Epom
         * @throws Exception on JSON errors.
         */
        public Inspector(String  in) throws Exception  {
                super(in);
                parseSpecial();
    }

        /**
         * Make a bid request using an input stream.
         * @param in InputStream. The contents of a HTTP post.
         * @throws Exception on JSON errors.
         */
        public Inspector(InputStream inputStream) throws Exception {
        	exchange = "inspector";
        	id = "";
        	blackListed = true;
        	if (fileName == null) {
        		fileName = "inspector-" + sdf.format(new Date());
        	}
        	
        	final int bufferSize = 1024;
        	final char[] buffer = new char[bufferSize];
        	final StringBuilder out = new StringBuilder();
        	Reader in = new InputStreamReader(inputStream, "UTF-8");
        	for (; ; ) {
        	    int rsz = in.read(buffer, 0, buffer.length);
        	    if (rsz < 0)
        	        break;
        	    out.append(buffer, 0, rsz);
        	}
        	
        	String content = out.toString();
        	StringBuilder report = new StringBuilder();
        	try {
        		BidRequest br = new BidRequest(new StringBuilder(content));
        		br.blackListed = true;
        		br.exchange = "inspector";
        		String good = br.toString();
        		CampaignSelector.getInstance().getMaxConnections(br);
        		report.append("GOOD, DATA: ");
        		report.append(good);
        	} catch (Exception error) {
        		error.printStackTrace();
        		report.append("BAD, REASON: ");
        		report.append(error.toString());
        		report.append(", DATA: ");
        		report.append(content);
        	}
        	report.append("\n");
        	
        	AppendToFile.item(fileName, report);
        }
        
        /**
         * Process special AdMedia stuff, sets the exchange name. Setss encoding.
         */
        @Override
        public boolean parseSpecial() {
                exchange = "inspector";
                usesEncodedAdm = false;
                return true;
        }
        
    	/**
    	 * Create a new object from this class instance.
    	 * @throws JsonProcessingException on parse errors.
    	 * @throws Exception on stream reading errors
    	 */
    	@Override
    	public Inspector copy(InputStream in) throws Exception  {
    		Inspector x = new Inspector(in);
    		return x;
    	}
 
}



        
