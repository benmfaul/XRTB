package com.xrtb.bidder;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.redisson.RedissonClient;
import org.redisson.core.RTopic;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.pojo.BidRequest;

import redis.clients.jedis.Jedis;

/**
 * A publisher for REDIS based messages, sharable by multiple threads.
 * @author Ben M. Faul
 *
 */
public class BidRequestPublisher<JsonNode> extends Publisher{

	public static ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
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
		
		Jedis jedis = Controller.bidCachePool.getResource();
		while(true) {
			try {
				if ((msg = queue.poll()) != null) {
					//logger.publish(msg);
					String content = mapper
							.writer()
							.writeValueAsString(msg);
					jedis.publish(channel, content);
				}
				Thread.sleep(1);
			} catch (Exception e) {
				e.printStackTrace();
				//return;
			}
		}
	}
	
}