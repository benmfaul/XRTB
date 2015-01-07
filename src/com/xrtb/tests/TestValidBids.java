package com.xrtb.tests;

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
import com.xrtb.common.HttpPostGet;
import com.xrtb.pojo.BidRequest;

import junit.framework.TestCase;

/**
 * A class for testing that the bid has the right parameters
 * @author Ben M. Faul
 *
 */
public class TestValidBids extends TestCase {
	static Controller c;
	static Jedis sub;
	static Jedis pub;
	static ResponseLoop loop;
	public static String test = "";
	static Gson gson = new Gson();
	
	@BeforeClass
	  public static void testSetup() {		
		try {
		sub = new Jedis("localhost");  // sub
		sub.connect();
		pub = new Jedis("localhost");
		pub.connect();

		Controller c = Controller.getInstance();
		loop = new ResponseLoop(sub,Controller.RESPONSES);
		Config.setup();
		} catch (Exception error) {
			error.printStackTrace();
			fail(error.toString());
		}
	  }

	  @AfterClass
	  public static void testCleanup() {
		Config.teardown();
	  }
	  
	  /**
	   * Test a valid bid response.
	   * @throws Exception. Throws exceptions on bad JSON data.
	   */
	  @Test 
	  public void testRespondWithBid() throws Exception {
			HttpPostGet http = new HttpPostGet();
			String s = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/nexage.txt")))).toString();
			long time = 0;
			
			try {
				 http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", s);
			} catch (Exception error) {
				fail("Network error");
			}
			String xtime = null;
			try {
				s = Charset
						.defaultCharset()
						.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
								.get("./SampleBids/nexage.txt")))).toString();
				try {
					time = System.currentTimeMillis();
					s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", s);
					time = System.currentTimeMillis() - time;
					xtime = http.getHeader("X-TIME");
				} catch (Exception error) {
					fail("Can't connect to test host: " + Config.testHost);
				}
				gson = new GsonBuilder().setPrettyPrinting().create();
				Map m = null;
				try {
					m = gson.fromJson(s,Map.class);
				} catch (Exception error) {
					fail("Bad JSON for bid");
				}
				List list =  (List)m.get("seatbid");
				m = (Map)list.get(0);
				assertNotNull(m);
				String test =(String) m.get("seat");
System.out.println("===============>"+test);
				assertTrue(test.equals("99999999"));
				list =(List)m.get("bid");
				assertEquals(list.size(),1);
				m = (Map)list.get(0);
				assertNotNull(m);
				test = (String)m.get("impid");
				assertTrue(test.equals("23skiddoo"));
				test = (String)m.get("id");
				assertTrue(test.equals("35c22289-06e2-48e9-a0cd-94aeb79fab43"));
				double d = (Double)m.get("price");
				assertEquals(d,5.0);
				
				test = (String)m.get("adid");
				assertTrue(test.equals("id123"));
				
				test = (String)m.get("cid");
				assertTrue(test.equals("id123"));
				
				test = (String)m.get("crid");
				assertTrue(test.equals("23skiddoo"));
				
				test = (String)m.get("adomain");
				assertTrue(test.equals("originator.com"));
				
				System.out.println("XTIME: " + xtime);
				System.out.println("RTTIME: " + time);
				System.out.println(s);

			} catch (Exception e) {
				e.printStackTrace();
				fail(e.toString());

			}
		} 
	  
	  
	  /**
	   * Test a valid bid response with no bid, the campaign doesn't match width or height of the bid request
	   * @throws Exception. Throws exceptions on bad JSON data.
	   */
	  @Test 
	  public void testRespondWithNoBid() throws Exception {
			HttpPostGet http = new HttpPostGet();
			String s = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/nexage50x50.txt")))).toString();
			try {
				 s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", s);
			} catch (Exception error) {
				fail("Network error");
			}
			assertTrue(http.getResponseCode()==204);
		}
}
