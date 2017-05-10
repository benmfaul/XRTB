package test.java;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.aerospike.client.AerospikeClient;
import com.aerospike.redisson.RedissonClient;
import com.xrtb.common.Configuration;

public class TestFreqCapLogc {

	@Test
	public void testExpire() throws Exception {
		AerospikeClient client = new AerospikeClient("localhost", 3000);
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
		AerospikeClient client = new AerospikeClient("localhost", 3000);
		RedissonClient redisson = new RedissonClient(client);
		Set<String> set = new HashSet();
		
		for (int i=0;i<1000000; i++) {
			String key = "JUNK" + i;
			set.add(key);
			redisson.set(key,"1000",120);
		}
		
		System.out.println("Assignment complete");
		
		for (String s : set) {
			assertNotNull(redisson.get(s));
		}
		
		System.out.println("Test 1 complete");
		
		Thread.sleep(120000);
		for (String s : set) {
			assertNull(redisson.get(s));
		}
		
	}
}
