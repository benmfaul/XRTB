package com.xrtb.tests;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
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

public class TestServer {
	/**
	 * Setup the RTB server for the test
	 */
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
	 * Shut the RTB server down.
	 */
	@AfterClass
	public static void testCleanup() {
		Config.teardown();
	}
	
	@Test
	public void testBid() throws Exception {
		Map m;
		HttpPostGet pg = new HttpPostGet();
		
		String s = Charset
				.defaultCharset()
				.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
						.get("./SampleBids/nexage.txt")))).toString();
	
		String read = pg.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", s);
		long time = System.currentTimeMillis();
		read = pg.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", s);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		m = gson.fromJson(read,Map.class);
		read = gson.toJson(m);
		
		s = (String)m.get("bidid");
		assertTrue(s.equals("35c22289-06e2-48e9-a0cd-94aeb79fab43"));
		s = (String)m.get("id");
		assertNotNull(s);
		List seats = (List)m.get("seatbid");
		Map seat = (Map)seats.get(0);
		List bids = (List)seat.get("bid");
		Map bid = (Map)bids.get(0);
		s = (String)bid.get("impid");
		assertTrue(s.contains("-skiddoo"));
		s = (String)bid.get("id");
		assertTrue(s.equals("35c22289-06e2-48e9-a0cd-94aeb79fab43"));
		Double d = (Double)bid.get("price");
		assertTrue(d==1.0);
		s = (String)bid.get("adid");
		assertTrue(s.equals("ben:payday"));
		s = (String)bid.get("cid");
		assertTrue(s.equals("ben:payday"));
		s = (String)bid.get("crid");
		List list = (List)bid.get("adomain");
		s = (String)list.get(0);
		assertTrue(s.equals("originator.com"));
		s = (String)bid.get("iurl");
		System.out.println(s);
		assertTrue(s.equals("http://localhost:8080/images/320x50.jpg?adid=ben:payday&bidid=35c22289-06e2-48e9-a0cd-94aeb79fab43"));
		//"\u003ctemplate here\u003e"
	}


}
