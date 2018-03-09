package test.java;

import com.xrtb.bidder.AbortableCountDownLatch;
import com.xrtb.bidder.CampaignProcessor;
import com.xrtb.bidder.SelectedCreative;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.pojo.BidRequest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

/**
 * Test campaign processing.
 * @author Ben M. Faul
 *
 */
public class TestCampaignProcessor {

	@BeforeClass
	public static void setup() {
		try {
			Config.setup();
			System.out.println("******************  TestCampaignProcessor");
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
		String str = Configuration.getInstance().masterTemplate.get("nexage");
		assertNotNull(str);
		str = Configuration.getInstance().masterTemplate.get("cappture");
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
		flag.countDown();
		proc.run();
		List<SelectedCreative> resp = proc.getSelectedCreative();
		// flag.countDown(); // back when proc was a thread
		try {
			latch.await();
			fail("This latch should have aborted");
		} catch (Exception e) {
		    	
		}
		assertTrue(resp.size()==0);
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
		Campaign c = cf.getCampaignsListReal().get(0);
		
		AbortableCountDownLatch latch = new AbortableCountDownLatch(1,1);
		CountDownLatch flag = new CountDownLatch(1);
		CampaignProcessor proc = new CampaignProcessor(c,request,  flag, latch);
		flag.countDown();
		latch.await();
		List<SelectedCreative> resp = proc.getSelectedCreative();
		assertNotNull(resp);
		assertTrue(resp.size()>0);
		assertTrue(resp.get(0).getCreative().dimensions.get(0).getLeftX() == 320);
	}
}