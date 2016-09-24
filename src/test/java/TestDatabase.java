package test.java;


import static org.junit.Assert.*;



import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xrtb.common.Campaign;
import com.xrtb.db.User;
import com.xrtb.tools.DbTools;

/**
 * Tests the Configurastion file handling.
 * @author Ben M. Faul
 *
 */
public class TestDatabase {

	@BeforeClass
	public static void setup() {
		try {
			Config.setup();
			System.out.println("******************  TestDatabase");
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
	 * Test making a campaign from a raw json file.
	 * @throws Exception if the values obejct is not recognized.
	 */
	@Test
	public void rawCampaign() throws Exception {
		String content = new String(Files.readAllBytes(Paths.get("stub.json")));
		Campaign c = new Campaign(content);
		assertTrue(c.adomain.equals("originator.com"));
		assertTrue(c.creatives.size()==2);
	}
	/**
	 * Test making the user database from scratch/
	 * @throws Exception on JSON parsing of the file.
	 */
	@Test
	public void makeFile() throws Exception    {
		List<User>  list = new ArrayList();
		User u = new User("ben");
		list.add(u);
		
		String content = new String(Files.readAllBytes(Paths.get("stub.json")));
		Campaign c = new Campaign(content);
		c.adId = "ben:new-campaign";
		u.campaigns.add(c);
		
		assertTrue(c.date.size()==2);
		
		
		content = DbTools.mapper.writer().withDefaultPrettyPrinter().writeValueAsString(list);
		
		System.out.println(content);;
		System.out.println("-------------------------");
		
		List<User> x = DbTools.mapper.readValue(content,
				DbTools.mapper.getTypeFactory().constructCollectionType(List.class, User.class));
		User z = x.get(0);
		System.out.println(z);
	}
}
