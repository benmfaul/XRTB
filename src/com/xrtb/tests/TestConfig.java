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
	
	/**
	 * Test making the bidder configuration from file.
	 * @throws Exception. Throws exceptions on JSON errors.
	 */
	@Test
	public void makeFromFile() throws Exception    {
		Configuration c = Configuration.getInstance();
		c.clear();
		c.initialize("Campaigns/payday.json");
		assertTrue(c.instanceName.equals("this-systems-instance-name-here"));
		assertEquals(1,c.campaignsList.size());
		assertEquals(5,c.seats.size());
		assertNotNull(c.BIDS_CHANNEL);
		assertNotNull(c.WINS_CHANNEL);
		assertNotNull(c.REQUEST_CHANNEL);
		assertNotNull(c.LOG_CHANNEL);
		
		assertTrue(c.LOG_CHANNEL.equals("log"));
	}
}
