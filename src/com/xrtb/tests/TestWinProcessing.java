package com.xrtb.tests;

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
	}

	/**
	 * Test the basic win processing system of the RTB
	 * @throws Exception. Throws exception on file or network errors.
	 */
	@Test
	public void testWinProcessing() throws Exception  {
		HttpPostGet http = new HttpPostGet();
		Jedis cache = new Jedis("localhost");
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
		assertTrue(price.equals("5.0"));
		
		/**
		 * Send the win notification
		 */
		try {
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

		Document doc = db.parse(is);
		
		// Check to see the bid was removed from the cache
		m = cache.hgetAll(bid.id);
		assertTrue(m.isEmpty());
		
	}
}
