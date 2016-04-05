package com.xrtb.pojo;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.ws.spi.http.HttpContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.bidder.Controller;
import com.xrtb.bidder.RTBServer;
import com.xrtb.common.ForensiqLog;
import com.xrtb.common.HttpPostGet;
import com.xrtb.common.URIEncoder;



/**
 * A Class that implements the Forenciq.com anti-fraud bid checking system.
 * @author Ben M. Faul
 *
 */
public enum  ForensiqClient {
	
	FORENSIQCLIENT;
	
	static CloseableHttpClient httpclient;
	
	/** Forensiq round trip time */
	public static AtomicLong forensiqXtime = new AtomicLong(0);
	/** forensiq count */
	public static AtomicLong forensiqCount = new AtomicLong(0);
	
	/** Endpoint of the forensiq api */
	public static String endpoint = "http://api.forensiq.com/check";
	/** Your Forensiq key */
	public static String key = "yourkeygoeshere";
	/** Default threshhold for non bidding */
	public static int threshhold = 64;
	/** If the forensiq site throws an error or is not available, bid anyway? */
	public boolean bidOnError = false;
	/** connection pool size */
	public static int connections = 100;
	
	static PoolingHttpClientConnectionManager cm;
	
	/** The precompiled preamble */
	@JsonIgnore
	transient public static String preamble;
	
	/** The object mapper for converting the return from forensiq */
	@JsonIgnore
	transient ObjectMapper mapper = new ObjectMapper();
	
	public static void main(String [] args) throws Exception {
		ForensiqClient q = ForensiqClient.build();
		q.bid("","","","","","");
		
	}
	
	/**
	 * Default constructor
	 */
	public static ForensiqClient build() {
		preamble = endpoint + "?" + "ck=" + key + "&output=JSON&sub=s&";
		setup();
		return FORENSIQCLIENT;
	}
	
	public static ForensiqClient build(String ck) {
		key = ck;
		preamble = endpoint + "?" + "ck=" + key + "&output=JSON&sub=s&";		
		setup();
		return FORENSIQCLIENT;
	}
	
	public static ForensiqClient getInstance() {
		return FORENSIQCLIENT;
	}
	
	public static void setup() {
		 cm = new PoolingHttpClientConnectionManager();
	     cm.setMaxTotal(connections);
	     cm.setDefaultMaxPerRoute(connections);

	    httpclient = HttpClients.custom().setConnectionManager(cm).build();
	}
	
	public static void reset() {
		cm.closeIdleConnections(1000,TimeUnit.SECONDS);
		cm.closeExpiredConnections();
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
		byte[] bytes = null;
		StringBuilder sb = new StringBuilder(preamble);
		JsonNode rootNode = null;		

		if (httpclient == null) {
			return null;
		}
		
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
		
		BasicHttpContext context = new BasicHttpContext();
		HttpGet httpget = new HttpGet(sb.toString());
		
		try {

			long xtime = System.currentTimeMillis();
			
			 CloseableHttpResponse response = httpclient.execute(httpget, context);

			 HttpEntity entity = response.getEntity();
             if (entity != null) {
                 bytes = EntityUtils.toByteArray(entity);
             }
             response.close();
			
			 String content = new String(bytes);;
			
			//System.out.println("--->"+content);
			
			// System.err.println("---->" + RTBServer.);
			
			rootNode = mapper.readTree(content);
			int risk = rootNode.get("riskScore").asInt();
			int time = rootNode.get("timeMs").asInt();
			
			forensiqXtime.addAndGet(System.currentTimeMillis() - xtime);
			forensiqCount.incrementAndGet();

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
			Controller.getInstance().sendLog(1, "ForensiqLog:bid-error",e.getMessage());
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
