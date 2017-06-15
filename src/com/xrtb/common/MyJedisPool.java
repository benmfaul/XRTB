package com.xrtb.common;
import redis.clients.jedis.Jedis;

public class MyJedisPool extends ObjectPool<Jedis> {

	public static String host = "localhost";
	public static int port = 6379;
	public MyJedisPool(int minIdle) {
		super(minIdle);
	}
	
	public MyJedisPool(final int minIdle, final int maxIdle, final long validationInterval) {
		super(minIdle, maxIdle, validationInterval);
	}

	@Override
	protected Jedis createObject() {
		Jedis jedis = new Jedis(host,port);
		jedis.connect();
		
		return jedis;
	}
	
}