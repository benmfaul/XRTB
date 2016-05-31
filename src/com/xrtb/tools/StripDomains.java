package com.xrtb.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.common.HttpPostGet;

public class StripDomains {

	static double LAT = 42.378;
	static double LON = -71.227;
	static double COST = 3.0;

	static List<GeoStuff> geo = new ArrayList();

     static String HOST = "localhost";
	//static String HOST = "btsoomrtb";
	// static String HOST = "54.175.237.122";
	// static String HOST = "rtb4free.com";
	static String winnah = "__COST__/__LAT__/__LON__/__ADID__/__CRID__/__BIDID__/http://__HOST__:8080/contact.html?99201&adid=__ADID__&crid=__CRID__/http://__HOST__:8080/images/320x50.jpg?adid=__ADID__&__BIDID__";

	static String pixel = "/pixel/__EXCHANGE__/__ADID__/__CRID__/__BIDID__/__COST__/__LAT__/__LON__";

	static String redirect = "/redirect/__EXCHANGE__/__ADID__/__CRID__/__COST__/__LAT__/__LON__?url=http://__HOST__:8080/contact.html";
	
	static boolean COUNT_MODE = false;							// set false to replay a file

	static Set set = new HashSet();
	static Set devices = new HashSet();
	static Set dups = new HashSet();
	
	public static void main(String[] args) throws Exception {		
		BufferedReader br = null;
		String fileName = "../requests";
		ObjectMapper mapper = new ObjectMapper();
		String data = null;

		br = new BufferedReader(new FileReader(fileName));
	
		double k = 0;
		double kk = 0;

		while((data = br.readLine()) != null) {
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
					false);

			Map map = mapper.readValue(data, Map.class);
			Map site =(Map) map.get("site");
			if (site != null) {
				String domain = (String)site.get("domain");
				String page = (String)site.get("page");
				if (domain != null) {
					set.add(domain);
					k++;
				}
				if (page != null) {
					//set.add(page);
					//k++;
				}
			}
			Map device = (Map)map.get("device");
			if (device != null) {
				String ip = (String)device.get("ip");
				if (devices.contains(ip)) {
					dups.add(ip);
				}
				devices.add(ip);
				kk++;
			}
			
		}
		System.out.println("K = " + k);
		System.out.println("KK = " + kk);
		
		double trueU = devices.size() - dups.size();
		
		System.out.println("Unique pages: " + set.size());
		System.out.println("Unique index: " + set.size()/k * 100);
		System.out.println("Unique devices: " + trueU);
		System.out.println("Unique devices: " + trueU/kk * 100);
		
	/*	Iterator<String> x = set.iterator();
		PrintWriter f0 = new PrintWriter(new FileWriter("domains.txt"));
		while(x.hasNext()) {
			f0.println(x.next());
		} */
	}
}