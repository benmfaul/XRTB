package com.xrtb.common;

import java.io.BufferedReader;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
 
/**
 * A class for sending HTTP post and get
 * @author Ben Faul
 *
 */
public class HttpPostGet {
 
	/** The HTTP object */
	private HttpURLConnection http;
	/** The fake user object */
	private final String USER_AGENT = "Mozilla/5.0";;
	/** The HTTP return code */
	private int code;

	/**
	 * Send an HTTP get, once the http url is defined.
	 */
	private void sendGet() throws Exception {
 
		String url = "http://www.google.com/search?q=mkyong";
 
		URL obj = new URL(url);
		http = (HttpURLConnection) obj.openConnection();
 
		// optional default is GET
		http.setRequestMethod("GET");
 
		//add request header
		http.setRequestProperty("User-Agent", USER_AGENT);
 
		int responseCode = http.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(http.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		//print result
		System.out.println(response.toString());
 
	}
 
	/**
	 * Send an HTTP post.
	 * @param targetURL String. The URL to transmit to.
	 * @param data String data. The payload.
	 * @return String. The contents of the POST return.
	 */
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
		http = (HttpURLConnection)connection;
		code =  http.getResponseCode();
		byte[] b = new byte[4096];
		int rc = response.read(b);
		if (rc < 0)
			return null;
		return new String(b,0,rc);
	}
	
	/**
	 * Return the value of the header
	 * @param name String. The header field name
	 * @return String. The value of the header if present, else null.
	 */
	public String getHeader(String name) {
		return http.getHeaderField(name);
	}
	
	/**
	 * Returns the HTTP response code.
	 * @return int. The HTTP response code.
	 */
	public int getResponseCode() {
		return code;
	}
}
