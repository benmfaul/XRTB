package com.xrtb.db;

import java.util.Set;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.RedissonClient;

public enum DataBaseObject  {

	INSTANCE;

	public static final String USERS_DATABASE = "users-database";
	static RedissonClient redisson;
	static ConcurrentMap<String, User> map;

	static String DBNAME = USERS_DATABASE;

	public static DataBaseObject getInstance() {
		return INSTANCE;
	}

	public static DataBaseObject getInstance(Config cfg) {
		redisson = Redisson.create(cfg);
		map = redisson.getMap("users-database");
		return INSTANCE;
	}

	public static DataBaseObject getInstance(String name) throws Exception{
		Config cfg = new Config();
		cfg.useSingleServer()
    	.setAddress("localhost:6379")
    	.setConnectionPoolSize(128);
		redisson = Redisson.create(cfg);
		map = redisson.getMap("users-database");
		return INSTANCE;
	}

	public User get(String userName) throws Exception {
		synchronized (INSTANCE) {
			return map.get(userName);
		}

	}

	public Set keySet() throws Exception {
		synchronized (INSTANCE) {
			return map.keySet();
		}
	}

	public void put(User u) throws Exception {

	
		synchronized (INSTANCE) {
			map.put(u.name,u);
		}
	}



	public synchronized void clear() throws Exception {
		
		synchronized(INSTANCE) {
			map.clear();
		}
	}

	public void remove(String who) throws Exception {
		synchronized (INSTANCE) {
			map.remove(who);
		}
	}
}
