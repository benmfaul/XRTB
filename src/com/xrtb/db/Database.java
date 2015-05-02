package com.xrtb.db;

import java.io.IOException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xrtb.common.Campaign;

/**
 * A class that makes a simple database if users::campaigns for use by the Campaign admin portal
 * 
 * @author ben
 *
 */
public class Database {
	
	transient public static String DB_NAME = "database.json";
	final static Charset ENCODING = StandardCharsets.UTF_8;
	public List<User> users;
	transient Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public Database() {
		try {
			read();
			for (User u : users) {
				for (Campaign c : u.campaigns) {
					c.encodeAttributes();
					c.encodeCreatives();
				}
			}
		} catch (Exception e) {
			users = new ArrayList();
			try {
				createUser("ben");
				write();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	public List<String> getUserList() {
		List<String> list = new ArrayList();
		for (int i=0; i<users.size();i++) {
			list.add(users.get(i).name);
		}
		return list;
	}
	
	public void createUser(String name) throws Exception {
		for (int i=0;i<users.size();i++) {
			User u = users.get(i);
			if (u.name.equals(name)) {
				return;
			}
		}
		String content = new String(Files.readAllBytes(Paths.get("stub.json")));
		Campaign c = new Campaign(content);
		c.adId = name + ":" + c.adId;
		User u = new User(name);
		editCampaign(u,c);
		users.add(u);
	}
	
	public void deleteUser(String name) {
		for (int i=0;i<users.size();i++) {
			User u = users.get(i);
			if (u.name.equals(name)) {
				users.remove(i);
				return;
			}
		}
	}
	
	public Campaign getCampaign(String name, String id) {
		User u = getUser(name);
		for (Campaign c : u.campaigns) {
			if (c.adId.equals(id)) {
				return c;
		}
	}
	return null;
}

	public String getCampaignAsString(String name, String id) {
		Campaign c = getCampaign(name,id);
		return c.toJson();
	}
	
	public List getCampaigns(String name) {
		User u = getUser(name);
		return u.campaigns;
	}
	
	public User getUser(String name) {
		for (User u : users) {
			if (u.name.equals(name))
				return u;
		}
		return null;
	}
	
	public String getCampaignsAsString(String name) {
		User u = getUser(name);
		return gson.toJson(u.campaigns);
	}
	
	public Campaign createStub(String name, String id) throws Exception {
		User u = getUser(name);
		String content = new String(Files.readAllBytes(Paths.get("stub.json")));
		Campaign c = new Campaign(content);
		c.adId = name + ":" + id;
		editCampaign(u,c);
		return c;
	}
	
	public void editCampaign(String name, Campaign c) {
		User u = getUser(name);
		editCampaign(u,c);
	}
	
	public List getAllCampaigns() {
		List camps = new ArrayList();
		for (User u : users) {
			for (Campaign c : u.campaigns) {
				camps.add(c);
			}
		}
		return camps;
	}
	
	public List deleteCampaign(User u, String name) {
		for (int i=0; i< u.campaigns.size();i++) {
			Campaign c = u.campaigns.get(i);
			if (c.adId.equals(name)) {
				u.campaigns.remove(i);
			}
			return u.campaigns;
		}
		return null;
	}
	
	public void editCampaign(User u, Campaign c) {
		for (int i=0;i<u.campaigns.size();i++) {
			Campaign x = u.campaigns.get(i);
			if(x.adId.equals(c.adId)) {
				u.campaigns.remove(i);
				u.campaigns.add(c);
				return;
			}
		}
		u.campaigns.add(c);
	}
	
	public void read() throws Exception {
		String content = new String(Files.readAllBytes(Paths.get(DB_NAME)));
		Database db = gson.fromJson(content,Database.class);
		users = db.users;
	}
	
	public void write() throws Exception{
		String content = gson.toJson(this);
	    Files.write(Paths.get(DB_NAME), content.getBytes());
	}
}
