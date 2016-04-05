package com.xrtb.tests;

import static org.junit.Assert.*;


import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import redis.clients.jedis.Jedis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xrtb.bidder.Controller;
import com.xrtb.bidder.RTBServer;
import com.xrtb.common.Configuration;
import com.xrtb.common.ForensiqLog;
import com.xrtb.common.HttpPostGet;
import com.xrtb.exchanges.Fyber;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.ForensiqClient;

import junit.framework.TestCase;

/**
 * A class for testing that the bid has the right parameters
 * @author Ben M. Faul
 *
 */
public class TestForensiq  {
	static Controller c;
	public static String test = "";
	static Gson gson = new Gson();
	
	@BeforeClass
	  public static void testSetup() {		
		try {
			Config.setup();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }

	  @AfterClass
	  public static void testCleanup() {
		  Config.teardown();
	  }
	  
	  /**
	   * Test a valid bid response.
	   * @throws Exception on networking errors.
	   */
	  @Test 
	  public void testBad() throws Exception {
			ForensiqClient forensiq = ForensiqClient.build("6RLzOXoxntkqr0PHJ1Z0");
			
			String rt = "display";                        												
			String ip = "52.35.123.110";					  												// device.ip
			String url = "http%3A%2F%2Fwww.myheretrtrtouse.com%2Fsections%2Fliving%3Fa%3D3%20";			// site.page
			String ua = "erererer%2F4.0%20(compatible%3B%20MSIE%207.0%3B%20Windoreererws%20NT%206.0";	// device.ua
			String seller = "seller1234";																// site.name		
			String crid = "creative1234";																// your creative id
			
			ForensiqLog m = forensiq.bid(rt, ip, url, ua, seller, crid);
			assertNotNull(m);
	  }
	  
	  /**
	   * Test a valid bid response.
	   * @throws Exception on networking errors.
	   */
	  @Test 
	  public void testGood() throws Exception {
		  
		  ForensiqClient forensiq = ForensiqClient.build("6RLzOXoxntkqr0PHJ1Z0");
			
			String rt = "display";
			String ip = "123.254.33.4";
			String url = "http%3A%2F%2Fwww.myheretrtrtouse.com%2Fsections%2Fliving%3Fa%3D3%20";
			String ua = "erererer%2F4.0%20(compatible%3B%20MSIE%207.0%3B%20Windoreererws%20NT%206.0)";
			String seller = "seller1234";
			String crid = "xyz1234";
			
			ForensiqLog m = forensiq.bid(rt, ip, url, ua, seller, crid);
			assertNull(m);
	  }
	  
	  @Test 
	  public void testARealBid() throws Exception {
		  	ForensiqClient forensiq =  ForensiqClient.build("6RLzOXoxntkqr0PHJ1Z0");
		  	
		  	String s = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/nexage.txt")))).toString();
		  	
		  	
		  	
			String rt = "display";
			String ip = "123.254.33.4";
			String url = "http%3A%2F%2Fwww.myheretrtrtouse.com%2Fsections%2Fliving%3Fa%3D3%20";
			String ua = "erererer%2F4.0%20(compatible%3B%20MSIE%207.0%3B%20Windoreererws%20NT%206.0)";
			String seller = "seller1234";
			String crid = "xyz1234";
			
			ForensiqLog m = forensiq.bid(rt, ip, url, ua, seller, crid);
			assertNull(m);
	  }
}