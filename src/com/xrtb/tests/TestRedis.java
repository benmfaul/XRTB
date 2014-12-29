package com.xrtb.tests;

import static org.junit.Assert.*;


import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.Map;

import com.google.gson.Gson;
import com.xrtb.bidder.CampaignSelector;
import com.xrtb.bidder.Controller;
import com.xrtb.bidder.RTBServer;
import com.xrtb.commands.DeleteCampaign;
import com.xrtb.commands.Echo;
import com.xrtb.common.Configuration;
import com.xrtb.common.HttpPostGet;
import com.xrtb.pojo.NoBid;

public class TestRedis  {
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
		sub = new Jedis("localhost");  // sub
		sub.connect();
		log = new Jedis("localhost");  // sub
		sub.connect();
		pub = new Jedis("localhost");
		pub.connect();

		
		Configuration config = Configuration.getInstance();
		config.clear();
		config.initialize("Campaigns/payday.json");
		
		loop = new ResponseLoop(sub,Controller.RESPONSES);
		logLoop = new ResponseLoop(log,Configuration.getInstance().LOG_CHANNEL);
		
		Config.setup();
		} catch (Exception error) {
			fail(error.toString());
		}
	  }

	  @AfterClass
	  public static void testCleanup() {
		  Config.teardown();
	  }
	  
	  
		@Test
		public void testEcho() {
			loop.msg = null;
			Echo e = new Echo();
			e.to = "Hello";
			e.id = "MyId";
			String str = e.toString();
			pub.publish(Controller.COMMANDS,str);
			try {
				Thread.sleep(2000);
				assertNotNull(loop.msg);

				Echo x = (Echo)gson.fromJson(loop.msg,Echo.class);
				assertNotNull(x);
				
				assertTrue(x.id.equals("MyId"));
				assertTrue(x.to.equals("Hello"));
				assertTrue(x.from.equals("this-systems-instance-name-here"));

				assertEquals(x.campaigns.size(),1);
				
			} catch (Exception error) {
				// TODO Auto-generated catch block
				error.printStackTrace();
			}
		}
	
	//@Test
	public void addCampaign() {

	}
	
	@Test
	public void deleteCampaign() {
		loop.msg = null;
		DeleteCampaign e = new DeleteCampaign("id123");
		e.to = "Hello";
		e.id = "MyId";
		String str = e.toString();
		pub.publish(Controller.COMMANDS,str);
		try {
			Thread.sleep(2000);
			assertNotNull(loop.msg);

			DeleteCampaign x = (DeleteCampaign)gson.fromJson(loop.msg,DeleteCampaign.class);
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
	
	//@Test
	public void startBidder() {

	}
	
	//@Test
	public void stopBidder() {

	}
	
	@Test 
	public void testLog() throws Exception {
		
		Controller c = Controller.getInstance();
		c.sendLog(0, "this is a test");
		Thread.sleep(2000);
		assertNotNull(logLoop.msg);
		assertTrue(logLoop.msg.contains("this is a test"));
		Map m = null;
		try {
			m = gson.fromJson(logLoop.msg, Map.class);
		} catch (Exception error) {
			System.err.println("BAD DATA: '" + logLoop.msg + "'");
			error.printStackTrace();
			fail(error.toString());
		}
	}
}

class ResponseLoop extends JedisPubSub implements Runnable {
	Thread me;
	Jedis conn;
	String topic;
	String msg;

	public ResponseLoop(Jedis conn, String topic) {
		this.conn = conn;
		this.topic = topic;
		me = new Thread(this);
		me.start();
	}

	public void run() {
		conn.subscribe(this, topic);
	}

	@Override
	public void onMessage(String arg0, String arg1) {
		System.out.println("A: " + arg0 + " = " + arg1);
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
