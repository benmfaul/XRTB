package com.xrtb.tests;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.net.URLDecoder;
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
public class TestSmaato  {
	static Controller c;
	public static String test = "";
	static Gson gson = new Gson();
	
	@BeforeClass
	  public static void testSetup() {		
		try {
			Config.setup();

		} catch (Exception error) {
			error.printStackTrace();
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
	  public void testBannerRespondWithBid() throws Exception {
			HttpPostGet http = new HttpPostGet();
			String s = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/smaato.json")))).toString();
			String xtime = null;
			long time = 0;
			try {
				s = Charset
						.defaultCharset()
						.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
								.get("./SampleBids/smaato.json")))).toString();
				try {
					time = System.currentTimeMillis();
					s = http.sendPost("http://" + Config.testHost + "/rtb/bids/smaato", s);
					time = System.currentTimeMillis() - time;
					xtime = http.getHeader("X-TIME");
				} catch (Exception error) {
					fail("Can't connect to test host: " + Config.testHost);
				}
				gson = new GsonBuilder().setPrettyPrinting().create();
				
				Map m = null;
				try {
					m = gson.fromJson(s,Map.class);
					System.out.println(gson.toJson(m));
				} catch (Exception error) {
					fail("Bad JSON for bid");
				}
				List list =  (List)m.get("seatbid");
				m = (Map)list.get(0);
				assertNotNull(m);
				String test =(String) m.get("seat");
				assertTrue(test.equals("seat1"));
				list =(List)m.get("bid");
				assertEquals(list.size(),1);
				m = (Map)list.get(0);
				assertNotNull(m);
				test = (String)m.get("impid");
				assertTrue(test.equals("23skiddoo"));
				test = (String)m.get("id");
				assertTrue(test.equals("K6t8sXXYdM"));
				double d = (Double)m.get("price");
				assertTrue(d==5.0);
				
				test = (String)m.get("adid");
				
				assertTrue(test.equals("ben:payday"));
				
				test = (String)m.get("cid");
				assertTrue(test.equals("ben:payday"));
				
				test = (String)m.get("crid");
				assertTrue(test.equals("23skiddoo"));
				
				list = (List)m.get("adomain");
				test = (String)list.get(0);
				assertTrue(test.equals("originator.com"));
				
				System.out.println("XTIME: " + xtime);
				System.out.println("RTTIME: " + time);
				System.out.println(s);
				
				
				assertFalse(s.contains("pub"));
				assertFalse(s.contains("ad_id"));
				assertFalse(s.contains("bid_id"));
				assertFalse(s.contains("site_id"));
				
				String adm = (String) m.get("adm");
				System.out.println(URLDecoder.decode(adm));

			} catch (Exception e) {
				e.printStackTrace();
				fail(e.toString());

			}
		} 
}
