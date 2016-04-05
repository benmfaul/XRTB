package com.xrtb.tests;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xrtb.bidder.AbortableCountDownLatch;
import com.xrtb.bidder.CampaignProcessor;
import com.xrtb.bidder.Controller;
import com.xrtb.bidder.SelectedCreative;
import com.xrtb.bidder.WebCampaign;
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
	//	Campaign camp =  WebCampaign.getInstance().db.getCampaign("ben","ben:extended-device");
	//	assertNotNull(camp);
	//	Controller.getInstance().addCampaign(camp);
		
		BidRequest br = new BidRequest(Configuration.getInputStream("SampleBids/nexage.txt"));
		assertNotNull(br);
		Campaign c = Configuration.getInstance().campaignsList.get(0);
		assertNotNull(c);
		
		AbortableCountDownLatch latch = new AbortableCountDownLatch(1,1);
		CountDownLatch flag = new CountDownLatch(1);
		CampaignProcessor proc = new CampaignProcessor(c,br, flag, latch);
		flag.countDown();
		latch.await();
		SelectedCreative test = proc.getSelectedCreative();
		assertNotNull(test);

	}
}
