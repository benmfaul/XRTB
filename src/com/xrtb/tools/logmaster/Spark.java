package com.xrtb.tools.logmaster;



import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
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
	ConcurrentMap<String,User> map;
	/** The redisson proxy object behind the map */
	RedissonClient redisson;
	/** The redisson configuration object */
	Config cfg = new Config();
	
	Set<Account> accounts = new HashSet();
	
	Thread me;
	
	public static ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	
	public RAtomicLong bidrequests;
	public RAtomicLong bidcount;
	public RAtomicLong wincount;
	public RAtomicLong bidcost;
	public RAtomicLong wincost;
	public RAtomicLong nobidcount;      // ??????????
	
	public static void main(String [] args) throws Exception {
		int i = 0;
		String redis = "localhost:6379";
		boolean init = false;
		if (args.length > 0) {
			while( i <args.length) {
				switch(args[i]) {
				case "-redis":
					redis = args[i+1];
					i+= 2;
					break;
				case "-init":
					init = Boolean.parseBoolean(args[i+1]);
					i+= 2;
					break;
				default:
					System.out.println("Huh?");
					System.exit(1);
				}
			}
		}
		Spark sp = new Spark(redis,init);
		System.out.println(sp.collect());
	}
	
	public Spark() {
		this("localhost:6379", false);
		me = new Thread(this);
		
		redisson = Redisson.create(cfg);

		map = redisson.getMap("users-database");
		
		bidrequests = redisson.getAtomicLong("bidrequests");
		bidcount = redisson.getAtomicLong("bidcount");
		wincount = redisson.getAtomicLong("wincount");
		bidcost = redisson.getAtomicLong("bidcost");
		wincost = redisson.getAtomicLong("wincost");
		nobidcount = redisson.getAtomicLong("nobidcount"); 
		
		me.start();
		System.out.println("Ready");
	}
	
	public void run() {
		while(true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public Spark(String redis, boolean init) {
		String pass = Configuration.setPassword();
		if (pass != null) {
		cfg.useSingleServer()
    	.setAddress(redis)
    	.setPassword(pass)
    	.setConnectionPoolSize(128);
		} else {
			cfg.useSingleServer()
	    	.setAddress(redis)
	    	.setConnectionPoolSize(128);
		}
		
		redisson = Redisson.create(cfg);

		map = redisson.getMap("users-database");
		
		bidrequests = redisson.getAtomicLong("bidrequests");
		bidcount = redisson.getAtomicLong("bidcount");
		wincount = redisson.getAtomicLong("wincount");
		bidcost = redisson.getAtomicLong("bidcost");
		wincost = redisson.getAtomicLong("wincost");
		nobidcount = redisson.getAtomicLong("nobidcount"); 
		
		if (init)
			initialize();
	}
	
	public void initialize() {
		
		bidrequests.set(0);;
		bidcount.set(0);
		wincount.set(0);
		bidcost.set(0);
		wincost.set(0);
		nobidcount.set(0);
		
		Set set = map.keySet();
		Iterator<String> it = set.iterator();
		while(it.hasNext()) {
			String key = it.next();
			User u = map.get(key);
			System.out.println("========>" + u.name);
			Account account = new Account(u.name);
			for (Campaign c : u.campaigns) {
					AcctCampaign  camp = new AcctCampaign(c.adId);
					account.campaigns.add(camp);
					for (Creative creat : c.creatives) {
						AcctCreative cr = new AcctCreative(creat.impid);
						camp.creatives.add(cr);
					}
			}
			accounts.add(account);
		}

		RTopic<BidRequest>requests = (RTopic) redisson.getTopic("requests");
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
		RTopic<WinObject>winners = (RTopic) redisson.getTopic("wins");
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
		
		RTopic<BidResponse>bidresponse = (RTopic) redisson.getTopic("bids");
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
		
		RTopic<NobidResponse>nobidresponse = (RTopic) redisson.getTopic("nobids");
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
		
	}
	
	public void processWin(WinObject win) {
		String campaign = win.adId;
		String impid = win.cridId;
		double cost = Double.parseDouble(win.price);
		
		wincount.incrementAndGetAsync();
		
		Object [] objs = getRecord(campaign,impid);
		if (objs == null)
			return;
		
		Account a = (Account)objs[0];
		AcctCampaign camp = (AcctCampaign)objs[1];
		AcctCreative creat = (AcctCreative)objs[2];

		a.wins++;
		camp.wins++;
		creat.wins++;
		a.bidPrice += cost;
		camp.bidPrice += cost;
		creat.winPrice += cost;
		
		wincost.addAndGet((long)(1000 * cost));
	}
	
	public void processRequests(BidRequest br) {
		bidrequests.incrementAndGetAsync();
	}
	
	public void processNobid(NobidResponse nb) {
		nobidcount.incrementAndGetAsync();
	}
	
	public void processBid(BidResponse br) {
		String campaign = br.adid;
		String impid = br.impid;
		double cost = br.cost;
		
		bidcount.incrementAndGetAsync();
		
		Object [] objs = getRecord(campaign,impid);
		if (objs == null)
			return;
		
		Account a = (Account)objs[0];
		AcctCampaign camp = (AcctCampaign)objs[1];
		AcctCreative creat = (AcctCreative)objs[2];

		a.bids++;
		camp.bids++;
		creat.bids++;
		a.bidPrice += cost;
		camp.bidPrice += cost;
		creat.bidPrice += cost;
		
		bidcost.addAndGetAsync((long)(1000 * cost));
		
	}
	
	public Object [] getRecord(String campaign, String impid) {
		Object [] objs = new Object[3];
		for (Account a : accounts) {
			for (AcctCampaign camp : a.campaigns) {
				if (camp.name.equals(campaign)) {
					for (AcctCreative creat : camp.creatives) {
						if (creat.name.equals(impid)) {
							objs[0] = a;
							objs[1] = camp;
							objs[2] = creat;
							return objs;
						}
					}
				}
			}
		}
		return null;
	}
	
	public String collect() throws Exception {
		HashMap data = new HashMap();
		Set<Account> acct = (Set)accounts;
		
		data.put("bidrequests", bidrequests.get());
		data.put("bidcount",bidcount.get());
		data.put("wincount",wincount.get());
		data.put("bidcost",bidcost.get());
		data.put("wincost",wincost.get());
		data.put("nobidcount",nobidcount.get());
		
		String content = mapper
				.writer()
				.withDefaultPrettyPrinter()
				.writeValueAsString(data);
		return content;
	}
}
