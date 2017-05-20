package test.java;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.aerospike.client.AerospikeClient;
import com.aerospike.redisson.AerospikeHandler;
import com.aerospike.redisson.RedissonClient;
import com.xrtb.bidder.DeadmanSwitch;

/**
 * Tests whether the bidders will stop if the accounting deadman switch is deleted in Redis
 * @author Ben M. Faul
 *
 */
public class TestDeadmanSwitch {

	/**
	 * 
	 * @throws Exception
	 */
	@Test 
	public void testSwitch() throws Exception {
		AerospikeHandler spike =  AerospikeHandler.getInstance("localhost",3000,300);
		RedissonClient redisson = new RedissonClient(spike);

			DeadmanSwitch.testmode = true;
			
			redisson.del("deadmanswitch");
			
			DeadmanSwitch d = new DeadmanSwitch(redisson,"deadmanswitch");
			Thread.sleep(1000);
			assertFalse(d.canRun());
			redisson.set("deadmanswitch", "ready",5);
			
			assertTrue(d.canRun());
			Thread.sleep(10000);
			assertFalse(d.canRun());
		}
	
	@Test
	public void testDelayedExpire() throws Exception {
		AerospikeHandler spike = AerospikeHandler.getInstance("localhost",3000,300);
		RedissonClient redisson = new RedissonClient(spike);

			
			redisson.del("xxx");
			Map m = new HashMap();
			m.put("Ben", 1);
			m.put("Peter", 2);
			redisson.hmset("xxx", m);
			
			m = redisson.hgetAll("xxx");
			assertNotNull(m);
			Long n = (Long)m.get("Ben");
			assertNotNull(n);
			
			redisson.expire("xxx", 2);
			
			m = redisson.hgetAll("xxx");
			assertNotNull(m);
			n = (Long)m.get("Ben");
			assertNotNull(n);
			
			Thread.sleep(5000);
			
			m = redisson.hgetAll("xxx");
			assertNull(m);
			
	}
}
