package com.xrtb.tests;



import static org.junit.Assert.*;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.xrtb.bidder.RTBServer;
import com.xrtb.common.Configuration;
import com.xrtb.common.Node;
import com.xrtb.pojo.Bid;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.NoBid;

public class TestNode {
	static Gson gson = new Gson();
	static List<Node> nodes = new ArrayList();
	@BeforeClass
	  public static void testSetup() {
	  }

	  @AfterClass
	  public static void testCleanup() {
	    // Teardown for data used by the unit tests
	  }
	  
	/**
	 * Trivial test of the payload atributes  
	 * @throws Exception
	 */
	@Test 
	public void makeSimpleCampaign() throws Exception {
		
		BidRequest br = new BidRequest(Configuration.getInputStream("SampleBids/nexage.txt"));
		assertNotNull(br);

		String str = Charset
				.defaultCharset()
				.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
						.get("Campaigns/payday.json")))).toString();
		
		Map m = (Map)TestNode.gson.fromJson(str,Map.class);
		Map app = (Map)m.get("app");
		assertNotNull(app);
		List<Map> camps = (List)app.get("campaigns");
		assertNotNull(camps);
		assertTrue(camps.size()==1);
		
		m = camps.get(0);
		List<Map<String,Object>> attrs = (List)m.get("attributes");
		assertNotNull(attrs);
		assertTrue(attrs.size()==2);
		List<String> keys = new ArrayList();
		for (Map o : attrs) {
			for (Object key : o.keySet()) {
			    keys.add(key.toString());
			}
		}

		assertTrue(keys.contains("site.domain"));
		assertTrue(keys.contains("user.geo.country"));

		assertTrue(keys.contains("site.domain"));
		
		m = getAttr(attrs,"site.domain");
		System.out.println(m);
		m = (Map)m.get("site.domain");
		assertNotNull(m);
		List<String> list = (List)m.get("values");
		assertNotNull(list);
		String op = (String)m.get("op");
		assertTrue(op.equals("NOT_MEMBER"));
	
	}
	
	// @Test
	public void testOperators() throws Exception {
		BidRequest br = new BidRequest(Configuration.getInputStream("SampleBids/nexage.txt"));
		assertNotNull(br);

		String str = Charset
				.defaultCharset()
				.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
						.get("Campaigns/payday.json")))).toString();
		
		Map m = (Map)TestNode.gson.fromJson(str,Map.class);
		Map app = (Map)m.get("app");
		assertNotNull(app);
		List<Map> camps = (List)app.get("campaigns");
		assertNotNull(camps);
		assertTrue(camps.size()==1);
		
		m = camps.get(0);
		List<Map<String,Object>> attrs = (List)m.get("campaign-attributes");
		List<String> keys = new ArrayList();
		for (Map o : attrs) {
			for (Object key : o.keySet()) {
			    keys.add(key.toString());
			}
		}
		
		m = getAttr(attrs,"site.domain");
		m = (Map)m.get("site.domain");
		List<String> list = (List)m.get("values");
		list.add("junk1.com");										// add this to the campaign blacklist
		String op = "NOT_MEMBER";
		Node node = new Node("blacklist","site.domain",op,list);
		
		list.add("junk1.com");
		Boolean b = node.test(br);	   // true means the constraint is satisfied.
		assertFalse(b);                // should be on blacklist and will not bid
		
		op = "MEMBER";
		node = new Node("blacklist","site.domain",op,list);
		b = node.test(br);	   // true means the constraint is satisfied.
		assertTrue(b);         // should be on blacklist and will not bid
		
		op = "EQUALS";
		node = new Node("=","user.yob",op,1961);
		b = node.test(br);	   // true means the constraint is satisfied.
		assertTrue(b);         // should be on blacklist and will not bid */
		node = new Node("=","user.yob",op,1960);
		b = node.test(br);	   // true means the constraint is satisfied.
		assertFalse(b);         // should be on blacklist and will not bid */
		node = new Node("=","user.yob",op,1962);
		b = node.test(br);	   // true means the constraint is satisfied.
		assertFalse(b);         // should be on blacklist and will not bid */
		
		op = "NOT_EQUALS";
		node = new Node("!=","user.yob",op,1901);
		b = node.test(br);	   // true means the constraint is satisfied.
		assertTrue(b);         // should be on blacklist and will not bid */
		
		op = "LESS_THAN";
		node = new Node("<","user.yob",op,1960);
		b = node.test(br);	   // true means the constraint is satisfied.
		assertTrue(b);         // should be on blacklist and will not bid */
		
		op = "LESS_THAN_EQUALS";
		node = new Node("<=","user.yob",op,1960);
		b = node.test(br);	   // true means the constraint is satisfied.
		assertTrue(b);         // should be on blacklist and will not bid */
		
		op = "GREATER_THAN";
		node = new Node(">","user.yob",op,1962);
		b = node.test(br);	   // true means the constraint is satisfied.
		assertTrue(b);         // should be on blacklist and will not bid */
		
		op = "GREATER_THAN_EQUALS";
		node = new Node(">=","user.yob",op,1961);
		b = node.test(br);	   // true means the constraint is satisfied.
		assertTrue(b);         // should be on blacklist and will not bid */
		
		op = "DOMAIN";
		List range = new ArrayList();
		range.add(new Double(1960));
		range.add(new Double(1962));
		node = new Node("inrangetest","user.yob",op,range);
		b = node.test(br);	   // true means the constraint is satisfied.
		assertTrue(b);         // should be on blacklist and will not bid */
		
	} 
	
	
	@Test
	public void testSets() throws Exception {
		BidRequest br = new BidRequest(Configuration.getInputStream("SampleBids/nexage.txt"));
		assertNotNull(br);

		String str = Charset
				.defaultCharset()
				.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
						.get("Campaigns/payday.json")))).toString();
		
		Map m = (Map)TestNode.gson.fromJson(str,Map.class);
		Map app = (Map)m.get("app");
		assertNotNull(app);
		List<Map> camps = (List)app.get("campaigns");
		assertNotNull(camps);
		assertTrue(camps.size()==1);
		
		m = camps.get(0);
		List<Map<String,Object>> attrs = (List)m.get("attributes");
		List<String> keys = new ArrayList();
		for (Map o : attrs) {
			for (Object key : o.keySet()) {
			    keys.add(key.toString());
			}
		}

		m = getAttr(attrs,"site.domain");
		m = (Map)m.get("site.domain");
		//List<String> list = (List)m.get("values");
		List<String> list = new ArrayList();
		
		list.add("junk.com");							
		String op = "INTERSECTS";
		Node node = new Node("blacklist","site.domain",op,list);
		Boolean b = node.test(br);	   // true means the constraint is satisfied.
		assertFalse(b);         // should be on blacklist and will not bid 
		
		list.add("junk1.com");
		node = new Node("blacklist","site.domain",op,list);
		b = node.test(br);	   // true means the constraint is satisfied.
		assertTrue(b);         // should be on blacklist and will not bid 
		
		list.clear();
		node = new Node("blacklist","site.domain",op,list);
		b = node.test(br);	   // true means the constraint is satisfied.
		assertFalse(b);         // should be on blacklist and will not bid 
		
		op = "NOT_INTERSECTS";
		node = new Node("blacklist","site.domain",op,list);
		b = node.test(br);	   // true means the constraint is satisfied.
		assertTrue(b);         // should be on blacklist and will not bid 


	}
	
	public Map getAttr(List<Map<String,Object>> attr, String what) {
		Map m = null;
		for (int i = 0; i< attr.size(); i++) {
			m = attr.get(i);
			if (m.get(what) != null)
				return m;
		}
		return m;
	}
}
