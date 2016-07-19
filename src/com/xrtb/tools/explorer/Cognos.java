package com.xrtb.tools.explorer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javarepl.console.ConsoleConfig;
import bsh.Interpreter;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.exchanges.Smaato;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;

public class Cognos {
	static ObjectMapper mapper = new ObjectMapper();

	public static void main(String[] args) throws Exception {

		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
				false);

		Map<String, BidRequest> requests = new HashMap();
		List<String> bidids = gatherBidIds("/home/ben/bin/dumplog/bids");
		indexRequests(bidids, requests, "/home/ben/bin/dumplog/request");

		for (String bid : bidids) {
			BidRequest r = requests.get(bid);

			if (r != null) {
				Object ob = r.interrogate("site.publisher.id");
				if (ob == null) {
					ob = r.interrogate("app.publisher.id");
				}
				if (ob != null) {
					JsonNode node = (JsonNode) ob;
					String value = node.asText();
					if (value.equals("130003498") || value.equals("130054054"))
						System.out.println("--------------\nBad ID: " + r.id
								+ "\n" + r.toString());
				}
				
				ob = r.interrogate("device.os");
				if (ob != null) {
					JsonNode node = (JsonNode)ob;
					String value = node.asText();
					if (value.equalsIgnoreCase("iOS")==false) {
							System.out.println("--------------\nBad OS: " + r.id
									+ "\n" + r.toString());
					}
				}
			}
		}

	}

	public static List<String> gatherBidIds(String file) throws Exception {

		List<String> bids = new ArrayList();
		BufferedReader br = new BufferedReader(new FileReader(file));
		int k = 0;
		for (String line; (line = br.readLine()) != null;) {
			Map map = mapper.readValue(line, Map.class);
			String id = (String) map.get("bidid");
			bids.add(id);
			k++;
		}
		System.out.println("Number of bids retrieved: " + k);
		return bids;
	}

	public static void indexRequests(List<String> list,
			Map<String, BidRequest> map, String file) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(file));
		int k = 0;
		for (String line; (line = br.readLine()) != null;) {
			if (line.contains("\0")) {
				System.out.println(line);
			} else {
				// System.out.println(line);
				try {
					BidRequest bidr = new BidRequest(new StringBuilder(line));
					if (list.contains(bidr.id)) {
						map.put(bidr.id, bidr);
						k++;
					}
				} catch (Exception error) {
					//System.out.println(error);
				}
			}
		}
		System.out.println("Number of requests indexed: " + k);
	}

	public Cognos() {

	}
}
