package com.xrtb.tools;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.xml.bind.DatatypeConverter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.bidder.RTBServer;
import com.xrtb.common.HttpPostGet;
import com.xrtb.exchanges.adx.AdxBidRequest;
import com.xrtb.exchanges.adx.AdxBidResponse;
import com.xrtb.exchanges.google.GoogleBidRequest;
import com.xrtb.exchanges.google.GoogleBidResponse;

public class LoadGoogle {

	public static void main(String args[]) throws Exception {
		BufferedReader br = null;
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		String data = null;
		String fileName = "SampleBids/google.txt";
		boolean print = true;
		int count = -1;
		boolean step = false;
		int timeout = 300000;
		String endPoint = "http://localhost:8080/rtb/bids/google";
		Scanner sc = null; 

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
			case "-s":
				sc= new Scanner(System.in);
				step = true;
				i++;
				break;
			case "-h":
			case "-help":
				System.out.println("Send bids to the Adx endpoint of RTB4FREE. Usage:\n");
				System.out.println(
						"-h, -help          [This message                                                              ]");
				System.out.println(
						"-c <number         [Send this many messages                                                   ]");
				System.out.println(
						"-s                 [step one message                                                          ]");
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
		}

		br = new BufferedReader(new FileReader(fileName));
		
		int k = 0;
		while ((data = br.readLine()) != null && k != count) {
			boolean printed = false;
			Map map = null;
			String protobuf = null;
			try {
				map = mapper.readValue(data, Map.class);
				protobuf = (String) map.get("protobuf");
			} catch (Exception error) {
				protobuf = data;
			}
			byte[] protobytes = DatatypeConverter.parseBase64Binary(protobuf);
			if (print || step) {
				ByteArrayInputStream bis = new ByteArrayInputStream(protobytes);
				GoogleBidRequest brx = new GoogleBidRequest(bis);
				String content = mapper.writer().withDefaultPrettyPrinter().writeValueAsString(brx.getOriginal());
				System.out.println(content);
				printed = true;
			}
			if (map == null || map.get("feedback") == null) {
				if (step) {
					if (!printed)
						System.out.println(protobuf);
					System.out.print("\nSTEP>");
					sc.nextLine();
				}

				try {
					HttpPostGet hp = new HttpPostGet();
					byte[] rets = hp.sendPost(endPoint, protobytes, timeout, timeout);
					if (print) {
						if (rets == null) {
							System.out.println("*** NO BID ****");
						} else {
							GoogleBidResponse r = new GoogleBidResponse(rets);
							System.out.println("-----------------------------------------");
							System.out.println(r.getInternal());
						}
						System.out.println("Next bid will be:");
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
