package com.xrtb.db;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.RedissonClient;

import com.xrtb.common.Configuration;

public enum DataBaseObject implements Runnable {

	INSTANCE;

	public static final String USERS_DATABASE = "users-database";
	static RedissonClient redisson;
	static ConcurrentMap<String, User> map;
	static Thread me;

	static volatile CountDownLatch latch;
	volatile CountDownLatch yourLatch;
	volatile User target;
	volatile Set keySet;
	volatile String name;
	volatile String operation;

	public static DataBaseObject getInstance() {
		return INSTANCE;
	}

	public static DataBaseObject getInstance(RedissonClient redisson2, Config redissonConfig) {
		me = new Thread(INSTANCE);
		latch = new CountDownLatch(1);
		me.start();
		redisson = redisson2;
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

	public synchronized User get(String userName) throws Exception {
		yourLatch = new CountDownLatch(1);
		//System.out.println("++++++++++ YOUR LATCH ARMED");
		name = userName;
		operation = "get";

		//System.out.println("++++++++++ BUNCE");
		bounce();
		//System.out.println("---------- BUNCE");

		//System.out.println("---------- YOUR LATCH DISARMED");
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

	private synchronized void bounce() throws Exception {
		latch.countDown();
		//System.out.println("++++++++++ WAIT ON YOUR LATCH");
		yourLatch.await();
		operation = "";
		yourLatch = null;
		latch = new CountDownLatch(1);
	}

	public void close() {

	}

	public synchronized void clear() throws Exception {
		yourLatch = new CountDownLatch(1);
		operation = "clear";

		bounce();
	}

	public synchronized void remove(String who) throws Exception {
		yourLatch = new CountDownLatch(1);
		name = who;
		operation = "remove";

		bounce();
	}

	public void run() {
		while (true) {
			try {
				if (latch != null) {
					//System.out
					//		.println("-------------------- LATCH WAITING  !!!!!!!!!!!!!");
					latch.await();
					//System.out
					//		.println("-------------------- LATCH FIRED  !!!!!!!!!!!!!");
					latch = null;
					switch (operation) {
					case "get":
						//System.out.println("-------------------- GET STARTING");
						target = map.get(name);
						//System.out.println("-------------------- GET COMPLETE!!!!!!!!!!!!!!");
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
					default:
						System.out.println("UNDEFINED OPERATION: " + operation);
					}
					if (yourLatch == null)
						Thread.sleep(100);
				//	System.out
					//		.println("-------------------- YOUR LATCH FIRING !!!!!!!!!!!!!");
					yourLatch.countDown();
				//	System.out
					//		.println("-------------------- YOUR LATCH COUNTED DOWN");
				} else
					Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}

	}

	public static void halt() {
		me.interrupt();
	}
}
