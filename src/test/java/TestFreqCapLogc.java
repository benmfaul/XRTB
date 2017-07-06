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

}
