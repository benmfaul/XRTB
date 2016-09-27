package test.java;

import static org.junit.Assert.*;



import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xrtb.db.DataBaseObject;
import com.xrtb.db.User;
import com.xrtb.tools.DbTools;

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
			db = DataBaseObject.getInstance("localhost");
			System.out.println("******************  TestDatabaseObject");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void stop() {
	
	}
	
	/*@Test
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
	}*/
	
	@Test 
	public void testTwoThreadAccessDatabase() {
		User u = new User();
		u.name = "Ben";
		
		CountDownLatch latch = new CountDownLatch(2);
		CountDownLatch flag = new CountDownLatch(1);
		
		JunkUser ben = new JunkUser(flag,latch,"Ben");
		JunkUser peter = new JunkUser(flag,latch,"Peter");
		
		flag.countDown();
		try {
			latch.await(5, TimeUnit.SECONDS);

			System.out.println("Check ben");
			u = db.get("Ben");
			System.out.println("BEN: " + DbTools.mapper.writer().withDefaultPrettyPrinter().writeValueAsString(u));
			assertTrue(u.name.equals("Ben"));
			
			u = null;

			System.out.println("Check peter");
			u = db.get("Peter");
			System.out.println("Peter" + DbTools.mapper.writer().withDefaultPrettyPrinter().writeValueAsString(u));
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
	
	@Test 
	public void testTwoThreadAccessBlackList() throws Exception {
		
		CountDownLatch latch = new CountDownLatch(1);
		CountDownLatch flag = new CountDownLatch(1);
		
		db.clearBlackList();
		db.addToBlackList("aaa");
		db.addToBlackList("bbb");
		
		assertTrue(db.isBlackListed("aaa"));
		assertTrue(db.isBlackListed("bbb"));
		assertFalse(db.isBlackListed("ccc"));
		
		JunkDomainUser u = new JunkDomainUser(flag,latch,"ben","aaa");
		flag.countDown();
		try {
			latch.await();
			
			assertTrue(u.test);
			
			latch = new CountDownLatch(1);
			flag = new CountDownLatch(1);
			u = new JunkDomainUser(flag,latch,"ben","aaa");
			
			Thread.sleep(500);
			db.removeFromBlackList("aaa");
			flag.countDown();
			latch.await();
			assertFalse(u.test);
			
			Thread.sleep(200);

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
	
		try {
			flag.await();
			System.out.println("User " + name + " running.");
			TestDatabaseObject.db.put(u);
			User x = TestDatabaseObject.db.get(name);
			if (x == null) {
				System.err.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			}
			
			latch.countDown();
			System.out.println("User " + name + " complete");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

class JunkDomainUser implements Runnable {
	Thread me = null;
	CountDownLatch latch;
	CountDownLatch flag;
	String name;
	boolean test = false;
	String what;
	
	public JunkDomainUser(CountDownLatch flag, CountDownLatch latch, String name, String what)  {
		this.flag = flag;
		this.latch = latch;
		this.name = name;
		this.what = what;
		me = new Thread(this);
		me.start();
		
	}
	public void run() {
	
		try {
			flag.await();
			System.out.println("User " + name + " running.");
			test =TestDatabaseObject.db.isBlackListed(what);	
			latch.countDown();
			System.out.println("User " + name + " complete");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}


