package com.xrtb.bidder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import com.xrtb.commands.DeleteCampaign;
import com.xrtb.commands.Echo;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
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
	Jedis commands;
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
	/** The current log level of the bidding engine */
	int logLevel = 0;
	
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
		commands = new Jedis(Configuration.cacheHost);
		commands.connect();
		loop = new CommandLoop(commands);
		
		responseQueue = new Publisher(publish,RESPONSES);
		
		if (Configuration.REQUEST_CHANNEL != null)
			requestQueue = new Publisher(publish,Configuration.REQUEST_CHANNEL);
		if (Configuration.WINS_CHANNEL != null)
			winsQueue = new Publisher(publish,Configuration.WINS_CHANNEL);
		if (Configuration.BIDS_CHANNEL != null)
			bidQueue = new Publisher(publish,Configuration.BIDS_CHANNEL);
		if (Configuration.LOG_CHANNEL != null)
			loggerQueue = new LogPublisher(publish,Configuration.LOG_CHANNEL);
	    if (Configuration.CLICKS_CHANNEL != null) {
	    	clicksCache = new Jedis(Configuration.cacheHost);
	    	clicksQueue = new ClicksPublisher(clicksCache,Configuration.CLICKS_CHANNEL);
	    }
		logLevel = Configuration.logLevel;
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
		Configuration.getInstance().deleteCampaign(c.adId);
		Configuration.getInstance().addCampaign(c);
		responseQueue.add("Response goes here");
	}

	/**
	 * Delete a campaign using REDIS.
	 * @param cmd Map. The Map of this command.
	 * @throws Exception if there is a JSON parse error.
	 */
	public void deleteCampaign(Map<String,Object> cmd) throws Exception {
		String id = (String)cmd.get("campaign");
		boolean b = Configuration.getInstance().deleteCampaign(id);
		DeleteCampaign m = new DeleteCampaign(id);
		if (!b)
			m.status = "error, no such campaign " + id;
		m.to = (String)cmd.get("to");
		m.id = (String)cmd.get("id");
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = mapper.writeValueAsString(m);
		responseQueue.add(jsonString);
	}

	/**
	 * Stop the bidder.
	 * @param cmd Map. The command as a map.
	 * @throws Exception if there is a JSON parsing error.
	 */
	public void stopBidder(Map<String,Object> cmd) throws Exception{
		RTBServer.stopped = true;
		cmd.put("stopped",true);
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = mapper.writeValueAsString(cmd);
		responseQueue.add(jsonString);
	}

	/**
	 * Start the bidder.
	 * @param cmd Map. The command as a map.
	 * @throws Exception if there is a JSON parsing error.
	 */
	public void startBidder(Map<String,Object> cmd) throws Exception  {
		RTBServer.stopped = false;
		cmd.put("stopped",false);
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
	 * @param source. Map. The echo command as a map.
	 * @throws Exception if there is a JSON parsing error.
	 */
	public void echo(Map<String,Object> source) throws Exception  {
		Echo m = RTBServer.getStatus();
		m.to = (String)source.get("to");
		m.id = (String)source.get("id");
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = mapper.writeValueAsString(m);
		responseQueue.add(jsonString);
	}
	
	/*
	 * The not handled response to the command entity. Used when an
	 * unrecognized command is sent.
	 * @param echo. Map - the error message to send.
	 * @throws Exception if there is a JSON parsing error.
	 */
	public void notHandled(Map<String,Object> echo) throws Exception  {
		Echo m = RTBServer.getStatus();
		echo.put("msg","error, unhandled event");
		echo.put("status", "error");
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = mapper.writeValueAsString(echo);
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
		if (loggerQueue != null && logLevel <= this.logLevel) {
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
		bidCache.expire(br.oidStr, 300);
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
class CommandLoop extends JedisPubSub implements Runnable {
	Thread me;
	Jedis conn;

	/**
	 * Constructor.
	 * @param conn. Jedis - the Jedis connection dedicated to receiving
	 * commands.
	 */
	public CommandLoop(Jedis conn) {
		this.conn = conn;
		me = new Thread(this);
		me.start();
	}

	/**
	 * Subscribes the Jedis commands queue, does not return.
	 */
	public void run() {
		conn.subscribe(this, Controller.COMMANDS);
	}

	/**
	 * On a message from REDIS, handle the command.
	 * @param arg0. String - the channel of this message.
	 * @param arg1. String - the JSON encoded message.
	 */
	@Override
	public void onMessage(String arg0, String arg1) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = null;
			rootNode = mapper.readTree(arg1);
			JsonNode node = rootNode.path("cmd");
			int command = node.getIntValue();
			
			/** 
			 * check to see if this is a targeted command 
			 */
			node = rootNode.path("target");
			if (node != null) {
				String target = node.getTextValue();
				if (target != null)
					if (target.contains(Configuration.instanceName)==false)
						return;
			}
			
			Map<String, Object> mapObject = mapper.readValue(rootNode, 
					new TypeReference<Map<String, Object>>(){});
			mapObject.put("from", Configuration.instanceName);
			
			switch(command) {
			case Controller.ADD_CAMPAIGN:
				Controller.getInstance().addCampaign(mapObject);
				break;
			case Controller.DEL_CAMPAIGN:
				Controller.getInstance().deleteCampaign(mapObject);
				break;
			case Controller.STOP_BIDDER:
				Controller.getInstance().stopBidder(mapObject);
				break;
			case Controller.START_BIDDER:
				Controller.getInstance().startBidder(mapObject);
				break;
			case Controller.ECHO:
				Controller.getInstance().echo(mapObject);
				break;
			default:
				Controller.getInstance().notHandled(mapObject);
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

	/**
	 * Unused
	 */
	@Override
	public void onPMessage(String arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub

	}

	/**
	 * Unused
	 */
	@Override
	public void onPSubscribe(String arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	/**
	 * Unused
	 */
	@Override
	public void onPUnsubscribe(String arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	/**
	 * Unused
	 */
	@Override
	public void onSubscribe(String arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	/**
	 * Unused
	 */
	@Override
	public void onUnsubscribe(String arg0, int arg1) {
		// TODO Auto-generated method stub
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
		String name = Configuration.instanceName;
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
