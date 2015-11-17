package com.xrtb.tests;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.common.Node;
import com.xrtb.db.User;
import com.xrtb.pojo.BidRequest;

/**
 * Tests the constraint node processing.
 * @author Ben M. Faul
 *
 */
public class TestNode {
	/** The GSON object the class will use */
	static Gson gson = new Gson();
	/** The list of constraint nodes */
	static List<Node> nodes = new ArrayList();
	
	@BeforeClass
	public static void setup() {
		try {
			Config.setup();
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
	 * Trivial test of the payload atributes  
	 * @throws Exception on configuration file errors.
	 */
	@Test 
	public void makeSimpleCampaign() throws Exception {
		
		BidRequest br = new BidRequest(Configuration.getInputStream("SampleBids/nexage.txt"));
		assertNotNull(br);

		String content = new String(Files.readAllBytes(Paths.get("database.json")));
		List<User> users = gson.fromJson(content, new TypeToken<List<User>>(){}.getType());
		User u = users.get(0);
		
		List<Campaign> camps = u.campaigns;
		assertNotNull(camps);
		assertTrue(camps.size()==3);
		
		Campaign c = null;
		for (Campaign x : camps) {
			if (x.adId.equals("ben:payday")) {
				c = x;
				break;
			}
		}
		
		Node n = c.getAttribute("site.domain");
		assertNotNull(n);                          
		List<String> list = (List)n.value;
		assertNotNull(list);
		String op = (String)n.op;
		assertTrue(op.equals("NOT_MEMBER"));
	
	}
	
/**
 * Test the various operators of the constraints.
 * @throws Exception on file errors in configuration file.
 */
	@Test
	public void testOperators() throws Exception {
		BidRequest br = new BidRequest(Configuration.getInputStream("SampleBids/nexage.txt"));
		assertNotNull(br);

		String content = new String(Files.readAllBytes(Paths.get("database.json")));
		List<User> users = gson.fromJson(content, new TypeToken<List<User>>(){}.getType());
		User u = users.get(0);
		
		List<Campaign> camps = u.campaigns;
		assertNotNull(camps);
		assertTrue(camps.size()==3);
		
		Campaign c = null;
		for (Campaign x : camps) {
			if (x.adId.equals("ben:payday")) {
				c = x;
				break;
			}
		}
		
		Node n = c.getAttribute("site.domain");
		List<String> list = (List)n.value;
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
	
	/**
	 * Test the set operations.
	 * @throws Exception on configuration file errors.
	 */
	@Test
	public void testSets() throws Exception {
		BidRequest br = new BidRequest(Configuration.getInputStream("SampleBids/nexage.txt"));
		assertNotNull(br);

		String content = new String(Files.readAllBytes(Paths.get("database.json")));
		List<User> users = gson.fromJson(content, new TypeToken<List<User>>(){}.getType());
		User u = users.get(0);
		
		List<Campaign> camps = u.campaigns;
		assertNotNull(camps);
		assertTrue(camps.size()==3);
		
		Campaign c = null;
		for (Campaign x : camps) {
			if (x.adId.equals("ben:payday")) {
				c = x;
				break;
			}
		}

		Node n = c.getAttribute("site.domain");
		List<String> list = (List)n.value;
		
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
	
	/**
	 * Get the attributes of the bidRequestValues of the specified name 'what'/
	 * @param attr List. The list of various attributes.
	 * @param what String. The name you are looking for.
	 * @return Map. The attributes of the requested name.
	 */
	public Map getAttr(List<Map<String,Object>> attr, String what) {
		Map m = null;
		for (int i = 0; i< attr.size(); i++) {
			m = attr.get(i);
			List<String>brv = (List)m.get("bidRequestValues");
			String s = "";
			for (int j=0;j<brv.size();j++) {
				s = s + brv.get(j);
				if (j != brv.size()-1)
					s += ".";
			}
			if (what.equals(s))
				return m;
		}
		return m;
	}
}
