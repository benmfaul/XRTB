package com.xrtb.tests;

import static org.junit.Assert.fail;

import com.xrtb.bidder.RTBServer;
import com.xrtb.common.Configuration;

/**
 * The JUNIT common configuration is done here.
 * 
 * @author Ben M. Faul
 *
 */
public class Config {
	/** The hostname the test programs will use for the RTB bidder */
	public static final String testHost = "localhost:8080";
	/** The RTBServer object used in the tests. */
	static RTBServer server;

	public static void setup() throws Exception {
		try {
			if (server == null)
			server = new RTBServer("./Campaigns/payday.json");
			Thread.sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	public static void teardown() {
		//if (server != null) 
		; //	server.halt();
	}
}
