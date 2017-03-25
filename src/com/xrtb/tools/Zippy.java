package com.xrtb.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;


import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.session.SessionHandler;

import com.xrtb.bidder.CustomListener;


/**
 * Creates the HTTP handler for the Minimal server. Whatever it gets, it dumps to stdout
 * @author Ben M. Faul.
 *
 */


public class Zippy implements Runnable {
	/** The thread the server runs on */
	Thread me;
	// The port I listen on
	int port;

	/**
	 * Creates the default Server
	 * 
	 * @param args
	 *            . String[]. Args[0] contains the name of the users file, if
	 *            not, presume "users.json"
	 * @throws Exception
	 *             on network or JSON parsing errors.
	 */

	public static void main(String[] args) throws Exception {
		int port = 9999;
		if (args.length != 0)
			port = Integer.parseInt(args[0]);
		new Zippy(port);
	}


	/**
	 * Creates the instance and starts it. T
	 * 
	 * @param port
	 *            int. The port to listen on.
	 * @throws Exception
	 *             or network errors.
	 */

	public Zippy(int port) throws Exception {
		this.port = port;
		me = new Thread(this);
		me.start();
	}


	/**
	 * Starts the JETTY server
	 */

	 public void run() {
		Server server = new Server(port);
		Handler handler = new Handler();

		try {

			SessionHandler sh = new SessionHandler(); // org.eclipse.jetty.server.session.SessionHandler
			sh.addEventListener(new CustomListener());
			sh.setHandler(handler);
			server.setHandler(sh);
			server.start();
			server.join();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} 
}


/**
 * The class that handles HTTP calls for Skrambler actions.
 * 
 * @author Ben M. Faul
 *
 */

@MultipartConfig

class Handler extends AbstractHandler {

	/**
	 * The property for temp files.
	 */
	private static final MultipartConfigElement MULTI_PART_CONFIG = new MultipartConfigElement(
			System.getProperty("java.io.tmpdir"));


	private NashHorn scripter;

	@Override
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		

		response.addHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Cache-Control","max-age=86400");
		response.setHeader("Cache-Control","must-revalidate");
		response.setContentType("text/html;charset=utf-8");

		String url = request.getQueryString();
		StringBuilder output = new StringBuilder(target);
		InputStream body = request.getInputStream();
		output.append(url);
		output.append("\n");
		if (body != null) {
			final int bufferSize = 1024;
        	final char[] buffer = new char[bufferSize];
        	final StringBuilder out = new StringBuilder();
        	Reader in = new InputStreamReader(body, "UTF-8");
        	for (; ; ) {
        	    int rsz = in.read(buffer, 0, buffer.length);
        	    if (rsz < 0)
        	        break;
        	    out.append(buffer, 0, rsz);
        	}
        	output.append(out.toString());
		}
		System.out.println(output.toString());
		baseRequest.setHandled(true);
		response.setStatus(200);
	}


	/**
	 * Return the IP address of this
	 * 
	 * @param request
	 *            HttpServletRequest. The web browser's request object.
	 * @return String the ip:remote-port of this browswer's connection.
	 */

	public String getIpAddress(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		ip += ":" + request.getRemotePort();
		return ip;
	}
}
