package com.xrtb.tools.explorer;

import java.io.BufferedReader;

import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.common.Configuration;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.ForensiqClient;
import com.xrtb.tools.accounting.Record;

public class AnylzeForensiq {
	public static ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	static Map<String, Integer> ips = new HashMap();
	static Map<String, Integer> names = new HashMap();
	
	
	public static void main(String [] args) throws Exception {
		ForensiqClient forensiq = ForensiqClient.build("6RLzOXoxntkqr0PHJ1Z0");
		Configuration.forensiq = forensiq;
		
		String content = null;
		String source = "../request.json";
		BidRequest br = null;
		
		FileReader fr = new FileReader(source); 
		BufferedReader bufr = new BufferedReader(fr); 
		
		double count = 0;
		double frauds = 0;
		double errored = 0;
		
		while((content = bufr.readLine()) != null && count < 1000) {
			count++;
			StringBuilder sb = new StringBuilder(content);
			try {
				br = new BidRequest(sb);
				if (br.isFraud) {
					frauds++;
					
					System.out.println(content);
					
					Map map = mapper.readValue(content, Map.class);
					List imp = (List) map.get("imp");
					
					Map x = (Map) imp.get(0);
					Map device = (Map) map.get("device");
					if (device != null) {
						String ip = (String)device.get("ip");
						Integer counts = ips.get(ip);
						if (counts == null) {
							counts = new Integer(0);
						}
						counts++;
						ips.put(ip,counts);
					}
					
					Map site = (Map)map.get("site");
					if (site != null) {
						String name = (String)site.get("name");
						Integer counts = names.get(name);
						if (counts == null) {
							counts = new Integer(0);
						}
						counts++;
						names.put(name,counts);
					}
					
				}
				System.out.println(count);
			} catch (Exception error) {
				errored++;
			}
		}
		
		System.out.println("Total = " + count + ", Errored = " + errored + ", Fraud = " + frauds + " (" + (frauds/count * 100.0) +")");
		System.out.println("\n\nIP analysis\n\n");
		List<Tuple>tups = RequestScanner.reduce(ips,(int)(count-errored));
		tups.forEach((q) -> System.out.printf("%s, %d, (%.3f%%)\n",q.site,q.count,q.percent));
		
		System.out.println("\n\nSite analysis\n\n");
		tups = RequestScanner.reduce(names,(int)(count-errored));
		tups.forEach((q) -> System.out.printf("%s, %d, (%.3f%%)\n",q.site,q.count,q.percent));
	}
}
