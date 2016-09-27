package com.aerospike.redisson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.policy.WritePolicy;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.db.User;

public class RedissonClient {

	static AerospikeClient client;

	public static ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public RedissonClient(AerospikeClient client) {
		this.client = client;
	}

	public ConcurrentHashMap getMap(String name) throws Exception {
		Key key = new Key("test", "database", "rtb4free");

		Record record = null;
		record = client.get(null, key);
		if (record == null) {
			ConcurrentHashMap map = new ConcurrentHashMap();
			return map;
		}

		String content = (String) record.bins.get("map");
		ConcurrentHashMap map = mapper.readValue(content, ConcurrentHashMap.class);
		return map;
	}

	public Set<String> getSet(String name) throws Exception {
		String content = null;
		Key key = new Key("test", "database", "rtb4free");

		Record record = null;
		record = client.get(null, key);
		if (record == null)
			return new HashSet();
		content = (String) record.bins.get("set");
		if (content == null)
			return new HashSet();

		Set<String> blacklist = mapper.readValue(content, Set.class);
		return blacklist;
	}

	public void addMap(String name, Map map) throws Exception {
		Key key = new Key("test", "database", "rtb4free");
		String data = mapper.writer().writeValueAsString(map);
		Bin bin1 = new Bin("map", data);
		client.put(null, key, bin1);
	}

	public void addSet(String name, Set set) throws Exception {
		Key key = new Key("test", "database", "rtb4free");
		String data = mapper.writer().writeValueAsString(set);
		Bin bin1 = new Bin("set", data);
		client.put(null, key, bin1);
	}

	public void del(String skey) throws Exception {
		Key key = new Key("test", "cache", skey);
		client.delete(null, key);
	}

	public void set(String skey, String value) throws Exception {
		Key key = new Key("test", "cache", skey);
		Bin bin1 = new Bin("value", value);
		client.delete(null, key);
		client.put(null, key, bin1);
	}

	public void set(String skey, String value, int expire) throws Exception {
		WritePolicy policy = new WritePolicy();
		policy.expiration = expire;
		Key key = new Key("test", "cache", skey);
		Bin bin1 = new Bin("value", value);
		client.put(policy, key, bin1);
	}

	public String get(String skey) {
		Key key = new Key("test", "cache", skey);
		Record record = null;
		record = client.get(null, key);
		if (record == null) {
			return null;
		}

		String content = (String) record.bins.get("value");
		return content;
	}

	public Map hgetAll(String id) throws Exception {
		Key key = new Key("test", "cache", id);
		Record record = null;
		record = client.get(null, key);
		if (record == null) {
			return null;
		}

		Map map = (Map) record.bins.get("value");
		return map;
	}

	public void hmset(String id, Map m) {
		Key key = new Key("test", "cache", id);
		Bin bin1 = new Bin("value", m);
		client.put(null, key, bin1);
	}

	public void hmset(String id, Map m, int expire) throws Exception {
		WritePolicy policy = new WritePolicy();
		policy.expiration = expire;
		Key key = new Key("test", "cache", id);
		Bin bin1 = new Bin("value", m);
		client.put(policy, key, bin1);
	}

	public long incr(String id) throws Exception {
		long k = 1;
		String str = get(id);
		if (str != null) {
			k = Long.parseLong(str);
			k++;
		} else {
			k++;
		}
		set(id, Long.toString(k));
		return k;
	}

	public void expire(String id, int expire) throws Exception {
		Key key = new Key("test", "cache", id);
		Record record = null;
		record = client.get(null, key);
		if (record == null) {
			return;
		}
		Bin bin = new Bin("value", record.bins.get("value"));

		WritePolicy policy = new WritePolicy();
		policy.expiration = expire;
		client.put(policy, key,bin);
	}

	private void addList(String id, List list) {
		Key key = new Key("test", "cache", id);
		Bin bin1 = new Bin("list", list);
		client.put(null, key, bin1);
	}

	private List getList(String id) {
		String content = null;
		Key key = new Key("test", "cache", id);

		Record record = null;
		record = client.get(null, key);
		if (record == null)
			return null;

		List list = (List) record.bins.get("list");
		if (list == null)
			return new ArrayList();
		return list;

	}

	public void zadd(String id, Map m) {
		List<Map> list = getList(id);
		if (list == null) {
			list = new ArrayList();
		} else {
			Set es = m.keySet();
			Iterator<String> it = es.iterator();
			String test = it.next();
			for (int i = 0; i < list.size(); i++) {
				Map x = list.get(i);
				es = x.keySet();
				it = es.iterator();
				String s = it.next();
				if (s.equals(test)) {
					list.remove(i);
					break;
				}
			}
		}
		list.add(m);
		addList(id, list);
	}

	public void zrem(String id, String name) {
		List<Map> list = getList(id);
		if (id == null)
			return;
		for (int i = 0; i < list.size(); i++) {
			Map m = list.get(i);
			Set es = m.keySet();
			Iterator<String> it = es.iterator();
			String test = it.next();
			if (test.equals(name)) {
				list.remove(i);
				addList(id, list);
				
				System.out.println("I REMOVED: " + name);;
				return;
			}
		}
	}

	public List<String> zrangeByScore(String id, double t0, double t1) {
		List<String> rets = new ArrayList();
		List<Map> list = getList(id);
		if (list == null)
			return null;
		
		for (int i = 0; i < list.size(); i++) {
			Map m = list.get(i);
			Set es = m.keySet();
			Iterator<String> it = es.iterator();
			String name = it.next();
			double value = (Double) m.get(name);
			if (value > t0 && value < t1)
				rets.add(name);
		}
		return rets;
	}

	public int zremrangeByScore(String id, double t0, double t1) {
		List<String> rets = new ArrayList();
		List<Map> list = getList(id);
		for (int i = 0; i < list.size(); i++) {
			Map m = list.get(i);
			Set es = m.keySet();
			Iterator<String> it = es.iterator();
			String name = it.next();
			double value = (Double) m.get(name);
			if (value > t0 && value < t1)
				rets.add(name);
		}
		int k = rets.size();
		for (int i=0;i<rets.size();i++) {
			String name = rets.get(i);
			for (int j=0;j<list.size();j++) {
				Map x = list.get(j);
				Set es = x.keySet();
				Iterator<String> it = es.iterator();
				String s = it.next();
				if (s.equals(name)) {
					list.remove(j);
					break;
				}
			}
		}
		addList(id,list);
		return k;
	}

	public void shutdown() {

	}

}
