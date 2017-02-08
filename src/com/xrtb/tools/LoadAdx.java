package com.xrtb.tools;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.bidder.RTBServer;
import com.xrtb.common.HttpPostGet;
import com.xrtb.exchanges.adx.AdxBidRequest;
import com.xrtb.exchanges.adx.AdxBidResponse;

public class LoadAdx {

	public static void main(String args[]) throws Exception {
		BufferedReader br = null;
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		String data = null;
		String fileName = null;
		boolean print = true;
		int count = -1;
		int timeout = 15000;
		String endPoint = "http://localhost:8080/rtb/bids/adx";

		int i = 0;
		while (i < args.length) {
			switch (args[i]) {
			case "-e":
				endPoint = args[i + 1];
				i += 2;
				break;
			case "-f":
				fileName = args[i + 1];
				i += 2;
				break;
			case "-p":
				print = Boolean.parseBoolean(args[i + 1]);
				i += 2;
				break;
			case "-c":
				count = Integer.parseInt(args[i + 1]);
				i += 2;
				break;
			case "-t":
				timeout = Integer.parseInt(args[i + 1]);
				timeout *= 1000;
				i += 2;
				break;
			case "-h":
			case "-help":
				System.out.println("Send bids to the Adx endpoint of RTB4FREE. Usage:\n");
				System.out.println(
						"-h, -help          [This message                                                              ]");
				System.out.println(
						"-c <number         [Send this many messages                                                   ]");
				System.out.println(
						"-e <http endpoint> [Send the bid to this endpoint, default: http://localhost:8080/rtb/bids/adx]");
				System.out.println(
						"-f <fileName>      [Use this file of canned bid requests.                                     ]");
				System.out.println(
						"-p <boolean>       [Print the results. Default is true                                        ]");
				System.out.println(
						"-t <seconds>       [Timeout and report error in seconds, default is 15                        ]");
				System.exit(1);
				break;
			default:
				System.out.println("Huh: " + args[i]);
				System.exit(0);
			}
		}

		if (fileName == null) {
			System.err.println("-f <fileName> is required");
			System.exit(1);
			;
		}

		br = new BufferedReader(new FileReader(fileName));
		int k = 0;
		while ((data = br.readLine()) != null && k != count) {
			Map map = mapper.readValue(data, Map.class);
			String protobuf = (String) map.get("protobuf");
			if (map.get("feedback") == null) {
				byte[] protobytes = DatatypeConverter.parseBase64Binary(protobuf);

				try {
					HttpPostGet hp = new HttpPostGet();
					byte[] rets = hp.sendPost(endPoint, protobytes, timeout, timeout);
					if (print) {
						AdxBidResponse r = new AdxBidResponse(rets);
						System.out.println("-----------------------------------------");
						System.out.println(r.getInternal());
					}
					if (count != -1)
						k++;
				} catch (Exception error) {
					System.err.println(error.toString());
				}
			}
		}

	}
}
