package com.xrtb.tests;

import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.xrtb.db.DataBaseObject;
import com.xrtb.db.User;
import com.xrtb.pojo.BidResponse;

import junit.framework.TestCase;

/**
 * Tests miscellaneous classes.
 * @author Ben M. Faul
 *
 */

public class TestDatabaseObject {

	static DataBaseObject db;
	@BeforeClass
	
	public static void setup() {
		try {
			db = DataBaseObject.getInstance("junk-db");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void stop() {
	
	}
	
	@Test
	public void testSingleThreadAccess()  {
		User u = new User();
		u.name = "Ben";
		
		try {
			db.getInstance().put(u);
			u = null;
			u = db.getInstance().get("Ben");
			assertTrue(u.name.equals("Ben"));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test 
	public void testTwoThreadAccess() {
		User u = new User();
		Gson gson = new Gson();
		u.name = "Ben";
		
		CountDownLatch latch = new CountDownLatch(2);
		CountDownLatch flag = new CountDownLatch(1);
		JunkUser ben = new JunkUser(flag,latch,"Ben");
		JunkUser peter = new JunkUser(flag,latch,"Peter");
		
		flag.countDown();
		try {
			latch.await();
			
			Thread.sleep(200);
			
			u = db.get("Ben");
			System.out.println("BEN: " + gson.toJson(u));
			assertTrue(u.name.equals("Ben"));
			
			u = null;
			
			Thread.sleep(200);
			
			u = db.get("Peter");
			System.out.println("Peter" + gson.toJson(u));
			assertTrue(u.name.equals("Peter"));
			
			db.clear();
			Thread.sleep(200);
			u = db.get("Ben");
			assertNull(u);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}

}

class JunkUser implements Runnable {
	Thread me = null;
	CountDownLatch latch;
	CountDownLatch flag;
	User u;
	String name;
	
	public JunkUser(CountDownLatch flag, CountDownLatch latch, String name)  {
		this.flag = flag;
		this.latch = latch;
		this.name = name;
		me = new Thread(this);
		u = new User();
		u.name = name;
		me.start();
	}
	public void run() {
	
		System.out.println("User " + name + " running.");
		try {
			flag.await();
			DataBaseObject.getInstance().put(u);
			latch.countDown();
			System.out.println("User " + name + " complete");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
