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
 * A class for sending HTTP post and get. Pretty simple, used for testing the
 * bidder. The sendPost() and sendGet() methods are used to transmit to the
 * bidder. Each returns a string which is the response from the bidder. You can
 * also retrieve the status code of the return, as well as query the response
 * headers Note, the connection remains open.
 * 
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
	 * 
	 * @param url
	 *            . The url string to send.
	 * @return String. The HTTP response to the GET
	 * @throws Exception
	 *             on network errors.
	 */
	public String sendGet(String url) throws Exception {

		URL obj = new URL(url);
		http = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		http.setRequestMethod("GET");

		// add request header
		http.setRequestProperty("User-Agent", USER_AGENT);

		int responseCode = http.getResponseCode();
		// System.out.println("\nSending 'GET' request to URL : " + url);
		// System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(
				http.getInputStream()));
		String inputLine;
		StringBuilder response = new StringBuilder();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		return response.toString();

	}

	/**
	 * Send an HTTP post.
	 * 
	 * @param targetURL
	 *            String. The URL to transmit to.
	 * @param data
	 *            String data. The payload.
	 * @return String. The contents of the POST return.
	 * @throws Exception
	 *             on network errors.
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
			try {
				output.close();
			} catch (IOException logOrIgnore) {
				logOrIgnore.printStackTrace();
			}
		}
		InputStream response = connection.getInputStream();
		http = (HttpURLConnection) connection;
		code = http.getResponseCode();

		byte [] b = getContents(response);;
		if (b.length == 0)
			return null;
		return new String(b, 0, b.length);
	}

	/**
	 * Return the value of the header
	 * 
	 * @param name
	 *            String. The header field name
	 * @return String. The value of the header if present, else null.
	 */
	public String getHeader(String name) {
		if (http == null)
			return null;
		
		return http.getHeaderField(name);
	}

	/**
	 * Returns the HTTP response code.
	 * 
	 * @return int. The HTTP response code.
	 */
	public int getResponseCode() {
		return code;
	}

	/** 
	 * Read all the bytes from the input stream
	 * @param stream InputStream. The stream to read.
	 * @return byte[]. The array of bytes from the stream.
	 * @throws Exception on I/O errors.
	 */
	private  byte [] getContents(InputStream stream) throws Exception {
		byte[] resultBuff = new byte[0];
		byte[] buff = new byte[1024];
		int k = -1;
		while ((k = stream.read(buff, 0, buff.length)) > -1) {
			byte[] tbuff = new byte[resultBuff.length + k]; // temp buffer size
															// = bytes already
															// read + bytes last
															// read
			System.arraycopy(resultBuff, 0, tbuff, 0, resultBuff.length); // copy
																			// previous
																			// bytes
			System.arraycopy(buff, 0, tbuff, resultBuff.length, k); // copy
																	// current
																	// lot
			resultBuff = tbuff; // call the temp buffer as your result buff
		}
		return resultBuff;

	}
}
