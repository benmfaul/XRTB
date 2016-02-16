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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.xrtb.commands.Echo;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.common.Creative;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;
import com.xrtb.pojo.NobidResponse;
import com.xrtb.pojo.WinObject;
import com.xrtb.tools.NameNode;

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
	public static AtomicLong percentage = new AtomicLong(100); // throttle is wide open at 100, closed
										// at 0

	/** Indicates of the server is not accepting bids */
	public static boolean stopped = false; // is the server not accepting bid
											// requests?

	/** a counter for the number of requests the bidder has received and processed */
	public static long request = 0;
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
	public static long win = 0;
	/** The number of clicks processed */
	public static long clicks = 0;
	/** The number of pixels fired */
	public static long pixels = 0;
	/** The number of connections */
	public static long connections;
	/** The average time */
	public static long avgBidTime;
	/** Fraud counter */
	public static long fraud = 0;
	/** xtime counter */
	public static long xtime = 0;
	/** The hearbead pool controller */
	public static MyNameNode node;
	/** double adpsend */
	public static volatile double adspend;
	/** is the server ready to receive data */
	boolean ready;

	static long deltaTime = 0, deltaWin = 0, deltaClick = 0, deltaPixel = 0,
			deltaNobid = 0, deltaBid = 0;
	static double qps = 0;
	static double avgx = 0;

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
	 *            port 8080. Options [-s shardkey] [-p port]
	 * @throws Exception
	 *             if the Server could not start (network error, error reading
	 *             configuration)
	 */
	public static void main(String[] args) throws Exception {

		String fileName = "Campaigns/payday.json";
		String shard = "";
		Integer port = 8080;
		if (args.length == 1)
			fileName = args[0];
		else {
			int i = 0;
			while (i < args.length) {
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

		Configuration.reset(); // this resquired so that when the server is
								// restarted, the old config won't stick around.
		AddShutdownHook hook = new AddShutdownHook();
		hook.attachShutDownHook();

		Configuration.getInstance(fileName);
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
	 * @param shard
	 *            String. The shard key of this bidder instance.
	 * @param port
	 *            . int. The port to use for this bidder.
	 * @throws Exception
	 *             if the Server could not start (network error, error reading
	 *             configuration)
	 */
	public RTBServer(String fileName, String shard, int port) throws Exception {

		Configuration.reset(); // this resquired so that when the server is
								// restarted, the old config won't stick around.
		AddShutdownHook hook = new AddShutdownHook();
		hook.attachShutDownHook();

		Configuration.getInstance(fileName, shard, port);
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
	 * Returns whether the server has started.
	 * 
	 * @return boolean. Returns true if ready to start.
	 */
	public boolean isReady() {
		return ready;
	}

	public static void panicStop() {
		try {
			Controller.getInstance().sendShutdown();
			Controller.getInstance().sendLog(1, "panicStop",
					("Bidder is shutting down *** NOW ****"));
			node.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set summary stats.
	 */
	public static void setSummaryStats() {
		if (xtime == 0)
			avgx = 0;
		else
			avgx = (nobid + bid) / (double) xtime;

		if (System.currentTimeMillis() - deltaTime < 30000)
			return;

		deltaWin = win - deltaWin;
		deltaClick = clicks - deltaClick;
		deltaPixel = pixels - deltaPixel;
		deltaBid = bid - deltaBid;
		deltaNobid = nobid - deltaNobid;

		// System.out.println("+++>"+win+", "+clicks+", "+pixels+", "+bid+", "+nobid);
		// System.out.println("--->"+deltaWin+", "+deltaClick+", "+deltaPixel+", "+deltaBid+", "+deltaNobid);

		qps = (deltaWin + deltaClick + deltaPixel + deltaBid + deltaNobid);
		long secs = (System.currentTimeMillis() - deltaTime) / 1000;
		qps = qps / secs;
		deltaTime = System.currentTimeMillis();
	}

	/**
	 * Retrieve a summary of activity.
	 * 
	 * @return String. JSON based stats of server performance.
	 */
	public static String getSummary() {
		setSummaryStats();
		Gson gson = new Gson();
		Map m = new HashMap();
		m.put("stopped", stopped);
		m.put("loglevel", Configuration.getInstance().logLevel);
		m.put("ncampaigns", Configuration.getInstance().campaignsList.size());
		m.put("qps", qps);
		m.put("deltax", avgx);
		m.put("nobidreason", Configuration.getInstance().printNoBidReason);
		return gson.toJson(m);
	}

	/**
	 * Establishes the HTTP Handler, creates the Jetty server and attaches the
	 * handler and then joins the server. This method does not return, but it is
	 * interruptable by calling the halt() method.
	 * 
	 */
	@Override
	public void run() {

		QueuedThreadPool threadPool = new QueuedThreadPool(256, 50);

		server = new Server(threadPool);
		ServerConnector connector = new ServerConnector(server);
		connector.setPort(port);
		server.setConnectors(new Connector[] { connector });

		Handler handler = new Handler();
		node = null;

		try {
			BidRequest.compile();
			SessionHandler sh = new SessionHandler(); // org.eclipse.jetty.server.session.SessionHandler
			sh.setHandler(handler);

			server.setHandler(sh); // set session handle

			Controller.getInstance().sendLog(1, "initialization",
					("System start on port: " + port));

			node = new MyNameNode(Configuration.cacheHost,
					Configuration.cachePort);
			

			/**
			 * Quickie task for periodic logging
			 */
			Runnable task = () -> {
				while (true) {
					try {
						Thread.sleep(60000);
						String msg = "connections= " + connections + ", avgBidTime= " + avgBidTime + ", total=" + handled + ", requests=" + request + ", bids=" + bid
								+ ", nobids=" + nobid + ", fraud=" + fraud + ", wins=" + win
								+ ", pixels=" + pixels + ", clicks=" + clicks
								+ ", stopped=" + stopped;
						Controller.getInstance().sendLog(1, "Hearbeat", msg);
					} catch (Exception e) {
						e.printStackTrace();
						return;
					}
				}
			};
			Thread thread = new Thread(task);
			thread.start();
			// ///////////////////////////////////////
			
			/**
			 * Override the start state if the deadmanswitch object is not null and the key doesn't exist
			 */
			if (Configuration.deadmanSwitch != null) {
				if (Configuration.deadmanSwitch.canRun()==false) {
					RTBServer.stopped = true;
				}
			}

			server.start();

			ready = true;
			deltaTime = System.currentTimeMillis(); // qps timer

			Controller.getInstance().responseQueue.add(getStatus());
			server.join();
		} catch (Exception error) {
			if (error.toString().contains("Interrupt"))

				try {
					Controller.getInstance().sendLog(1, "initialization",
							"HALT: : " + error.toString());
					node.halt();
				} catch (Exception e) {
					// TODO Auto-generated catch block
				}
			else
				error.printStackTrace();
		} finally {
			node.stop();
			return;
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
		setSummaryStats();
		Echo e = new Echo();
		e.from = Configuration.getInstance().instanceName;
		e.percentage = percentage.intValue();
		e.stopped = stopped;
		e.request = request;
		e.bid = bid;
		e.win = win;
		e.nobid = nobid;
		e.error = error;
		e.handled = handled;
		e.unknown = unknown;
		e.clicks = clicks;
		e.pixel = pixels;
		e.fraud = fraud;
		e.adspend = adspend;
		e.loglevel = Configuration.getInstance().logLevel;
		e.qps = qps;
		e.campaigns = Configuration.getInstance().campaignsList;
		e.avgx = avgx;
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
	
	static long totalBidTime = 0;
	static long window = 0;

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

				RTBServer.connections++;
				
				RTBServer.request++;
				
				/************* Uncomment to run smaato compliance testing ****************************************/
				/*Enumeration<String> params = request.getParameterNames();
				String tester = null;
				if (params.hasMoreElements()) {
					smaatoCompliance(target, baseRequest, request, response,body);
					return;

				} */
				/************************************************************************************************/
			

				BidRequest x = RTBServer.exchanges.get(target);

				if (x == null) {
					json = "Wrong target: " + target;
					code = RTBServer.NOBID_CODE;
					Controller.getInstance().sendLog(2, "Handler:handle:error",
							json);
				} else {

					unknown = false;
					// RunRecord log = new RunRecord("bid-request");
					br = x.copy(body);

					Controller.getInstance().sendRequest(br);
					id = br.getId();
					if (br.isFraud) {
						json = "Forensiq score is too high: " + br.fraudRecord.risk;
						code = RTBServer.NOBID_CODE;
						RTBServer.nobid++;
						RTBServer.fraud++;
						Controller.getInstance().sendNobid(new NobidResponse(br.id, br.exchange));
						Controller.getInstance().publishFraud(br.fraudRecord);
					} else
					if (CampaignSelector.getInstance().size() == 0) {
						json = "No campaigns loaded";
						code = RTBServer.NOBID_CODE;
						RTBServer.nobid++;
						Controller.getInstance().sendNobid(
								new NobidResponse(br.id, br.exchange));
					} else if (RTBServer.stopped) {
						json = "Server stopped";
						code = RTBServer.NOBID_CODE;
						RTBServer.nobid++;
						Controller.getInstance().sendNobid(
								new NobidResponse(br.id, br.exchange));
					} else if (!checkPercentage()) {
						json = "Server throttled";
						code = RTBServer.NOBID_CODE;
						RTBServer.nobid++;
						Controller.getInstance().sendNobid(
								new NobidResponse(br.id, br.exchange));
					} else {
						BidResponse bresp = CampaignSelector.getInstance().get(
								br); // 93% time here
						// log.add("select");
						if (bresp == null) {
							json = "No matching campaign";
							code = RTBServer.NOBID_CODE;
							RTBServer.nobid++;
							Controller.getInstance().sendNobid(
									new NobidResponse(br.id, br.exchange));
						} else {
							json = bresp.toString();
							Controller.getInstance().sendBid(bresp);
							Controller.getInstance().recordBid(bresp);
							code = RTBServer.BID_CODE;
							RTBServer.bid++;
						}
					}
					// log.dump();
				}

				time = System.currentTimeMillis() - time;

				response.setHeader("X-TIME", "" + time);
				RTBServer.xtime += time;

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

				Controller.getInstance().sendLog(5, "Handler:response", json);
				
				if (code == 200) {
					totalBidTime += time;
					if (window++ > 20) {
						RTBServer.avgBidTime = totalBidTime / window;
						window = 0;
						totalBidTime = 0;
					}
				}
				RTBServer.connections--;
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
				return;
			}

			if (target.contains("/pixel")) {
				Controller.getInstance().publishPixel(target);
				response.setContentType("image/bmp;charset=utf-8");
				response.setStatus(HttpServletResponse.SC_OK);
				baseRequest.setHandled(true);
				response.getWriter().println("");
				RTBServer.pixels++;
				return;
			}

			if (target.contains("/redirect")) {
				Controller.getInstance().publishClick(target);
				StringBuffer url = request.getRequestURL();
				String queryString = request.getQueryString();
				String params[] = queryString.split("url=");

				baseRequest.setHandled(true);
				response.sendRedirect(params[1]);
				RTBServer.clicks++;
				return;
			}

			if (target.contains("info")) {
				response.setContentType("text/javascript;charset=utf-8");
				response.setStatus(HttpServletResponse.SC_OK);
				baseRequest.setHandled(true);
				Echo e = RTBServer.getStatus();
				String rs = e.toJson();
				response.getWriter().println(rs);
				return;
			}

			if (target.contains("summary")) {
				response.setContentType("text/javascript;charset=utf-8");
				response.setStatus(HttpServletResponse.SC_OK);
				baseRequest.setHandled(true);
				response.getWriter().println(RTBServer.getSummary());
				return;
			}

			/**
			 * These are not part of RTB, but are used as part of the simulator
			 * and campaign administrator that sit on the same port as the RTB.
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
				RTBServer.handled--; // don't count this useless turd.
				response.setStatus(HttpServletResponse.SC_OK);
				baseRequest.setHandled(true);
				response.getWriter().println("");
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
				return;
			}

			// ///////////////////////////
			if (target.contains("ajax")) {
				response.setContentType("text/javascript;charset=utf-8");
				response.setStatus(HttpServletResponse.SC_OK);
				baseRequest.setHandled(true);
				String data = WebCampaign.getInstance().handler(request, body);
				response.getWriter().println(data);
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
				;
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
			baseRequest.setHandled(true);
			StringBuffer str = new StringBuffer("{ \"error\":\"");
			str.append(e.toString());
			str.append("\"}");
			code = RTBServer.NOBID_CODE;
			response.getWriter().println(str.toString());
			return;
		}

		/**
		 * This set of if's handle non bid request transactions.
		 * 
		 */
		try {
			type = null;
			/**
			 * Get rid of artifacts coming from embedde urls
			 */
			if (target.contains("simulator/temp/test") == false)
				target = target = target.replaceAll("xrtb/simulator/temp/", ""); // load
																					// the
																					// html
																					// test
																					// file
																					// from
																					// here
																					// but
																					// not
																					// resources
			target = target = target.replaceAll("xrtb/simulator/", "");
			
			if (target.equals("/"))
				target = "/index.html";

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
		} catch (Exception err) {
			// err.printStackTrace();
		}
	}

	private void smaatoCompliance(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response,
			InputStream body) throws Exception {
		String tester = null;
		String json = null;
		BidRequest br = null;

		Enumeration<String> params = request.getParameterNames();
		if (params.hasMoreElements()) {
			String[] dobid = request.getParameterValues(params.nextElement());
			tester = dobid[0];
			System.out
					.println("=================> SMAATO TEST ====================");
		}

		if (tester.equals("nobid")) {
			RTBServer.nobid++;
			baseRequest.setHandled(true);
			response.setStatus(RTBServer.NOBID_CODE);
			response.getWriter().println("");
			Controller.getInstance().sendLog(1, "Handler:handle",
					"SMAATO NO BID TEST ENDPOINT REACHED");
			Controller.getInstance().sendNobid(
					new NobidResponse(br.id, br.exchange));
			return;
		} else {
			BidRequest x = RTBServer.exchanges.get(target);
			br = x.copy(body);
			Controller.getInstance().sendRequest(br);

			Controller.getInstance().sendLog(1, "Handler:handle",
					"SMAATO MANDATORY BID TEST ENDPOINT REACHED");
			BidResponse bresp = CampaignSelector.getInstance().getSpecific(br,
					"ben", "smaato-test", "image-test");
			if (bresp == null) {
				baseRequest.setHandled(true);
				response.setStatus(RTBServer.NOBID_CODE);
				response.getWriter().println("");
				Controller.getInstance().sendLog(1, "Handler:handle",
						"SMAATO FORCED BID TEST ENDPOINT FAILED");
				Controller.getInstance().sendNobid(
						new NobidResponse(br.id, br.exchange));
				return;
			}
			json = bresp.toString();
			baseRequest.setHandled(true);
			Controller.getInstance().sendBid(bresp);
			Controller.getInstance().recordBid(bresp);
			RTBServer.bid++;
			response.setStatus(RTBServer.BID_CODE);

			response.getWriter().println(json);

			System.out
					.println("+++++++++++++++++++++ SMAATO REQUEST ++++++++++++++++++++++\n\n"
							+

							br.toString() +

							"\n\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

			System.out
					.println("===================== SMAATO BID ==========================\n\n"
							+ json
							+ "\n\n==========================================================");

			Controller.getInstance().sendLog(1, "Handler:handle",
					"SMAATO FORCED BID TEST ENDPOINT REACHED OK");
			return;
		}
		/************************************************************************************/
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
		if (RTBServer.percentage.intValue() == 100)
			return true;
		int x = rand.nextInt(101);
		if (x < RTBServer.percentage.intValue())
			return true;
		return false;
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

/**
 * This bidder's instance of name node
 * 
 * @author en M. Faul
 *
 */
class MyNameNode extends NameNode {

	public MyNameNode(String host, int port) throws Exception {
		super(Configuration.getInstance().instanceName, host, port, Configuration.getInstance().password);
	}

	@Override
	public void log(int level, String where, String msg) {
		try {
			Controller.getInstance().sendLog(3, where, msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

class AddShutdownHook {
	public void attachShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				RTBServer.panicStop();
			}
		});
		System.out.println("*** Shut Down Hook Attached. ***");
	}
}