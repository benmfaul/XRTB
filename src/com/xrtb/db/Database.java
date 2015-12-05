package com.xrtb.db;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.redisson.Redisson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;

/**
 * A class that makes a simple database for use by the Campaign admin portal
 * @author Ben M. Faul
 *
 */
public class Database {
	
	public static final String USERS_DATABASE = "users-database";
	/** The name of the database file */
	transient public static String DB_NAME = "database.json";
	/** The file's encoding */
	final static Charset ENCODING = StandardCharsets.UTF_8;
	/** A list of users, this is the root node of the database (a list of users, which has a name and a map of campaigns */
	//public List<User> users;
	/** Serialier for the JSON of this class */
	public static transient Gson gson = new GsonBuilder().setPrettyPrinting().create();
	

	public void clear() {
		Redisson redisson = Redisson.create(Configuration.getInstance().redissonConfig);
		ConcurrentMap<String,User> map = (ConcurrentMap) Configuration.getInstance().redisson.getMap(USERS_DATABASE);
		
		map.clear();
		
		redisson.shutdown();
	}

	/**
	 * Open (and create if necessary) the database file
	 */
	public Database() {
		Redisson redisson = null;
		try {
			redisson = Redisson.create(Configuration.getInstance().redissonConfig);
			ConcurrentMap<String,User> map = (ConcurrentMap) Configuration.getInstance().redisson.getMap(USERS_DATABASE);
			
			map = (ConcurrentMap) Configuration.getInstance().redisson.getMap(USERS_DATABASE);
			Set set = map.keySet();
			Iterator<String> it = set.iterator();
			while(it.hasNext()) {
				User u = map.get(it.next());
				for (Campaign c : u.campaigns) {
					c.encodeAttributes();
					c.encodeCreatives();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (redisson != null)
			redisson.shutdown();
	}
	
	/**
	 * Returns a list of user names in the database.
	 * @return List. The list of the user names in the database.
	 */
	public List<String> getUserList() {
		List<String> list = new ArrayList();
		Redisson redisson = Redisson.create(Configuration.getInstance().redissonConfig);
		ConcurrentMap<String,User> map = (ConcurrentMap) Configuration.getInstance().redisson.getMap(USERS_DATABASE);
		
		Set set = map.keySet();
		Iterator<String> it = set.iterator();
		while(it.hasNext()) {
			list.add(it.next());
		}
		
		redisson.shutdown();
		return list;
	}
	
	/**
	 * Creates a user of the given name.
	 * @param name String name.
	 * @throws Exception if there is an error reading or writing the database file.
	 */
	public void createUser(String name) throws Exception {
		Redisson redisson = Redisson.create(Configuration.getInstance().redissonConfig);
		ConcurrentMap<String,User> map = (ConcurrentMap) Configuration.getInstance().redisson.getMap(USERS_DATABASE);
		Set set = map.keySet();
		Iterator<String> it = set.iterator();
		while(it.hasNext()) {
			String id = it.next();
			if (id.equals(name))
				redisson.shutdown();
				return;
		}

		String content = new String(Files.readAllBytes(Paths.get("stub.json")));
		Campaign c = new Campaign(content);
		c.adId = name + ":" + c.adId;
		User u = new User(name);
		editCampaign(u,c);
		map.put(u.name,u);
		
		redisson.shutdown();
	}
	
	/**
	 * Delete the user from the database.
	 * @param name String. The name of the user to delete
	 */
	public void deleteUser(String name) {
		Redisson redisson = Redisson.create(Configuration.getInstance().redissonConfig);
		ConcurrentMap<String,User> map = (ConcurrentMap) Configuration.getInstance().redisson.getMap(USERS_DATABASE);
		
		map.remove(name);
		
		redisson.shutdown();
	}
	
	/**
	 * Return a campaign of the given name and adId.
	 * @param name String name. The user name.
	 * @param id String. the adId of the campaign to return.
	 * @return Campaign. The campaign to return.
	 */
	public Campaign getCampaign(String name, String id) {
		Redisson redisson = Redisson.create(Configuration.getInstance().redissonConfig);
		ConcurrentMap<String,User> map = (ConcurrentMap) Configuration.getInstance().redisson.getMap(USERS_DATABASE);
		User u = map.get(name);
		redisson.shutdown();
		if (u == null) {
			return null;
		}
		for (Campaign c : u.campaigns) {
			if (c.adId.equals(id))  {
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
		Redisson redisson = Redisson.create(Configuration.getInstance().redissonConfig);
		ConcurrentMap<String,User> map = (ConcurrentMap) Configuration.getInstance().redisson.getMap(USERS_DATABASE);
		User u =  map.get(name);
		redisson.shutdown();
		return u;
	}
	
	/**
	 * Return campaigns of the given user as a JSON string.
	 * @param name String. The user name.
	 * @return String. The campaigns of this user in a JSON string.
	 */
	public String getCampaignsAsString(String name) {
		User u = getUser(name);
		return gson.toJson(u);
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
	 * Update the user map in the global map
	 * @param u User. The user object to put into global context.
	 */
	public  void update(User u) {
		Redisson redisson = Redisson.create(Configuration.getInstance().redissonConfig);
		ConcurrentMap<String,User> map = (ConcurrentMap) Configuration.getInstance().redisson.getMap(USERS_DATABASE);
		
		map.put(u.name,u);
		
		redisson.shutdown();
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
		Redisson redisson = Redisson.create(Configuration.getInstance().redissonConfig);
		ConcurrentMap<String,User> map = (ConcurrentMap) Configuration.getInstance().redisson.getMap(USERS_DATABASE);
		
		List camps = new ArrayList();
		Set set = map.keySet();
		Iterator<String> it = set.iterator();
		while(it.hasNext()) {
			User u = map.get(it.next());
			for (Campaign c : u.campaigns) {
				camps.add(c);
			}
		}
		redisson.shutdown();
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
		Redisson redisson = Redisson.create(Configuration.getInstance().redissonConfig);
		ConcurrentMap<String,User> map = (ConcurrentMap) Configuration.getInstance().redisson.getMap(USERS_DATABASE);
		
		for (int i=0; i< u.campaigns.size();i++) {
			Campaign c = u.campaigns.get(i);
			if (c.adId.equals(adId)) {
				u.campaigns.remove(i);
				map.put(u.name,u);
				update(u);
				redisson.shutdown();
				return u.campaigns;
			}
		}
		
		redisson.shutdown();
		return null;
	}
	
	/**
	 * Edit a campaign in the user. The campaign object (of the same adId) in the user object is replaced by this campaign/
	 * @param u User. The user record to edit.
	 * @param c Campaign. The campaign that replaces the campaign in user of the same adId.
	 */
	public void editCampaign(User u, Campaign c) {
		Redisson redisson = Redisson.create(Configuration.getInstance().redissonConfig);
		ConcurrentMap<String,User> map = (ConcurrentMap) Configuration.getInstance().redisson.getMap(USERS_DATABASE);
		
		for (int i=0;i<u.campaigns.size();i++) {
			Campaign x = u.campaigns.get(i);
			if(x.adId.equals(c.adId)) {
				u.campaigns.remove(i);
				u.campaigns.add(c);
				map.put(u.name,u);
				update(u);
				redisson.shutdown();
				return;
			}
		}
		u.campaigns.add(c);
		update(u);
		redisson.shutdown();
	}
	
	/**
	 * Read the database.json file into this object.
	 * @param db String. The name of the JSON database file to read
	 * @return List. A list of user objects in the database.
	 * @throws Exception on file errors.
	 */
	public List<User> read(String db) throws Exception {
		String content = new String(Files.readAllBytes(Paths.get(db)));
		List<User> users = gson.fromJson(content, new TypeToken<List<User>>(){}.getType());
		return users;
	}
	
	/**
	 * Write the database object to the database.json file.
	 * @throws Exception on file errors.
	 */
	public void write() throws Exception{
		Redisson redisson = Redisson.create(Configuration.getInstance().redissonConfig);
		ConcurrentMap<String,User> map = (ConcurrentMap) Configuration.getInstance().redisson.getMap(USERS_DATABASE);
		
		List<User> list = new ArrayList();
		
		List camps = new ArrayList();
		Set set = map.keySet();
		Iterator<String> it = set.iterator();
		while(it.hasNext()) {
			User u = map.get(it.next());
			list.add(u);
		}
		String content = gson.toJson(list);
	    Files.write(Paths.get(DB_NAME), content.getBytes());
	    
	    redisson.shutdown();
	}
}
