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
import com.xrtb.common.Utils;
import com.xrtb.pojo.NoBid;

public class TestRedis  {
	static Controller c;
	static Jedis sub;
	static Jedis pub;
	static ResponseLoop loop;
	public static String test = "";
	static RTBServer server;
	static Gson gson = new Gson();
	@BeforeClass
	  public static void testSetup() {		
		try {
		sub = new Jedis("localhost");  // sub
		sub.connect();
		pub = new Jedis("localhost");
		pub.connect();


		loop = new ResponseLoop(sub);
		
		Configuration config = Configuration.getInstance();
		config.initialize("Campaigns/payday.json");
		server = new RTBServer();
		Thread.sleep(5000);
		} catch (Exception error) {
			fail(error.toString());
		}
	  }

	  @AfterClass
	  public static void testCleanup() {
		  server.halt();
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
				assertTrue(loop.topic.equals("responses"));

				Echo x = (Echo)gson.fromJson(loop.msg,Echo.class);
				assertNotNull(x);
				
				assertTrue(x.id.equals("MyId"));
				assertTrue(x.to.equals("Hello"));
				assertTrue(x.from.equals("Sample payday loan campaigns"));
				
				assertEquals(x.campaigns.size(),1);
				
			} catch (Exception error) {
				// TODO Auto-generated catch block
				error.printStackTrace();
			}
		}
	
	//@Test
	public void addCampaign() {

	}
	
	//@Test
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
			assertTrue(loop.topic.equals("responses"));

			DeleteCampaign x = (DeleteCampaign)gson.fromJson(loop.msg,DeleteCampaign.class);
			assertNotNull(x);
			
			assertTrue(x.id.equals("MyId"));
			assertTrue(x.to.equals("Hello"));
			assertTrue(x.status.equals("ok"));
			assertTrue(x.from.equals("Sample payday loan campaigns"));

			
		} catch (Exception error) {
			// TODO Auto-generated catch block
			error.printStackTrace();
		}
	}
	
	@Test
	public void percentage() {

	}
	
	//@Test
	public void startBidder() {

	}
	
	//@Test
	public void stopBidder() {

	}
}

class ResponseLoop extends JedisPubSub implements Runnable {
	Thread me;
	Jedis conn;
	String topic;
	String msg;

	public ResponseLoop(Jedis conn) {
		this.conn = conn;
		me = new Thread(this);
		me.start();
	}

	public void run() {
		conn.subscribe(this, Controller.RESPONSES);
	}

	@Override
	public void onMessage(String arg0, String arg1) {
		System.out.println("A: " + arg0 + " = " + arg1);
		topic = arg0;
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
