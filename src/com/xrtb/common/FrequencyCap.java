package com.xrtb.common;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

public class FrequencyCap {

	public String specification;
	public int hours;
	public int cap;
	String redisKey;
	int time;
	
	boolean isCapped = false;
	
	public FrequencyCap(String campaignid, String creativeid, String specification, int hours, int cap) {
		redisKey = campaignid + ":" + creativeid + ":";
		time = 3600 * hours;
		this.cap = cap;
	}
	
	/**
	 * Call this on a bid
	 * @return
	 */
	public boolean isCapped() {
		return isCapped;
	}
	
	/**
	 * Call this on a win
	 * @param jedis
	 * @param kvalue
	 */
	public void doCapped(Jedis jedis, String kvalue) {
		StringBuilder sb = new StringBuilder(redisKey);
		sb.append(kvalue);
		String key = sb.toString();
		Response<Long> response = null;
		long value = 0;
		
		// if less than 0, then establish a new cap
		synchronized (jedis) {
			Pipeline p = jedis.pipelined();
			try {
				response = p.decr(key);
				p.exec();
			} catch (Exception error) {

			} finally {
				p.sync();
			}
		}
		
		if (response != null)
			value = response.get();
		
		if (value <= 0)      
			isCapped = true;
		isCapped = false;
	}
	
	public void setCapped(Jedis jedis, String kvalue) {
		StringBuilder sb = new StringBuilder(redisKey);
		sb.append(kvalue);
		String key = sb.toString();
		if (jedis.exists(key))
			return;
		
		synchronized (jedis) {
			Pipeline p = jedis.pipelined();
			try {
				p.incrBy(key, cap);
				p.expire(key, time);
				p.exec();
			} catch (Exception error) {

			} finally {
				p.sync();
			}
		}
	}
}
