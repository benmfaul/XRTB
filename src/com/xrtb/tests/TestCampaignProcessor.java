package com.xrtb.tests;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import junit.framework.TestCase;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xrtb.bidder.AbortableCountDownLatch;
import com.xrtb.bidder.CampaignProcessor;
import com.xrtb.bidder.CampaignSelector;
import com.xrtb.bidder.SelectedCreative;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.common.Creative;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;

/**
 * Test campaign processing.
 * @author Ben M. Faul
 *
 */
public class TestCampaignProcessor  {

	@BeforeClass
	public static void setup() {
		try {
			Config.setup();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void stop() {
		Config.teardown();
	}
	
	@Test
	public void testTemplate() throws Exception {
		String str = Configuration.masterTemplate.get("nexage");
		assertNotNull(str);
		str = Configuration.masterTemplate.get("cappture");
		assertNotNull(str);
		assertTrue(str.indexOf("cappture") != -1);
	}
	
	/**
	 * Test the situation where no campaigns are loaded in the system.
	 * @throws Exception when the bid JSON file fails to load or has a JSON error in it.
	 */
	@Test
	public void testNoCampaigns() throws Exception {
		InputStream is = Configuration.getInputStream("SampleBids/nexage.txt");
		BidRequest request = new BidRequest(is);
		
		AbortableCountDownLatch latch = new AbortableCountDownLatch(1,1);
		CountDownLatch flag = new CountDownLatch(1);
		CampaignProcessor proc = new CampaignProcessor(null,request,flag,latch);
		SelectedCreative resp = proc.getSelectedCreative();
		flag.countDown();
		try {
			latch.await();
			fail("This latch should have aborted");
		} catch (Exception e) {
		    	
		}
		assertNull(resp);
	} 
	
	/**
	 * Load a campaign and then use the bidder's campaign processor to make a bid response.
	 * @throws Exception if the config file or the sample bid file fails to load, or they contain JSON errors.
	 */
//	@Test
	public void testOneMatching() throws Exception {
		InputStream is = Configuration.getInputStream("SampleBids/nexage.txt");
		BidRequest request = new BidRequest(is);
		Configuration cf = Configuration.getInstance();
		cf.clear();
		cf.initialize("Campaigns/payday.json");
		Campaign c = cf.campaignsList.get(0);
		
		AbortableCountDownLatch latch = new AbortableCountDownLatch(1,1);
		CountDownLatch flag = new CountDownLatch(1);
		CampaignProcessor proc = new CampaignProcessor(c,request,  flag, latch);
		flag.countDown();
		latch.await();
		SelectedCreative resp = proc.getSelectedCreative();
		assertNotNull(resp);
		assertTrue(resp.getCreative().w == 320.0);
	}
	
	@Test
	public void testJavascriptCreative() throws Exception {
		
		Configuration cf = Configuration.getInstance();
		cf.clear();
		cf.initialize("Campaigns/payday.json");
		for (Campaign c : cf.campaignsList) {
			for (Creative cc : c.creatives) {
				if (cc.impid.equals("iamrichmedia")) {
					System.out.println(cc.forwardurl);
					assertTrue((cc.forwardurl.contains("\\")));
					return;
				}
			}
		}
	}
	

}