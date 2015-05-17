package com.xrtb.tests;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redisson.Redisson;
import org.redisson.core.MessageListener;
import org.redisson.core.RTopic;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import com.google.gson.Gson;
import com.xrtb.bidder.Controller;
import com.xrtb.commands.AddCampaign;
import com.xrtb.commands.BasicCommand;
import com.xrtb.commands.DeleteCampaign;
import com.xrtb.commands.Echo;
import com.xrtb.commands.LogMessage;
import com.xrtb.commands.StartBidder;
import com.xrtb.commands.StopBidder;
import com.xrtb.common.Configuration;
import com.xrtb.common.HttpPostGet;

/**
 * A class for testing all the redis functions, such as logging, recording bids,
 * etc.
 * 
 * @author Ben M. Faul
 *
 */

public class TestRedis {
	static Controller c;
	public static String test = "";
	static Gson gson = new Gson();
	static BasicCommand rcv = null;
	static Redisson redisson;
	static RTopic commands;

	@BeforeClass
	public static void testSetup() {
		try {

			Config.setup();
			
			org.redisson.Config cfg = new org.redisson.Config();
			cfg.useSingleServer()
	    	.setAddress("localhost:6379")
	    	.setConnectionPoolSize(10);
			redisson = Redisson.create(cfg);
			
			commands = redisson
					.getTopic(Controller.COMMANDS);
			RTopic channel = Configuration.getInstance().redisson
					.getTopic(Controller.RESPONSES);
			channel.addListener(new MessageListener<BasicCommand>() {
				@Override
				public void onMessage(BasicCommand cmd) {
					rcv = cmd;
				}
			}); 
		} catch (Exception error) {
			fail("No connection: " + error.toString());
		}
	}

	@AfterClass
	public static void testCleanup() {
		Config.teardown();
	}

	/**
	 * Test the echo/status message
	 * @throws Exception if the Controller is not complete.
	 */
	@Test
	public void testEcho() throws Exception  {
		Echo e = new Echo();
		String str = e.toString();
		e.to = "Hello";
		e.id = "MyId";
		rcv = null;
		commands.publish(e);
		
		while (rcv == null) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		assertTrue(rcv.cmd == 5);

	}

	/**
	 * Test adding a campaign
	 */
	//@Test
	public void addCampaign() {
		AddCampaign e = new AddCampaign("","ben:payday");
		e.to = "Hello";
		e.id = "MyId";
		rcv = null;
		commands.publish(e);
		while (rcv == null) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		assertTrue(rcv.id.equals("MyId"));
		assertTrue(rcv.to.equals("Hello"));
		assertTrue(rcv.status.equals("ok"));
		assertTrue(rcv.from.equals("this-systems-instance-name-here"));

	}

	/**
	 * Test deleting a campaign
	 */
	@Test
	public void deleteUnknownCampaign() {
		DeleteCampaign e = new DeleteCampaign(null,"id123");

		e.to = "Hello";
		e.id = "MyId";
		rcv = null;
		commands.publish(e);
		while (rcv == null) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Test starting and stopping the rtb bidder engine.
	 * @throws Exception on Redis errors.
	 */
	@Test
	public void stopStartBidder() throws Exception {
		StopBidder e = new StopBidder();
		e.to = "Hello";
		e.id = "MyId";
		rcv = null;
		commands.publish(e);
		while (rcv == null)
			Thread.sleep(1000);
		assertTrue(rcv.msg.equals("stopped"));

		// Now make a bid
		HttpPostGet http = new HttpPostGet();
		String s = Charset
				.defaultCharset()
				.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
						.get("./SampleBids/nexage.txt")))).toString();
		long time = 0;
		String str = null;
		try {
			str = http.sendPost("http://" + Config.testHost
					+ "/rtb/bids/nexage", s);
		} catch (Exception error) {
			fail("Network error");
		}
		assertNull(str);
		assertTrue(http.getResponseCode() == 204);
		str = http.getHeader("X-REASON");
		assertTrue(str.contains("Server stopped"));

		StartBidder ee = new StartBidder();
		ee.to = "Hello";
		ee.id = "MyId";

		rcv = null;
		commands.publish(ee);
		while (rcv == null)
			Thread.sleep(1);
		String test = rcv.msg;
		assertTrue(test.equals("running"));
	}

	/**
	 * Test the logging function
	 */
	// @Test
	public void testLog() {

	}
}
