package com.xrtb.tests;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xrtb.bidder.CampaignSelector;
import com.xrtb.bidder.RTBServer;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;

import junit.framework.TestCase;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class TestJJS extends TestCase {

/*	static RTBServer server;
	@BeforeClass
	public static void testSetup() {
		try {
			Configuration c = Configuration.getInstance();
			c.initialize("Campaigns/payday.json");
			server = new RTBServer(c.port);
			Thread.sleep(5000);
		} catch (Exception e) {
			fail(e.toString());
		}
	}
	  @AfterClass
	  public static void testCleanup() {
	    if (server != null)
	    	server.halt();
	  } */
	
	/**
	 * Tests
	 * @throws Exception
	 */
	@Test
	public void testNashorn() throws Exception {
		
		try {
		    ScriptEngineManager factory = new ScriptEngineManager();
		    ScriptEngine engine = factory.getEngineByName("nashorn");
		    
		    Configuration conf = (Configuration)engine.eval("conf = com.xrtb.common.Configuration.getInstance()");
		    assertNotNull(conf);
		    engine.eval("conf.initialize(\"Campaigns/payday.json\")");
		    assertEquals(conf.port,8080);
		    
		    RTBServer x = (RTBServer)engine.eval("Server = new com.xrtb.bidder.RTBServer(conf.port)");
		    Map m = x.getStatus();
		    Boolean stopped = (Boolean)m.get("stopped");
		    assertFalse(stopped);
		    
		    CampaignSelector camps = (CampaignSelector)engine.eval("camps = Server.getCampaigns()");
		    assertEquals(camps.size(),1);
		    
		    /**
		     * Now let's test a bid request
		     */
		    BidRequest br =  (BidRequest)engine.eval("br = new com.xrtb.exchanges.Nexage(\"SampleBids/nexage.txt\")");
		    assertEquals(br.getId(),"35c22289-06e2-48e9-a0cd-94aeb79fab43");
		    
		    // Now let's bid...
		    BidResponse response = (BidResponse)engine.eval("response = camps.get(br)");
		    assertNotNull(response);
	
		    Boolean ret = (Boolean)engine.eval("ret = 5 == response.price");
		    assertTrue(ret);
		    } catch (Exception ex) {
		            ex.printStackTrace();
		    }
	}
}
