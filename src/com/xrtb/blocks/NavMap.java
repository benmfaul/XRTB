package com.xrtb.blocks;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.xrtb.bidder.Controller;
import com.xrtb.common.Configuration;
import com.xrtb.tools.LookingGlass;

public class NavMap extends LookingGlass {

	public static List<Long> in = new ArrayList();
	public static List<Long> out = new ArrayList();

	NavigableMap<Long, XRange> map = new TreeMap<Long, XRange>();

	public static boolean searchTable(String key, String ip) {
		NavMap x = (NavMap) symbols.get(key);
		if (x == null)
			return false;
		return x.search(ip);
	}

	public static boolean searchTable(String key, long ip) {
		NavMap x = (NavMap) symbols.get(key);
		if (x == null)
			return false;
		return x.search(ip);
	}

	public static void main(String args[]) throws Exception {
		// NavMap sr = new NavMap("ISP", "/home/ben/Downloads/ISP.txt", false);
		
		NavMap sr = new NavMap("ISP", "data/METHBOT.txt", true);

		/*for (int i = 0; i < 1; i++) {
			// String ip = "1.0.10.0";
			String ip = "223.255.192.255";
			long now = System.nanoTime();
			boolean x = sr.search(ip);
			now = System.nanoTime() - now;
			now /= 1000;
			System.out.println(ip + " " + x + " " + now + " micro seconds");
		} */
		// 203.180.58.167-203.180.58.187
		// 203.212.37.105-203.212.37.125
	}

	public NavMap(String name, String file, boolean isCidr) throws Exception {
		super();

		if (isCidr)
			doCidr(name, file);
		else
			doRanges(name, file);

	}

	void doCidr(String name, String file) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(file));
		long start = 0;
		long end = 0;
		String message = null;
		XRange r = null;
		int k = 0;

		message = "Initialize CIDR navmap: " + file + " as " + name;
		for (String line; (line = br.readLine()) != null;) {
			if (!(line.startsWith("#") || line.length() < 10)) {
				CIDR cidr = new CIDR(line);
				start = cidr.getLongAddressFrom();
				end = cidr.getLongAddressTo();
				r = new XRange(end, k);
				k++;
				map.put(start, r);
			}
		}
		message += ", total records = " + k;

		if (Configuration.isInitialized()) {
			System.out.format("[%s] - %d - %s - %s - %s\n", Controller.sdf.format(new Date()), 1,
				Configuration.instanceName, "NavMap", message);
		} else {
			System.out.format("[%s] - %d - %s - %s - %s\n", "", 1,
					"", "NavMap", message);
		}
		symbols.put(name, this);
	}

	void doRanges(String name, String file) throws Exception {
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

		message = "Initialize RANGE navmap: " + file + " as " + name;
		for (String line; (line = br.readLine()) != null;) {
			if (!(line.startsWith("#") || line.length() < 10)) {
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
							r = new XRange(oldend, k);
							k++;
							map.put(oldstart, r);
							oldstart = start;
							oldend = end;
						}
					}
					if (k % 1000 == 0 && in.size() < 10) {
						in.add(end + 10);
					}
				}
				linek++;
			}
		}

		r = new XRange(end, k);
		k++;
		map.put(start, r);

		double d = (double) over / (double) linek;
		message += ", overlaps = " + over + ", total records = " + k + ", % overlap = " + d;

		System.out.format("[%s] - %d - %s - %s - %s\n", Controller.sdf.format(new Date()), 1,
				Configuration.instanceName, "NavMap", message);
		symbols.put(name, this);

	}

	public boolean search(String ip) {
		long address = ipToLong(ip);
		return search(address);
	}

	public boolean search(long key) {
		Map.Entry<Long, XRange> entry = map.floorEntry(key);
		if (entry == null)
			return false;
		else if (key <= entry.getValue().upper) {
			return true; // return entry.getValue().value;
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
