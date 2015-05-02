package com.xrtb.tests;


import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.db.Database;

/**
 * Tests the Configurastion file handling.
 * @author Ben M. Faul
 *
 */
public class TestDatabase {

	/**
	 * Test making a campaign from a raw json file.
	 * @throws Exception
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
		Database.DB_NAME = "database.json";
		Database db = new Database();
		List<String> list = db.getUserList();
		assertEquals(list.size(),1);
		String s = list.get(0);
		assertTrue(s.equals("ben"));
		s = db.getCampaignAsString("ben","ben:default-campaign");
		Campaign c = db.getCampaign("ben","ben:default-campaign");
		assertEquals(c.creatives.size(),2);
	
		String content = new String(Files.readAllBytes(Paths.get("stub.json")));
		c = new Campaign(content);
		c.adId = "new-campaign";
		db.editCampaign("ben", c);
		
		assertTrue(c.date.size()==2);
		
		s = db.getCampaignsAsString("ben");
		System.out.println(s);
	}
}
