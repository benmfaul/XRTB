package com.xrtb.tests;


import static org.junit.Assert.*;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xrtb.common.Configuration;

/**
 * Tests the Configurastion file handling.
 * @author Ben M. Faul
 *
 */
public class TestConfig {
	
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
	 * Test making the bidder configuration from file
	 * @throws Exception on config file errors or JSON parsing of the file.
	 */
	@Test
	public void makeFromFile() throws Exception    {
		Configuration c = Configuration.getInstance();
		c.clear();
		c.initialize("Campaigns/payday.json");
		assertTrue(c.instanceName.equals("this-systems-instance-name-here"));
		assertEquals(1,c.campaignsList.size());
		assertEquals(2,c.seats.size());
		assertNotNull(c.BIDS_CHANNEL);
		assertNotNull(c.WINS_CHANNEL);
		assertNotNull(c.REQUEST_CHANNEL);
		assertNotNull(c.LOG_CHANNEL);
		
		assertTrue(c.LOG_CHANNEL.equals("log"));
	}
}
