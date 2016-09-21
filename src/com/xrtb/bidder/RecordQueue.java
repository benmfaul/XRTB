package com.xrtb.bidder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.xrtb.common.Configuration;
import com.xrtb.pojo.BidResponse;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

/**
 * A consumer that ingests bid requests and records them to either REDIS pub or to disk file
 * @author Ben M. Faul
 *
 */
public class RecordQueue implements Runnable {

	ConcurrentLinkedQueue<BidResponse> queue = new ConcurrentLinkedQueue();
	Thread me;
	Jedis bidCache;

	public RecordQueue(JedisPool jedis) throws Exception {
		bidCache = jedis.getResource();
		me = new Thread(this);
		me.start();
	}

	@Override
	public void run() {
		Map m = new HashMap();
		while (true) {
			BidResponse br = queue.poll();
			if (br  != null) {
			m.clear();

			Pipeline p = bidCache.pipelined();
			m.put("ADM", br.getAdmAsString());
			m.put("PRICE", Double.toString(br.creat.price));
			if (br.capSpec != null) {
				m.put("SPEC", br.capSpec);
				m.put("EXPIRY", br.creat.capTimeout);
			}
			try {
				p.hmset(br.oidStr, m);
				p.sync();
				p.expire(br.oidStr, Configuration.getInstance().ttl);
				p.sync();
			} catch (Exception error) {
				error.printStackTrace();
			} finally {

			}
			br = null;
			} else
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
		}
	}

	public void add(BidResponse s) {
		queue.add(s);
	}

}
