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
		AerospikeClient client = new AerospikeClient("localhost", 3000);
		String skey = "junk";
		Set<String> set = null;
		
		double time = System.currentTimeMillis();
		if (args.length != 0 && args[0].equals("r")) {
			Key key = new Key("test", "cache", skey);
			Record record = null;
			record = client.get(null, key);
			set = (Set)record.bins.get("value");
		} else {
			set = new HashSet();
			for (int i=0;i<1000000;i++) {
				set.add(UUID.randomUUID().toString());
			}
			Key key = new Key("test", "cache", skey);
			Bin bin1 = new Bin("value", set);
			client.delete(null, key);
			client.put(null, key, bin1);
			
		}
		time = System.currentTimeMillis() - time;
		time/=1000;
		System.out.println("t = " + time);
		long sz = sizeOf.deepSizeOf(set);
		System.out.println("SIZE OF: " + sz);
	}
}
	