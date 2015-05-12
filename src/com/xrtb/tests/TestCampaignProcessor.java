package com.xrtb.tests;

import java.io.InputStream;

import junit.framework.TestCase;

import org.junit.BeforeClass;
import org.junit.Test;

import com.xrtb.bidder.CampaignProcessor;
import com.xrtb.bidder.CampaignSelector;
import com.xrtb.bidder.SelectedCreative;
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
	 * Test the situation where no campaigns are loaded in the system.
	 * @throws Exception when the bid JSON file fails to load or has a JSON error in it.
	 */
	@Test
	public void testNoCampaigns() throws Exception {
		InputStream is = Configuration.getInputStream("SampleBids/nexage.txt");
		BidRequest request = new BidRequest(is);
		
		CampaignProcessor proc = new CampaignProcessor(null,request);
		SelectedCreative resp = proc.call();
		assertNull(resp);
	} 
	
	/**
	 * Load a campaign and then use the bidder's campaign processor to make a bid response.
	 * @throws Exception if the config file or the sample bid file fails to load, or they contain JSON errors.
	 */
	@Test
	public void testOneMatching() throws Exception {
		InputStream is = Configuration.getInputStream("SampleBids/nexage.txt");
		BidRequest request = new BidRequest(is);
		Configuration cf = Configuration.getInstance();
		cf.clear();
		cf.initialize("Campaigns/payday.json");
		Campaign c = cf.campaignsList.get(0);
		
		CampaignProcessor proc = new CampaignProcessor(c,request);
		SelectedCreative resp = proc.call();
		assertNotNull(resp);
		assertTrue(resp.getCreative().w == 320.0);
	}
	
	/**
	 * Test the campaign processor with 2 campaigns that will match the bid. Then test each campaign is chosen at least some of the time.
	 * @throws Exception if the config or bid file fails to open or they have JSON errors within them.
	 */
	@Test
	public void testTwoMatchingCampaigns() throws Exception {
		InputStream is = Configuration.getInputStream("SampleBids/nexage.txt");
		BidRequest request = new BidRequest(is);
		Configuration cf = Configuration.getInstance();
		cf.clear();
		cf.initialize("Campaigns/payday.json");
		Campaign camp = Configuration.getInstance().campaignsList.get(0);
		assertTrue(camp.adId.equals("ben:payday"));
		Campaign newCampaign = camp.copy();
		assertTrue(camp.adId.equals("ben:payday"));
		newCampaign.adId = "xxx";
		cf.addCampaign(newCampaign);
		
		int x = 0, y = 0;
		for (int i=0;i<10;i++) {
			BidResponse resp = CampaignSelector.getInstance().get(request);
			if (resp.adid.equals("xxx"))
					x++;
			else
				if (resp.adid.equals("ben:payday"))
					y++;
			
			assertTrue(resp.width==320.0);
		}
		assertTrue(x+y == 10);
		assertTrue(x > 0 && y > 0);
	}
	
}