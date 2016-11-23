package com.xrtb.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class NavMap {

	public static List<Long> in = new ArrayList();
	public static List<Long> out = new ArrayList();

	public static Map<String, NavMap> symbols = new HashMap();
	
	NavigableMap<Long, XRange> map = new TreeMap<Long, XRange>();

	public static boolean searchTable(String key, String ip) {
		NavMap x = NavMap.symbols.get(key);
		if (x == null)
			return false;
		return x.search(ip);
	}

	public static boolean searchTable(String key, long ip) {
		NavMap x = NavMap.symbols.get(key);
		if (x == null)
			return false;
		return x.search(ip);
	}

	public static void main(String args[]) throws Exception {
		NavMap sr = new NavMap("ISP", "/home/ben/Downloads/ISP.txt");

		for (int i = 0; i < 1; i++) {
			String ip = "1.0.10.0";
			long now = System.nanoTime();
			boolean x = sr.search(ip);
			now = System.nanoTime() - now;
			now /= 1000;
			System.out.println(ip + " " + x + " " + now + " micro seconds");
		}
		// 203.180.58.167-203.180.58.187
		// 203.212.37.105-203.212.37.125
	}

	public NavMap(String name, String file) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(file));
		long x = 0, k = 0;
		long old = 0;
		String[] parts = null;
		String oldpart = null;
		XRange r = null;
		long over = 0;
		
		for (String line; (line = br.readLine()) != null;) {
			long start = 0;
			long end = 0;
			parts = line.split("-");
			if (parts[0].length() > 0) {
				
				start = ipToLong(parts[0]);
				end = ipToLong(parts[1]);
				
				if (start == old + 1) {
					over++;
				}
				old = start;
				
				map.put(start, new XRange(end, k)); 
				if (k % 1000 == 0 && in.size() < 10) {
					in.add(end+10);
				}
			}

			k++;
		}
		double d = (double)over/(double)k;
		System.out.println("OVER = " + over + ", k = " + k + ", d=" + d);
		symbols.put(name, this);

	}

	public boolean search(String ip) {
		long address = ipToLong(ip);
		return search(address);
	}

	public boolean search(long key) {
		Map.Entry<Long,XRange> entry = map.floorEntry(key);
		if (entry == null)
			return false;
		else if (key <= entry.getValue().upper) {
		    return true; //return entry.getValue().value;
		} else {
		   return false;
		}
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
	public long value;

	public XRange(long upper, long value) {
		this.upper = upper;
		this.value = value;
	}
}