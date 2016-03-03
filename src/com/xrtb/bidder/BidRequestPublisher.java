package com.xrtb.bidder;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.redisson.RedissonClient;
import org.redisson.core.RTopic;

/**
 * A publisher for REDIS based messages, sharable by multiple threads.
 * @author Ben M. Faul
 *
 */
public class BidRequestPublisher<JsonNode> extends Publisher{

	/**
	 * Constructor for base class.
	 * @param conn Jedis. The REDIS connection.
	 * @param channel String. The topic name to publish on.
	 * @throws Exception. Throws exceptions on REDIS errors
	 */
	public BidRequestPublisher(RedissonClient redisson, String channel)  throws Exception {
		super(redisson,channel);
		
	}
	

	@Override
	public void run() {
		String str = null;
		Object msg = null;
		while(true) {
			try {
				if ((msg = queue.poll()) != null) {
					logger.publish(msg);
				}
				Thread.sleep(1);
			} catch (Exception e) {
				e.printStackTrace();
				//return;
			}
		}
	}
	
}