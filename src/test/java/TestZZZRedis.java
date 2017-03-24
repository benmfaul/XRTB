package test.java;

import static org.junit.Assert.*;




import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.xrtb.bidder.Controller;
import com.xrtb.bidder.ZPublisher;
import com.xrtb.commands.AddCampaign;
import com.xrtb.commands.BasicCommand;
import com.xrtb.commands.DeleteCampaign;
import com.xrtb.commands.DeleteCreative;
import com.xrtb.commands.Echo;
import com.xrtb.commands.LogLevel;
import com.xrtb.commands.StartBidder;
import com.xrtb.commands.StopBidder;
import com.xrtb.common.HttpPostGet;

/**
 * A class for testing all the redis functions, such as logging, recording bids,
 * etc.
 * 
 * @author Ben M. Faul
 *
 */

public class TestZZZRedis {
	static Controller c;
	public static String test = "";
	static BasicCommand rcv = null;
	static ZPublisher commands;
	
	static CountDownLatch latch;

	@BeforeClass
	public static void testSetup() {
		try {

			Config.setup();
			System.out.println("******************  TestZZZRedis");
			
			com.xrtb.jmq.RTopic channel = new com.xrtb.jmq.RTopic("tcp://*:5575");
			channel.subscribe("responses");
			channel.addListener(new com.xrtb.jmq.MessageListener<BasicCommand>() {
				@Override
				public void onMessage(String channel, BasicCommand cmd) {
					System.out.println("<<<<<<<<<<<<<<<<<" + cmd);
					rcv = cmd;
					latch.countDown();
				}
			}); 
			
			commands = new ZPublisher("tcp://*:5580","commands");
		} catch (Exception error) {
			error.printStackTrace();
			fail("No connection: " + error.toString());
		}
	}

	@AfterClass
	public static void testCleanup() {
		Config.teardown();
	}

	@Test
	public  void testStub() {
		
	}
	/**
	 * Test the echo/status message
	 * @throws Exception if the Controller is not complete.
	 */
	@Test
	public void testEcho() throws Exception  {
		Echo e = new Echo();
		String str = e.toString();
		e.id = "ECHO-ID";
		
		rcv = null;
		latch = new CountDownLatch(1);
		commands.add(e);
		latch.await(5, TimeUnit.SECONDS);
		assertTrue(rcv.cmd == 5);

	}
	
	/**
	 * Test the echo/status message
	 * @throws Exception if the Controller is not complete.
	 */
	@Test
	public void testSetLogLevel() throws Exception  {
		LogLevel e = new LogLevel("*","-3");	
		e.id = "SETLOG-ID";
		rcv = null;
		latch = new CountDownLatch(1);
		commands.add(e);
		latch.await(5,TimeUnit.SECONDS);
		Echo echo = (Echo)rcv;
		//System.out.println(echo.toString());
		assertEquals(echo.loglevel,-3);

	}

	/**
	 * Test adding a campaign
	 */
	@Test
	public void addCampaign() throws Exception {
		AddCampaign e = new AddCampaign("","ben","ben:payday");
		e.id = "ADDCAMP-ID";
		Thread.sleep(1000);
		rcv = null;
		latch = new CountDownLatch(1);
		commands.add(e);
		latch.await(5,TimeUnit.SECONDS);
		
		assertTrue(rcv.id.equals("ADDCAMP-ID"));
		assertTrue(rcv.status.equals("ok"));

	}

	/**
	 * Test deleting a campaign
	 */
	@Test
	public void deleteUnknownCampaign() throws Exception {
		DeleteCampaign e = new DeleteCampaign("","ben","id123");

		e.id = "DELETECAMP-ID";
		rcv = null;
		latch = new CountDownLatch(1);
		commands.add(e);
		latch.await(5,TimeUnit.SECONDS); 
	}

	/**
	 * Test starting and stopping the rtb bidder engine.
	 * @throws Exception on Redis errors.
	 */
	@Test
	public void stopStartBidder() throws Exception {
		StopBidder e = new StopBidder();
		e.id = "STOPBIDDER-ID";
		rcv = null;
		latch = new CountDownLatch(1);
		commands.add(e);
		latch.await(5,TimeUnit.SECONDS);
	
		System.out.println("------------>" + rcv);
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

		StartBidder ee = new StartBidder();
		ee.id = "STARTBIDDER-ID";

		latch = new CountDownLatch(1);
		commands.add(ee);
		latch.await(5, TimeUnit.SECONDS);
		time = System.currentTimeMillis();

		 test = rcv.msg;
		assertTrue(test.equals("running"));
	}
	
	@Test
	public void testRemoveCreative() throws Exception {
		DeleteCreative e = new DeleteCreative();
		Thread.sleep(1000);
		e.owner = "xxx";
		e.id = "DELETECREATIVE-ID";
		rcv = null;
		latch = new CountDownLatch(1);
		commands.add(e);
		latch.await(5,TimeUnit.SECONDS);
	
		if (rcv.msg.contains("No such campaign found") == false)
			fail("Expected 'No such campaign' but got: " + rcv.msg);
		
		Thread.sleep(1000);;
		e.owner = "ben";
		e.name = "ben:payday";
		e.target = "xxx";
		
		rcv = null;
		latch = new CountDownLatch(1);
		commands.add(e);
		latch.await(5,TimeUnit.SECONDS);
	
		assertTrue(rcv.msg.contains("No such creative found"));
		
		Thread.sleep(1000);
		e.target = "66skiddoo";
		
		rcv = null;
		latch = new CountDownLatch(1);
		commands.add(e);
		latch.await(5,TimeUnit.SECONDS);
	
		System.out.println("------------>" + rcv);
		assertTrue(rcv.msg.contains("succeeded"));
	}

	/**
	 * Test the logging function
	 */
	// @Test
	public void testLog() {

	}
}
