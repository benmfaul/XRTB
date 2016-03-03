package com.xrtb.bidder;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.redisson.RedissonClient;
import org.redisson.core.RTopic;

import redis.clients.jedis.Jedis;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A publisher for REDIS based messages, sharable by multiple threads.
 * @author Ben M. Faul
 *
 */
public class Publisher implements Runnable {
	/** The objects thread */
	Thread me;
	/** The JEDIS connection used */
	String channel;
	/** The topic of messages */
	RTopic logger;
	/** The queue of messages */
	ConcurrentLinkedQueue queue = new ConcurrentLinkedQueue();

 ObjectMapper mapper = new ObjectMapper();

	/**
	 * Constructor for base class.
	 * @param conn Jedis. The REDIS connection.
	 * @param channel String. The topic name to publish on.
	 * @throws Exception. Throws exceptions on REDIS errors
	 */
	public Publisher(RedissonClient redisson, String channel)  throws Exception {
		this.channel = channel;
		logger = redisson.getTopic(channel);

		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		me = new Thread(this);
		me.start();
		
	}
	
	/**
	 * Return the publishing channel
	 * @return RTopic. The RTopic channel.
	 */
	public RTopic getChannel() {
		return logger;
	}

	@Override
	public void run() {
		String str = null;
		Object msg = null;
		Jedis jedis = Controller.bidCachePool.getResource();
		while(true) {
			try {
				if ((msg = queue.poll()) != null) {
					//System.out.println("message");
					//logger.publishAsync(msg);
					
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
	
	/**
	 * Out of band write, like when you absolutely have to send a notice now (Like a shutdown notice)
	 * @param Object
	 */
	public void writeFast(Object msg) {
		logger.publishAsync(msg);
	}

	/**
	 * Add a message to the messages queue.
	 * @param s. String. JSON formatted message.
	 */
	public void add(Object s) {
		queue.add(s);
	}
}