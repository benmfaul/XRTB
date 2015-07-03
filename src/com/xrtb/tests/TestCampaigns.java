package com.xrtb.tests;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xrtb.bidder.CampaignSelector;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.exchanges.Nexage;
import com.xrtb.pojo.BidResponse;

/**
 * Tests for campaign selections.
 * @author Ben M. Faul
 *
 */
public class TestCampaigns {

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
	
	
	/**
	 * Test simple macro replace
	 */
	@Test
	public void testReplace() {
		String test = "price={price},item={item}";
		StringBuilder x = BidResponse.replace(new StringBuilder(test),"{price}","5");
		assertTrue(x.toString().contains("5"));
	}

	/**
	 * Test the campaign selector with a simple bid. Checks to see if the correct sized creative is chosen.
	 * @throws Exception of file errors or JSON parsing fails on the file.
	 */
	@Test
	public void testCampaign() throws Exception {
		Configuration.getInstance().initialize("./Campaigns/payday.json");
		Configuration cf = Configuration.getInstance();
	
		Campaign c = cf.campaignsList.get(0);
		File f = new File("./SampleBids/nexage.txt");
		FileInputStream fis = new FileInputStream(f);
		Nexage br = new Nexage(fis);
	
		/**
		 * First creative
		 */
		CampaignSelector select = CampaignSelector.getInstance();
		BidResponse response = select.get(br);
		assertNotNull(response);
		// This bid request is 320x50 which is 23skiddoo
		assertTrue(response.impid.equals("23skiddoo"));
		
		/**
		 * No bid
		 */
		br.w = new Double(50);
		br.h = new Double(50);
		response = select.get(br);
		// This bid request does not match a campaign, no bid
		assertNull(response);
	}
	
	/**
	 * Tests the bidder's ADM template processing.
	 * @throws Exception when the file fails to load, or there are JSON errors within it.
	 */
	@Test
	public void testTemplate() throws Exception {
		Configuration.getInstance().initialize("./Campaigns/payday.json");
		Configuration cf = Configuration.getInstance();
	
		Campaign c = cf.campaignsList.get(0);
		File f = new File("./SampleBids/nexage.txt");
		FileInputStream fis = new FileInputStream(f);
		Nexage br = new Nexage(fis);
	
		/**
		 * First creative
		 */
		CampaignSelector select = CampaignSelector.getInstance();
		BidResponse response = select.get(br);
		assertNotNull(response);
		// This bid request is 320x50 which is 23skiddoo
		assertTrue(response.impid.equals("23skiddoo"));

	}

	
}
