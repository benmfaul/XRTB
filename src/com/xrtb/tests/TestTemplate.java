package com.xrtb.tests;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;



import com.xrtb.bidder.RTBServer;
import com.xrtb.common.Configuration;

public class TestTemplate {
	static RTBServer server;
	@BeforeClass
	public static void testSetup() {
		try {
			Configuration c = Configuration.getInstance();
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
}
