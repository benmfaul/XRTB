package test.java;

import static org.junit.Assert.*;


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
import com.xrtb.pojo.SmaatoTemplate;

import junit.framework.TestCase;

/**
 * A class for testing SMAATO Bids. Note, all tests are stubbed out. Enable the test with the annotation @Test after you enable the SMAATO test code
 * in RTBServer.java.
 * 
 * @author Ben M. Faul
 *
 */
public class TestSmaato {
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
	 * You need at least one test when all the others are stubbed out or this will cause a problem on the
	 * reports.
	 */
	@Test
	public void testStub() {
		
	}
	/**
	 * Issue a NO BID directive to the RTB
	 * @throws Exception on network errors.
	 */
	//@Test
	public void testNoBid() throws Exception {
		HttpPostGet http = new HttpPostGet();
		String s = Charset
				.defaultCharset()
				.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
						.get("./SampleBids/smaato.json")))).toString();
		String xtime = null;
		long time = 0;
		s = Charset
				.defaultCharset()
				.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
						.get("./SampleBids/smaato.json")))).toString();
		time = System.currentTimeMillis();
		s = http.sendPost("http://" + Config.testHost
				+ "/rtb/bids/smaato?testbid=nobid", s);
		time = System.currentTimeMillis() - time;
		xtime = http.getHeader("X-TIME");
		assertNull(s);
		assertTrue(http.getResponseCode()==204);
	}
	
	/**
	 * Yoy should not bid on a text ad or rich media ad
	 * @throws Exception on network errors.
	 */
	//@Test
	public void testNoBidOnTextAd() throws Exception {
		HttpPostGet http = new HttpPostGet();
		long time = 0;
		String s = Charset
				.defaultCharset()
				.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
						.get("./SampleBids/smaatoTXTAD.json")))).toString();
		time = System.currentTimeMillis();
		s = http.sendPost("http://" + Config.testHost
				+ "/rtb/bids/smaato?testbid=bid", s,100000,100000);
		time = System.currentTimeMillis() - time;
		String xtime = http.getHeader("X-TIME");
		assertNull(s);
		assertTrue(http.getResponseCode()==204);
	}
	
	/**
	 * Test the RTB will bid as ordered.
	 * @throws Exception on network errors
	 */
	//@Test
	public void testIntegrationid() throws Exception {
		HttpPostGet http = new HttpPostGet();
		String xtime = null;
		long time = 0;
		String s = Charset
				.defaultCharset()
				.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
						.get("./SampleBids/smaato.json")))).toString();
		time = System.currentTimeMillis();
		s = http.sendPost("http://" + Config.testHost
				+ "/rtb/bids/smaato?testbid=bid", s,100000,100000);
		time = System.currentTimeMillis() - time;
		xtime = http.getHeader("X-TIME");
		assertTrue(http.getResponseCode()!=204);
		assertNotNull(s);
	}
	
	/**
	 * Don't bid on richmedia (campaign is banner)
	 * @throws Exception on network errors.
	 */
	//@Test  RichMedia test file has wrong w X h
	public void testRichMedia() throws Exception {
		HttpPostGet http = new HttpPostGet();
		String xtime = null;
		long time = 0;
		String s = Charset
				.defaultCharset()
				.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
						.get("./SampleBids/smaatoRICHMEDIA.json")))).toString();
		time = System.currentTimeMillis();
		s = http.sendPost("http://" +  Config.testHost
				+ "/rtb/bids/smaato?testbid=bid", s,100000,100000);
		time = System.currentTimeMillis() - time;
		xtime = http.getHeader("X-TIME");
		assertTrue(http.getResponseCode()!=204);
		assertNotNull(s);
	}


	/**
	 * Test a valid bid response.
	 * 
	 * @throws Exception
	 *             on networking errors.
	 */
	//@Test
	public void testBannerRespondWithBid() throws Exception {
		HttpPostGet http = new HttpPostGet();
		String xtime = null;
		long time = 0;
		try {
			String s = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/smaato.json")))).toString();
			try {
				time = System.currentTimeMillis();
				s = http.sendPost("http://" + Config.testHost
						+ "/rtb/bids/smaato?testbid=bid", s);
				time = System.currentTimeMillis() - time;
				xtime = http.getHeader("X-TIME");
			} catch (Exception error) {
				fail("Can't connect to test host: " + Config.testHost);
			}
			gson = new GsonBuilder().setPrettyPrinting().create();

			Map m = null;
			try {
				m = gson.fromJson(s, Map.class);
				System.out.println(gson.toJson(m));
			} catch (Exception error) {
				System.out.println("\\n\n\n\n" + s + "\n\n\n\n");
				fail("Bad JSON for bid");
			}
			List list = (List) m.get("seatbid");
			m = (Map) list.get(0);
			assertNotNull(m);
			String test = (String) m.get("seat");
			assertTrue(test.equals("seat1"));
			list = (List) m.get("bid");
			assertEquals(list.size(), 1);
			m = (Map) list.get(0);
			assertNotNull(m);
			test = (String) m.get("impid");
			assertTrue(test.equals("image-test"));
			test = (String) m.get("id");
			assertTrue(test.equals("K6t8sXXYdM"));
			double d = (Double) m.get("price");
			assertTrue(d == 1.0);

			test = (String) m.get("adid");

			assertTrue(test.equals("smaato-test"));

			list = (List) m.get("adomain");
			test = (String) list.get(0);
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

			System.out.println("\n\n\n" + SmaatoTemplate.IMAGEAD_TEMPLATE);

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());

		}
	}
}
