package com.xrtb.tools;

import java.io.DataInputStream;

import java.io.DataOutputStream;
import java.net.Socket;

public class RawHttp {

	Socket s = null;
	DataInputStream input;
	DataOutputStream output;
	byte[] outputBytes;

	public static void main(String args[]) throws Exception {
		String test = "GET /adex/test.html HTTP/1.0\r\n"
				+ "From: someuser@jmarshall.com\r\n"
				+ "User-Agent: HTTPTool/1.0\r\n" + "\r\n";
		RawHttp http = new RawHttp("adextoll.com", 80, test);
		http.run();
	}

	public RawHttp(String host, int port, String data) throws Exception {
		s = new Socket(host, port);
		input = new DataInputStream(s.getInputStream());
		output = new DataOutputStream(s.getOutputStream());
		outputBytes = data.getBytes();
	}

	public void run() {
		byte[] rcvd = new byte[4096];
		try {
			output.write(outputBytes);

			int rc = input.read(rcvd);
			String what = new String(rcvd, 0, rc);
			System.out.println(what);
		} catch (Exception error) {
			error.printStackTrace();
		}
	}
}
