package com.xrtb.tests;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xrtb.bidder.CampaignSelector;
import com.xrtb.bidder.RTBServer;
import com.xrtb.commands.Echo;
import com.xrtb.common.Configuration;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;

import junit.framework.TestCase;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * Tests for accessing the Nashhorn scripting engine.
 * @author Ben M. Faul
 *
 */
public class TestJJS extends TestCase {

	/** The RTBServer that Nashhorn will use */
	static RTBServer server;
	
	@BeforeClass
	public static void testSetup() {
		com.xrtb.common.Configuration.getInstance().clear();
	}
	  @AfterClass
	  public static void testCleanup() {
	    if (server != null)
	    	server.halt();
	  } 
	
	/**
	 * Tests basic nashhorn operations.
	 * @throws Exception if the script engine fails or the requested classes can't be found.
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
		    RTBServer x = null;
		 
		    	x = (RTBServer)engine.eval("Server = new com.xrtb.bidder.RTBServer()");
		    	Echo m = x.getStatus(); 
		    	assertFalse(m.stopped);
		    
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
		            fail(ex.toString());
		    }
	}
}
