package com.xrtb.db;

import com.xrtb.RedissonClient;
import com.xrtb.common.Campaign;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public enum DataBaseObject {

	INSTANCE;

	public static ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	public static RedissonClient redisson;

	public static final String USERS_DATABASE = "users-database";
	public static final String MASTER_BLACKLIST = "master-blacklist";

	static volatile List<Campaign> campaigns;
	static volatile Set<String> set;

	static String DBNAME = USERS_DATABASE;

	public static DataBaseObject getInstance() {
	    if (redisson == null) {
            redisson = new RedissonClient();
        }
		return INSTANCE;
	}

	public static DataBaseObject getInstance(RedissonClient r) throws Exception {
	    if (redisson == null)
		    redisson = r;
		campaigns = redisson.getList(USERS_DATABASE);
		set = redisson.getSet(MASTER_BLACKLIST);
		return INSTANCE;
	}

	public static DataBaseObject getInstance(String name) throws Exception {
		redisson = new RedissonClient();
		campaigns = redisson.getList(USERS_DATABASE);
		set = redisson.getSet(MASTER_BLACKLIST);
		return INSTANCE;
	}
	
	public static boolean isBlackListed(String test) throws Exception  {
		set = redisson.getSet(MASTER_BLACKLIST);
		if (set == null)
			return false;
		return set.contains(test);
	}


	public List<Campaign> getCampaigns() throws Exception {
		List<Map> list = (List)redisson.get(USERS_DATABASE);

		String content = mapper.writeValueAsString(list);
		List<Campaign> clist = mapper.readValue(content,
				mapper.getTypeFactory().constructCollectionType(List.class, Campaign.class));

		return clist;
	}

	public void putCampaigns(List<Campaign> list) throws Exception {
		redisson.set(USERS_DATABASE, list, 300000);
	}

	public void put(List<Campaign> list) throws Exception {
		synchronized (INSTANCE) { ;
			redisson.addList(USERS_DATABASE, list);
		}
	}

	public void put(String key, Object value, long time) throws Exception {
		synchronized (INSTANCE) { ;
			redisson.set(key, value, time);
		}
	}

	public synchronized void clear() throws Exception {
		
		synchronized(INSTANCE) {
			if (campaigns == null)
				campaigns = new ArrayList<Campaign>();
			campaigns.clear();
			redisson.addList(USERS_DATABASE,campaigns);
		}
	}
}
