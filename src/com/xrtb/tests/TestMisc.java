package com.xrtb.tests;

import org.junit.Test;

import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;

import junit.framework.TestCase;

public class TestMisc extends TestCase {

	@Test
	public void testAssortedMethods() {
		String test = "site_id and then some text and site_id and some more text and finally site_id";
		
		StringBuffer sb = new StringBuffer(test);
		
		sb = BidResponse.replaceAll(sb,null,"XXX");
		assertTrue(sb.toString().equals(test));
		
		sb = BidResponse.replaceAll(sb,"XXX",null);
		assertTrue(sb.toString().equals(test));
		
		sb = BidResponse.replaceAll(sb,"site_id","XXX");
		test = sb.toString();
		assertFalse(test.contains("site_id"));
	}
}
