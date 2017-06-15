package com.aerospike.redisson;

import java.util.ArrayList;


import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.policy.WritePolicy;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A Replacement for the Redisson object. This class is a serialized (JSON) interface to the Aerospike/Cache2k database.
 * @author Ben M. Faul
 *
 */
public class RedissonClient {

	/** The aerorpike client */
	//static AerospikeClient client;
	static AerospikeHandler ae;
	
	/** If aerospike is not used, the cache (bids) database in cache2k form */
	static Cache cache;
	/** If aerospike is not used, the cache database of the User and Blacklist object */
	static Cache cacheDb;

	/** The JSON encoder/decoder object */
	public static ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	/**
	 * Instantiate the Redisson object with the aerospike client.
	 * @param client AerospileClient. The global aerospike object 
	 */
	//public RedissonClient(AerospikeClient client) {
		//this.client = client;
	//}

	public RedissonClient(AerospikeHandler handler) {
		ae = handler;
	}
	
	/**
	 * Instantiate the Redisson object using the Cache2k systen (embedded cache, single server system).
	 */
	public RedissonClient() {
		cache = new Cache2kBuilder<String,Object>(){}.expireAfterWrite(300, TimeUnit.SECONDS).build();
		cacheDb = new Cache2kBuilder<String,Object>(){}.build();
	}
	
	/**
	 * Is this a cache2k system?
	 * @return boolean. Returns true if NOT using aerospike
	 */
	public boolean isCache2k() {
		if (cache != null)
			return true;
		return false;
	}

	/**
	 * Return the User object (as a map) from the database.
	 * @param name String. the name of the user.
	 * @return ConcurrentHashMap. The map representation of the user.
	 * @throws Exception on cache2k/aerorpike errors.
	 */
	public ConcurrentHashMap getMap(String name) throws Exception {
		if (ae == null) {
            return (ConcurrentHashMap)cacheDb.peek(name);
		}
		
		Key key = new Key("test", "database", "rtb4free");

		Record record = null;
		record = ae.getClient().get(null, key);
		if (record == null) {
			return new ConcurrentHashMap();
		}

		String content = (String) record.bins.get("map");
		return mapper.readValue(content, ConcurrentHashMap.class);
	}

	/**
	 * Return a Set of Strings from the cache
	 * @param name. The name of the set.
	 * @return Set. A set of strings.
	 * @throws Exception on cache2k/aerospike errors.
	 */
	public Set<String> getSet(String name) throws Exception {
		String content = null;
		if (ae == null) {
			Set<String> set = (Set<String>) cacheDb.peek(name);
			if (set == null)
				return new HashSet();
			return set;
		}
		
		Key key = new Key("test", "database", "rtb4free");

		Record record = null;
		record = ae.getClient().get(null, key);
		if (record == null)
			return new HashSet();
		content = (String) record.bins.get("set");
		if (content == null)
			return new HashSet();

		return mapper.readValue(content, Set.class);
	}

	/**
	 * Add a map (a user map) to the the cache or aerorspike.
	 * @param name String. The name of the map.
	 * @param map Map. The map of the User object.
	 * @throws Exception on cache2k/aerospike errors
	 */
	public void addMap(String name, Map map) throws Exception {
		if (ae == null) {
			cacheDb.put(name,map);
			return;
		}
		
		Key key = new Key("test", "database", "rtb4free");
		String data = mapper.writer().writeValueAsString(map);
		Bin bin1 = new Bin("map", data);
		ae.getClient().put(null, key, bin1);
	}

	/**
	 * Ass a set of strings to the cache or aerospike. Used for blacklists.
	 * @param name String. The name of the set of strings.
	 * @param set Set. The set of strings.
	 * @throws Exception
	 */
	public void addSet(String name, Set set) throws Exception {
		if (ae == null) {
			cacheDb.put(name,set);
			return;
		}
		
		Key key = new Key("test", "database", "rtb4free");
		String data = mapper.writer().writeValueAsString(set);
		Bin bin1 = new Bin("set", data);
		ae.getClient().put(null, key, bin1);
	}

	/**
	 * Delete a bid key from the cache/aerospile.
	 * @param skey String. The key name.
	 * @throws Exception on Aerospike/cache errors.
	 */
	public void del(String skey) {
		if (ae == null) {
			cache.remove(skey);
			return;
		}
		
		Key key = new Key("test", "cache", skey);
		ae.getClient().delete(null, key);
	}

	/**
	 * Set a key value as string.
	 * @param skey String. The key name.
	 * @param value String. The value.
	 * @throws Exception on aerorpike or cache errors.
	 */
	public void set(String skey, String value)  {
		if (ae == null) {
			cache.put(skey, value);
			return;
		}
		Key key = new Key("test", "cache", skey);
		Bin bin1 = new Bin("value", value);
		ae.getClient().delete(null, key);
		ae.getClient().put(null, key, bin1);
	}

	/**
	 * Set a key value as string with an expiration (No expiration set on cache2k, it is already set 
	 * @param skey String. The key name.
	 * @param value String. The value.
	 * @param expire int. The number of seconds before expiring.
	 * @throws Exception on aerorpike or cache errors.
	 */
	public void set(String skey, String value, int expire) throws Exception {
		if (ae == null) {
			cache.put(skey, value);
			return;
		}
		
		AerospikeClient client = ae.getClient();
		if (client == null)
			return;
		
		WritePolicy policy = new WritePolicy();
		policy.expiration = expire;
		Key key = new Key("test", "cache", skey);
		Bin bin1 = new Bin("value", value);
		ae.getClient().put(policy, key, bin1);
	}
	
	

	/**
	 * Given a key, return the string value.
	 * @param skey String.
	 * @return String. The value of the key.
	 */
	public String get(String skey) throws Exception {
		if (ae == null) {
			return (String) cache.peek(skey);
		}
		
		AerospikeClient client = ae.getClient();
		if (client == null) {
			throw new Exception("NULL POINTER FOR GET");
		}
		
		Key key = new Key("test", "cache", skey);
		Record record = null;
		record = client.get(null, key);
		if (record == null) {
			return null;
		}

		return (String) record.bins.get("value");
	}

	/**
	 * Mimic a REDIS hgetAll operation.
	 * @param id String. They key to get.
	 * @return Map. The map stored at 'key'
	 * @throws Exception on aerospike/cache2k errors.
	 */
	public Map hgetAll(String id)  {
		if (ae == null) {
			return (Map)cache.peek(id);
		}
		
		Key key = new Key("test", "cache", id);
		Record record = null;
		record = ae.getClient().get(null, key);
		if (record == null) {
			return null;
		}

		Map map = (Map) record.bins.get("value");
		return map;
	}

	/**
	 * Mimic a REDIS mhset operation.
	 * @param id String. The key of the map.
	 * @param m Map. The map to set.
	 */
	public void hmset(String id, Map m) throws Exception {
		if (ae == null) {
			cache.put(id, m);
			return;
		}
		
		Key key = new Key("test", "cache", id);
		Bin bin1 = new Bin("value", m);
		ae.getClient().put(null, key, bin1);
	}

	/**
	 * Do a mhset with expire (No op on cache2k, expiry already set globally
	 * @param id String. The key name.
	 * @param m Map. The value to set.
	 * @param expire int. The number of seconds before expiry.
	 * @throws Exception on Cache2k or aerospike errors.
	 */
	public void hmset(String id, Map m, int expire) throws Exception  {
		if (ae == null) {
			cache.put(id, m);
			return;
		}
		WritePolicy policy = new WritePolicy();
		policy.expiration = expire;
		Key key = new Key("test", "cache", id);
		Bin bin1 = new Bin("value", m);
		ae.getClient().put(policy, key, bin1);
	}

	/**
	 * Mimic a REDIS incr operation.
	 * @param id String. The key value to increment.
	 * @return long. The incremented value. Returns 1 if id didn't exist.
	 * @throws Exception on cache2k or aerospike errors.
	 */
	public long incr(String id) throws Exception {
		if (ae == null) {
			Long v = (Long)cache.peek(id);
			if (v == null) {
				v = new Long(0);
			}
			v++;
			cache.put(id, v);
			return v;
		}
		
		long k = 0;
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

	/**
	 * Expire a key (no op on Cache2k, expirt is set globally for it).
	 * @param id String. The key to expire.
	 * @param expire int. The number of seconds before expiration.
	 * @throws Exception on cache2k or Aerorpike errors.
	 */
	public void expire(String id, int expire) throws Exception {
		if (cache != null) {
			return;
		}
		
		Key key = new Key("test", "cache", id);
		Record record = null;
		record = ae.getClient().get(null, key);
		if (record == null) {
			return;
		}
		Bin bin = new Bin("value", record.bins.get("value"));

		WritePolicy policy = new WritePolicy();
		policy.expiration = expire;
		ae.getClient().put(policy, key,bin);
	}

	/**
	 * Add a list to the cach2k/Aerorpike
	 * @param id String. The name of the value.
	 * @param list List. The value to set, a list.
	 */
	public void addList(String id, List list) throws Exception {
		if (ae == null) {
			cacheDb.put(id, list);
			return;
		}
		
		Key key = new Key("test", "cache", id);
		Bin bin1 = new Bin("list", list);
		ae.getClient().put(null, key, bin1);
	}

	/**
	 * Return a list from the aerorpike or cache2k.
	 * @param id String. The key to get.
	 * @return List. The list to return.
	 */
	public List getList(String id) throws Exception {
		if (ae == null) {
			Object o = cacheDb.peek(id);
			if (o != null)
				return (List)o;
			else
				return new ArrayList();
		}
		String content = null;
		Key key = new Key("test", "cache", id);

		Record record = null;
		record = ae.getClient().get(null, key);
		if (record == null)
			return null;

		List list = (List) record.bins.get("list");
		if (list == null)
			return new ArrayList();
		return list;

	}

	/**
	 * Mimic a REDIS ZADD.
	 * @param id String. The key to add.
	 * @param m Map. The map to add to the sorted list.
	 */
	public void zadd(String id, Map m) throws Exception {
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

	/**
	 * Remove a map from the mimicked zadd
	 * @param id String. The key of the list
	 * @param name String. The name what to remove from the list.
	 */
	public void zrem(String id, String name) throws Exception {
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

	/**
	 * Return a range of names from the list by score
	 * @param id String. The key of the list to range over.
	 * @param t0 double. The lower value to range over.
	 * @param t1 double. The upper value to range over.
	 * @return List. A list of strings that exist between t0 and t1
	 */
	public List<String> zrangeByScore(String id, double t0, double t1) throws Exception {
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

	/**
	 * Remove all names with scores > t0 and < t1
	 * @param id String. The list to range over.
	 * @param t0 double. The lower range.
	 * @param t1 double. The upper range.
	 * @return List. The list of names removed.
	 */
	public int zremrangeByScore(String id, double t0, double t1) throws Exception {
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

	/**
	 * No op, not used only for redisson compatibility.
	 */
	public void shutdown() {

	}
	
	/**
	 * Set a key value as string with an expiration (No expiration set on cache2k, it is already set 
	 * @param skey String. The key name.
	 * @param value String. The value.
	 * @throws Exception on aerorpike or cache errors.
	 */
	public void set(String set, String skey, Object value) throws Exception   {
		WritePolicy policy = new WritePolicy();
		Key key = new Key("test", set, skey);
		Bin bin1 = new Bin("value", value);
		ae.getClient().put(null, key, bin1);
	}
	
	

	/**
	 * Given a key, return the string value.
	 * @param skey String.
	 * @return String. The value of the key.
	 */
	public Object get(String set, String skey) throws Exception {
	
		Key key = new Key("test", set, skey);
		Record record = null;
		record = ae.getClient().get(null, key);
		if (record == null) {
			return null;
		}

		return record.bins.get("value");
	}

}
