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
			server = new RTBServer("./Campaigns/payday.json");
			int wait = 0;
			while(!server.isReady() && wait < 10) {
				Thread.sleep(1000);
				wait++;
			}
			if (wait == 10) {
				fail("Server never started");
			}
			Thread.sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}
	
	public static void setup(String shard, int port) throws Exception {
		try {
			server = new RTBServer("./Campaigns/payday.json", shard, port);
			int wait = 0;
			while(!server.isReady() && wait < 10) {
				Thread.sleep(1000);
				wait++;
			}
			if (wait == 10) {
				fail("Server never started");
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	public static void teardown() {
		if (server != null) {
			server.halt();
		}
	}
}
