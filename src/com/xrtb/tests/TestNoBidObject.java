package com.xrtb.tests;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xrtb.bidder.RTBServer;
import com.xrtb.common.Configuration;
import com.xrtb.pojo.NoBid;

public class TestNoBidObject {
	static RTBServer server;
	@BeforeClass
	public static void setup() {
		Configuration c = Configuration.getInstance();
		try {
			c.clear();
			c.initialize("./Campaigns/payday.json");
			server = new RTBServer();
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
	public void nobidWithReasonWithString() {
		NoBid b = new NoBid();
		assertEquals(b.toString(),"{\"reason\":\"na\"}");
		b.setReason("AAA");
		assertEquals(b.toString(),"{\"reason\":\"AAA\"}");
	}
	
	
	@Test
	public void noBidWithIdWithString() {
		NoBid b = new NoBid();
		assertEquals(b.toString(),"{\"reason\":\"na\"}");
		b.setId("123");
		assertTrue(b.toString().contains("\"id\":\"123\""));
	}
}
