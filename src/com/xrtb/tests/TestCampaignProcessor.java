package com.xrtb.tests;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import junit.framework.TestCase;

import org.junit.Test;

import com.xrtb.bidder.CampaignProcessor;
import com.xrtb.bidder.CampaignSelector;
import com.xrtb.bidder.RTBServer;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;

/**
 * Test campaign processing.
 * @author Ben M. Faul
 *
 */
public class TestCampaignProcessor extends TestCase {

	/**
	 * Test the situation where no campaigns are loaded in the system.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testNoCampaigns() throws Exception {
		InputStream is = Configuration.getInputStream("SampleBids/nexage.txt");
		BidRequest request = new BidRequest(is);
		
		CampaignProcessor proc = new CampaignProcessor(null,request);
		BidResponse resp = proc.call();
		assertNull(resp);
	} 
	
	@Test
	public void testOneMatching() throws Exception {
		InputStream is = Configuration.getInputStream("SampleBids/nexage.txt");
		BidRequest request = new BidRequest(is);
		Configuration.getInstance().initialize("Campaigns/payday.json");
		Configuration cf = Configuration.getInstance();
	
		Campaign c = cf.campaignsList.get(0);
		
		CampaignProcessor proc = new CampaignProcessor(c,request);
		BidResponse resp = proc.call();
		assertNotNull(resp);
		assertTrue(resp.width == 320.0);
	}
	
	@Test
	public void testTwoMatchingCampaigns() throws Exception {
		InputStream is = Configuration.getInputStream("SampleBids/nexage.txt");
		BidRequest request = new BidRequest(is);
		Configuration.getInstance().initialize("Campaigns/payday.json");
		Campaign camp = Configuration.getInstance().campaignsList.get(0);
		assertTrue(camp.adId.equals("id123"));
		Campaign newCampaign = camp.copy();
		assertTrue(camp.adId.equals("id123"));
		newCampaign.adId = "xxx";
		Configuration.getInstance().addCampaign(newCampaign);
		
		int x = 0, y = 0;
		for (int i=0;i<10;i++) {
			BidResponse resp = CampaignSelector.getInstance().get(request);
			if (resp.adid.equals("xxx"))
					x++;
			else
				if (resp.adid.equals("id123"))
					y++;
			
			assertTrue(resp.width==320.0);
		}
		assertTrue(x+y == 10);
		assertTrue(x > 0 && y > 0);
	}
	
}