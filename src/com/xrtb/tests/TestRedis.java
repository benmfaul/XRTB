package com.xrtb.tests;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import com.google.gson.Gson;
import com.xrtb.bidder.Controller;
import com.xrtb.commands.DeleteCampaign;
import com.xrtb.commands.Echo;
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
	static Jedis sub;
	static Jedis log;
	static Jedis pub;
	static ResponseLoop loop;
	static ResponseLoop logLoop;
	public static String test = "";
	static Gson gson = new Gson();

	@BeforeClass
	public static void testSetup() {
		try {

			Config.setup();

			sub = new Jedis("localhost"); // sub
			sub.connect();
			log = new Jedis("localhost"); // sub
			sub.connect();
			pub = new Jedis("localhost");
			pub.connect();

			loop = new ResponseLoop(sub, Controller.RESPONSES);
			logLoop = new ResponseLoop(log,
					Configuration.getInstance().LOG_CHANNEL);

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
	 */
	@Test
	public void testEcho() {
		loop.msg = null;
		Echo e = new Echo();
	//	e.to = "Hello";
	//	e.id = "MyId";
		String str = e.toString();
		pub.publish(Controller.COMMANDS, str);
		try {
			Thread.sleep(2000);
			assertNotNull(loop.msg);
			
			System.out.println(loop.msg);

			Echo x = (Echo) gson.fromJson(loop.msg, Echo.class);
			assertNotNull(x);

			assertTrue(x.id.equals("MyId"));
			assertTrue(x.to.equals("Hello"));
			assertTrue(x.from.equals("this-systems-instance-name-here"));

			assertEquals(x.campaigns.size(), 1);

		} catch (Exception error) {
			// TODO Auto-generated catch block
			error.printStackTrace();
		}
	}

	/**
	 * Test adding a campaign
	 */
	// @Test
	public void addCampaign() {

	}

	/**
	 * Test deleting a campaign
	 */
	@Test
	public void deleteCampaign() {
		loop.msg = null;
		DeleteCampaign e = new DeleteCampaign("id123");
		e.to = "Hello";
		e.id = "MyId";
		String str = e.toString();
		pub.publish(Controller.COMMANDS, str);
		try {
			Thread.sleep(2000);
			assertNotNull(loop.msg);

			DeleteCampaign x = (DeleteCampaign) gson.fromJson(loop.msg,
					DeleteCampaign.class);
			assertNotNull(x);

			assertTrue(x.id.equals("MyId"));
			assertTrue(x.to.equals("Hello"));
			assertTrue(x.status.equals("ok"));
			assertTrue(x.from.equals("this-systems-instance-name-here"));

		} catch (Exception error) {
			// TODO Auto-generated catch block
			error.printStackTrace();
		}
	}

	/**
	 * Test starting and stopping the rtb bidder engine.
	 */
	@Test
	public void stopStartBidder() {
		loop.msg = null;
		StopBidder e = new StopBidder();
		e.to = "Hello";
		e.id = "MyId";
		String str = e.toString();
		pub.publish(Controller.COMMANDS, str);
		try {
			Thread.sleep(2000);
			assertNotNull(loop.msg);
			Map m = gson.fromJson(loop.msg, Map.class);
			Boolean stopped = (Boolean) m.get("stopped");
			assertTrue(stopped);

			// Now make a bid
			HttpPostGet http = new HttpPostGet();
			String s = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/nexage.txt")))).toString();
			long time = 0;

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
			str = ee.toString();
			pub.publish(Controller.COMMANDS, str);
			Thread.sleep(2000);
			assertNotNull(loop.msg);
			m = gson.fromJson(loop.msg, Map.class);
			stopped = (Boolean) m.get("stopped");
			assertFalse(stopped);

			// Make a bid now
			try {
				str = http.sendPost("http://" + Config.testHost
						+ "/rtb/bids/nexage", s);
			} catch (Exception error) {
				fail("Network error");
			}
			assertNotNull(str);
			assertTrue(http.getResponseCode() == 200);

		} catch (Exception error) {
			error.printStackTrace();
			fail();
		}
	}

	/**
	 * Test the logging function
	 */
	@Test
	public void testLog() {
		try {
			Controller c = Controller.getInstance();
			c.sendLog(0, "this is a test");
			Thread.sleep(5000);

			assertNotNull(logLoop.msg);
			assertTrue(logLoop.msg.contains("this is a test"));
			Map m = null;

			m = gson.fromJson(logLoop.msg, Map.class);
		} catch (Exception error) {
			System.err.println("BAD DATA: '" + logLoop.msg + "'");
			error.printStackTrace();
			fail(error.toString());
		}
	}
}

/**
 * A subscriber class that waits for input.
 * 
 * @author Ben M. Faul
 *
 */
class ResponseLoop extends JedisPubSub implements Runnable {
	/** The class thread */
	Thread me;
	/** The JEDIS connection that will be used */
	Jedis conn;
	/** The redis topic we are reading from */
	String topic;
	/** The message received is contained in this string */
	volatile String msg;

	/**
	 * Construct a redis subscribe loop class.
	 * 
	 * @param conn Jedis. The connection to REDIS.
	 * @param topic String. The topic name we are subscribing to.
	 */
	public ResponseLoop(Jedis conn, String topic) {
		this.conn = conn;
		this.topic = topic;
		me = new Thread(this);
		me.start();
	}

	/**
	 * Calls the subscribe, never returns
	 */
	public void run() {
		conn.subscribe(this, topic);
	}

	/**
	 * Record the response from REDIS
	 */
	@Override
	public void onMessage(String arg0, String arg1) {
		msg = arg1;
	}

	@Override
	public void onPMessage(String arg0, String arg1, String arg2) {

	}

	@Override
	public void onPSubscribe(String arg0, int arg1) {

	}

	@Override
	public void onPUnsubscribe(String arg0, int arg1) {

	}

	@Override
	public void onSubscribe(String arg0, int arg1) {

	}

	@Override
	public void onUnsubscribe(String arg0, int arg1) {

	}
}
