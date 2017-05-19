package com.xrtb.tools.logmaster;

import java.io.File;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import java.util.concurrent.atomic.AtomicLong;

import com.aerospike.client.AerospikeClient;
import com.aerospike.redisson.RedissonClient;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.commands.ClickLog;
import com.xrtb.commands.PixelClickConvertLog;
import com.xrtb.commands.PixelLog;
import com.xrtb.common.Campaign;

import com.xrtb.common.Creative;
import com.xrtb.db.DataBaseObject;
import com.xrtb.db.User;
import com.xrtb.fraud.FraudLog;
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
	List<AcctCreative> creatives = new ArrayList();
	Map<String, AcctCreative> accountHash = new HashMap();

	Thread me;
	String zeromq = "localhost";

	static int INTERVAL = 60000;
	static String BIDCHANNEL = "5571&bids";
	static String WINCHANNEL = "5572&wins";
	static String CLICKCHANNEL = "5573&clicks";
	
	static String BIDFILE = null;
	static String WINFILE = "wins";
	static String CLICKFILE = null;
	static String PIXELFILE = null;

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
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

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
		boolean purge = false;
		String zeromq = "localhost";
		if (args.length > 0) {
			while (i < args.length) {
				switch (args[i]) {
				case "-h":
					System.out.println("-aero  host:port   [Set the Aerospike host and port, default localhost:6379  ]");
					System.out.println("-logdir dirname    [Where to place the logs, default is ./logs               ]");
					System.out
							.println("-purge               [Delete the log records already produced, default no purge]");
					System.out
							.println("-interval            [Set the accounting interval, default is 60000 (60 seconds)]");
					i++;
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
					zeromq = args[i + 1];
					i += 2;
					break;
				case "-bidfile":
					BIDFILE = args[i+1];
					i+=2;
					break;
				case "-winfile":
					WINFILE = args[i+1];
					i+=2;
					break;
				case "-clickfile":
					CLICKFILE = args[i+1];
					i+=2;
					break;
				case "-pixelfile":
					BIDFILE = args[i+1];
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

		Spark sp = new Spark(zeromq);
	}

	/**
	 * Create a default spark logger.
	 */
	public Spark() throws Exception {
		this("localhost");
		me = new Thread(this);

		me.start();
	}

	/**
	 * Periodic processor. This writes the summary account/creative records.
	 * Which are summaries from the last period (which is 1 minute).
	 */
	public void run() {
		while (true) {
			try {
				Thread.sleep(60000);
				System.out.println("CREATIVES = " + creatives.size());
					String content = null;
					for (int i = 0; i < creatives.size(); i++) {
						AcctCreative c = creatives.get(i);
						if (!c.isZero()) {
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
				winCostX = winCostX.divide(oneThousand);
				bidCostX = bidCostX.divide(oneThousand);

				System.out.println("-------------------- STATS ------------------------");
				System.out.println("REQUESTS = " + requests.get());
				System.out.println("FRAUD = " + fraud.get());
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
	 * 
	 * @param redis
	 *            String. The redishost:port info.
	 * @param init
	 *            boolean. Set to true to zero the atomic counts.
	 */
	public Spark(String zeromq) throws Exception {
		this.zeromq = zeromq;

		me = new Thread(this);
		me.start();
		initialize();
	}

	/**
	 * Get the message handlers lashed up to handle all the accounting
	 * information.
	 */
	public void initialize() throws Exception {

		logger = new FileLogger(INTERVAL); // Instantiate your own logger if you
											// don't want to log to files.
		/**
		 * Win Notifications HERE
		 */
		String address = getAddress(zeromq, WINCHANNEL);
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

		address = getAddress(zeromq, BIDCHANNEL);
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

		address = getAddress(zeromq, CLICKCHANNEL);
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
        return "tcp://" + host + ":" + channel;
	}

	/**
	 * Process a win record.
	 * 
	 * @param win
	 *            WinObject. The object to be counted and logged.
	 * @throws Exception
	 *             on atomic access errors.
	 */
	
	static BigDecimal oneThousand = new BigDecimal(1000);
	public void processWin(WinObject win) throws Exception {
		String campaign = win.adId;
		String impid = win.cridId;
		BigDecimal cost = new BigDecimal(win.price);
		//double cost = Double.parseDouble(win.price);

		Double x = null;
		Integer y = null;
		AcctCreative creat = null;
		synchronized (Spark.class) {
			creat = getRecord(campaign, impid);
			if (creat == null) {
				creat = new AcctCreative("unknown", win.adId, win.cridId);
				creatives.add(creat);
				accountHash.put(win.adId + ":" + win.cridId, creat);	
			}
		}
		
		wins.incrementAndGet();

		synchronized (creat) {
			
			x = creat.slices.cost.get(win.pubId);
			if (x == null)
				x = new Double(0);
			y = creat.slices.wins.get(win.pubId);
			if (y == null)
				y = new Integer(0);

			
			creat.wins.incrementAndGet();
			creat.winPrice = creat.winPrice.add(cost);
			
			x += cost.doubleValue();
			y++;
			creat.slices.cost.put(win.pubId, x);
			creat.slices.wins.put(win.pubId, y);

			cost = cost.multiply(oneThousand);
			winCost.addAndGet((int)cost.longValue());
		}

		if (WINFILE != null) {
			String content = mapper.writer().writeValueAsString(win);
			logger.offer(new LogObject(WINFILE, content));
		}
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

	public void processForensiq(FraudLog log) throws Exception {
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
		PixelClickConvertLog ev;
		if (x instanceof PixelLog) {
			ev = (PixelClickConvertLog)x;
			ev = new PixelLog(ev.payload, ev.instance);
		
		} else {
			ev = (PixelClickConvertLog) x;
			ev = new ClickLog(ev.payload,ev.instance);
		}

		Map m = new HashMap();
		m.put("time", ev.time);
		String type = null;


		AcctCreative creat = getRecord(ev.ad_id, ev.creative_id);

		if (creat == null) {
			creat = new AcctCreative("unknown", ev.ad_id, ev.creative_id);
			creatives.add(creat);
			accountHash.put(ev.ad_id + ":" + ev.creative_id, creat);
		}

		m.put("campaign", creat.campaignName);
		m.put("creative", creat.name);

		synchronized (creat) {
			if (ev.type == PixelClickConvertLog.CLICK) {
				creat.clicks.incrementAndGet();
				clicks.incrementAndGet();
				Integer y = creat.slices.clicks.get(ev.exchange);
				if (y == null)
					y = new Integer(0);
				creat.slices.clicks.put(ev.exchange, y);
			} else if (ev.type == PixelClickConvertLog.PIXEL) {;
				creat.pixels.incrementAndGet();
				pixels.incrementAndGet();
				Integer y = creat.slices.pixels.get(ev.exchange);
				if (y == null)
					y = new Integer(0);
				creat.slices.pixels.put(ev.exchange, y);
			} else {

			}
		}
		
		if (ev.type == PixelClickConvertLog.CLICK && CLICKFILE != null ) {
			String content = mapper.writer().writeValueAsString(ev);
			logger.offer(new LogObject(CLICKFILE, content));
		}
		
		if (ev.type == PixelClickConvertLog.PIXEL && PIXELFILE != null ) {
			String content = mapper.writer().writeValueAsString(ev);
			logger.offer(new LogObject(PIXELFILE, content));
		}
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
		Integer x = null;
		if (creat == null) {
			creat = new AcctCreative("unknown", br.adid, br.crid);
			creatives.add(creat);
			accountHash.put(br.adid + ":" + br.crid, creat);
		}
		
		x = creat.slices.bids.get(br.exchange);
		if (x == null)
			x = new Integer(0);

		bids.incrementAndGet();
		synchronized (creat) {
			creat.bids.incrementAndGet();
			creat.bidPrice = creat.bidPrice.add(new BigDecimal(cost));
			x++;
			creat.slices.bids.put(br.exchange, x);
			bidCost.addAndGet((int) (cost * 1000));
		}

		if (BIDFILE != null) {
			String content = mapper.writer().writeValueAsString(br);
			logger.offer(new LogObject(BIDFILE, content));
		}

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
		StringBuilder sb = new StringBuilder(campaign);
		sb.append(":");
		sb.append(impid);
		return accountHash.get(sb.toString());
	}
}