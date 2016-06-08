package com.xrtb.tests;


import static org.junit.Assert.*;


import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.xrtb.common.Campaign;
import com.xrtb.db.User;

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
		Gson g= new GsonBuilder().setPrettyPrinting().create();
		List<User>  list = new ArrayList();
		User u = new User("ben");
		list.add(u);
		
		String content = new String(Files.readAllBytes(Paths.get("stub.json")));
		Campaign c = new Campaign(content);
		c.adId = "ben:new-campaign";
		u.campaigns.add(c);
		
		assertTrue(c.date.size()==2);
		
		System.out.println(g.toJson(list));
		
		List<User> x = g.fromJson(g.toJson(list), new TypeToken<List<User>>(){}.getType());
		User z = x.get(0);
		System.out.println(z);
	}
}
