package com.xrtb.bidder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.session.SessionHandler;

import com.xrtb.commands.Echo;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;
import com.xrtb.pojo.WinObject;;

/**
 * A JAVA based RTB2.2 server.<br>
 * This is the RTB Bidder's main class. It is a Jetty based http server that
 * encapsulates the Jetty server. The class is Runnable, with the Jetty server
 * joining in the run method. This allows other parts of the bidder to interact
 * with the server, mainly to obtain status information from a command sent via
 * REDIS.
 * <p>
 * Prior to calling the RTBServer the configuration file must be Configuration
 * instance needs to be created - which is a singleton. The RTBServer class (as
 * well as many of the other classes also use configuration data in this
 * singleton.
 * <p>
 * A Jetty based Handler class is used to actually process all of the HTTP
 * requests coming into the bidder system.
 * <p>
 * Once the RTBServer.run method is invoked, the Handler is attached to the
 * Jetty server.
 * 
 * @author Ben M. Faul
 * 
 */
public class RTBServer implements Runnable {
	/** The url of the simulator */
	public static final String SIMULATOR_URL = "/xrtb/simulator/exchange";
	/** The url of where the simulator's resources live */
	public static final String SIMULATOR_ROOT = "web/exchange.html";

	public static final String CAMPAIGN_URL = "/xrtb/simulator/campaign";
	public static final String LOGIN_URL = "/xrtb/simulator/login";
	public static final String ADMIN_URL = "/xrtb/simulator/admin";
	public static final String CAMPAIGN_ROOT = "web/test.html";
	public static final String LOGIN_ROOT = "web/login.html";
	public static final String ADMIN_ROOT = "web/admin.html";

	/** The HTTP code for a bid object */
	public static final int BID_CODE = 200; // http code ok
	/** The HTTP code for a no-bid objeect */
	public static final int NOBID_CODE = 204; // http code no bid

	/** The percentage of bid requests to consider for bidding */
	public static int percentage = 100; // throttle is wide open at 100, closed
										// at 0

	/** Indicates of the server is not accepting bids */
	public static boolean stopped = false; // is the server not accepting bid
											// requests?

	/** Counter for number of bids made */
	public static long bid = 0; // number of bids processed
	/** Counter for number of nobids made */
	public static long nobid = 0; // number of nobids processed
	/** Number of errors in accessing the bidder */
	public static long error = 0;
	/** Number of actual requests */
	public static long handled = 0;
	/** Number of unknown accesses */
	public static long unknown = 0;
	/** The configuration of the bidder */
	public static Configuration config;
	/** The number of win notifications */
	public static long win;

	public static volatile int concurrentConnections = 0;

	/** The JETTY server used by the bidder */
	Server server;
	/** The default port of the JETTY server */
	int port = 8080;
	/**
	 * The bidder's main thread for handling the bidder's actibities outside of
	 * the JETTY processing
	 */
	Thread me;

	/** The campaigns that the bidder is using to make bids with */
	CampaignSelector campaigns;

	/** Bid target to exchange class map */
	public static Map<String, BidRequest> exchanges = new HashMap();

	/**
	 * This is the entry point for the RTB server.
	 * 
	 * @param args
	 *            . String[]. Config file name. If not present, uses default and
	 *            port 8080.
	 *            Options [-s shardkey] [-p port]
	 * @throws Exception
	 *             if the Server could not start (network error, error reading
	 *             configuration)
	 */
	public static void main(String[] args) throws Exception {
		String fileName = "Campaigns/payday.json";
		String shard = "";
		Integer port = 8080;
		if (args.length == 0)
			fileName = args[0];
		else {
			int i = 0;
			while(i < args.length) {
				switch (args[i]) {
				case "-p":
					i++;
					port = Integer.parseInt(args[i]);
					break;
				case "-s":
					i++;
					shard = args[i];
					break;
				default:
					fileName = args[i];
					i++;
					break;
				}
			}
		}

		new RTBServer(fileName, shard, port);
	}

	/**
	 * Class instantiator of the RTBServer.
	 * 
	 * @param fileName
	 *            String. The filename of the configuration file.
	 * @throws Exception
	 *             if the Server could not start (network error, error reading
	 *             configuration)
	 */
	public RTBServer(String fileName) throws Exception {
		Configuration.getInstance("Campaigns/payday.json");
		// Controller.getInstance();
		campaigns = CampaignSelector.getInstance(); // used to
		// select
		// campaigns
		// against
		// bid
		// requests
		me = new Thread(this);
		me.start();
		Thread.sleep(500);
	}
	
	/**
	 * Class instantiator of the RTBServer.
	 * 
	 * @param fileName
	 *            String. The filename of the configuration file.
	 * @param shard String. The shard key of this bidder instance.
	 * @param port. int. The port to use for this bidder.
	 * @throws Exception
	 *             if the Server could not start (network error, error reading
	 *             configuration)
	 */
	public RTBServer(String fileName, String shard, int port) throws Exception {
		Configuration.getInstance("Campaigns/payday.json", shard,port);
		// Controller.getInstance();
		campaigns = CampaignSelector.getInstance(); // used to
		// select
		// campaigns
		// against
		// bid
		// requests
		me = new Thread(this);
		me.start();
		Thread.sleep(500);
	}

	/**
	 * Establishes the HTTP Handler, creates the Jetty server and attaches the
	 * handler and then joins the server. This method does not return, but it is
	 * interruptable by calling the halt() method.
	 * 
	 */
	@Override
	public void run() {
		server = new Server(port);
		Handler handler = new Handler();

		
		try {
			BidRequest.compile();
			SessionHandler sh = new SessionHandler(); // org.eclipse.jetty.server.session.SessionHandler
			sh.setHandler(handler);
			server.setHandler(sh); // set session handle

			Controller.getInstance().sendLog(1, "initialization",
					("System start on port: " + port));
			server.start();
			server.join();
		} catch (Exception error) {
			if (error.toString().contains("Interrupt"))
				return;
			try {
				Controller.getInstance().sendLog(1, "initialization",
						"FATAL Error: " + error.toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
			}
			error.printStackTrace();
		}
	}

	/**
	 * Returns the sertver's campaign selector used by the bidder. Generally
	 * used by javascript programs.
	 * 
	 * @return CampaignSelector. The campaign selector object used by this
	 *         server.
	 */
	public CampaignSelector getCampaigns() {
		return campaigns;
	}

	/**
	 * Stop the RTBServer, this will cause an interrupted exception in the run()
	 * method.
	 */
	public void halt() {
		Configuration.getInstance().redisson.shutdown();
		try {
			me.interrupt();
		} catch (Exception error) {

		}
		try {
			server.stop();
			while (server.isStopped() == false)
				;
		} catch (Exception error) {
			error.printStackTrace();
		}
		try {
			Controller.getInstance().sendLog(0, "initalization",
					"System shutdown");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Is the Jetty server running and processing HTTP requests?
	 * 
	 * @return boolean. Returns true if the server is running, otherwise false
	 *         if null or isn't running
	 */
	public boolean isRunning() {
		if (server == null)
			return false;

		return server.isRunning();
	}

	/**
	 * Returns the status of this server.
	 * 
	 * @return Map. A map representation of this status.
	 */
	public static Echo getStatus() {
		Echo e = new Echo();
		e.percentage = percentage;
		e.stopped = stopped;
		e.bid = bid;
		e.win = win;
		e.nobid = nobid;
		e.error = error;
		e.handled = handled;
		e.unknown = unknown;
		e.loglevel = Configuration.getInstance().logLevel;
		e.campaigns = Configuration.getInstance().campaignsList;

		return e;
	}
}

/**
 * JETTY handler for incoming bid request.
 * 
 * This HTTP handler processes RTB2.2 bid requests, win notifications, click
 * notifications, and simulated exchange transactions.
 * <p>
 * Based on the target URI contents, several actions could be taken. A bid
 * request can be processed, a file resource read and returned, a click or pixel
 * notification could be processed.
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
	/**
	 * The randomizer used for determining to bid when percentage is less than
	 * 100
	 */
	Random rand = new Random();

	/**
	 * Handle the HTTP request. Basically a list of if statements that
	 * encapsulate the various HTTP requests to be handled. The server makes no
	 * distinction between POST and GET and ignores DELETE>
	 * <p>>
	 * 
	 * @throws IOException
	 *             if there is an error reading a resource.
	 * @throws ServletException
	 *             if the container encounters a servlet problem.
	 */
	@Override
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		response.addHeader("Access-Control-Allow-Origin", "*");

		if (RTBServer.concurrentConnections >= Configuration.getInstance().maxConnections) {
			RTBServer.handled++;
			RTBServer.nobid++;
			response.setStatus(RTBServer.NOBID_CODE);
			baseRequest.setHandled(true);
			response.getWriter().println("");
			return;
		}
		RTBServer.concurrentConnections++;

		InputStream body = request.getInputStream();
		String type = request.getContentType();
		BidRequest br = null;
		String json = "{}";
		String id = "";
		Campaign campaign = null;
		boolean unknown = true;
		RTBServer.handled++;
		int code = RTBServer.BID_CODE;
		long time = System.currentTimeMillis();

		/**
		 * This set of if's handle the bid request transactions.
		 */
		try {
			/**
			 * Convert the uri to a bid request object based on the exchange..
			 */

			if (target.contains("/rtb/bids")) {
				BidRequest x = RTBServer.exchanges.get(target);

				if (x == null) {
					json = "Wrong target: " + target;
					code = RTBServer.NOBID_CODE;
					Controller.getInstance().sendLog(2,"Handler:handle:error",json);
				} else {				
					
					unknown = false;
					// RunRecord log = new RunRecord("bid-request");
					br = x.copy(body);
					
					// Controller.getInstance().sendLog(5,"Handler:handle",br.toString());

					// BidRequestX xx = new BidRequestX(body);
					// System.out.println(xx);
					// log.add("copy");
					Controller.getInstance().sendRequest(br);
					id = br.getId();
					if (CampaignSelector.getInstance().size() == 0) {
						json = "No campaigns loaded";
						code = RTBServer.NOBID_CODE;
					} else if (RTBServer.stopped) {
						json = "Server stopped";
						code = RTBServer.NOBID_CODE;
					} else if (!checkPercentage()) {
						json = "Server throttled";
						code = RTBServer.NOBID_CODE;
					} else {
						BidResponse bresp = CampaignSelector.getInstance().get(
								br); // 93% time here
						// log.add("select");
						if (bresp == null) {
							json = "No matching campaign";
							code = RTBServer.NOBID_CODE;
							RTBServer.nobid++;
						} else {
							json = bresp.toString();
							Controller.getInstance().recordBid(bresp);
							Controller.getInstance().sendBid(bresp);
							code = RTBServer.BID_CODE;
							RTBServer.bid++;
						}
					}
					// log.dump();
				}

				time = System.currentTimeMillis() - time;

				response.setHeader("X-TIME", "" + time);
				response.setContentType("application/json;charset=utf-8");
				if (code == 204) {
					response.setHeader("X-REASON", json);
					if (Configuration.getInstance().printNoBidReason)
						System.out.println("No bid: " + json);
				}
				response.setStatus(code);
				baseRequest.setHandled(true);
				response.getWriter().println(json);
				if (unknown)
					RTBServer.unknown++;
				RTBServer.concurrentConnections--;
				
				Controller.getInstance().sendLog(5,"Handler:response",json);
				return;
			}

			// //////////////////////////////////////////////////////////////////////
			if (target.contains("/rtb/win")) {
				StringBuffer url = request.getRequestURL();
				String queryString = request.getQueryString();
				response.setStatus(HttpServletResponse.SC_OK);
				json = "";
				if (queryString != null) {
					url.append('?');
					url.append(queryString);
				}
				String requestURL = url.toString();

				try {
					json = WinObject.getJson(requestURL);
					RTBServer.win++;
				} catch (Exception error) {
					response.setHeader("X-ERROR",
							"Error processing win response");
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					Controller.getInstance().sendLog(2, "Handler:handle",
							"Bad win response " + requestURL);
					error.printStackTrace();
				}
				response.setContentType("text/html;charset=utf-8");
				baseRequest.setHandled(true);
				response.getWriter().println(json);
				RTBServer.concurrentConnections--;
				return;
			}

			if (target.contains("/pixel")) {
				Controller.getInstance().publishPixel(target);
				response.setContentType("image/bmp;charset=utf-8");
				response.setStatus(HttpServletResponse.SC_OK);
				baseRequest.setHandled(true);
				response.getWriter().println("");
				RTBServer.concurrentConnections--;
				return;
			}

			if (target.contains("/redirect")) {
				Controller.getInstance().publishClick(target);
				
				StringBuffer url = request.getRequestURL();
				String queryString = request.getQueryString();
				String params [] = queryString.split("url=");
		
				baseRequest.setHandled(true);			
				response.sendRedirect(params[1]);
				RTBServer.concurrentConnections--;
				return;
			}
			
			if (target.contains("info")) {
				response.setContentType("text/javascript;charset=utf-8");
				response.setStatus(HttpServletResponse.SC_OK);
				baseRequest.setHandled(true);
				Echo e = RTBServer.getStatus();
				String rs = e.toJson();
				response.getWriter().println(rs);
				RTBServer.concurrentConnections--;
				return;
			}

			/**
			 * These are not part of RTB, but are used as part of the simulator and campaign administrator that sit on the
			 * same port as the RTB.
			 */

			if (type != null && type.contains("multipart/form-data")) {
				try {
					json = WebCampaign.getInstance().multiPart(baseRequest,
							request, MULTI_PART_CONFIG);
					response.setStatus(HttpServletResponse.SC_OK);
				} catch (Exception err) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					Controller.getInstance().sendLog(2, "Handler:handle",
							"Bad non-bid transaction on multiform reqeues");
				}
				baseRequest.setHandled(true);
				response.getWriter().println(json);
				return;
			}

			if (target.contains("favicon")) {
				RTBServer.handled--;      // don't count this useless turd.
				response.setStatus(HttpServletResponse.SC_OK);
				baseRequest.setHandled(true);
				response.getWriter().println("");
				RTBServer.concurrentConnections--;
				return;
			}

			if (target.contains(RTBServer.SIMULATOR_URL)) {
				String page = Charset
						.defaultCharset()
						.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
								.get(RTBServer.SIMULATOR_ROOT)))).toString();

				response.setContentType("text/html");
				response.setStatus(HttpServletResponse.SC_OK);
				baseRequest.setHandled(true);
				response.getWriter().println(page);
				RTBServer.concurrentConnections--;
				return;
			}

			// ///////////////////////////
			if (target.contains("ajax")) {
				response.setContentType("text/javascript;charset=utf-8");
				response.setStatus(HttpServletResponse.SC_OK);
				baseRequest.setHandled(true);
				String data = WebCampaign.getInstance().handler(request, body);
				response.getWriter().println(data);
				RTBServer.concurrentConnections--;
				return;
			}

			// ///////////////////////////

			if (target.contains(RTBServer.CAMPAIGN_URL)) {
				String page = Charset
						.defaultCharset()
						.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
								.get(RTBServer.CAMPAIGN_ROOT)))).toString();

				response.setContentType("text/html");
				response.setStatus(HttpServletResponse.SC_OK);
				baseRequest.setHandled(true);
				response.getWriter().println(page);
				RTBServer.concurrentConnections--;
				return;
			}

			if (target.contains(RTBServer.LOGIN_URL)) {
				String page = Charset
						.defaultCharset()
						.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
								.get(RTBServer.LOGIN_ROOT)))).toString();

				response.setContentType("text/html");
				response.setStatus(HttpServletResponse.SC_OK);
				baseRequest.setHandled(true);
				response.getWriter().println(page);
				RTBServer.concurrentConnections--;
				return;
			}
			
			if (target.contains(RTBServer.ADMIN_URL)) {
				String page = Charset
						.defaultCharset()
						.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
								.get(RTBServer.ADMIN_ROOT)))).toString();

				response.setContentType("text/html");
				response.setStatus(HttpServletResponse.SC_OK);
				baseRequest.setHandled(true);
				response.getWriter().println(page);
				RTBServer.concurrentConnections--;
				return;
			}

			// /////////////////////////////////////////////////////////////////////////////////////////////////////////////

		} catch (Exception e) {
			try {
				Controller.getInstance().sendLog(2, "Handler:handle",
						"Bad html processing on " + target);
				e.printStackTrace();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			RTBServer.error++;
			json = "{ \"error\":\"" + e.toString() + "\"}";
			code = RTBServer.NOBID_CODE;
			return;
		}

		/**
		 * This set of if's handle non bid request transactions.
		 * 
		 */
		try {
			type = null;
			target = target = target.replaceAll("xrtb/simulator/", "");
			int x = target.lastIndexOf(".");
			if (x >= 0) {
				type = target.substring(x);
			}
			if (type != null) {
				type = type.toLowerCase().substring(1);
				type = MimeTypes.substitute(type);
				response.setContentType(type);
				File f = new File("./www/" + target);
				if (f.exists() == false) {
					f = new File("./web/" + target);
					if (f.exists() == false) {
						f = new File(target);
						if (f.exists() == false) {
							f = new File("." + target);
							if (f.exists() == false) {
								response.setStatus(HttpServletResponse.SC_NOT_FOUND);
								baseRequest.setHandled(true);
								return;
							}
						}
					}
				}
					FileInputStream fis = new FileInputStream(f);
					OutputStream out = response.getOutputStream();

					// write to out output stream
					while (true) {
						int bytedata = fis.read();

						if (bytedata == -1) {
							break;
						}

						try {
							out.write(bytedata);
						} catch (Exception error) {
							break; // screw it, pray that it worked....
						}
					}

					// flush and close streams.....
					fis.close();
					try {
						out.close();
					} catch (Exception error) {

					}
					RTBServer.concurrentConnections--;
					return;

				}

			/**
			 * Ok, we don't have a .type on the file, so we are assuming .html
			 */
			target = "www" + target;


			String page = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get(target)))).toString();

			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
			response.getWriter().println(page);
			RTBServer.concurrentConnections--;
		} catch (Exception err) {
		//	err.printStackTrace();
			RTBServer.concurrentConnections--;
			return;
		}

	}


	/**
	 * Checks to see if the bidder wants to bid on only a certain percentage of
	 * bid requests coming in - a form of throttling.
	 * <p>
	 * If percentage is set to .20 then twenty percent of the bid requests will
	 * be rejected with a NO-BID return on 20% of all traffic received by the
	 * Handler.
	 * 
	 * @return boolean. True means try to bid, False means don't bid
	 */
	boolean checkPercentage() {
		if (RTBServer.percentage == 100)
			return true;
		int x = rand.nextInt(101);
		if (x < RTBServer.percentage)
			return true;
		return false;
	}
}