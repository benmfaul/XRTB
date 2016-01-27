package com.xrtb.pojo;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.common.HttpPostGet;

/**
 * A Class that implements the Forenciq.com anti-fraud bid checking system.
 * @author Ben M. Faul
 *
 */
public class Forensiq {

	
	/** Endpoint of the forensiq api */
	public String endpoint = "http://api.forensiq.com/check";
	/** Your Forensiq key */
	public String ck = "yourkeygoeshere";
	/** Default threshhold for non bidding */
	public int threshhold = 64;
	/** If the forensiq site throws an error or is not available, bid anyway? */
	public boolean bidOnError = false;
	
	/** The precompiled preamble */
	@JsonIgnore
	transient String preamble;
	
	/** The object mapper for converting the return from forensiq */
	@JsonIgnore
	transient ObjectMapper mapper = new ObjectMapper();
	
	/** A queue of HTTP get objects we can reuse */
	@JsonIgnore
	transient Queue<HttpPostGet> httpQueue = new ConcurrentLinkedQueue<HttpPostGet>();
	
	/**
	 * Default constructor
	 */
	public Forensiq() {
		preamble = endpoint + "?" + "ck=" + ck + "&output=JSON&sub=s&";
	}
	
	/**
	 * Should I bid, or not?
	 * @param rt String. The type, always "display".
	 * @param ip String. The IP address of the user.
	 * @param url String. The URL of the publisher.
	 * @param ua String. The user agent.
	 * @param seller String. The seller's domain.
	 * @param crid String. The creative id
	 * @return boolean. If it returns true, good to bid. Or, false if it fails the confidence test.
	 * @throws Exception on missing rwquired fields - seller and IP.
	 */
	public boolean bid(String rt, String ip, String url, String ua, String seller, String crid) throws Exception {
		StringBuilder sb = new StringBuilder(preamble);
		JsonNode rootNode = null;
		
		if (seller == null || ip == null) {
			if (seller == null)
				throw new Exception("Required field seller is missing");
			else
				throw new Exception("Required field ip is missing");
		}
		
		sb.append("rt=");
		sb.append(rt);
		sb.append("&");
		
		sb.append("ip=");
		sb.append(ip);
		sb.append("&");
		
		sb.append("seller=");
		sb.append(seller);
		
		if (url != null) {
			sb.append("&");
			sb.append("url=");
			sb.append(url);
		}
		
		if (ua != null) {
			sb.append("&");
			sb.append("ua=");
			sb.append(ua);
		}
		
		if (crid != null) {
			sb.append("&");
			sb.append("cmp=");
			sb.append(crid);
		}
		
		sb.append("&sub=s");
		
		HttpPostGet http = null;
		
		if (httpQueue.isEmpty())
			http = new HttpPostGet();
		else
			http = httpQueue.remove();
		
		try {

			long xtime = System.currentTimeMillis();
			
			String content = http.sendGet(sb.toString());
			
			xtime = System.currentTimeMillis() - xtime;
			
			System.out.println("--->"+content);
			
			// System.err.println("---->" + xtime);
			
			rootNode = mapper.readTree(content);
			int risk = rootNode.get("riskScore").asInt();
			int time = rootNode.get("timeMs").asInt();
			
			if (risk > threshhold)
				return false;
			return true;
		} catch (Exception e) {
			// e.printStackTrace();
		} finally {
			httpQueue.add(http);
		}
		
		return true;
	}
}
