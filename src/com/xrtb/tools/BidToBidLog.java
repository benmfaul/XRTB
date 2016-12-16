package com.xrtb.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.pojo.BidResponse;
import com.xrtb.tools.logmaster.LogObject;

/**
 * Purpose is to convert an RTB bid request into a bid log record
 * @author ben
 *
 */
public class BidToBidLog {

	public static void main(String[] args) throws Exception {
		BufferedReader br = null;
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
				false);
		String data = null;
		br = new BufferedReader(new FileReader("logs/bids"));
		int k = 0;
		while((data=br.readLine()) != null) {
			BidResponse r = mapper.readValue(data, BidResponse.class);
			//Map m = new HashMap();
			//m.put("width",r.width);
			//m.put("height",r.height);
			//m.put("lat",r.lat);
			//m.put("lon",r.lon);
			//m.put("forwardUrl",r.forwardUrl);
			//m.put("imageUrl",r.imageUrl);
			//m.put("impid",r.impid);
			//m.put("adid",r.adid);
			//m.put("crid",r.crid);
			//m.put("domain",r.domain);
			//m.put("xrime",r.xtime);
			//m.put("width",r.width);
			//m.put("oidStr",r.oidStr);
			//m.put("exchange",r.exchange);
			//m.put("cost",r.cost);
			//m.put("utc",r.utc);
			//m.put("protobuf",r.protobuf);
			System.out.println(mapper.writer().writeValueAsString(r));
		
		}
	}
}
