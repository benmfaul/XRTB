package com.xrtb.tests;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import redis.clients.jedis.Jedis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xrtb.bidder.Controller;
import com.xrtb.bidder.RTBServer;
import com.xrtb.common.Configuration;
import com.xrtb.common.HttpPostGet;
import com.xrtb.exchanges.Fyber;
import com.xrtb.pojo.BidRequest;

import junit.framework.TestCase;

/**
 * A class for testing that the bid has the right parameters
 * @author Ben M. Faul
 *
 */
public class TestFyber  {
	static Controller c;
	public static String test = "";
	static Gson gson = new Gson();
	
	@BeforeClass
	  public static void testSetup() {		
		try {
			Config.setup();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }

	  @AfterClass
	  public static void testCleanup() {
		  Config.teardown();
	  }
	  
	  /**
	   * Test a valid bid response.
	   * @throws Exception on networking errors.
	   */
	  @Test 
	  public void testFyberDisplay() throws Exception {
			BidRequest br = new Fyber("./SampleBids/fyberDisplay640x480.txt");
	  }
	  
	  /**
	   * Test a valid bid response.
	   * @throws Exception on networking errors.
	   */
	  @Test 
	  public void testFyberVideoPvt() throws Exception {
			BidRequest br = new Fyber("./SampleBids/fyberVideoPvtMkt.txt");
	  }
	  
	  @Test 
	  public void testMobileInApp() throws Exception {
			BidRequest br = new Fyber("./SampleBids/fyberMobileInApp.txt");
	  }
}