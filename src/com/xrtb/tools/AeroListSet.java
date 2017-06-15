package com.xrtb.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Map;

import com.aerospike.client.AerospikeClient;
import com.aerospike.redisson.AerospikeHandler;
import com.aerospike.redisson.RedissonClient;

public class AeroListSet {

	public static void main(String args[]) throws Exception {
		int i = 0;
		String aero = "localhost:3000";
		String key = null;
		String setName = null;
		String mapName = null;
		String op = null;
		String name = null;
		String file = null;
		boolean range = false;

		while (i < args.length) {
			switch (args[i]) {
			case "-file":
				file = args[i + 1];
				i += 2;
				break;
			case "-aero":
				aero = args[i + 1];
				i += 2;
				break;
			case "-load-set":
				setName = args[i + 1];
				op = "load";
				i += 2;
				break;
			case "-range":
				range = true;
				i ++;
				break;
			case "-delete-set":
				setName = args[i + 1];
				op = "delete";
				i += 2;
				break;
			case "-read-set":
				op = "read";
				setName = args[i + 1];
				i += 2;
				break;
			case "-load-map":
				op = "load";
				mapName = args[i + 1];
				i += 2;
				break;
			case "-delete-map":
				op = "delete";
				mapName = args[i + 1];
				i += 2;
				break;
			case "-read-map":
				op = "read";
				mapName = args[i + 1];
				i += 2;
				break;
			case "-key":
				key = args[i + 1];
				i += 2;
			case "-name":
				name = args[i + 1];
				i += 2;
				break;
			default:
				System.out.println("Huh? " + args[i]);
				System.exit(0);
				;
			}
			;
		}

		String parts[] = aero.split(":");
		int port = 3000;
		String host = parts[0];
		if (parts.length > 1) {
			port = Integer.parseInt(parts[1]);
		}
		
		AerospikeHandler client = AerospikeHandler.getInstance(host,port,300);
		RedissonClient redisson = new RedissonClient(client);

		if (setName != null) {
			if (op.equals("load")) {
				loadSet(redisson,file, setName, range);
				return;
			}
			if (op.equals("query")) {
				Object value = redisson.get("userstore",key);
				System.out.println(value);
			}
		}

		if (mapName != null) {

		}

		System.out.println("Can't figure out what you want to do");
	}

	public static void loadSet(RedissonClient redisson, String file, String name, boolean range) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(file));
		long x = 0, k = 0;
		for (String line; (line = br.readLine()) != null;) {
			
			if (range) {
				String[] parts = line.split("-");
				if (parts.length != 2)
					System.out.println(line);
				else {
					long start = ipToLong(parts[0]);
					long end = ipToLong(parts[1]);
					for (long i=start;i<=end;i++) {
						String ip = longToIp(i);
						redisson.set("userstore",name,ip);
						x++;
					}
				
				}
				if (k % 1000 == 0)
					System.out.println(k);
				k++;
			}
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
