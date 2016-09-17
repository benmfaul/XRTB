package com.xrtb.tools;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.xrtb.common.HttpPostGet;

public class MaxLoad implements Runnable {
	String fileName = "SampleBids/nexage.txt";
	String url = "http://localhost:8080/rtb/bids/nexage";
	HttpPostGet post = new HttpPostGet();
	String content;
	
	Thread me;
	
	static double count = 0;
	
	public static void main(String [] args) throws  Exception {		
		int threads = 10;
		int i = 0;
		String host = "localhost";
		
		while(i<args.length) {
			switch(args[i]) {
			case "-h":
				System.out.println("-h                  [This message                               ]");
				System.out.println("-host host-or-ip    [Where to send the bid (default is localhost]");
				System.out.println("-threads n          [How many threads (default=10)              ]");
			case "-host":
				host = args[i+1];
				i+=2; 
				break;
			case "-threads":
				threads = Integer.parseInt(args[i+1]);
				i+=2;
				break;
			default:
				System.err.println("Huh? " + args[i]);
			}
		}
		
		i=0;
		while(true) {
			if (i < threads) {
				new MaxLoad(host);
				i++;
			}
			count = 0;
			Thread.sleep(2000);
			double x = count/2000;
			System.out.println("Threads="+i + ", QPS=" + count/2);
		}
	}
	
	public MaxLoad(String host) throws Exception  {
		url = "http://" + host + ":8080/rtb/bids/nexage";
		content = new String(Files.readAllBytes(Paths
				.get(fileName)), StandardCharsets.UTF_8);
		me = new Thread(this);
		me.start();

	}
	
	public void run() {
		
		while(true) {
			try {
				String rc = post.sendPost(url, content,1000,1000);
				count++;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				
			}
		}
	}

}
