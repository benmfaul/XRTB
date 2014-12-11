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

import com.xrtb.bidder.Controller;
import com.xrtb.commands.Echo;
import com.xrtb.pojo.NoBid;

public class TestRedis  {
	static Controller c;
	static Jedis sub;
	static Jedis pub;
	static ResponseLoop loop;
	@BeforeClass
	  public static void testSetup() {		
		sub = new Jedis("localhost");  // sub
		sub.connect();
		pub = new Jedis("localhost");
		pub.connect();

		Controller c = Controller.getInstance();
		loop = new ResponseLoop(sub);
	  }

	  @AfterClass
	  public static void testCleanup() {
	    // Teardown for data used by the unit tests
	  }
	
	@Test
	public void addCampaign() {

	}
	
	@Test
	public void deleteCampaign() {

	}
	
	@Test
	public void percentage() {

	}
	
	@Test
	public void testEcho() {
		loop.msg = null;
		
		Echo e = new Echo();
		e.from = "ben";
		e.to = "ben";
		e.msg = null;
		String str = e.toString();
		pub.publish(Controller.COMMANDS,str);
		try {
			Thread.sleep(1000);
			assertNotNull(loop.msg);
		} catch (Exception error) {
			// TODO Auto-generated catch block
			error.printStackTrace();
		}
	}
	
	@Test
	public void startBidder() {

	}
	
	@Test
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
