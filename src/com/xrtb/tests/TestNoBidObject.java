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
