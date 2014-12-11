package com.xrtb.common;

import java.io.BufferedReader;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
 
import javax.net.ssl.HttpsURLConnection;
 
public class HttpPostGet {
 
	private final String USER_AGENT = "Mozilla/5.0";
	String url;
	String runTime;
	int code;
 
	public static void main(String[] args) throws Exception {
 
		HttpPostGet http = new HttpPostGet();
 
		//System.out.println("Testing 1 - Send Http GET request");
		//http.sendGet();
 
		String s = http.sendPost("http://localhost:8080/rtb/bids/nexage", "{\"id\":\"123\"}");
	}
	
	public HttpPostGet() {
		
	}
 
	// HTTP GET request
	private void sendGet() throws Exception {
 
		String url = "http://www.google.com/search?q=mkyong";
 
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		// optional default is GET
		con.setRequestMethod("GET");
 
		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);
 
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		//print result
		System.out.println(response.toString());
 
	}
 
	// HTTP POST request
	public String sendPost(String targetURL, String data) throws Exception {
		URLConnection connection = new URL(targetURL).openConnection();
		connection.setRequestProperty("Content-Type", "application/json");
	    connection.setDoInput(true);
	    connection.setDoOutput(true);
		OutputStream output = connection.getOutputStream();
		try {
		     output.write(data.getBytes());
		} finally {
		     try { output.close(); } catch (IOException logOrIgnore) {
		    	 logOrIgnore.printStackTrace();
		     }
		}
		InputStream response = connection.getInputStream();
		runTime = connection.getHeaderField("X-runtime");
		HttpURLConnection http = (HttpURLConnection)connection;
		code =  http.getResponseCode();
		byte[] b = new byte[4096];
		int rc = response.read(b);
		if (rc < 0)
			return null;
		return new String(b,0,rc);
	}
	
	public String getRunTime() {
		return runTime;
	}
	
	public int getResponseCode() {
		return code;
	}
}
