package com.aerospike.redisson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.aerospike.client.AerospikeClient;

public enum AerospikeHandler {

	INSTANCE;
	
	static List<AerospikeClient> clients = new ArrayList();
	static int count = 0;
	static int port;
	static String host;
	
	public static AerospikeHandler getInstance() {
		return INSTANCE;
	}
	
	public static AerospikeHandler getInstance(String host, int port, int connections) {
		INSTANCE.host = host;
		INSTANCE.port = port;
		INSTANCE.count = connections / 300;
		if (connections % 300 > 0)
			INSTANCE.count++;
		
		for (int i=0;i<INSTANCE.count;i++) {
			AerospikeClient x = new AerospikeClient(host,port);
			INSTANCE.clients.add(x);
		}
		INSTANCE.count = INSTANCE.clients.size();
		return INSTANCE;
	}
	
	public int getCount() {
		return count;
	}
	
	public AerospikeClient getClient() {
		int randomNum = ThreadLocalRandom.current().nextInt(0, count);
		return clients.get(randomNum);
	}
	
	public static void reset() throws Exception {
		for (AerospikeClient c : clients) {
			try {
				c.close();
			} catch (Exception error) {
				
			}
		}
		clients.clear();
		for (int i=0;i<count;i++) {
			AerospikeClient x = new AerospikeClient(host,port);
			clients.add(x);
		}
		
		return;
		
	}
}
