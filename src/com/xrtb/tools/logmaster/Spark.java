package com.xrtb.tools.logmaster;

import java.io.File;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.util.Set;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import com.aerospike.client.AerospikeClient;
import com.aerospike.redisson.RedissonClient;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.commands.PixelClickConvertLog;
import com.xrtb.common.Campaign;

import com.xrtb.common.Creative;
import com.xrtb.common.ForensiqLog;
import com.xrtb.db.DataBaseObject;
import com.xrtb.db.User;
import com.xrtb.jmq.MessageListener;
import com.xrtb.jmq.RTopic;
import com.xrtb.pojo.BidResponse;
import com.xrtb.pojo.NobidResponse;
import com.xrtb.pojo.WinObject;

/**
 * A class that implements a file logger of the various REDISSON channels of RTB
 * information. Also, creates accounting records for all the campaigns in the
 * database, summarizing win cost, pixel fires, and clicks. These accounting
 * records are summaries over the accounting period, which is 1 minutes. The
 * different channels are loaded into different files, named: bids, wins,
 * requests, nobids, clicks, and accounting. All of the records are JSON, one
 * record per line ending in '\n'.
 * 
 * While this example a file logger, it is easy to make different loggers, say
 * for Hadoop's HDFS or Mongodb. To do this, extend your own logger from
 * AbstractSparkLogger. Create the appropriate constructor (call super(interval)
 * and then implement the abstract method execute(). Then just instantiate the
 * logger appropriately in this class.
 * 
 * @author Ben M. Faul
 *
 */

public class Spark implements Runnable {

	/** The redisson backed shared map that represents this database */

	RedissonClient redisson;


	List<AcctCreative> creatives = new ArrayList();
	Map<String, AcctCreative> accountHash = new HashMap();
	
	DataBaseObject dbo;

	Thread me;
	String zeromq = "localhost";

	static int INTERVAL = 60000;
	static String BIDCHANNEL = "5571&bids";
	static String WINCHANNEL = "5572&wins";
	static String CLICKCHANNEL = "5573&clicks";

	public AtomicLong requests = new AtomicLong(0);
	public AtomicLong bids = new AtomicLong(0);
	public AtomicLong wins = new AtomicLong(0);
	public AtomicLong nobids = new AtomicLong(0);
	public AtomicLong clicks = new AtomicLong(0);
	public AtomicLong pixels = new AtomicLong(0);
	public AtomicLong fraud = new AtomicLong(0);

	public AtomicLong winCost = new AtomicLong(0);
	public AtomicLong bidCost = new AtomicLong(0);

	public static ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
				false);
	}

	static boolean init = false;
	static String logDir = "logs";

	AbstractSparkLogger logger;

	/**
	 * Set up the spark logger.
	 * 
	 * @param args
	 *            . The arguments: -redis host:port Set the redis host/port,
	 *            default localhost:6379 -clicks clicklogchannel Set the click
	 *            log channel to listen to, default is clicks -bids Set the bids
	 *            channel to listen to, default is bids -nobids Set the nobids
	 *            channel to listen to, default is nobids -wins Set the wins
	 *            channel to listen to, default is wins -requests Set the
	 *            requests channel to listen to, default is requests -logdir Set
	 *            the logdir, default is ./logs -purge Delete the contents of
	 *            logdir before starting
	 * @throws Exception
	 *             on REDIS errors.
	 */
	public static void main(String[] args) throws Exception {
		int i = 0;
		String spike = "localhost:3000";
		boolean purge = false;
	    String zeromq = "localhost";
		if (args.length > 0) {
			while (i < args.length) {
				switch (args[i]) {
				case "-h":
					System.out.println("-aero  host:port   [Set the Aerospike host and port, default localhost:6379]");
					System.out.println("-init true | false [Initialize the accounting system, default is false]");
					System.out.println("-logdir dirname    [Where to place the logs, default is ./logs]");
					System.out.println("-purge             [Delete the log records already produced, default no purge]");
					System.out.println("-interval          [Set the accounting interval, default is 60000 (60 seconds)]");
					i++;
					break;
				case "-spike":
					spike = args[i + 1];
					i += 2;
					break;
				case "-clicks":
					CLICKCHANNEL = args[i + 1];
					i += 2;
					break;
				case "-wins":
					WINCHANNEL = args[i + 1];
					i += 2;
					break;
				case "-bids":
					BIDCHANNEL = args[i + 1];
					i += 2;
					break;
				case "-init":
					init = Boolean.parseBoolean(args[i + 1]);
					i += 2;
					break;
				case "-logdir":
					logDir = args[i + 1];
					i += 2;
					break;
				case "-purge":
					i++;
					purge = true;
					break;
				case "-interval":
					INTERVAL = Integer.parseInt(args[i + 1]);
					i += 2;
					break;
				case "-zeromq":
					zeromq = args[i+1];
					i+=2;
					break;
				default:
					System.out.println("Huh? " + args[i]);
					System.exit(1);
				}
			}
		}

		if (purge) {
			File dir = new File(logDir);
			for (File file : dir.listFiles())
				file.delete();
		}

		Spark sp = new Spark(spike, zeromq, init);
	}

	/**
	 * Create a default spark logger.
	 */
	public Spark() throws Exception {
		this("localhost:3000", "localhost", false);
		me = new Thread(this);

		me.start();
	}

	/**However. since you are using a password, how are you starting spark? It too e
	 * Periodic processor. This writes the summary account/creative records.
	 * Which are summaries from the last period (which is 1 minute).
	 */
	public void run() {
		while (true) {
			try {
				Thread.sleep(60000);
				if (init) {
					String content = null;
					for (AcctCreative c : creatives) {
						synchronized (c) {
							c.time = System.currentTimeMillis();
							content = mapper.writer().writeValueAsString(c);
							c.clear();
						}
						logger.offer(new LogObject("accounting", content));
					}
				}
				BigDecimal winCostX = new BigDecimal(winCost.get());
				BigDecimal bidCostX = new BigDecimal(bidCost.get());
				winCostX = winCostX.divide(new BigDecimal(1000));
				bidCostX = bidCostX.divide(new BigDecimal(1000));

				System.out
						.println("-------------------- STATS ------------------------");
				System.out.println("REQUESTS = " + requests.get());
				System.out.println("FRAUD = " + fraud.get());
				System.out.println("BIDS = " + bids.get());
				System.out.println("NO-BIDS = " + nobids.get());
				System.out.println("WINS = " + wins.get());
				System.out.println("PIXELS = " + pixels.get());
				System.out.println("CLICKS = " + clicks.get());
				System.out.println("BID COST = " + bidCostX.doubleValue());
				System.out.println("WIN COST = " + winCostX.doubleValue());
				System.out
						.println("----------------------------------------------------");

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Create a spark logger using the specified redis info.
	 * 
	 * @param redis
	 *            String. The redishost:port info.
	 * @param init
	 *            boolean. Set to true to zero the atomic counts.
	 */
	public Spark(String host, String zeromq, boolean init) throws Exception {
		this.zeromq = zeromq;
		int port = 3000;
		String parts[] = host.split(":");
		if (parts.length > 1)
			port = Integer.parseInt(parts[1]);

		AerospikeClient spike = new AerospikeClient(parts[0],port);
		redisson = new com.aerospike.redisson.RedissonClient(spike);
		dbo = DataBaseObject.getInstance(redisson);;

		me = new Thread(this);
		me.start();
		if (init)
			initialize();
	}

	/**
	 * Get the message handlers lashed up to handle all the accounting
	 * information.
	 */
	public void initialize() throws Exception  {

		logger = new FileLogger(INTERVAL); // Instantiate your own logger if you
											// don't want to log to files.

		List<String> users = dbo.listUsers();

		for (String user : users) {
			User u = dbo.get(user);
			System.out.println("========>" + u.name);
			String acctName = u.name;
			for (Campaign c : u.campaigns) {
				String campName = c.adId;
				for (Creative creat : c.creatives) {
					AcctCreative cr = new AcctCreative(acctName, campName,
							creat.impid);
					creatives.add(cr);
					accountHash.put(campName + ":" + creat.impid, cr);
					System.out.println("Loaded: " + campName + ":"
							+ creat.impid);
				}
			}
		}

		/**
		 * Win Notifications HERE
		 */
		String address = getAddress(zeromq,WINCHANNEL);
		RTopic winners = new RTopic(address);
		winners.addListener(new MessageListener<WinObject>() {
			@Override
			public void onMessage(String channel, WinObject msg) {
				try {
					processWin(msg);
				} catch (Exception error) {
					error.printStackTrace();
				}
			}
		});

		System.out.println("Ok Spark is running!");

		address = getAddress(zeromq,BIDCHANNEL);
		RTopic bidresponse = new RTopic(address);
		bidresponse.addListener(new MessageListener<BidResponse>() {
			@Override
			public void onMessage(String channel, BidResponse msg) {
				try {
					processBid(msg);
				} catch (Exception error) {
					error.printStackTrace();
				}
			}
		});

		address = getAddress(zeromq,CLICKCHANNEL);
		RTopic pixelandclicks = new RTopic(address);
		pixelandclicks.addListener(new MessageListener<Object>() {
			@Override
			public void onMessage(String channel, Object msg) {
				try {
					processClickAndPixel(msg);
				} catch (Exception error) {
					error.printStackTrace();
				}
			}
		});

	}
	
	public String getAddress(String host, String channel) {
		String address = "tcp://"+host +":" + channel;
		return address;
	}

	/**
	 * Process a win record.
	 * 
	 * @param win
	 *            WinObject. The object to be counted and logged.
	 * @throws Exception
	 *             on atomic access errors.
	 */
	public void processWin(WinObject win) throws Exception {
		String campaign = win.adId;
		String impid = win.cridId;
		double cost = Double.parseDouble(win.price);

		AcctCreative creat = getRecord(campaign, impid);
		if (creat == null) {
			creat = new AcctCreative("unknown", win.adId,
					win.cridId);
			creatives.add(creat);
			accountHash.put("unknown" + ":" + win.cridId,creat);
		}


		wins.incrementAndGet();

		synchronized (creat) {
			creat.wins++;
			creat.winPrice += cost;

			winCost.addAndGet((int) (cost * 1000));
		}

		String content = mapper.writer().writeValueAsString(win);
		logger.offer(new LogObject("win", content));
	}

	/**
	 * Process a bid request.
	 * 
	 * @param br
	 *            BidRequest. The request to be processed.
	 * @throws Exception
	 */
	public void processRequests(JsonNode br) throws Exception {
		requests.incrementAndGet();
		String content = mapper.writer().writeValueAsString(br);
		logger.offer(new LogObject("request", content));
	}

	public void processForensiq(ForensiqLog log) throws Exception {
		fraud.incrementAndGet();
		String content = mapper.writer().writeValueAsString(log);
		logger.offer(new LogObject("forensiq", content));
	}

	/**
	 * Process a nobid notification.
	 * 
	 * @param nb
	 *            NobidResponse. The request not processed. Just counts it.
	 * @throws Exception
	 *             on atomic access errors.
	 */
	public void processNobid(NobidResponse nb) throws Exception {
		nobids.incrementAndGet();
		String content = mapper.writer().writeValueAsString(nb);
		logger.offer(new LogObject("nobid", content));
	}

	/**
	 * Process clicks and pixels.
	 * 
	 * @param ev
	 *            PixelClickConvertLog. The object to be counted.
	 * @throws ExceptioncampName
	 *             on atomic access errors.
	 */
	public void processClickAndPixel(Object x) throws Exception {
		PixelClickConvertLog ev = (PixelClickConvertLog)x;
		Map m = new HashMap();
		m.put("time", ev.time);
		String type = null;

		AcctCreative creat = getRecord(ev.ad_id, ev.creative_id);

		if (creat == null) {
			creat= new AcctCreative("unknown", ev.ad_id,
					ev.creative_id);
			creatives.add(creat);
			accountHash.put("unknown" + ":" + ev.creative_id,creat);
		}

		m.put("campaign", creat.campaignName);
		m.put("creative", creat.name);

		synchronized (creat) {
			if (ev.type == PixelClickConvertLog.CLICK) {
				type = "click";
				creat.clicks++;
				clicks.incrementAndGet();
			} else if (ev.type == PixelClickConvertLog.PIXEL) {
				type = "pixel";
				creat.pixels++;
				pixels.incrementAndGet();
			} else {

			}
		}
		String content = mapper.writer().writeValueAsString(ev);
		logger.offer(new LogObject(type, content));
	}

	/**
	 * Process a bid from the bids channel.
	 * 
	 * @param br
	 *            BidRequest. The bid request object.
	 * @throws Exception
	 *             on bad access to atomic variables.
	 */
	public void processBid(BidResponse br) throws Exception {
		String campaign = br.adid;
		String impid = br.crid;
		double cost = br.cost;

		AcctCreative creat = getRecord(campaign, impid);
		if (creat == null) {
			creat= new AcctCreative("unknown", br.adid,
					br.crid);
			creatives.add(creat);
			accountHash.put("unknown" + ":" + br.crid,creat);
		}


		bids.incrementAndGet();
		synchronized (creat) {
			creat.bids++;
			creat.bidPrice += cost;
			bidCost.addAndGet((int) (cost * 1000));
		}

		String content = mapper.writer().writeValueAsString(br);
		logger.offer(new LogObject("bid", content));

	}

	/**
	 * Given a campaign name and impression id, retrieve the account creative
	 * record.
	 * 
	 * @param campaign
	 *            String. The name of the campaign.
	 * @param impid
	 *            String. The impression id.
	 * @return AcctCreative. A summary accounting record this campaign/creative.
	 */
	public AcctCreative getRecord(String campaign, String impid) {
		AcctCreative cr = accountHash.get(campaign + ":" + impid);
		return cr;
	}
}