package test.java;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import redis.clients.jedis.Jedis;

import com.xrtb.bidder.DeadmanSwitch;
import com.xrtb.common.Configuration;

public class TestDeadmanSwitch {

	/**
	 * Setup the RTB server for the test
	 */
	@BeforeClass
	public static void setup() {
		try {
			Config.setup();
			System.out.println("******************  TestDeadmanSwitch");
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
		System.out.println("We are done!");
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test 
	public void testSwitch() throws Exception {
			Jedis redis = new Jedis("localhost");
			redis.connect();
			redis.auth(Config.password);

			DeadmanSwitch.testmode = true;
			
			if (Configuration.getInstance().password != null)
				redis.auth(Configuration.getInstance().password);
			
			redis.del("deadmanswitch");
			
			DeadmanSwitch d = new DeadmanSwitch("localhost",6379, Config.password, "deadmanswitch");
			Thread.sleep(1000);
			assertFalse(d.canRun());
			redis.set("deadmanswitch", "ready");
			redis.expire("deadmanswitch", 5);
			
			assertTrue(d.canRun());
			Thread.sleep(10000);
			assertFalse(d.canRun());
		}
}
