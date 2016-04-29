package com.xrtb.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.xrtb.bidder.Controller;
import com.xrtb.bidder.RTBServer;
import com.xrtb.common.Configuration;
import com.xrtb.common.HttpPostGet;
import com.xrtb.pojo.Bid;

/**
 * A class to test all aspects of the win processing.
 * @author Ben M. Faul
 *
 */
public class TestWinProcessing  {
	/**
	 * Setup the RTB server for the test
	 */
	@BeforeClass
	public static void setup() {
		try {
			Config.setup();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Shut the RTB server down.
	 */
	@AfterClass
	public static void testCleanup() {
		Config.teardown();
		System.out.println("We are done!");
	}

	/**
	 * Test the basic win processing system of the RTB
	 * @throws Exception on networking problems.
	 */
	@Test
	public void testWinProcessing() throws Exception  {
		HttpPostGet http = new HttpPostGet();
		Jedis cache = new Jedis("localhost");
		if (Configuration.password != null)
			cache.auth(Configuration.password);
		cache.connect();
		cache.del("35c22289-06e2-48e9-a0cd-94aeb79fab43");
		// Make the bid
		
		String s = Charset
				.defaultCharset()
				.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
						.get("./SampleBids/nexage.txt")))).toString();
		/**
		 * Send the bid
		 */
		try {
			s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", s);
		} catch (Exception error) {
			fail("Can't connect to test host: " + Config.testHost);
		}
		int code = http.getResponseCode();
		assertTrue(code==200);
		Bid bid = null;
		System.out.println(s);
		try {
			bid = new Bid(s);
		} catch (Exception error) {
			error.printStackTrace();
			fail();
		}
		
		// Now retrieve the bid information from the cache
		Map m = cache.hgetAll(bid.id);
		assertTrue(!m.isEmpty());
		String price = (String)m.get("PRICE");
		assertTrue(price.equals("1.0"));
		
		/**
		 * Send the win notification
		 */
		try {

			String repl = bid.nurl.replaceAll("\\$", "");
			bid.nurl = repl.replace("{AUCTION_PRICE}", ".05");
			
			s = http.sendPost(bid.nurl, "");
		} catch (Exception error) {
			error.printStackTrace();
			fail();
		}

		System.out.println(s);
		/*
		 * Make sure the returned adm is not crap html 
		 */
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(s));

	//	Document doc = db.parse(is);
		
		// Check to see the bid was removed from the cache
		m = cache.hgetAll(bid.id);
		assertTrue(m.isEmpty());
		
	}
	
	  /**
	   * Test a valid bid response with no bid, the campaign doesn't match width or height of the bid request
	   * @throws Exception on network errors.
	   */
	  @Test 
	  public void testCapping() throws Exception {
			JedisPoolConfig cfg = new JedisPoolConfig();
			
			cfg.setMaxTotal(1000);
			JedisPool pool  = new JedisPool(cfg, Configuration.cacheHost,
					Configuration.cachePort, 10000, Configuration.password);
			
			Jedis jedis = pool.getResource();
			jedis.del("capped_blocker166.137.138.18");
			
			HttpPostGet http = new HttpPostGet();
			String bid = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/nexage50x50.txt")))).toString();
			
			// Get 3 times is ok, but 4th is a no bid
			String s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", bid, 100000, 100000);
			assertNotNull(s);
			int rc = http.getResponseCode();
			assertTrue(rc==200);
			String value = jedis.get("capped_blocker166.137.138.18");
			assertTrue(value == null);
			Bid win = new Bid(s);
			String repl = win.nurl.replaceAll("\\$", "");
			win.nurl = repl.replace("{AUCTION_PRICE}", ".05");	
			s = http.sendPost(win.nurl, "");
			value = jedis.get("capped_blocker166.137.138.18");
			assertTrue(value.equals("1"));
			
			
			s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", bid, 100000, 100000);
			assertNotNull(s);
			rc = http.getResponseCode();
			assertTrue(rc==200);
			s = http.sendPost(win.nurl, "");
			value = jedis.get("capped_blocker166.137.138.18");
			assertTrue(value.equals("2"));
			
			s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", bid, 100000, 100000);
			assertNotNull(s);
			rc = http.getResponseCode();
			assertTrue(rc==200);
			s = http.sendPost(win.nurl, "");
			value = jedis.get("capped_blocker166.137.138.18");
			assertTrue(value.equals("3"));
			
			// better no bid.
			s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", bid, 100000, 100000);
			rc = http.getResponseCode();
			assertTrue(rc==204);
			assertNull(s);
			rc = http.getResponseCode();
			
		    value = jedis.get("capped_blocker166.137.138.18");
			assertTrue(value.equals("3"));
			
			System.out.println("DONE!");
		} 
}
