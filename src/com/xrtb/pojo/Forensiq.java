package com.xrtb.pojo;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.bidder.RTBServer;
import com.xrtb.common.ForensiqLog;
import com.xrtb.common.HttpPostGet;
import com.xrtb.common.URIEncoder;

/**
 * A Class that implements the Forenciq.com anti-fraud bid checking system.
 * @author Ben M. Faul
 *
 */
public class Forensiq {

	/** Forensiq round trip time */
	public static AtomicLong forensiqXtime = new AtomicLong(0);
	/** forensiq count */
	public static AtomicLong forensiqCount = new AtomicLong(0);
	
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
	public static transient Queue<HttpPostGet> httpQueue = new ConcurrentLinkedQueue<HttpPostGet>();
	
	/**
	 * Default constructor
	 */
	public Forensiq() {
		preamble = endpoint + "?" + "ck=" + ck + "&output=JSON&sub=s&";
	}
	
	public Forensiq(String ck) {
		this.ck = ck;
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
	public ForensiqLog bid(String rt, String ip, String url, String ua, String seller, String crid) throws Exception {
		StringBuilder sb = new StringBuilder(preamble);
		JsonNode rootNode = null;		

		if (seller == null || ip == null) {
			if (seller == null)
				throw new Exception("Required field seller is missing");
			else
				throw new Exception("Required field ip is missing");
		}
		
		String sellerE = URIEncoder.encodeURI(seller);
		
		sb.append("rt=");
		sb.append(rt);
		sb.append("&");
		
		sb.append("ip=");
		sb.append(ip);
		sb.append("&");
		
		sb.append("seller=");
		sb.append(sellerE);
		
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
			
			String content = http.sendGet(sb.toString(),250,250);
			if (httpQueue.size() < 50)
				httpQueue.add(http);
			
			forensiqXtime.addAndGet(System.currentTimeMillis() - xtime);
			forensiqCount.incrementAndGet();
			
			//System.out.println("--->"+content);
			
			// System.err.println("---->" + RTBServer.);
			
			rootNode = mapper.readTree(content);
			int risk = rootNode.get("riskScore").asInt();
			int time = rootNode.get("timeMs").asInt();
			

			if (risk > threshhold) {
				ForensiqLog m = new ForensiqLog();
				m.ip = ip;
				m.url = url;
				m.ua = ua;
				m.seller = seller;
				m.risk = risk;
				return m;
			}
			
			return null;
		} catch (Exception e) {
			System.err.println("->>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ERROR IN FORENSIQ");
		} finally {

		}
		
		ForensiqLog m = new ForensiqLog();
		m.ip = ip;
		m.url = url;
		m.ua = ua;
		m.seller = seller;
		return m;
	}
}
