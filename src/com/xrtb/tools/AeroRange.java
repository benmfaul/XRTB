package com.xrtb.tools;
import java.io.BufferedReader;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.ehcache.sizeof.SizeOf;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.Value;
import com.aerospike.client.query.Filter;
import com.aerospike.client.query.RecordSet;
import com.aerospike.client.query.Statement;
import com.aerospike.redisson.RedissonClient;
import com.xrtb.bidder.Controller;
import com.xrtb.blocks.NavMap;
import com.xrtb.common.Configuration;

public class AeroRange {
	
	static final SizeOf sizeOf = SizeOf.newInstance();
	
	public static void main(String args[]) throws Exception {
		AerospikeClient client = new AerospikeClient(args[0], 3000);
		String skey = "accountingsystem";
		Key key = new Key("test", "cache", skey);
		
		while(true) {
			Record record = null;
			record = client.get(null, key);
			String value = (String)record.bins.get("value");
			System.out.println(value);
			Thread.sleep(1000);
		}
	}
}
	