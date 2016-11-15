package com.xrtb.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchableIpList {

	public static Map<String,SearchableIpList> symbols = new HashMap();
	List<Range> list = new ArrayList();
	int high;
	
	public static boolean searchTable(String key, String ip) {
		SearchableIpList x = SearchableIpList.symbols.get(key);
		if (x == null)
			return false;
		return x.search(ip); 
	}
	
	public static boolean searchTable(String key, long ip) {
		SearchableIpList x = SearchableIpList.symbols.get(key);
		if (x == null)
			return false;
		return x.search(ip); 
	}

	public static void main(String args[]) throws Exception {
		SearchableIpList sr = new SearchableIpList("ISP","/home/ben/Downloads/ISP.txt");
		long now = System.nanoTime();
		boolean x = sr.search("223.255.192.0");
		now = System.nanoTime() - now;
		now /= 1000;
		System.out.println(now + " = " + x );
		// 203.180.58.167-203.180.58.187
		// 203.212.37.105-203.212.37.125
	}

	public SearchableIpList(String name, String file) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(file));
		long x = 0, k = 0;
		long old = 0;
		for (String line; (line = br.readLine()) != null;) {

			String[] parts = line.split("-");
			if (parts[0].length() > 0) {

				long start = ipToLong(parts[0]);
				long end = ipToLong(parts[1]);
				
				Range r = new Range(start, end);
				list.add(r);
			}
			k++;
		}
		high = list.size() - 1;
		symbols.put(name, this);
		
	}

	public boolean search(String ip) {
		long address = ipToLong(ip);
		return search(address);
	}

	public boolean search(long key) {
		int low = 0;
		while(high>=low) {
			 int middle = (low + high) / 2;
			 Range data = list.get(middle);
			 if(key >= data.start && key <= data.end) {
				 return true;
			 }
			 if(data.start < key) {
			    low = middle + 1;
			 }
			  if(data.start > key) {
			      high = middle - 1;
			 }
		}
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

class Range {
	public long start;
	public long end;

	public Range(long start, long end) {
		this.start = start;
		this.end = end;
	}
}