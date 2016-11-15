package test.java;

import static org.junit.Assert.*;



import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.common.Node;
import com.xrtb.db.User;
import com.xrtb.pojo.BidRequest;
import com.xrtb.tools.DbTools;

/**
 * Tests the constraint node processing.
 * @author Ben M. Faul
 *
 */
public class TestNode {
	/** The GSON object the class will use */
	/** The list of constraint nodes */
	static List<Node> nodes = new ArrayList();
	
	@BeforeClass
	public static void setup() {
		try {
			Config.setup();
			System.out.println("******************  TestNode");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void stop() {
		Config.teardown();
	}
	
	  
/*	@Test
	public void testOs() throws Exception {
		List<String> parts = new ArrayList();
		parts.add("Android");
		Node node = new Node("test","device.os","MEMBER",parts);
		node.notPresentOk = false;
		BufferedReader br = new BufferedReader(new FileReader("../requests"));
		for(String line; (line = br.readLine()) != null; ) {
			BidRequest bidR = new BidRequest(new StringBuilder(line));
			boolean x = node.test(bidR);
			if (x == true) {
				JsonNode os = (JsonNode)bidR.interrogate("device.os");
				if (os.textValue() == null) {
					System.out.println("NULL");
					return;
				}
				assertTrue(os.textValue().equals("Android"));
			}
		}
		
	} */
	
	/**
	 * Trivial test of the payload atributes  
	 * @throws Exception on configuration file errors.
	 */
	@Test 
	public void makeSimpleCampaign() throws Exception {
		
		BidRequest br = new BidRequest(Configuration.getInputStream("SampleBids/nexage.txt"));
		assertNotNull(br);

		String content = new String(Files.readAllBytes(Paths.get("database.json")));
		List<User> users = DbTools.mapper.readValue(content,
				DbTools.mapper.getTypeFactory().constructCollectionType(List.class, User.class));
		User u = users.get(0);
		
		List<Campaign> camps = u.campaigns;
		assertNotNull(camps);
		
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
	
	@Test
	public void getIntAndDoubleValue() throws Exception {
		Node node = new Node("intTest","user.yob","EQUALS",1961);
		Integer ix = node.intValue();
		assertEquals(ix.intValue(),1961);
		
		Double dx = node.doubleValue();
		assertTrue(dx.doubleValue()==1961.0);
	}
	
/**
 * Test the various operators of the constraints.
 * @throws Exception on file errors in configuration file.
 */
	@Test
	public void testOperators() throws Exception {
		BidRequest br = new BidRequest(Configuration.getInputStream("SampleBids/nexage.txt"));
		br.exchange = "nexage";
		assertNotNull(br);

		String content = new String(Files.readAllBytes(Paths.get("database.json")));
		List<User> users = DbTools.mapper.readValue(content,
				DbTools.mapper.getTypeFactory().constructCollectionType(List.class, User.class));
		
		User u = users.get(0);
		
		List<Campaign> camps = u.campaigns;
		assertNotNull(camps);
		
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
		
		op = "STRINGIN";
		node = new Node("stringintest","site.page",op,"xxx");
		b = node.test(br);	   // true means the constraint is satisfied.
		assertFalse(b);         // should be on blacklist and will not bid */
		
		op = "NOT_STRINGIN";
		node = new Node("stringintest","site.page",op,"xxx");
		b = node.test(br);	   // true means the constraint is satisfied.
		assertTrue(b);         // should be on blacklist and will not bid */
		
		op = "STRINGIN";
		node = new Node("stringintest","site.page",op,"nexage");
		b = node.test(br);	   // true means the constraint is satisfied.
		assertTrue(b);         // should be on blacklist and will not bid */
		
		op = "NOT_STRINGIN";
		node = new Node("stringintest","site.page",op,"nexage");
		b = node.test(br);	   // true means the constraint is satisfied.
		assertFalse(b);         // should be on blacklist and will not bid */
		
		
		op = "STRINGIN";
		list = new ArrayList();
		list.add("nexage");
		list.add("xxx");
		
		node = new Node("stringintest","site.page",op,list);
		b = node.test(br);	   // true means the constraint is satisfied.
		assertTrue(b);         // should be on blacklist and will not bid */
		
		op = "STRINGIN";
		String [] parts = new String[2];
		parts[0] = "nexage";
		parts[1] = "xxx";
		
		node = new Node("stringintest","site.page",op,parts);
		b = node.test(br);	   // true means the constraint is satisfied.
		assertTrue(b);         // should be on blacklist and will not bid */
		
		node = new Node("exchangetest","exchange","EQUALS","smartyads");
		b = node.test(br);
		assertFalse(b);
		
		Arrays.asList("site.name", "app.name");
	//	node = new Node("eitheror",Arrays.asList("site.domain", "app.domain"),"EQUALS","smartyads");
		b = node.test(br);
		assertFalse(b);
		
		
		br = new BidRequest(Configuration.getInputStream("SampleBids/atomx.txt"));
		br.exchange = "atomx";
		assertNotNull(br);
		List<Integer> ilist = new ArrayList();
		ilist.add(2);
		node = new Node("devicetypes", "device.devicetype", Node.INTERSECTS,ilist);
		b = node.test(br);
		assertTrue(b);
		
		br = new BidRequest(Configuration.getInputStream("SampleBids/atomx.txt"));
		br.exchange = "atomx";
		assertNotNull(br);
	    ilist = new ArrayList();
		ilist.add(1);
		ilist.add(4);
		ilist.add(5);
		node = new Node("devicetypes", "device.devicetype", Node.INTERSECTS,ilist);
		b = node.test(br);
		assertFalse(b);
		
		br = new BidRequest(Configuration.getInputStream("SampleBids/atomx.txt"));
		br.exchange = "atomx";
		assertNotNull(br);
	    ilist = new ArrayList();
		ilist.add(1);
		ilist.add(2);
		ilist.add(5);
		node = new Node("devicetypes", "device.devicetype", Node.INTERSECTS,ilist);
		b = node.test(br);
		assertTrue(b);
		
		br = new BidRequest(Configuration.getInputStream("SampleBids/atomx.txt"));
		br.exchange = "atomx";
		assertNotNull(br);
		node = new Node("site-test", "site", Node.EXISTS,null);
		b = node.test(br);
		assertTrue(b);
		
		br = new BidRequest(Configuration.getInputStream("SampleBids/atomx.txt"));
		br.exchange = "atomx";
		assertNotNull(br);
		node = new Node("aoo-test", "app", Node.EXISTS,null);
		b = node.test(br);
		assertFalse(b);
		
		br = new BidRequest(Configuration.getInputStream("SampleBids/atomx.txt"));
		br.exchange = "atomx";
		assertNotNull(br);
		node = new Node("aoo-test", "app", Node.NOT_EXISTS,null);
		b = node.test(br);
		assertTrue(b);
		
		ilist.clear();
		ilist.add(1);
		ilist.add(2);
		ilist.add(3);
		node = new Node("aoo-test", "member test", Node.MEMBER,1);
		boolean test = node.testInternal(ilist);

	} 
	
	@Test
	public void testMember() throws Exception {
		ArrayList ilist = new ArrayList();
		ilist.clear();
		ilist.add(1);
		ilist.add(2);
		ilist.add(3);
		Node node = new Node("aoo-test", "member test", Node.MEMBER,1);
		boolean test = node.testInternal(ilist);
		assertTrue(test);
		
		node = new Node("aoo-test", "member test", Node.NOT_MEMBER,1);
		test = node.testInternal(ilist);
		assertFalse(test);
		
		ilist.clear();
		ilist.add("one");
		ilist.add("two");
		ilist.add("three");
		node = new Node("aoo-test", "member test", Node.MEMBER,"two");
		test = node.testInternal(ilist);
		assertTrue(test);
		
		node = new Node("aoo-test", "member test", Node.NOT_MEMBER,"two");
		test = node.testInternal(ilist);
		assertFalse(test);
	}
	
	@Test
	public void testOr() throws Exception {
		BidRequest br = new BidRequest(Configuration.getInputStream("SampleBids/atomx.txt"));
		br.exchange = "atomx";
		assertNotNull(br);
	    List ilist = new ArrayList();
	    Node a = new Node("site","site.publisher.id",Node.EQUALS,"3456");
	    ilist.add(a);
	    
	    a = new Node("site","app.publisher.id",Node.EQUALS,"3456");
	    ilist.add(a);

		Node node = new Node("ortest", null, Node.OR,ilist);
		Boolean b = node.test(br);
		assertTrue(b);
		
		// reverse order
		ilist = new ArrayList();
		a = new Node("site","app.publisher.id",Node.EQUALS,"3456");
		ilist.add(a);

		a = new Node("site","site.publisher.id",Node.EQUALS,"3456");
		ilist.add(a);
		    
		  
		node = new Node("ortest", null, Node.OR,ilist);
		b = node.test(br);
		assertTrue(b);
		
		// actual is second and bad
		ilist = new ArrayList();
		a = new Node("app","app.publisher.id",Node.EQUALS,"3456");
		ilist.add(a);

		a = new Node("site","site.publisher.id",Node.EQUALS,"666");
		ilist.add(a);
		    	  
		node = new Node("ortest", null, Node.OR,ilist);
		b = node.test(br);
		assertFalse(b);
		
		// actual is first and bad
		ilist = new ArrayList();
		a = new Node("site","site.publisher.id",Node.EQUALS,"666");
		ilist.add(a);
		
		a = new Node("app","app.publisher.id",Node.EQUALS,"3456");
		ilist.add(a);
				    	  
		node = new Node("ortest", null, Node.OR,ilist);
		b = node.test(br);
		assertFalse(b);
		
	}
	
	@Test
	public void testQueryMap() throws Exception {
		BidRequest br = new BidRequest(Configuration.getInputStream("SampleBids/atomx.txt"));
		br.exchange = "atomx";
		assertNotNull(br);
		List ilist = new ArrayList();
		ilist.add("builtin");
		ilist.add("test");
		ilist.add("EQUALS");
		ilist.add(1);
		
		Node node = new Node("query", "site.publisher.id", Node.QUERY,ilist);
		boolean b = node.test(br);
		assertTrue(b);

	}
	
	@Test
	public void testInstl() throws Exception {
		BidRequest br = new BidRequest(Configuration.getInputStream("SampleBids/interstitial.txt"));
		assertNotNull(br);

		String content = new String(Files.readAllBytes(Paths.get("database.json")));
		List<User> users = DbTools.mapper.readValue(content,
				DbTools.mapper.getTypeFactory().constructCollectionType(List.class, User.class));
		User u = users.get(0);
		
		List<Campaign> camps = u.campaigns;
		assertNotNull(camps);

		
		Campaign c = null;
		for (Campaign x : camps) {
			if (x.adId.equals("ben:payday")) {
				c = x;
				break;
			}
		}
		
		Node n = c.getAttribute("imp.0.instl");
		
		assertNotNull(n);
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
		List<User> users = DbTools.mapper.readValue(content,
				DbTools.mapper.getTypeFactory().constructCollectionType(List.class, User.class));
		User u = users.get(0);
		
		List<Campaign> camps = u.campaigns;
		assertNotNull(camps);
		
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
		Boolean b = node.test(br);	   	// true means the constraint is satisfied.
		assertFalse(b);         		// should be on blacklist and will not bid 
		
		
		/** 
		 * Test adding an array of objects
		 */
		String [] parts = new String[1];
		op = "INTERSECTS";
		parts[0] = "junk.com";
		node = new Node("blacklist-array","site.domain",op,parts);
		b = node.test(br);	   // true means the constraint is satisfied.
		assertFalse(b);         // should be on blacklist and will not bid 
		
		n = new Node("matching-categories","site.cat",Node.INTERSECTS,parts);
		
		
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


		op = "MEMBER";
		node = new Node("mimes","imp.0.banner.mimes",op,"image/jpg");
		b = node.test(br);
		assertTrue(b);
	}
	
	@Test
	public void testMemberOfBuiltin() throws Exception {
		BidRequest br = new BidRequest(Configuration.getInputStream("SampleBids/nexage.txt"));
		assertNotNull(br);
		String op = "MEMBER";
		Node node = new Node("mimes","imp.0.banner.mimes",op,"image/jpg");
		boolean b = node.test(br);
		assertTrue(b);
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
