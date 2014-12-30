package com.xrtb.tests;

import static org.junit.Assert.*;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.xrtb.pojo.NoBid;

/**
 * Test the no bid object
 * @author Ben Faul
 *
 */

public class TestNoBidObject {
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
	
	/** 
	 * Set a couple of attributes.
	 */
	@Test 
	public void nobidWithReasonWithString() {
		NoBid b = new NoBid();
		assertEquals(b.toString(),"{\"reason\":\"na\"}");
		b.setReason("AAA");
		assertEquals(b.toString(),"{\"reason\":\"AAA\"}");
	}
	
	
	/**
	 * Test the ID string.
	 */
	@Test
	public void noBidWithIdWithString() {
		NoBid b = new NoBid();
		assertEquals(b.toString(),"{\"reason\":\"na\"}");
		b.setId("123");
		assertTrue(b.toString().contains("\"id\":\"123\""));
	}
}
