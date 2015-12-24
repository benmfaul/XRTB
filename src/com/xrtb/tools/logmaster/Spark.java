package com.xrtb.tools.logmaster;

import java.io.File;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.RedissonClient;
import org.redisson.core.MessageListener;
import org.redisson.core.RTopic;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.commands.PixelClickConvertLog;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.common.Creative;
import com.xrtb.db.User;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;
import com.xrtb.pojo.NobidResponse;
import com.xrtb.pojo.WinObject;

/**
 * A class that implements a file logger of the various REDISSON channels of RTB information. Also, creates accounting
 * records for all the campaigns in the database, summarizing win cost, pixel fires, and clicks. These accounting records
 * are summaries over the accounting period, which is 1 minutes. The different channels are loaded into different files, named:
 * bids, wins, requests, nobids, clicks, and accounting. All of the records are JSON, one record per line ending in '\n'.
 * 
 * While this example a file logger, it is easy to make different loggers, say for Hadoop's HDFS or Mongodb. To do this,
 * extend your own logger from AbstractSparkLogger. Create the appropriate constructor (call super(interval) and then
 * implement the abstract method execute(). Then just instantiate the logger appropriately in this class.
 * 
 * @author Ben M. Faul
 *
 */

public class Spark implements Runnable {

	/** The redisson backed shared map that represents this database */
	ConcurrentMap<String, User> map;
	/** The redisson proxy object behind the map */
	RedissonClient redisson;
	/** The redisson configuration object */
	Config cfg = new Config();

	List<AcctCreative> creatives = new ArrayList();
	Map<String, AcctCreative> accountHash = new HashMap();

	Thread me;
	
	static int INTERVAL = 60000;
	static String BIDCHANNEL = "bids";
	static String WINCHANNEL = "wins";
	static String REQUESTCHANNEL = "requests";
	static String NOBIDCHANNEL = "nobids";
	static String CLICKCHANNEL = "clicks";
	
	AtomicLong requests = new AtomicLong(0);
	AtomicLong bids = new AtomicLong(0);
	AtomicLong wins = new AtomicLong(0);
	AtomicLong nobids = new AtomicLong(0);
	AtomicLong clicks = new AtomicLong(0);
	AtomicLong pixels = new AtomicLong(0);
	
	AtomicLong winCost = new AtomicLong(0);
	AtomicLong bidCost = new AtomicLong(0);

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
	 * @param args. The arguments:
	 * -redis host:port					Set the redis host/port, default localhost:6379
	 * -clicks clicklogchannel			Set the click log channel to listen to, default is clicks
	 * -bids							Set the bids channel to listen to, default is bids
	 * -nobids							Set the nobids channel to listen to, default is nobids
	 * -wins							Set the wins channel to listen to, default is wins
	 * -requests						Set the requests channel to listen to, default is requests
	 * -logdir							Set the logdir, default is  ./logs
	 * -purge							Delete the contents of logdir before starting
	 * @throws Exception on REDIS errors.
	 */
	public static void main(String[] args) throws Exception {
		int i = 0;
		String redis = "localhost:6379";
		boolean purge = false;
		if (args.length > 0) {
			while (i < args.length) {
				switch (args[i]) {
				case "-redis":
					redis = args[i + 1];
					i += 2;
					break;
				case "-clicks":
					CLICKCHANNEL = args[i+1];
					i += 2;
					break;
				case "-wins":
					WINCHANNEL = args[i+1];
					i += 2;
					break;
				case "requests":
					REQUESTCHANNEL = args[i+1];
					i += 2;
					break;
				case "nobids":
					NOBIDCHANNEL = args[i+1];
					i += 2;
					break;
				case "bids":
					BIDCHANNEL = args[i+1];
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
					INTERVAL = Integer.parseInt(args[i+1]);
					i+= 2;
					break;
				default:
					System.out.println("Huh?");
					System.exit(1);
				}
			}
		}
		
		if (purge) {
			File dir = new File(logDir);
			for(File file: dir.listFiles()) file.delete();
		}
		
		Spark sp = new Spark(redis, init);
	}

	/**
	 * Create a default spark logger.
	 */
	public Spark() {
		this("localhost:6379", false);
		me = new Thread(this);

		redisson = Redisson.create(cfg);

		map = redisson.getMap("users-database");

		me.start();
	}

	/**
	 * Periodic processor. This writes the summary account/creative records. Which are summaries from the last period
	 * (which is 1 minute).
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

				
				System.out.println("-------------------- STATS ------------------------");
				System.out.println("REQUESTS = " + requests.get());
				System.out.println("BIDS = " + bids.get());
				System.out.println("NO-BIDS = " + nobids.get());
				System.out.println("WINS = " + wins.get());
				System.out.println("PIXELS = " + pixels.get());
				System.out.println("CLICKS = " + clicks.get());
				System.out.println("BID COST = " + bidCostX.doubleValue());
				System.out.println("WIN COST = " + winCostX.doubleValue());
				System.out.println("----------------------------------------------------");
				

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Create a spark logger using the specified redis info.
	 * @param redis String. The redishost:port info.
	 * @param init boolean. Set to true to zero the atomic counts.
	 */
	public Spark(String redis, boolean init) {
		String pass = Configuration.setPassword();
		if (pass != null) {
			cfg.useSingleServer().setAddress(redis).setPassword(pass)
					.setConnectionPoolSize(128);
		} else {
			cfg.useSingleServer().setAddress(redis).setConnectionPoolSize(128);
		}

		redisson = Redisson.create(cfg);

		map = redisson.getMap("users-database");

		me = new Thread(this);
		me.start();
		if (init)
			initialize();
	}

	/**
	 * Get the message handlers lashed up to handle all the accounting information.
	 */
	public void initialize() {

		logger = new FileLogger(INTERVAL);                  // Instantiate your own logger if you don't want to log to files.

		Set set = map.keySet();
		Iterator<String> it = set.iterator();
		while (it.hasNext()) {
			String key = it.next();
			User u = map.get(key);
			System.out.println("========>" + u.name);
			String acctName = u.name;
			for (Campaign c : u.campaigns) {
				String campName = c.adId;
				for (Creative creat : c.creatives) {
					AcctCreative cr = new AcctCreative(acctName, campName,
							creat.impid);
					creatives.add(cr);
					accountHash.put(campName + ":" + creat.impid, cr);
					System.out.println("Loaded: " + campName + ":" + creat.impid);
				}
			}
		}

		RTopic<BidRequest> requests = (RTopic) redisson.getTopic(REQUESTCHANNEL);
		requests.addListener(new MessageListener<BidRequest>() {
			@Override
			public void onMessage(String channel, BidRequest msg) {
				try {
					processRequests(msg);
				} catch (Exception error) {
					error.printStackTrace();
				}
			}
		});
		/**
		 * Win Notifications HERE
		 */
		RTopic<WinObject> winners = (RTopic) redisson.getTopic(WINCHANNEL);
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

		RTopic<BidResponse> bidresponse = (RTopic) redisson.getTopic(BIDCHANNEL);
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

		RTopic<NobidResponse> nobidresponse = (RTopic) redisson
				.getTopic(NOBIDCHANNEL);
		nobidresponse.addListener(new MessageListener<NobidResponse>() {
			@Override
			public void onMessage(String channel, NobidResponse msg) {
				try {
					processNobid(msg);
				} catch (Exception error) {
					error.printStackTrace();
				}
			}
		});

		RTopic<PixelClickConvertLog> pixelandclicks = (RTopic) redisson
				.getTopic(CLICKCHANNEL);
		pixelandclicks.addListener(new MessageListener<PixelClickConvertLog>() {
			@Override
			public void onMessage(String channel, PixelClickConvertLog msg) {
				try {
					processClickAndPixel(msg);
				} catch (Exception error) {
					error.printStackTrace();
				}
			}
		});

	}
	

	/**
	 * Process a win record.
	 * @param win WinObject. The object to be counted and logged.
	 * @throws Exception on atomic access errors.
	 */
	public void processWin(WinObject win) throws Exception {
		String campaign = win.adId;
		String impid = win.cridId;
		double cost = Double.parseDouble(win.price);

		AcctCreative creat = getRecord(campaign, impid);
		if (creat == null)
			return;
		
		wins.incrementAndGet();

		synchronized (creat) {
			creat.wins++;
			creat.winPrice += cost;
			
			winCost.addAndGet((int)(cost*1000));
		}

		String content = mapper.writer().writeValueAsString(win);
		logger.offer(new LogObject("win", content));
	}

	/**
	 * Process a bid request.
	 * @param br BidRequest. The request to be processed.
	 * @throws Exception
	 */
	public void processRequests(BidRequest br) throws Exception {
		requests.incrementAndGet();
		String content = mapper.writer().writeValueAsString(br);
		logger.offer(new LogObject("request", content));
	}

	/**
	 * Process a nobid notification.
	 * @param nb NobidResponse. The request not processed. Just counts it.
	 * @throws Exception on atomic access errors.
	 */
	public void processNobid(NobidResponse nb) throws Exception {
		nobids.incrementAndGet();
		String content = mapper.writer().writeValueAsString(nb);
		logger.offer(new LogObject("nobid", content));
	}

	/**
	 * Process clicks and pixels.
	 * @param ev PixelClickConvertLog. The object to be counted.
	 * @throws Exception on atomic access errors.
	 */
	public void processClickAndPixel(PixelClickConvertLog ev) throws Exception {
		Map m = new HashMap();
		m.put("time", ev.time);
		String type = null;

		String[] parts = ev.payload.split("/");
		AcctCreative creat = getRecord(parts[3], parts[4]);
		
		if (creat == null) {
			System.out.println("Gotcha!");
		}

		m.put("campaign", creat.campaignName);
		m.put("creative", creat.name);

		synchronized (creat) {
			if (ev.type == PixelClickConvertLog.CLICK) {
				type = "click";
				m.put("type", "click");
				creat.clicks++;
				clicks.incrementAndGet();
			} else if (ev.type == PixelClickConvertLog.PIXEL) {
				type = "pixel";
				m.put("type", "pixel");
				creat.pixels++;
				pixels.incrementAndGet();
			} else {

			}
		}
		String content = mapper.writer().writeValueAsString(m);
		logger.offer(new LogObject(type, content));
	}

	/**
	 * Process a bid from the bids channel.
	 * @param br BidRequest. The bid request object.
	 * @throws Exception on bad access to atomic variables.
	 */
	public void processBid(BidResponse br) throws Exception {
		String campaign = br.adid;
		String impid = br.impid;
		double cost = br.cost;

		AcctCreative creat = getRecord(campaign, impid);
		if (creat == null)
			return;

		bids.incrementAndGet();
		synchronized(creat) {
		creat.bids++;
		creat.bidPrice += cost;
		bidCost.addAndGet((int)(cost*1000));
		}

		String content = mapper.writer().writeValueAsString(br);
		logger.offer(new LogObject("bid", content));

	}

	/**
	 * Given a campaign name and impression id, retrieve the account creative record.
	 * @param campaign String. The name of the campaign.
	 * @param impid String. The impression id.
	 * @return AcctCreative. A summary accounting record this campaign/creative.
	 */
	public AcctCreative getRecord(String campaign, String impid) {
		AcctCreative cr = accountHash.get(campaign + ":" + impid);
		return cr;
	}
}