package com.xrtb.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.redisson.RedissonClient;

public class CookieList {

	public static void main(String args[]) throws Exception {
		int i = 0;
		String aero = "localhost:3000";
		String setName = null;
		String mapName = null;
		String op = null;
		String name = null;
		String file = null;
		boolean range = false;

		AerospikeClient client = new AerospikeClient("localhost",3000);
		
		Key key = new Key("test", "database", "rtb4free");
		
		ArrayList<String> list = new ArrayList<String>();
		
		TreeSet set = new TreeSet();
		for (i=0; i < 100000; i++) {
			list.add(Integer.toString(i));
			set.add(Integer.toString(i));
		}
		Bin bin1 = new Bin("c1x-cookies", set);
		client.put(null, key, bin1);
		
		System.out.println("Done!");
		
		Record record = client.get(null, key);
		long time = System.currentTimeMillis();
		Set<String> receivedList = (Set<String>) record.getValue("c1x-cookies");
		receivedList.contains("99999");
		System.out.println(System.currentTimeMillis() - time);
		System.out.println("Received List = " + receivedList.size());
	}
	
}
