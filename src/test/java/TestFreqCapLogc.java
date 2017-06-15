package test.java;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.aerospike.client.AerospikeClient;
import com.aerospike.redisson.AerospikeHandler;
import com.aerospike.redisson.RedissonClient;
import com.xrtb.common.Configuration;

public class TestFreqCapLogc {

	@Test
	public void testExpire() throws Exception {
		AerospikeHandler client = AerospikeHandler.getInstance("localhost", 3000,300);
		RedissonClient redisson = new RedissonClient(client);
			
		redisson.del("JUNK");
		redisson.set("JUNK", "1000",5);
		Thread.sleep(4000);
		assertTrue(redisson.get("JUNK").equals("1000"));
		Thread.sleep(2000);
		assertNull(redisson.get("JUNK"));

	}
	
	@Test
	public void test1M() throws Exception {
		AerospikeHandler client = AerospikeHandler.getInstance("localhost", 3000,300);
		RedissonClient redisson = new RedissonClient(client);
		Set<String> set = new HashSet();
		
		for (int i=0;i<1000; i++) {
			String key = "JUNK" + i;
			System.out.println(key);
			set.add(key);
			redisson.set(key,key,120);
		}
		
		System.out.println("Assignment complete");
		
		for (String s : set) {
			assertNotNull(redisson.get(s));
		}
		
		System.out.println("Test 1 complete");
		
		Thread.sleep(125000);
		for (String s : set) {
			assertNull(redisson.get(s));
		}
		
	}
}
