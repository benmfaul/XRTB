package test.java;

import static org.junit.Assert.*;


import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xrtb.bidder.Controller;

import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.common.HttpPostGet;
import com.xrtb.common.Node;
import com.xrtb.pojo.BidRequest;

/**
 * Test Geo fencing
 * @author Ben M. Faul
 *
 */

public class TestRanges {
	/**
	 * Setup the RTB server for the test
	 */
	@BeforeClass
	public static void setup() {
		try {
			Config.setup();
			Controller.getInstance().deleteCampaign("ben","ben:extended-device");
			System.out.println("******************  TestRanges");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void stop() {
		Config.teardown();
	}
	
	/**
	 * Shut the RTB server down.
	 */
	@AfterClass
	public static void testCleanup() {
		Config.teardown();
	}  
	
	/**
	 * Test distance calaculations
	 */
	@Test 
	public void testLosAngelesToSF() {
		Number laLat = 34.05;
		Number laLon =  -118.25;
		
		Number sfLat = 37.62;
		Number sfLon = -122.38;
		double dist = Node.getRange(laLat, laLon, sfLat, sfLon);
		assertTrue(dist==544720.8629416309);
	}
	
	/**
	 * Test a single geo fence region in an isolated node.
	 * @throws Exception on I/O errors.
	 */
	@Test
	public void testGeoInBidRequest() throws Exception {
		InputStream is = Configuration.getInputStream("SampleBids/smaato.json");
		BidRequest br = new BidRequest(is);
		assertEquals(br.getId(),"K6t8sXXYdM");
		
		Map m = new HashMap();
		m.put("lat", 34.05);
		m.put("lon",-118.25);
		m.put("range",600000.0);
		List list = new ArrayList();
		list.add(m);
		
		Node node = new Node("LATLON","device.geo", Node.INRANGE, list);
     	node.test(br);
		ObjectNode map = (ObjectNode)node.getBRvalue();
		assertTrue((Double)map.get("lat").doubleValue()==37.62);
		assertTrue((Double)map.get("lon").doubleValue()==-122.38);
		assertTrue((Double)map.get("type").doubleValue()==3);
		
		List<Map>test = new ArrayList();
		test.add(m);
		node = new Node("LATLON","device.geo", Node.INRANGE, test);
		node.test(br);

	}
	
}
