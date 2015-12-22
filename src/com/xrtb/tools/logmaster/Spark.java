package com.xrtb.tools.logmaster;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.RedissonClient;
import org.redisson.core.MessageListener;
import org.redisson.core.RAtomicLong;
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

	public static ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
				false);
	}

	public RAtomicLong pixels;
	public RAtomicLong clicks;
	public RAtomicLong bidrequests;
	public RAtomicLong bidcount;
	public RAtomicLong wincount;
	public RAtomicLong bidcost;
	public RAtomicLong wincost;
	public RAtomicLong nobidcount;

	static boolean init = false;
	static String logDir = "logs";

	FileLogger logger;

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
				case "-init":
					init = Boolean.parseBoolean(args[i + 1]);
					i += 2;
					break;
				case "-logir":
					logDir = args[i + 1];
					i += 2;
					break;
				case "-purge":
					i++;
					break;
				default:
					System.out.println("Huh?");
					System.exit(1);
				}
			}
		}
		
		purge = true;
		if (purge) {
			File dir = new File(logDir);
			for(File file: dir.listFiles()) file.delete();
		}
		
		Spark sp = new Spark(redis, init);
		System.out.println(sp.collect());
	}

	public Spark() {
		this("localhost:6379", false);
		me = new Thread(this);

		redisson = Redisson.create(cfg);

		map = redisson.getMap("users-database");

		pixels = redisson.getAtomicLong("pixels");
		clicks = redisson.getAtomicLong("clicks");
		bidrequests = redisson.getAtomicLong("bidrequests");
		bidcount = redisson.getAtomicLong("bidcount");
		wincount = redisson.getAtomicLong("wincount");
		bidcost = redisson.getAtomicLong("bidcost");
		wincost = redisson.getAtomicLong("wincost");
		nobidcount = redisson.getAtomicLong("nobidcount");

		me.start();
	}

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

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

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

		pixels = redisson.getAtomicLong("pixels");
		clicks = redisson.getAtomicLong("clicks");
		bidrequests = redisson.getAtomicLong("bidrequests");
		bidcount = redisson.getAtomicLong("bidcount");
		wincount = redisson.getAtomicLong("wincount");
		bidcost = redisson.getAtomicLong("bidcost");
		wincost = redisson.getAtomicLong("wincost");
		nobidcount = redisson.getAtomicLong("nobidcount");

		me = new Thread(this);
		me.start();
		if (init)
			initialize();
	}

	public void initialize() {

		logger = new FileLogger();

		pixels.set(0);
		;
		clicks.set(0);
		;
		bidrequests.set(0);
		bidcount.set(0);
		wincount.set(0);
		bidcost.set(0);
		wincost.set(0);
		nobidcount.set(0);

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
				}
			}
		}

		RTopic<BidRequest> requests = (RTopic) redisson.getTopic("requests");
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
		RTopic<WinObject> winners = (RTopic) redisson.getTopic("wins");
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

		System.out.println("Hello!");

		RTopic<BidResponse> bidresponse = (RTopic) redisson.getTopic("bids");
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
				.getTopic("nobids");
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
				.getTopic("clicks");
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

	public void processWin(WinObject win) throws Exception {
		String campaign = win.adId;
		String impid = win.cridId;
		double cost = Double.parseDouble(win.price);

		wincount.incrementAndGetAsync();

		AcctCreative creat = getRecord(campaign, impid);
		if (creat == null)
			return;

		synchronized (creat) {
			creat.wins++;
			creat.winPrice += cost;
		}

		wincost.addAndGet((long) (1000 * cost));
		String content = mapper.writer().writeValueAsString(win);
		logger.offer(new LogObject("win", content));
	}

	public void processRequests(BidRequest br) throws Exception {
		bidrequests.incrementAndGetAsync();
		String content = mapper.writer().writeValueAsString(br);
		logger.offer(new LogObject("request", content));
	}

	public void processNobid(NobidResponse nb) throws Exception {
		nobidcount.incrementAndGetAsync();

		String content = mapper.writer().writeValueAsString(nb);
		logger.offer(new LogObject("nobid", content));
	}

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
				clicks.incrementAndGetAsync();
			} else if (ev.type == PixelClickConvertLog.PIXEL) {
				type = "pixel";
				m.put("type", "pixel");
				creat.pixels++;
				pixels.incrementAndGetAsync();
			} else {

			}
		}
		String content = mapper.writer().writeValueAsString(m);
		logger.offer(new LogObject(type, content));
	}

	public void processBid(BidResponse br) throws Exception {
		String campaign = br.adid;
		String impid = br.impid;
		double cost = br.cost;

		bidcount.incrementAndGetAsync();

		AcctCreative creat = getRecord(campaign, impid);
		if (creat == null)
			return;

		synchronized(creat) {
		creat.bids++;
		creat.bidPrice += cost;
		}

		bidcost.addAndGetAsync((long) (1000 * cost));
		String content = mapper.writer().writeValueAsString(br);
		logger.offer(new LogObject("bid", content));

	}

	public AcctCreative getRecord(String campaign, String impid) {
		AcctCreative cr = accountHash.get(campaign + ":" + impid);
		return cr;
	}

	public String collect() throws Exception {
		HashMap data = new HashMap();

		data.put("pixels", pixels.get());
		data.put("clicks", clicks.get());
		data.put("bidrequests", bidrequests.get());
		data.put("bidcount", bidcount.get());
		data.put("wincount", wincount.get());
		data.put("bidcost", bidcost.get());
		data.put("wincost", wincost.get());
		data.put("nobidcount", nobidcount.get());

		String content = mapper.writer().withDefaultPrettyPrinter()
				.writeValueAsString(data);
		return content;
	}
}

class LogObject {
	public String name;
	public String content;

	public LogObject(String name, String content) {
		this.name = name;
		this.content = content;
	}
}

class FileLogger implements Runnable {
	public static final int LOG_INTERVAL = 60000;

	ConcurrentLinkedQueue<LogObject> queue = new ConcurrentLinkedQueue();
	Thread me;

	Map mapper = new HashMap();

	Set<List> setOfLists = new HashSet();

	public FileLogger() {

		me = new Thread(this);
		me.start();
	}

	public void run() {
		long time = System.currentTimeMillis() + LOG_INTERVAL;
		while (true) {
			if (System.currentTimeMillis() > time) {
				System.out
						.println("---------- HAMMER TIME -------------------");

				time = System.currentTimeMillis() + LOG_INTERVAL;
				Set<Entry> entries = mapper.entrySet();
				for (Entry e : entries) {
					String name = (String) e.getKey();
					List<String> values = (List) e.getValue();
					System.out.println("-->" + name);

					StringBuilder sb = new StringBuilder();

					for (String contents : values) {
						sb.append(contents);
						sb.append("\n");
					}
					if (sb.length() > 0)
						try {
							AppendToFile.item(Spark.logDir + "/" + name, sb);
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					//System.out.println("\t" + sb.toString());
					values.clear();
				}
			}
			if (queue.isEmpty() == false) {
				LogObject o = queue.poll();
				List list = (List) mapper.get(o.name);
				if (list == null) {
					list = new ArrayList();
					mapper.put(o.name, list);
				}
				list.add(o.content);
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void offer(LogObject offering) {
		queue.offer(offering);
	}
}
