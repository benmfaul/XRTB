package com.xrtb.tests;


import static org.junit.Assert.*;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.xrtb.common.Configuration;
import com.xrtb.pojo.Bid;
import com.xrtb.pojo.NoBid;

public class TestConfig {
	
	@BeforeClass
	  public static void testSetup() {
		
	  }

	  @AfterClass
	  public static void testCleanup() {
	    // Teardown for data used by the unit tests
	  }
	@Test 
	public void makeSimpleCampaign() {
		
	}
	
	@Test
	public void makeFromFile() throws Exception    {
		Configuration c = Configuration.getInstance();
		c.clear();
		c.initialize("Campaigns/payday.json");
		assertTrue(c.instanceName.equals("Sample payday loan campaigns"));
		assertEquals(1,c.campaigns.size());
		assertEquals(5,c.seats.size());
		assertNotNull(c.BIDS_CHANNEL);
		assertNotNull(c.WINS_CHANNEL);
		assertNotNull(c.REQUEST_CHANNEL);
		assertNotNull(c.LOG_CHANNEL);
		
		assertTrue(c.LOG_CHANNEL.equals("log"));
	}
}
