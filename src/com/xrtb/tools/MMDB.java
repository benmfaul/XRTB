package com.xrtb.tools;

import java.io.File;
import java.net.InetAddress;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.IspResponse;

public class MMDB {
	final DatabaseReader reader;

	public MMDB(String fileName) throws Exception {
		File f = new File("data/GeoIP2-ISP.mmdb");
		reader = new DatabaseReader.Builder(f).build();
	}

	public boolean contains(String key) {
		try {
			InetAddress ip = InetAddress.getByName(key);
			IspResponse r = reader.isp(ip);
			String test = r.getIsp().toLowerCase();
			if (test.contains("hosting")) {
				return false;
			}
		} catch (Exception error) {
			error.printStackTrace();
		}
		return true;
	}

	public String solve(String key) {
		String value = null;
		try {
			String parts[] = key.split("/");
			if (parts.length < 3)
				return null;
			String sip = parts[2];
			InetAddress ip = InetAddress.getByName(sip);
			IspResponse r = reader.isp(ip);
			switch (parts[1]) {
			case "org":
				return r.getOrganization();
			case "isp":
				return r.getIsp();
			case "json":
				return r.toJson();
			default:
				return null;
			}
		} catch (Exception e) {

		}
		return value;
	}
}
