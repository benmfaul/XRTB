package com.xrtb.tests;

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
import com.xrtb.common.Utils;

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
	static RTBServer server;
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
		
		Configuration config = Configuration.getInstance();
		config.clear();
		config.initialize("Campaigns/payday.json");
		server = new RTBServer();
		Thread.sleep(5000);
		} catch (Exception error) {
			fail(error.toString());
		}
	  }

	  @AfterClass
	  public static void testCleanup() {
		  server.halt();
	  }
	  
	  @Test 
	  public void testRespondWithBid() throws Exception {
			HttpPostGet http = new HttpPostGet();
			String s = Utils.readFile("./SampleBids/nexage.txt");
			long time = 0;
			
			try {
				 http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", s);
			} catch (Exception error) {
				
			}
			String xtime = null;
			try {
				s = Utils.readFile("./SampleBids/nexage.txt");
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
			//	s = gson.toJson(m);
				//System.out.println(s);
				List list =  (List)m.get("seatbid");
				m = (Map)list.get(0);
				assertNotNull(m);
				String test =(String) m.get("seat");
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
				

			} catch (Exception e) {
				e.printStackTrace();
				fail(e.toString());

			}
		}
}
