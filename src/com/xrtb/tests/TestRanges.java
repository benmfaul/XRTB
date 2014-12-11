package com.xrtb.tests;

import static org.junit.Assert.*;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.node.ObjectNode;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xrtb.common.Configuration;
import com.xrtb.common.Node;

import com.xrtb.pojo.BidRequest;


public class TestRanges {
	@BeforeClass
	  public static void testSetup() {
	  }

	  @AfterClass
	  public static void testCleanup() {
	    // Teardown for data used by the unit tests
	  }
	  
	@Test 
	public void testLosAngelesToSF() {
		Number laLat = 34.05;
		Number laLon =  -118.25;
		
		Number sfLat = 37.62;
		Number sfLon = -122.38;
		double dist = Node.getRange(laLat, laLon, sfLat, sfLon);
		System.out.println(dist);
		assertTrue(dist==544720.8629416309);
	}
	
	@Test
	public void testGeoInBidRequest() throws Exception {
		InputStream is = Configuration.getInputStream("SampleBids/smaato.json");
		BidRequest br = new BidRequest(is);
		assertEquals(br.getId(),"K6t8sXXYdM");
		
		Map m = new HashMap();
		m.put("lat", 34.05);
		m.put("lon",-118.25);
		m.put("range",600000);
		
		Node node = new Node("LATLON","device.geo", Node.QUERY, m);
     	node.test(br);
		ObjectNode map = (ObjectNode)node.getBRvalue();
		assertTrue((Double)map.get("lat").getDoubleValue()==37.62);
		assertTrue((Double)map.get("lon").getDoubleValue()==-122.38);
		assertTrue((Double)map.get("type").getDoubleValue()==3);
		
		List<Map>test = new ArrayList();
		test.add(m);
		node = new Node("LATLON","device.geo", Node.INRANGE, test);
		node.test(br);

	}
	
}
