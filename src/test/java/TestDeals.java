package test.java;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.xml.bind.DatatypeConverter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import com.xrtb.bidder.Controller;
import com.xrtb.common.Configuration;
import com.xrtb.common.HttpPostGet;
import com.xrtb.jmq.MessageListener;
import com.xrtb.jmq.RTopic;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;
import com.xrtb.tools.DbTools;

/**
 * A class for testing that the bid has the right parameters
 * 
 * @author Ben M. Faul
 *
 */
public class TestDeals {
	static Controller c;
	public static String test = "";

	static BidResponse response;
	static CountDownLatch latch;

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
	 * Test a private auction with a deal not present in the campaigns.
	 * @throws Exception
	 */
	@Test
	public void testPrivateBidNoResponse() throws Exception {
		HttpPostGet http = new HttpPostGet();
		String bid = Charset.defaultCharset()
				.decode(ByteBuffer.wrap(Files.readAllBytes(Paths.get("./SampleBids/nexagePrivateAuction.txt")))).toString();
		String s = null;
		long time = 0;
	
		String xtime = null;
		try {
			time = System.currentTimeMillis();
			s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", bid, 300000, 300000);
			time = System.currentTimeMillis() - time;
			xtime = http.getHeader("X-TIME");
		} catch (Exception error) {
			fail("Can't connect to test host: " + Config.testHost);
		}
		assertNull(s);
		int code = http.getResponseCode();
		assertEquals(code,204);
	}
	
	/**
	 * Test a private auction with a deal not present in the campaigns.
	 * @throws Exception
	 */
	@Test
	public void testPreferredOkToBid() throws Exception {
		HttpPostGet http = new HttpPostGet();
		String bid = Charset.defaultCharset()
				.decode(ByteBuffer.wrap(Files.readAllBytes(Paths.get("./SampleBids/nexagePreferredAuction.txt")))).toString();
		String s = null;
		long time = 0;
	
		String xtime = null;
		try {
			time = System.currentTimeMillis();
			s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", bid, 300000, 300000);
			time = System.currentTimeMillis() - time;
			xtime = http.getHeader("X-TIME");
		} catch (Exception error) {
			fail("Can't connect to test host: " + Config.testHost);
		}
		/**
		 * Any bid will do
		 */
		assertNotNull(s);
		int code = http.getResponseCode();
		assertEquals(code,200);
	}
	
	@Test
	public void testPrivateResponse() throws Exception {
		HttpPostGet http = new HttpPostGet();
		String bid = Charset.defaultCharset()
				.decode(ByteBuffer.wrap(Files.readAllBytes(Paths.get("./SampleBids/nexagePrivateAuction1.txt")))).toString();
		String s = null;
		long time = 0;
	
		String xtime = null;
		try {
			time = System.currentTimeMillis();
			s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", bid, 300000, 300000);
			time = System.currentTimeMillis() - time;
			xtime = http.getHeader("X-TIME");
		} catch (Exception error) {
			fail("Can't connect to test host: " + Config.testHost);
		}
		assertTrue(s.contains("ThisShouldBid"));
		assertNotNull(s);
		int code = http.getResponseCode();
		assertEquals(code,200);
	}
}
