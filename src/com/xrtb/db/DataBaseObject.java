package com.xrtb.db;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.aerospike.client.AerospikeClient;
import com.aerospike.redisson.RedissonClient;


public enum DataBaseObject  {

	INSTANCE;
	
	public static void main(String [] args) throws Exception  {
		AerospikeClient client = new AerospikeClient("localhost", 3000);
		RedissonClient redisson = new RedissonClient(client);
		
		String content = new String(Files.readAllBytes(Paths.get("/home/ben/RTB/XRTB/database.json")), StandardCharsets.UTF_8);

		System.out.println(content);

		List<User> users = RedissonClient.mapper.readValue(content,
				RedissonClient.mapper.getTypeFactory().constructCollectionType(List.class, User.class));
		
		DataBaseObject db = DataBaseObject.getInstance(redisson);
		
		for (User u: users) {
			db.put(u);		
		}
		
		User test = db.get("ben");
		System.out.println(test);
		
	}
	
	static RedissonClient redisson;

	public static final String USERS_DATABASE = "users-database";
	public static final String MASTER_BLACKLIST = "master-blacklist";
	
	static ConcurrentMap<String, User> map;
	static Set<String> set;

	static String DBNAME = USERS_DATABASE;

	public static DataBaseObject getInstance() {
		return INSTANCE;
	}

	public static DataBaseObject getInstance(RedissonClient r) throws Exception {
		redisson = r;
		map = redisson.getMap(USERS_DATABASE);
		set = redisson.getSet(MASTER_BLACKLIST);
		return INSTANCE;
	}

	public static DataBaseObject getInstance(String name, int port) throws Exception{
		AerospikeClient spike = new AerospikeClient(name,3000);
		RedissonClient r = new RedissonClient(spike);
		redisson = r;
		map = (ConcurrentMap<String, User>) redisson.getMap(USERS_DATABASE);
		set = redisson.getSet(MASTER_BLACKLIST);
		return INSTANCE;
	}
	
	public static DataBaseObject getInstance(String name) throws Exception {
		RedissonClient r = new RedissonClient();
		redisson = r;
		map = (ConcurrentMap<String, User>) redisson.getMap(USERS_DATABASE);
		set = redisson.getSet(MASTER_BLACKLIST);
		return INSTANCE;
	}
	
	/**
	 * Return the list of users in the REDISSON database
	 * @return List<String>. The list of users
	 */
	public List<String> listUsers() throws Exception {
		map = (ConcurrentMap<String, User>) redisson.getMap(USERS_DATABASE);
		Set<Entry<String,User>> set = map.entrySet();
		List<String> list = new ArrayList();
		for (Entry<String,User> e : set) {
			list.add(e.getKey());
		}
		return list;
	}
	
	public static boolean isBlackListed(String test) throws Exception  {
		set = redisson.getSet(MASTER_BLACKLIST);
		if (set == null)
			return false;
		return set.contains(test);
	}

	public User get(String userName) throws Exception {
		synchronized (INSTANCE) {
			ConcurrentHashMap x = redisson.getMap(USERS_DATABASE);
			Object test = x.get(userName);                         // Aerospike returns map, cache2k returns User
			if (test instanceof User) {
				return (User)test;
			}
			Map z = (Map)x.get(userName);
			String content = RedissonClient.mapper.writeValueAsString(z);
			User u = RedissonClient.mapper.readValue(content,User.class);
			return u;
		}

	}

	public Set keySet() throws Exception {
		synchronized (INSTANCE) {
			map = redisson.getMap(USERS_DATABASE);
			if (map == null) {
				return new HashSet();
			}
			return map.keySet();
		}
	}

	public void put(User u) throws Exception {
		synchronized (INSTANCE) {
			map = (ConcurrentMap<String, User>) redisson.getMap(USERS_DATABASE);
			if (map == null)
				map = new ConcurrentHashMap<String, User>();
			map.put(u.name,u);
			redisson.addMap(USERS_DATABASE, map);
		}
	}
	
	public void addToBlackList(String domain) throws Exception {

		synchronized (INSTANCE) {
			set = redisson.getSet(MASTER_BLACKLIST);
			set.add(domain);
			redisson.addSet(MASTER_BLACKLIST,set);
		}
	}
	
	public void addToBlackList(List<String> list) throws Exception {
		synchronized (INSTANCE) {
			set = redisson.getSet(MASTER_BLACKLIST);
			set.addAll(list);
			redisson.addSet(MASTER_BLACKLIST,set);
		}
	}
	
	public void removeFromBlackList(String domain) throws Exception {
		synchronized (INSTANCE) {
			set = redisson.getSet(MASTER_BLACKLIST);
			set.remove(domain);
			redisson.addSet(MASTER_BLACKLIST,set);
		}
	}
	
	public List<String> getBlackList() throws Exception {
		set = redisson.getSet(MASTER_BLACKLIST);
		List<String> blackList = new ArrayList();
		Iterator<String> iter = set.iterator();
		while(iter.hasNext()) {
			try {
				
				blackList.add(iter.next());
			} catch (Exception error) {
				error.printStackTrace();
			}
		}
		Collections.sort(blackList);;
		return blackList;
	}
	
	public void clearBlackList() throws Exception {
		if (set == null)
			return;
		synchronized(INSTANCE) {
			set.clear();
			redisson.addSet(MASTER_BLACKLIST,set);
		}
	}
	
	public synchronized void clear() throws Exception {
		
		synchronized(INSTANCE) {
			if (map == null)
				map = new ConcurrentHashMap();
			map.clear();
			redisson.addMap(USERS_DATABASE,map);
		}
	}

	public void remove(String who) throws Exception {
		synchronized (INSTANCE) {
			redisson.getMap(USERS_DATABASE);
			map.remove(who);
			redisson.addMap(USERS_DATABASE,map);
		}
	}
}
