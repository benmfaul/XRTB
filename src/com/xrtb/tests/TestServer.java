package com.xrtb.tests;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xrtb.bidder.RTBServer;
import com.xrtb.common.Configuration;
import com.xrtb.common.HttpPostGet;
import com.xrtb.common.Utils;

public class TestServer {
	static RTBServer server;
	@BeforeClass
	public static void testSetup() {
		try {
			Configuration c = Configuration.getInstance();
			c.clear();
			c.initialize("Campaigns/payday.json");
			server = new RTBServer(c.port);
			Thread.sleep(5000);
		} catch (Exception e) {
			fail(e.toString());
		}
	}
	  @AfterClass
	  public static void testCleanup() {
	    if (server != null)
	    	server.halt();
	  }
	
	@Test
	public void testBid() throws Exception {
		Map m;
		HttpPostGet pg = new HttpPostGet();
		String s = Utils.readFile("SampleBids/nexage.txt");
	
		String read = pg.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", s);
		long time = System.currentTimeMillis();
		read = pg.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", s);
		System.out.println("--->" + (System.currentTimeMillis() - time));
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		m = gson.fromJson(read,Map.class);
		read = gson.toJson(m);
		System.out.println("--->" + read);
		
		s = (String)m.get("bidid");
		assertTrue(s.equals("35c22289-06e2-48e9-a0cd-94aeb79fab43"));
		s = (String)m.get("id");
		assertNotNull(s);
		List seats = (List)m.get("seatbid");
		Map seat = (Map)seats.get(0);
		List bids = (List)seat.get("bid");
		Map bid = (Map)bids.get(0);
		s = (String)bid.get("impid");
		assertTrue(s.equals("23skiddoo"));
		s = (String)bid.get("id");
		assertTrue(s.equals("35c22289-06e2-48e9-a0cd-94aeb79fab43"));
		Double d = (Double)bid.get("price");
		assertTrue(d==5.0);
		s = (String)bid.get("adid");
		assertTrue(s.equals("id123"));
		s = (String)bid.get("cid");
		assertTrue(s.equals("id123"));
		s = (String)bid.get("crid");
		assertTrue(s.equals("23skiddoo"));
		s = (String)bid.get("adomain");
		assertTrue(s.equals("originator.com"));
		s = (String)bid.get("iurl");
		assertTrue(s.equals("http://d21a3h018cqvjt.cloudfront.net/rtbiq/IQ_070913_320x50.gif?adid\u003d{adid}\u0026#38;bidid\u003d{oid}"));
		//"\u003ctemplate here\u003e"
	}


}
