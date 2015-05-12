package com.xrtb.tests;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xrtb.pojo.BidResponse;

import junit.framework.TestCase;

/**
 * Tests miscellaneous classes.
 * @author Ben M. Faul
 *
 */

public class TestMisc {

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
	 * Test the string replace functions used for macro substitutions.
	 */
	@Test
	public void testAssortedMethods() {
		String test = "site_id and then some text and site_id and some more text and finally site_id";
		
		StringBuilder sb = new StringBuilder(test);
		
		sb = BidResponse.replaceAll(sb,null,"XXX");
		assertTrue(sb.toString().equals(test));
		
		sb = BidResponse.replaceAll(sb,"XXX",null);
		assertTrue(sb.toString().equals(test));
		
		sb = BidResponse.replaceAll(sb,"site_id","XXX");
		test = sb.toString();
		assertFalse(test.contains("site_id"));
	}
}
