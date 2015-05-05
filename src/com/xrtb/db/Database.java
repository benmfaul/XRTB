package com.xrtb.db;

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
 * A class that makes a simple database for use by the Campaign admin portal
 * @author Ben M. Faul
 *
 */
public class Database {
	
	/** The name of the database file */
	transient public static String DB_NAME = "database.json";
	/** The file's encoding */
	final static Charset ENCODING = StandardCharsets.UTF_8;
	/** A list of users, this is the root node of the database (a list of users, which has a name and a map of campaigns */
	public List<User> users;
	/** Serialier for the JSON of this class */
	transient Gson gson = new GsonBuilder().setPrettyPrinting().create();

	/**
	 * Open (and create if necessary) the database file
	 */
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
	
	/**
	 * Returns a list of user names in the database.
	 * @return List. The list of the user names in the database.
	 */
	public List<String> getUserList() {
		List<String> list = new ArrayList();
		for (int i=0; i<users.size();i++) {
			list.add(users.get(i).name);
		}
		return list;
	}
	
	/**
	 * Creates a user of the given name.
	 * @param name String name.
	 * @throws Exception if there is an error reading or writing the database file.
	 */
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
	
	/**
	 * Delete the user from the database.
	 * @param name String. The name of the user to delete
	 */
	public void deleteUser(String name) {
		for (int i=0;i<users.size();i++) {
			User u = users.get(i);
			if (u.name.equals(name)) {
				users.remove(i);
				return;
			}
		}
	}
	
	/**
	 * Return a campaign of the given name and adId.
	 * @param name String name. The user name.
	 * @param id String. the adId of the campaign to return.
	 * @return Campaign. The campaign to return.
	 */
	public Campaign getCampaign(String name, String id) {
		User u = getUser(name);
		for (Campaign c : u.campaigns) {
			if (c.adId.equals(id)) {
				return c;
		}
	}
	return null;
}

	/**
	 * Given the name of the user and the adId, return the campaign as a JSON string.
	 * @param name String. The name of the user.
	 * @param id String. The adId of the campaign.
	 * @return String. The campaign represented as a JSON string.
	 */
	public String getCampaignAsString(String name, String id) {
		Campaign c = getCampaign(name,id);
		return c.toJson();
	}
	
	/**
	 * Return a list of campaign ids that belong to the given user name.
	 * @param name. String the user name.
	 * @return List. The campaigns that belong to this user.
	 */
	public List<Campaign> getCampaigns(String name) {
		User u = getUser(name);
		return u.campaigns;
	}
	
	/**
	 * Get the user object.
	 * @param name String. The user name.
	 * @return User. The user object. Is null if no user exists.
	 */
	public User getUser(String name) {
		for (User u : users) {
			if (u.name.equals(name))
				return u;
		}
		return null;
	}
	
	/**
	 * Return campaigns of the given user as a JSON string.
	 * @param name String. The user name.
	 * @return String. The campaigns of this user in a JSON string.
	 */
	public String getCampaignsAsString(String name) {
		User u = getUser(name);
		return gson.toJson(u.campaigns);
	}
	
	/**
	 * Create a stub campaign from 'stub.json'
	 * @param name String. The user name.
	 * @param id String. The adId to use for this campaign.
	 * @return Campaign. The campaign that was created.
	 * @throws Exception on file errors.
	 */
	public Campaign createStub(String name, String id) throws Exception {
		User u = getUser(name);
		String content = new String(Files.readAllBytes(Paths.get("stub.json")));
		Campaign c = new Campaign(content);
		c.adId = name + ":" + id;
		editCampaign(u,c);
		return c;
	}
	
	/**
	 * Given the user name. edit the campaign of this user with the provided campaign (adId is the key).
	 * @param name String. The user name.
	 * @param c Campaign. The campaign you will use as the source of the changes.
	 */
	public void editCampaign(String name, Campaign c) {
		User u = getUser(name);
		editCampaign(u,c);
	}
	
	/**
	 * Return a list of all campaigns in the database, irrespective of user.
	 * @return List. The campaigns in the database.
	 */
	public List getAllCampaigns() {
		List camps = new ArrayList();
		for (User u : users) {
			for (Campaign c : u.campaigns) {
				camps.add(c);
			}
		}
		return camps;
	}
	
	/**
	 * Delete a campaign for the specified user, of the provided adId
	 * @param u User. The user record to edit.
	 * @param adId String. The adid of the campaign to delete.
	 * @return List. The resulting list of campaigns of this user.
	 * @throws Exception on file errors.
	 */
	public List deleteCampaign(User u, String adId) throws Exception {
		for (int i=0; i< u.campaigns.size();i++) {
			Campaign c = u.campaigns.get(i);
			if (c.adId.equals(adId)) {
				u.campaigns.remove(i);
				write();
				return u.campaigns;
			}
		}
		return null;
	}
	
	/**
	 * Edit a campaign in the user. The campaign object (of the same adId) in the user object is replaced by this campaign/
	 * @param u User. The user record to edit.
	 * @param c Campaign. The campaign that replaces the campaign in user of the same adId.
	 */
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
	
	/**
	 * Read the database.json file into this object.
	 * @throws Exception on file errors.
	 */
	public void read() throws Exception {
		String content = new String(Files.readAllBytes(Paths.get(DB_NAME)));
		Database db = gson.fromJson(content,Database.class);
		users = db.users;
	}
	
	/**
	 * Write the database object to the database.json file.
	 * @throws Exception on file errors.
	 */
	public void write() throws Exception{
		String content = gson.toJson(this);
	    Files.write(Paths.get(DB_NAME), content.getBytes());
	}
}
