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
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;

public class TestCampaigns {
	static String fake;
	static BidRequest request;

	@BeforeClass
	public static void testSetup() {
		try {
			File f = new File("./SampleBids/nexage.txt");
			System.out.println(f.exists());
			FileInputStream fis = new FileInputStream(f);
			request = new BidRequest(fis);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void testCleanup() {

	}

	@Test
	public void testCampaign() throws Exception {
		Configuration.getInstance().initialize("./Campaigns/payday.json");
		Configuration cf = Configuration.getInstance();
	
		Campaign c = cf.campaignsList.get(0);
		File f = new File("./SampleBids/nexage.txt");
		FileInputStream fis = new FileInputStream(f);
		Nexage br = new Nexage(fis);
		CampaignSelector select = CampaignSelector.getInstance();
		BidResponse response = select.get(br);
		if (response != null) {
			System.out.println("Campaign will bid");
		} else
			System.out.println("Campaign will not bid");
		System.out.println(response.prettyPrint());
	}
	
	public void testCampaignAdm() throws Exception {
	}
}
