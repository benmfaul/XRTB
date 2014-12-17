package com.xrtb.simulator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.xrtb.bidder.RTBServer;
import com.xrtb.common.Campaign;

import com.xrtb.pojo.BidRequest;

/**
 * This class implements a simple web server that will send bid requests to the rtb engine and then will
 * analyze the return to determine if the response is valid.
 * 
 * Also, this web site will also monitor pixel tracking too, so that if you click on the Bid image that is returned, then
 * you should see the pixel tracking counter updated.
 * 
 * @author Ben M. Faul
 *
 */
public class Exchange implements Runnable {
	Server server;
	Thread me;
	int port;
	static String page = "";;
	static {
		try {
			Scanner in = new Scanner(new FileReader("exchange.html"));
			while(in.hasNextLine()) {
				page += in.nextLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String [] args) {
		new Exchange(8080);
	}
	
	public Exchange(int port) {
		server = new Server(port);
		server.setHandler(new ExchangeHandler());
		me = new Thread(this);
		me.start();
	}
	
	public void run() {
		try {
			server.start();
			server.join();
		} catch (Exception error) {
			error.printStackTrace();
		}
	}
	
	/**
	 * Stop the Jetty server
	 */
	public void halt() {
		me.interrupt();
	}
}


/**
 * JETTY handler for incoming bid request.
 * 
 * This handler processes RTB2.1 bid requests.
 * 
 * @author Ben M. Faul
 * 
 */
class ExchangeHandler extends AbstractHandler {
	Random rand = new Random();

	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		InputStream body = request.getInputStream();
		BidRequest br = null;
		String json = "{}";
		String id = "";
		Campaign campaign = null;
		int code = RTBServer.BID_CODE;
		long time = System.currentTimeMillis();

		System.out.println("TARGET: " + target);
		try {
			if (target.contains("/xrtb/simulator/exchange")) {
				response.setContentType("text/html;charset=utf-8");
				response.setStatus(HttpServletResponse.SC_OK);
				baseRequest.setHandled(true);
				response.getWriter().println(Exchange.page);
				return;
			}
			if (target.contains("jquery")) {
				int i = target.indexOf("web");
				target = target.substring(i);
				Scanner in = new Scanner(new FileReader(target));
				String jquery = "";
				while(in.hasNextLine()) {
					jquery += in.nextLine();
				}
				response.setContentType("text/html;charset=utf-8");
				response.setStatus(HttpServletResponse.SC_OK);
				baseRequest.setHandled(true);
				response.getWriter().println(jquery);
				return;
			}
			
			/**
			 * Send the bid request to the bidder and return the response
			 */
			if (target.contains("/xrtb/simulator/run")) {				
				response.setContentType("application/json;charset=utf-8");
				response.setStatus(HttpServletResponse.SC_OK);
				baseRequest.setHandled(true);
				response.getWriter().println("{\"a\":100}");
			}
		} catch (Exception err) {
			err.printStackTrace();
			return;
		}
	}
}

