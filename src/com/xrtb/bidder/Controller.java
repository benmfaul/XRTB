package com.xrtb.bidder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.redisson.core.MessageListener;
import org.redisson.core.RTopic;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import com.xrtb.commands.BasicCommand;
import com.xrtb.commands.DeleteCampaign;
import com.xrtb.commands.Echo;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.db.User;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;

/**
 * A class for handling REDIS based commands to the RTB server. The Controller open REDIS channels to the
 * requested channels to handle commands, and logging channels for log messages, win  notifications,
 * bid requests and bids. The idea is to transmit all this information through REDIS so that you can\
 * build your own database, accounting, and analystic processes outside of the bidding engine.
 * 
 * Another job of the Controller is to create the REDIS cache. There could be multiple bidders running in the
 * infrastructure, but handling a win notification requires that you have information about the original bid. This
 * means the system receiving the notification may not be the same system that made the bid. The bid is stored in the
 * cache as a map so the win handling system can handle the win, even though it did not actually make the bid.
 * 
 * @author Ben M. Faul
 *
 */
public class Controller {
	
	/** Add campaign REDIS command id */
	public static final int ADD_CAMPAIGN = 0;
	/** Delete campaign REDIS command id */
	public static final int DEL_CAMPAIGN = 1;
	/** Stop the bidder REDIS command id */
	public static final int STOP_BIDDER = 2;
	/** Start the bidder REDIS command id */
	public static final int START_BIDDER = 3;
	/** The percentage REDIS command id */
	public static final int PERCENTAGE = 4;
	/** The echo status REDIS command id */
	public static final int ECHO = 5;
	
	/** The REDIS channel for sending commands to the bidders */
	public static final String COMMANDS = "commands";
	/** The REDIS channel the bidder sends command responses out on */
	public static final String RESPONSES = "responses";
	
	/** The JEDIS object for the bidder uses to subscribe to commands */
	RTopic<BasicCommand> commands;
	/** The JEDOS object for publishing command responses on */
	Jedis publish;
	/** The JEDIS object for creating bid hash objects */
	Jedis bidCache;
	/** The JEDIS object for clicks processing */
	Jedis clicksCache;
	
	/** The loop object used for reading commands */
	CommandLoop loop;

	/** The queue for posting responses on */
	Publisher responseQueue;
	/** The queue for reading commands from */
	Publisher publishQueue;
	/** Queue used to send wins */
	Publisher winsQueue;
	/** Queue used to send bids */
	Publisher bidQueue;
	/** Queue used to send bid requests */
	Publisher requestQueue;
	/** Queue for sending log messages */
	LogPublisher loggerQueue;
	/** Queue for sending clicks */
	ClicksPublisher clicksQueue;
	
	/** The campaigns known by the bidder */
	Set<Campaign> campaigns = new TreeSet<Campaign>();
	/* The configuration object used bu the controller */
	protected Configuration config = Configuration.getInstance();
	
	/** The singleton instance of the controller */
	static Controller theInstance;

	/**
	 * Private construcotr with specified hosts
	 * @throws Exception on REDIS errors.
	 */
	private Controller() throws Exception {
		/** the cache of bid adms */
		bidCache = new Jedis(Configuration.cacheHost);
		bidCache.connect();
		
		/** transmit command responses */
		publish = new Jedis(Configuration.cacheHost);
		publish.connect();

		/** Reads commands */
		commands = config.redisson.getTopic(COMMANDS);
		commands.addListener(new CommandLoop());
		
		responseQueue = new Publisher(publish,RESPONSES);
		
		if (config.REQUEST_CHANNEL != null)
			requestQueue = new Publisher(publish,config.REQUEST_CHANNEL);
		if (config.WINS_CHANNEL != null)
			winsQueue = new Publisher(publish,config.WINS_CHANNEL);
		if (config.BIDS_CHANNEL != null)
			bidQueue = new Publisher(publish,config.BIDS_CHANNEL);
		if (config.LOG_CHANNEL != null)
			loggerQueue = new LogPublisher(publish,config.LOG_CHANNEL);
	    if (config.CLICKS_CHANNEL != null) {
	    	clicksCache = new Jedis(config.cacheHost);
	    	clicksQueue = new ClicksPublisher(clicksCache,config.CLICKS_CHANNEL);
	    }
	}

	/**
	 * Get the controller using localhost for REDIS connections.
	 * @return Controller. The singleton object of the controller.
	 * @throws Exception on errors instantiating the controller (like REDIS errors).
	 */
	public static Controller getInstance() throws Exception{
		if (theInstance == null) {
			synchronized (Controller.class) {
				if (theInstance == null) {
					theInstance = new Controller();
				}
			}
		}
		return theInstance;
	}
	
	/**
	 * Add a campaign over REDIS.
	 * @param source Map. THe map of the campaign.
	 * @throws Exception if there is a configuration error.
	 */
	public void addCampaign(Map<String,Object> source) throws Exception {
		Campaign c = new Campaign();
		c.adId = (String)source.get("id");
		c.price = (Double)source.get("price");
		c.adomain = (String)source.get("adomain");
		c.template = (Map)source.get("template");
		c.attributes = (List)source.get("attributes");
		c.creatives = (List)source.get("creatives");
		addCampaign(c);
	}
	
	/**
	 * Simplest form of the add campaign
	 * @param c Campaign. The campaign to add.
	 * @throws Exception on redis errors.
	 */
	public void addCampaign(Campaign c) throws Exception {
		Configuration.getInstance().deleteCampaign(c.adId);
		Configuration.getInstance().addCampaign(c);
		responseQueue.add("Response goes here");
	}

	/**
	 * Delete a campaign using REDIS.
	 * @param cmd Map. The Map of this command.
	 * @throws Exception if there is a JSON parse error.
	 */
	public void deleteCampaign(BasicCommand cmd) throws Exception {
		String id =  cmd.target;
		boolean b = Configuration.getInstance().deleteCampaign(id);
		DeleteCampaign m = new DeleteCampaign(id);
		if (!b)
			m.status = "error, no such campaign " + id;
		m.to = cmd.to;
		m.id = cmd.id;
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = mapper.writeValueAsString(m);
		responseQueue.add(jsonString);
	}
	
	/**
	 * Simplest form of the delete campaign
	 * @param adId. String. The adid to delete
	 * @throws Exception ton REDIS errors.
	 */
	public void deleteCampaign(String adId) throws Exception 
	{
		Configuration.getInstance().deleteCampaign(adId);	
	}

	/**
	 * Stop the bidder.
	 * @param cmd BasicCommand. The command as a map.
	 * @throws Exception if there is a JSON parsing error.
	 */
	public void stopBidder(BasicCommand cmd) throws Exception{
		RTBServer.stopped = true;
		cmd.msg = "stopped";
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = mapper.writeValueAsString(cmd);
		responseQueue.add(jsonString);
	}

	/**
	 * Start the bidder.
	 * @param cmd BasicCmd. The command.
	 * @throws Exception if there is a JSON parsing error.
	 */
	public void startBidder(BasicCommand cmd) throws Exception  {
		RTBServer.stopped = false;
		cmd.msg = "running";
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = mapper.writeValueAsString(cmd);
		responseQueue.add(jsonString);
	}

	/**
	 * Set the throttle percentage.
	 * @param node. JsoNode - JSON  of the command.
	 * TODO: this needs implementation.
	 */
	public void setPercentage(JsonNode node) {
		responseQueue.add("Response goes here");
	}
	
	/**
	 * THe echo command and its response.
	 * @param source BasicCommand. The command used
	 * @throws Exception if there is a JSON parsing error.
	 */
	public void echo(BasicCommand cmd) throws Exception  {
		Echo m = RTBServer.getStatus();
		m.to = cmd.to;
		m.id = cmd.id;
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = mapper.writeValueAsString(m);
		responseQueue.add(jsonString);
	}
	
	/*
	 * The not handled response to the command entity. Used when an
	 * unrecognized command is sent.
	 * @param cmd. BasicCommand - the error message.
	 * @throws Exception if there is a JSON parsing error.
	 */
	public void notHandled(BasicCommand cmd) throws Exception  {
		Echo m = RTBServer.getStatus();
		m.msg = "error, unhandled event";
		m.status = "error";
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = mapper.writeValueAsString(m);
		responseQueue.add(jsonString);
	}
	
	/**
	 * Sends an RTB request out on the appropriate REDIS queue
	 * @param s String. The JSON of the message
	 */
	public void sendRequest(String s) {
		if (requestQueue != null)
			requestQueue.add(s);
	}
	
	public void sendRequest(BidRequest br) {
		if (requestQueue != null)
			requestQueue.add(br.toString());
	}
	
	
	/**
	 * Sends an RTB bid out on the appropriate REDIS queue
	 * @param s String. The JSON of the message
	 */
	public void sendBid(String s) {
		if (bidQueue != null)
			bidQueue.add(s);
	}
	
	
	/**
	 * Sends an RTB win out on the appropriate REDIS queue
	 * @param s String. The JSON of the message
	 */
	public void sendWin(String s) {
		if (winsQueue != null)
			winsQueue.add(s);
	}
	
	/**
	 * Sends a log message on the appropriate REDIS queue
	 * @param logLevel int. The log level of this message.
	 * @param msg String. The JSON of the message
	 */
	public void sendLog(int logLevel, String msg) {
		if (loggerQueue != null && logLevel <= config.logLevel) {
			loggerQueue.add(msg);
		}
	}
	
	/**
	 * Send click info.
	 * @param target String. The URI of this click data
	 */
	public void publishClick(String target) {
		if (clicksQueue != null) {
			clicksQueue.add(target);
		}
	}
	
	/**
	 * Record a bid in REDIS
	 * @param br BidRequest. The bid request that we made earlier.
	 */
	public void recordBid(BidResponse br) {
		Map m = new HashMap();
		m.put("ADM",br.admAsString);
		m.put("PRICE",""+br.price);
		bidCache.hmset(br.oidStr,m);
		bidCache.expire(br.oidStr, Configuration.getInstance().ttl);
	}
	
	/**
	 * Remove a bid object from the cache.
	 * @param hash String. The bid object id.
	 */
	public void deleteBidFromCache(String hash) {
		bidCache.del(hash);
	}
	
	/**
	 * Retrieve previously recorded bid data
	 * @param oid String. The object id of the bid.
	 * @return Map. A map of the returned data, will be null if not found.
	 */
	public Map getBidData(String oid) {
		Map m = bidCache.hgetAll(oid);
		return m;
	}
	
}

/**
 * A class to retrieve RTBServer commands from REDIS.
 * 
 * 
 * @author Ben M. Faul
 *
 */
class CommandLoop implements MessageListener<BasicCommand> {
	/** The thread this command loop uses to process REDIS subscription messages */
	/** The configuration object */
	Configuration config = Configuration.getInstance();
	
	/**
	 * On a message from REDIS, handle the command.
	 * @param arg0. String - the channel of this message.
	 * @param arg1. String - the JSON encoded message.
	 */
	@Override
	public void onMessage(BasicCommand item) {
		ConcurrentMap<String,User>  map = config.redisson.getMap("users-database");
		try {
			switch(item.cmd) {
			case Controller.ADD_CAMPAIGN:
				Campaign c = WebCampaign.getInstance().db.getCampaign(item.target);
				Controller.getInstance().addCampaign(c);
				break;
			case Controller.DEL_CAMPAIGN:
				Controller.getInstance().deleteCampaign(item);
				break;
			case Controller.STOP_BIDDER:
				Controller.getInstance().stopBidder(item);
				break;
			case Controller.START_BIDDER:
				Controller.getInstance().startBidder(item);
				break;
			case Controller.ECHO:
				Controller.getInstance().echo(item);
				break;
			default:
				Controller.getInstance().notHandled(item);
			}
				
		} catch (Exception error) {
			try {
				Controller.getInstance().responseQueue.add(error.toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			error.printStackTrace();
		}
	}
}

/**
 * A publisher for REDIS based messages, sharable by multiple threads.
 * @author Ben M. Faul
 *
 */
class Publisher implements Runnable {
	/** The objects thread */
	Thread me;
	/** The JEDIS connection used */
	Jedis conn;
	/** The channel to publish on */
	String channel;
	/** The queue of messages */
	ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<String>();

	/**
	 * Constructor for base class.
	 * @param conn Jedis. The REDIS connection.
	 * @param channel String. The topic name to publish on.
	 * @throws Exception. Throws exceptions on REDIS errors
	 */
	public Publisher(Jedis conn, String channel)  throws Exception {
		this.conn = conn;
		this.channel = channel;
		me = new Thread(this);
		me.start();
	}

	/**
	 * Run the message pump.
	 */
	public void run() {
		String str = null;
		while(true) {
			try {
				if ((str = queue.poll()) != null) {
					conn.publish(channel, str);
				}
				Thread.sleep(1);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
	}

	/**
	 * Add a message to the messages queue.
	 * @param s. String. JSON formatted message.
	 */
	public void add(String s) {
		queue.add(s);
	}
}

/**
 * A type of Publisher, but used specifically for logging, contains the instance name
 * and the current time in EPOCH.
 * 
 * @author Ben M. Faul
 *
 */
class LogPublisher extends Publisher {
	/** The configuration of the bidder */
	Configuration config = Configuration.getInstance();
	
	/**
	 * Constructor for logging class.
	 * @param conn Jedis. The REDIS connection.
	 * @param channel String. The topic name to publish on.
	 */
	public LogPublisher(Jedis conn, String channel) throws Exception  {
		super(conn, channel);
	}
	
	@Override
	public void run() {
		String str = null;
		Configuration.getInstance();
		String name = config.instanceName;
		while(true) {
			try {
				if ((str = queue.poll()) != null) {
					long time = System.currentTimeMillis();
					String log = "{\"instance\":\"" + name + "\",\"time\":"+time+",\"payload\":\""+str+"\"}";
					conn.publish(channel, log);
				}
				Thread.sleep(1);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
	}
	
}

/**
 * A type of Publisher, but used specifically for logging, contains the instance name
 * and the current time in EPOCH.
 * 
 * @author Ben M. Faul
 *
 */
class ClicksPublisher extends Publisher {

	/**
	 * Constructor for clicls publisher class.
	 * @param conn Jedis. The REDIS connection.
	 * @param channel String. The topic name to publish on.
	 */
	public ClicksPublisher(Jedis conn, String channel) throws Exception {
		super(conn, channel);
	}
	
	@Override
	public void run() {
		String str = null;
		String name = Configuration.getInstance().instanceName;
		while(true) {
			try {
				if ((str = queue.poll()) != null) {
					long time = System.currentTimeMillis();
					String log = "{\"instance\":\"" + name + "\",\"time\":"+time+",\"payload\":\""+str+"\"}";
					conn.publish(channel, log);
				}
				Thread.sleep(1);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
	}
	
}
