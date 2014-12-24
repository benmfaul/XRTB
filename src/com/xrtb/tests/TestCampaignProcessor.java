package com.xrtb.tests;

import java.io.InputStream;


import org.junit.Test;

import com.xrtb.bidder.CampaignProcessor;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;

public class TestCampaignProcessor {
	static BidRequest request;

	@Test
	public void testNoCampaigns() throws Exception {

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
		System.out.println("-->"+resp.prettyPrint());
	}
	
	@Test
	public void testTwoMatchingCampaigns() {
		
	}
	
}