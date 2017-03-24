package test.java;

import static org.junit.Assert.assertNotNull;


import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.aerospike.client.AerospikeClient;
import com.aerospike.redisson.RedissonClient;
import com.xrtb.bidder.Controller;
import com.xrtb.bidder.RTBServer;
import com.xrtb.commands.BasicCommand;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.common.Creative;
import com.xrtb.common.HttpPostGet;
import com.xrtb.common.Node;
import com.xrtb.pojo.Bid;
import com.xrtb.pojo.BidResponse;
import com.xrtb.pojo.WinObject;


/**
 * A class to test all aspects of the win processing.
 * @author Ben M. Faul
 *
 */
public class TestWinProcessing  {
	/**
	 * Setup the RTB server for the test
	 */
	
	static RedissonClient redisson;
	
	static String password;
	@BeforeClass
	public static void setup() {
		try {
			
			AerospikeClient spike = new AerospikeClient("localhost",3000);
			redisson = new RedissonClient(spike);
			
			Config.setup();
			System.out.println("******************  TestWinProcessing");
			password = Configuration.getInstance().password;
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
	public void testWinProcessingNexage() throws Exception  {
		HttpPostGet http = new HttpPostGet();

		redisson.del("35c22289-06e2-48e9-a0cd-94aeb79fab43");
		// Make the bid
		
		String s = Charset
				.defaultCharset()
				.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
						.get("./SampleBids/nexage.txt")))).toString();
		/**
		 * Send the bid
		 */
		try {
			s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", s, 3000000, 3000000);
		} catch (Exception error) {
			fail("Can't connect to test host: " + Config.testHost);
		}
		int code = http.getResponseCode();
		assertTrue(code==200);
		Bid bid = null;
		System.out.println(s);
		int x = s.indexOf("{bid_id");
		assertTrue(x == -1);
		x = s.indexOf("%7Bbid_id");
		assertTrue(x == -1);
		
		try {
			bid = new Bid(s);
		} catch (Exception error) {
			error.printStackTrace();
			fail();
		}
		
		// Now retrieve the bid information from the cache
		Map m = redisson.hgetAll(bid.id);
		assertTrue(!m.isEmpty());
		String price = (String)m.get("PRICE");
		assertNotNull(price);
		assertTrue(!price.equals("0.0"));
		
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
		
		x = s.indexOf("{creative_");
		assertTrue(x == -1);
		
		/*
		 * Make sure the returned adm is not crap html 
		 */
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(s));

	//	Document doc = db.parse(is);
		
		// Check to see the bid was removed from the cache
		m = redisson.hgetAll(bid.id);
		assertNull(m);
		
	}
	
	/**
	 * Test the basic win processing system of the RTB
	 * @throws Exception on networking problems.
	 */
	@Test
	public void testWinProcessingSmartyAds() throws Exception  {
		HttpPostGet http = new HttpPostGet();
		redisson.del("35c22289-06e2-48e9-a0cd-94aeb79fab43");
		// Make the bid
		
		String s = Charset
				.defaultCharset()
				.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
						.get("./SampleBids/nexage.txt")))).toString();
		/**
		 * Send the bid
		 */
		try {
			s = http.sendPost("http://" + Config.testHost + "/rtb/bids/smartyads", s, 3000000, 3000000);
		} catch (Exception error) {
			fail("Can't connect to test host: " + Config.testHost);
		}
		int code = http.getResponseCode();
		assertTrue(code==200);
		Bid bid = null;
		System.out.println(s);
		int x = s.indexOf("{bid_id");
		assertTrue(x == -1);
		x = s.indexOf("%7Bbid_id");
		assertTrue(x == -1);
		
		try {
			bid = new Bid(s);
		} catch (Exception error) {
			error.printStackTrace();
			fail();
		}
		
		// Now retrieve the bid information from the cache
		Map m = redisson.hgetAll(bid.id);
		assertTrue(!m.isEmpty());
		String price = (String)m.get("PRICE");
		assertTrue(!price.equals("0.0"));
		
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
		
		x = s.indexOf("{creative_");
		assertTrue(x == -1);
		
		/*
		 * Make sure the returned adm is not crap html 
		 */
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(s));

	//	Document doc = db.parse(is);
		
		// Check to see the bid was removed from the cache
		m = redisson.hgetAll(bid.id);
		assertNull(m);
		
	}
	
	@Test
	public void testWinProcessingCappture() throws Exception  {
		HttpPostGet http = new HttpPostGet();
		redisson.del("35c22289-06e2-48e9-a0cd-94aeb79fab43");
		// Make the bid
		
		String s = Charset
				.defaultCharset()
				.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
						.get("./SampleBids/nexage.txt")))).toString();
		/**
		 * Send the bid
		 */
		try {
			s = http.sendPost("http://" + Config.testHost + "/rtb/bids/cappture", s, 3000000, 3000000);
		} catch (Exception error) {
			error.printStackTrace();
			fail("Can't connect to test host: " + Config.testHost);
		}
		int code = http.getResponseCode();
		assertTrue(code==200);
		Bid bid = null;
		System.out.println(s);
		int x = s.indexOf("{bid_id");
		assertTrue(x == -1);
		x = s.indexOf("%7Bbid_id");
		assertTrue(x == -1);
		
		try {
			bid = new Bid(s);
		} catch (Exception error) {
			error.printStackTrace();
			fail();
		}
		
		// Now retrieve the bid information from the cache
		Map m = redisson.hgetAll(bid.id);
		assertTrue(!m.isEmpty());
		String price = (String)m.get("PRICE");
		assertNotNull(price);
		assertTrue(!price.equals("0"));
		
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
		
		x = s.indexOf("{creative_");
		assertTrue(x == -1);
		
		/*
		 * Make sure the returned adm is not crap html 
		 */
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(s));

	//	Document doc = db.parse(is);
		
		// Check to see the bid was removed from the cache
		m = redisson.hgetAll(bid.id);
		assertNull(m);
		
	}
	
	@Test
	public void testWinProcessingEpom() throws Exception  {
		HttpPostGet http = new HttpPostGet();
		redisson.del("35c22289-06e2-48e9-a0cd-94aeb79fab43");
		// Make the bid
		
		String s = Charset
				.defaultCharset()
				.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
						.get("./SampleBids/nexage.txt")))).toString();
		/**
		 * Send the bid
		 */
		try {
			s = http.sendPost("http://" + Config.testHost + "/rtb/bids/epomx", s, 3000000, 3000000);
		} catch (Exception error) {
			fail("Can't connect to test host: " + Config.testHost);
		}
		int code = http.getResponseCode();
		assertTrue(code==200);
		Bid bid = null;
		System.out.println(s);
		int x = s.indexOf("{bid_id");
		assertTrue(x == -1);
		x = s.indexOf("%7Bbid_id");
		assertTrue(x == -1);
		
		try {
			bid = new Bid(s);
		} catch (Exception error) {
			error.printStackTrace();
			fail();
		}
		
		// Now retrieve the bid information from the cache
		Map m = redisson.hgetAll(bid.id);
		assertTrue(!m.isEmpty());
		String price = (String)m.get("PRICE");
		System.out.println("PRICE: " + price);
		assertNotNull(price);
		assertTrue(!price.equals("0.0"));
		
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
		
		x = s.indexOf("{creative_");
		assertTrue(x == -1);
		
		/*
		 * Make sure the returned adm is not crap html 
		 */
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(s));

	//	Document doc = db.parse(is);
		
		// Check to see the bid was removed from the cache
		m = redisson.hgetAll(bid.id);
		assertNull(m);
		
	}
	
	@Test
	public void testWinProcessingAtomx() throws Exception  {
		HttpPostGet http = new HttpPostGet();
		redisson.del("35c22289-06e2-48e9-a0cd-94aeb79fab43");
		// Make the bid
		
		String s = Charset
				.defaultCharset()
				.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
						.get("./SampleBids/nexage.txt")))).toString();
		/**
		 * Send the bid
		 */
		try {
			s = http.sendPost("http://" + Config.testHost + "/rtb/bids/atomx", s, 3000000, 3000000);
		} catch (Exception error) {
			fail("Can't connect to test host: " + Config.testHost);
		}
		int code = http.getResponseCode();
		assertTrue(code==200);
		Bid bid = null;
		System.out.println(s);
		int x = s.indexOf("{bid_id");
		assertTrue(x == -1);
		x = s.indexOf("%7Bbid_id");
		assertTrue(x == -1);
		
		try {
			bid = new Bid(s);
		} catch (Exception error) {
			error.printStackTrace();
			fail();
		}
		
		// Now retrieve the bid information from the cache
		Map m = redisson.hgetAll(bid.id);
		assertTrue(!m.isEmpty());
		String price = (String)m.get("PRICE");
		assertNotNull(price);
		assertTrue(!price.equals("0.0"));		
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
		
		x = s.indexOf("{creative_");
		assertTrue(x == -1);
		
		/*
		 * Make sure the returned adm is not crap html 
		 */
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(s));

	//	Document doc = db.parse(is);
		
		// Check to see the bid was removed from the cache
		m = redisson.hgetAll(bid.id);
		assertNull(m);
		
	}
	
	@Test
	public void testWinProcessingJavaScriptSmaato() throws Exception  {
		HttpPostGet http = new HttpPostGet();
		redisson.del("MXv5wEiniR");
		// Make the bid
		
		String s = Charset
				.defaultCharset()
				.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
						.get("./SampleBids/apptest.txt")))).toString();
		/**
		 * Send the bid
		 */
		try {
			s = http.sendPost("http://" + Config.testHost + "/rtb/bids/smaato", s, 3000000, 3000000);
		} catch (Exception error) {
			fail("Can't connect to test host: " + Config.testHost);
		}
		int code = http.getResponseCode();
		assertTrue(code==200);
		Bid bid = null;
		System.out.println(s);
		int x = s.indexOf("{bid_id");
		assertTrue(x == -1);
		x = s.indexOf("{app_id");
		assertTrue(x == -1);
		
		try {
			bid = new Bid(s);
		} catch (Exception error) {
			error.printStackTrace();
			fail();
		}
		
		// Now retrieve the bid information from the cache
		Map m = redisson.hgetAll(bid.id);
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
		
		x = s.indexOf("{app_id");
		assertTrue(x == -1);
		x = s.indexOf("{bid_id");
		assertTrue(x == -1);
		
		/*
		 * Make sure the returned adm is not crap html 
		 */
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(s));

	//	Document doc = db.parse(is);
		
		// Check to see the bid was removed from the cache
		m = redisson.hgetAll(bid.id);
		assertNull(m);
		
	}
	
	  /**
	   * Test a valid bid response with no bid, the campaign doesn't match width or height of the bid request
	   * @throws Exception on network errors.
	   */
	  @Test 
	  public void testCappingTimes3() throws Exception {
			redisson.del("capped_blocker166.137.138.18");
			
			
			for (Campaign c : Configuration.getInstance().campaignsList) {
				if (c.adId.equals("ben:payday")) {
					for (Creative cc : c.creatives) {
						if (cc.impid.equals("blocker")) {
							cc.capFrequency = 3;
							break;
						}
					}
				}
			}
			
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
			String value = redisson.get("capped_blocker166.137.138.18");
			assertTrue(value == null);
			Bid win = new Bid(s);
			String repl = win.nurl.replaceAll("\\$", "");
			win.nurl = repl.replace("{AUCTION_PRICE}", ".05");	
			s = http.sendPost(win.nurl, "");
			value = redisson.get("capped_blocker166.137.138.18");
			assertTrue(value.equals("1"));
			
			
			s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", bid, 100000, 100000);
			assertNotNull(s);
			rc = http.getResponseCode();
			assertTrue(rc==200);
			s = http.sendPost(win.nurl, "");
			value = redisson.get("capped_blocker166.137.138.18");
			assertTrue(value.equals("2"));
			
			s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", bid, 100000, 100000);
			assertNotNull(s);
			rc = http.getResponseCode();
			assertTrue(rc==200);
			s = http.sendPost(win.nurl, "");
			value = redisson.get("capped_blocker166.137.138.18");
			assertTrue(value.equals("3"));
			
			// better no bid.
			s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", bid, 100000, 100000);
			rc = http.getResponseCode();
			assertTrue(rc==204);
			assertNull(s);
			rc = http.getResponseCode();
			
		    value = redisson.get("capped_blocker166.137.138.18");
			assertTrue(value.equals("3"));
			
			System.out.println("DONE!");
		} 
	  
	  @Test 
	  public void testCappingTimes1() throws Exception {
			redisson.del("capped_blocker166.137.138.18");
			
			
			for (Campaign c : Configuration.getInstance().campaignsList) {
				if (c.adId.equals("ben:payday")) {
					for (Creative cc : c.creatives) {
						if (cc.impid.equals("blocker")) {
							cc.capFrequency = 1;
							break;
						}
					}
				}
			}
			
			
			HttpPostGet http = new HttpPostGet();
			String bid = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/nexage50x50.txt")))).toString();
			
			// Get 1 time is ok, but 2d time is a no bid
			String s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", bid, 100000, 100000);
			assertNotNull(s);
			int rc = http.getResponseCode();
			assertTrue(rc==200);
			String value = redisson.get("capped_blocker166.137.138.18");
			assertTrue(value == null);
			Bid win = new Bid(s);
			String repl = win.nurl.replaceAll("\\$", "");
			win.nurl = repl.replace("{AUCTION_PRICE}", ".05");	
			
			System.out.println(win.nurl);
			s = http.sendPost(win.nurl, "",30000,30000);
			value = redisson.get("capped_blocker166.137.138.18");
			assertTrue(value.equals("1"));
			
			// better no bid.
			s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", bid, 100000, 100000);
			rc = http.getResponseCode();
			assertTrue(rc==204);
			assertNull(s);
			rc = http.getResponseCode();
			
		    value = redisson.get("capped_blocker166.137.138.18");
			assertTrue(value.equals("1"));
			
			System.out.println("DONE!");
		} 
	  
	  @Test
		public void testWinProcessingInvalidHttp() throws Exception  {
			HttpPostGet http = new HttpPostGet();
			// Make the bid
			
			String s = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/nexage.txt")))).toString();
			
			s = s.replaceAll("35c22289-06e2-48e9-a0cd-94aeb79fab4", "ADM#ssp#1023#56425#1490316943.792#68738174.223.128.39-1490316943883-130-0-0-6808053509727162318");
			if (s.indexOf("ADM#") == -1) {
				s = s.replaceAll("123", "ADM#ssp#1023#56425#1490316943.792#68738174.223.128.39-1490316943883-130-0-0-6808053509727162318");
			}
			/**
			 * Send the bid
			 */
			try {
				s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", s, 3000000, 3000000);
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
				
		
			/**
			 * Send the win notification
			 */
			try {

				String repl = bid.nurl.replaceAll("\\$", "");
				bid.nurl = repl.replace("{AUCTION_PRICE}", ".05");
				
				s = http.sendPost(bid.nurl, "",300000,300000);
			} catch (Exception error) {
				error.printStackTrace();
				fail();
			}
			System.out.println("---->" + s);;
			assertTrue(s.length() > 10);
		}
	  
	  @Test
		public void testNegative() throws Exception  {
			HttpPostGet http = new HttpPostGet();
			final CountDownLatch latch = new CountDownLatch(1);
			final CountDownLatch wlatch = new CountDownLatch(1);
			final List<Double> price = new ArrayList();
			
			String s = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/negative.txt")))).toString();
			
			com.xrtb.jmq.RTopic channel = new com.xrtb.jmq.RTopic("tcp://*:5571&bids");
			channel.subscribe("bids");
			channel.addListener(new com.xrtb.jmq.MessageListener<BidResponse>() {
				@Override
				public void onMessage(String channel, BidResponse bid) {
					price.add(bid.cost);
					System.out.println("BID COST: " + bid.cost);
					latch.countDown();
				}
			}); 
			
			com.xrtb.jmq.RTopic wchannel = new com.xrtb.jmq.RTopic("tcp://*:5572&wins");
			wchannel.subscribe("wins");
			wchannel.addListener(new com.xrtb.jmq.MessageListener<WinObject>() {
				@Override
				public void onMessage(String channel, WinObject win) {;
					price.add(new Double(win.price));
					price.add(new Double(win.cost));
					wlatch.countDown();
				}
			}); 
			
			/**
			 * Send the bid request
			 */
			try {
				s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", s, 3000000, 3000000);
			} catch (Exception error) {
				fail("Can't connect to test host: " + Config.testHost);
			}
			int code = http.getResponseCode();
			assertTrue(code==200);
			Bid bid = null;
			System.out.println(s);
			
			long time = 5;
			latch.await(time,TimeUnit.SECONDS);
			assertTrue(price.get(0) == 1.1);
			try {
				bid = new Bid(s);
			} catch (Exception error) {
				error.printStackTrace();
				fail();
			}
			assertTrue(bid.price == 1.1);
		
			
			/**
			 * Send the win notification
			 */
			try {

				price.clear();
				String repl = bid.nurl.replaceAll("\\$", "");
				bid.nurl = repl.replace("{AUCTION_PRICE}", Double.toString(bid.price));
				
				s = http.sendPost(bid.nurl, "",300000,300000);
			} catch (Exception error) {
				error.printStackTrace();
				fail();
			}
			assertTrue(s.length() > 10);
			wlatch.await(time,TimeUnit.SECONDS);
			assertTrue(price.get(0) == 1.1);
			System.out.println("xxxxxx: " + price.get(1));
			assertTrue(price.get(1) == 1.1);
		}
}
