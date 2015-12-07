package com.xrtb.db;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

import org.redisson.Config;
import org.redisson.Redisson;

import com.xrtb.common.Configuration;


public enum DataBaseObject implements Runnable {

	INSTANCE;
	
	public static final String USERS_DATABASE = "users-database";
	static Redisson redisson;
	static ConcurrentMap<String,User> map;
	static Thread me;
	
	static CountDownLatch latch;
	CountDownLatch yourLatch;
	User target;
	Set keySet;
	String name;
	String operation;
	
	public static DataBaseObject getInstance() {
		return INSTANCE;
	}
	
	
	public static DataBaseObject getInstance(Redisson red, Config redissonConfig) {
		me = new Thread(INSTANCE);
		latch = new CountDownLatch(1);
		me.start();
		redisson = red;
		map = (ConcurrentMap) redisson.getMap(USERS_DATABASE);
		return INSTANCE;
	}
	
	public static DataBaseObject getInstance(String name) {
		me = new Thread(INSTANCE);
		latch = new CountDownLatch(1);
		me.start();
		redisson = Redisson.create();
		map = (ConcurrentMap) redisson.getMap(name);
		return INSTANCE;
	}
	
	
	public  User get(String userName) throws Exception {
		yourLatch = new CountDownLatch(1);
		name = userName;
		operation = "get";

		bounce();

		return target;
	}
	
	public synchronized Set keySet() throws Exception {
		yourLatch = new CountDownLatch(1);
		operation = "keyset";

		bounce();

		return keySet;
	}
	
	public synchronized void put(User u) throws Exception {
		yourLatch = new CountDownLatch(1);
		target = u;
		operation = "put";
		
		bounce();
	}
	
	private void bounce() throws Exception{
		latch.countDown();
		yourLatch.await();
	}
	
	public void close() {
		
	}
	
	public synchronized void clear() throws Exception {
		yourLatch = new CountDownLatch(1);
		operation = "clear";
		
		latch.countDown();
		yourLatch.await();
	}
	
	public synchronized void remove(String who) throws Exception {
		yourLatch = new CountDownLatch(1);
		name = who;
		operation = "remove";
		
		latch.countDown();
		yourLatch.await();
	}
	
	public void run() {
		while(true) {
			try {
				latch.await();
				switch(operation) {
				case "get":
					target = map.get(name);
					break;
				case "put":
					map.put(target.name, target);
					break;
				case "clear":
					map.clear();
					break;
				case "keyset":
					keySet = map.keySet();
					break;
				case "remove":
					map.remove(name);
					break;
				}	
				yourLatch.countDown();
				latch = new CountDownLatch(1);
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				return;
			}
		}
	}
	
	public static void halt() {
		me.interrupt();
	}
}
