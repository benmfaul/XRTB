package com.xrtb.tests;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xrtb.bidder.CampaignProcessor;
import com.xrtb.bidder.SelectedCreative;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.geo.GeoTag;
import com.xrtb.geo.Solution;
import com.xrtb.pojo.BidRequest;

import junit.framework.TestCase;

/**
 * A class to test the DeviceMapper extended device attributes of the user agent 
 * @author Ben M. Faul
 *
 */
public class TestExtendedDevice  {

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
	 * Test a bid request for the right display width
	 * @throws Exception on file errors
	 */
	
	@Test
	public void testCambridgeCity() throws Exception  {
		Configuration.getInstance().initialize("Campaigns/extendedDevice-test.json");
		BidRequest br = new BidRequest(Configuration.getInputStream("SampleBids/nexage.txt"));
		assertNotNull(br);
		Campaign c = Configuration.getInstance().campaignsList.get(0);
		assertNotNull(c);
		
		CampaignProcessor proc = new CampaignProcessor(c,br);
		SelectedCreative test = proc.call();
		assertNotNull(test);

	}
}
