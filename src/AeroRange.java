import java.io.BufferedReader;
import java.io.FileReader;

import com.aerospike.client.AerospikeClient;
import com.aerospike.redisson.RedissonClient;

public class AeroRange {

	public static void main(String ranges[]) throws Exception  {
		AerospikeClient client = new AerospikeClient("localhost", 3000);
		loadSet("/home/ben/Downloads/ISPMOB.txt");
		
	}
	
	public static void loadSet(String file) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(file));
		long x = 0, k = 0;
		Tuple old = null;
		for (String line; (line = br.readLine()) != null;) {
			
				String[] parts = line.split("-");
				if (parts.length != 2)
					System.out.println(line);
				else {
					long start = ipToLong(parts[0]);
					long end = ipToLong(parts[1]);		
					
					if (old != null) {
						if (old.end == start) {
							old.end = end;
						}
					} else {
						old = new Tuple();
						old.start = start;
						old.end = end;
					}
				}
				if (k % 1000 == 0)
					System.out.println(k);
				k++;
			}
		System.out.println("read " + k + ", total = " + x);
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
		StringBuilder result = new StringBuilder(15);
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < 4; i++) {

			result.insert(0, Long.toString(ip & 0xff));

			if (i < 3) {
				sb.insert(0, '.');
			}

			ip = ip >> 8;
		}
		return result.toString();
	}
}

class Tuple {
	public long start;
	public long end;
}
