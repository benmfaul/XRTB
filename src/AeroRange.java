import java.io.BufferedReader;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Value;
import com.aerospike.client.query.Filter;
import com.aerospike.client.query.RecordSet;
import com.aerospike.client.query.Statement;
import com.aerospike.redisson.RedissonClient;
import com.xrtb.bidder.Controller;
import com.xrtb.common.Configuration;
import com.xrtb.tools.NavMap;

public class AeroRange {
	List<XRange> list = new ArrayList();
	
	public static void main(String args[]) throws Exception {
		AerospikeClient client = new AerospikeClient("localhost", 3000);
		//AeroRange sr = new AeroRange("ISP", "/home/ben/Downloads/ISP.txt");
		AeroRange sr = new AeroRange("ISP", "junk.txt");
		sr.load(client);
		
	}

	public AeroRange(String name, String file) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(file));
		long x = 0, k = 0;
		long oldstart = 0;
		long oldend = 0;
		long start = 0;
		long end = 0;
		String message = null;
		
		String[] parts = null;

		XRange r = null;
		long over = 0;
		int linek = 0;
		
		message = "Initialize CIDR navmap: " + file + " as " + name;
		for (String line; (line = br.readLine()) != null;) {
			parts = line.split("-");
			if (parts[0].length() > 0) {
				
				start = ipToLong(parts[0]);
				end = ipToLong(parts[1]);
				
				if (oldstart == 0) {
					oldstart = start;
					oldend = end;
				} else {
					if (start == oldend + 1) {
						over++;
						oldend = end;
					} else {
						r = new XRange(start, end);
						k++;
						list.add(r);
						oldstart = start;
						oldend = end;
					}
				}
			}
			linek++;
		}
		
		r = new XRange(start, end);
		k++;
		list.add(r);
		
		double d = (double)over/(double)linek;
		message += ", overlaps = " + over + ", total records = " + k + ", % overlap = " + d;
		
		System.out.format("%s\n",message);

	}
	
	public void load(AerospikeClient client) throws Exception {
		
		for (int i=0;i<list.size();i++) {
			XRange x = list.get(i);
			Key key = new Key("test", "junk", x.lower);
			Bin bin1 = new Bin("upper", x.upper);
			client.delete(null, key);
			client.put(null, key, bin1);
		}
	}

	public boolean search(long lower, long upper) {
		Statement stmt = new Statement();
		stmt.setNamespace("junk");
		stmt.setSetName("lower");
		stmt.setBinNames("upper");
	//	stmt.setFilters(Filter.range("upper", ));
		RecordSet rs = null;
		return false;
	}

	public static long ipToLong(String ipAddress) {

		String[] ipAddressInArray = ipAddress.split("\\.");

		long result = 0;
		for (int i = 0; i < ipAddressInArray.length; i++) {

			int power = 3 - i;
			int ip = Integer.parseInt(ipAddressInArray[i]);
			result += ip * Math.pow(256, power);

		}

		return result;
	}

	public static String longToIp(long ip) {
		if (ip > 4294967295l || ip < 0) {
			throw new IllegalArgumentException("invalid ip");
		}
		StringBuilder ipAddress = new StringBuilder();
		for (int i = 3; i >= 0; i--) {
			int shift = i * 8;
			ipAddress.append((ip & (0xff << shift)) >> shift);
			if (i > 0) {
				ipAddress.append(".");
			}
		}
		return ipAddress.toString();
	}
}

class XRange {
	public long upper;
	public long lower;

	public XRange(long lower, long upper) {
		this.upper = upper;
		this.lower = lower;
	}
}
	