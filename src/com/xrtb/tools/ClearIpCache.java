package com.xrtb.tools;
import com.aerospike.redisson.AerospikeHandler;
import com.xrtb.db.Database;

public class ClearIpCache {

	public static void main(String [] args) throws Exception {
		String key = "capped_" + args[1] + args[2];
		System.out.println("key: " + key);
		AerospikeHandler spike =  AerospikeHandler.getInstance(args[0],3000 , 300);
		com.aerospike.redisson.RedissonClient redisson = new com.aerospike.redisson.RedissonClient(spike);
		Database.getInstance(redisson);
		long s = redisson.incr(key);
		System.out.println(s);
	}
}
