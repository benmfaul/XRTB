package com.xrtb.tests;

import static org.junit.Assert.*;


import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.xrtb.bidder.CampaignSelector;
import com.xrtb.bidder.RTBServer;
import com.xrtb.common.Campaign;
import com.xrtb.common.HttpPostGet;


/**
 * Test Bid request object and it's behavior
 * @author Ben M. Faul
 *
 */

public class TestBidRequests {
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
	 * Test a no bid response
	 */
	@Test 
	public void respondWithNoBid() {
		Gson gson = new Gson();
		HttpPostGet http = new HttpPostGet();
		
		try {
			CampaignSelector.getInstance().clear();	

			String s = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/nexage.txt")))).toString();
			
			try {
				s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", s);
			} catch (Exception error) {
				fail("Can't connect to test host: " + Config.testHost);
			}
			int code = http.getResponseCode();
			assertTrue(code==204);
			assertNull(s);
			s = http.getHeader("X-REASON");
			assertTrue(s.equals("No campaigns loaded"));

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}


	/**
	 * Test a bid with no campaigns loaded to make sure the reason field is the correct message.
	 */
	@Test
	public void nobidWithReasonWithNoCamps()   {
		Gson gson = new Gson();
		HttpPostGet http = new HttpPostGet();
	
		try {
			CampaignSelector.getInstance().clear();
			String s = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/nexage.txt")))).toString();
			try {
				s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", s);
			} catch (Exception error) {
				fail("Can't connect to test host: " + Config.testHost);
			}
			assertNull(s);
			s = http.getHeader("X-REASON");
			assertTrue(s.equals("No campaigns loaded"));
			assertTrue(http.getResponseCode()==204);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}  
	
	
	/**
	 * Load a campaign, then send a bid that doesn't match, and check that it no bid.
	 * @throws Exception om network errors.
	 */
	@Test
	public void nobidWithReasonWithNoCampMatch() throws Exception  {
		Gson gson = new Gson();
		CampaignSelector.getInstance().clear();
		CampaignSelector.getInstance().add(new Campaign());
		HttpPostGet http = new HttpPostGet();
	
		try {
			String s = "";
			
			try {
				s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", "{\"id\":\"123\"}");
			} catch (Exception error) {
				fail("Can't connect to the test host: " + Config.testHost);
			}
			assertNull(s);
			s = http.getHeader("X-REASON");
			assertTrue(s.equals("No matching campaign"));
			assertTrue(http.getResponseCode()==204);
		} catch (Exception e) {
			e.printStackTrace();
		}
	} 
	
	/**
	 * Send nonesense to the bidder, will cause the bidder to not bid and send an x-reason header
	 * @throws Exception on network errors.
	 */
	@Test
	public void sendCrapRequest() throws Exception {
		CampaignSelector.getInstance().clear();
		HttpPostGet http = new HttpPostGet();
	
		try {
			String s = "";
			RTBServer.percentage = 100;
			RTBServer.nobid = 0;
			try {
				s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", "this is junk");
			} catch (Exception error) {
				fail("Can't connect to test server: " + Config.testHost);
			}
			assertTrue(http.getResponseCode()==204);
			s = http.getHeader("X-REASON");
			assertNotNull(s);
			assertTrue(s.contains("org.codehaus.jackson.JsonParseException"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	} 
	
	/**
	 * Send a total garbage bid request target, test that it sends null.
	 * @throws Exception on network errors.
	 */
	@Test
	public void sendCrapTarget()  throws Exception {
		CampaignSelector.getInstance().clear();
		HttpPostGet http = new HttpPostGet();
	
		try {
			String s = "";
			RTBServer.percentage = 100;
			RTBServer.nobid = 0;
			try {
				s = http.sendPost("http://" + Config.testHost + "/rtb/bids/BAD", "this is junk");
			} catch (Exception error) {
				fail("Can't connect to test server: "  + Config.testHost);
			}
			assertNull(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}  
	

}
