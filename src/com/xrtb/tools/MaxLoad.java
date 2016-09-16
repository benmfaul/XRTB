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
		int i = 0;
		while(true) {
			if (i++ < 6) {
				new MaxLoad();
				System.out.println("New: " + i);
			}
			count = 0;
			Thread.sleep(2000);
			double x = count/2000;
			System.out.println(count/2);
		}
	}
	
	public MaxLoad() throws Exception  {
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
