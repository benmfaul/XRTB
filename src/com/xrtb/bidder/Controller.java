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
 * A class for handling REDIS based commands to the RTB server.
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
	
	/** The campaigns known by the bidder */
	Set<Campaign> campaigns = new TreeSet<Campaign>();
	/** The current log level of the bidding engine */
	int logLevel = 0;
	
	/** The singleton instance of the controller */
	static Controller theInstance;

	/**
	 * Private construcotr with specified hosts
	 */
	private Controller() {
		Configuration c = Configuration.getInstance();
		
		/** the cache of bid adms */
		bidCache = new Jedis(c.cacheHost);
		bidCache.connect();
		
		/** transmit */
		publish = new Jedis(c.cacheHost);
		publish.connect();

		/** Reads commands */
		commands = new Jedis(c.cacheHost);
		commands.connect();
		loop = new CommandLoop(commands);
		
		responseQueue = new Publisher(publish,RESPONSES);
		
		if (c.REQUEST_CHANNEL != null)
			requestQueue = new Publisher(publish,c.REQUEST_CHANNEL);
		if (c.WINS_CHANNEL != null)
			winsQueue = new Publisher(publish,c.WINS_CHANNEL);
		if (c.BIDS_CHANNEL != null)
			bidQueue = new Publisher(publish,c.BIDS_CHANNEL);
		if (c.LOG_CHANNEL != null)
			loggerQueue = new LogPublisher(publish,c.LOG_CHANNEL);
		logLevel = c.logLevel;
	}

	/**
	 * Get the controller using localhost for REDIS connections.
	 * @return Controller. The singleton object of the controller.
	 */
	public static Controller getInstance() {
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
	 * @param node. JsonNode -JSON of command.
	 */
	public void addCampaign(Map<String,Object> source) {
		Campaign c = new Campaign();
		c.adId = (String)source.get("id");
		c.price = (Double)source.get("price");
		c.adomain = (String)source.get("adomain");
		c.template = (Map)source.get("template");
		c.nodes = (List)source.get("nodes");
		c.creatives = (List)source.get("creatives");
		Configuration.getInstance().deleteCampaign(c.adId);
		Configuration.getInstance().addCampaign(c);
		responseQueue.add("Response goes here");
	}

	/**
	 * Delete a campaign using REDIS.
	 * @param node. JsonNode - JSON of command.
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
	 * @param node. JsonNode - JSON of command.
	 */
	public void stopBidder(Map<String,Object> cmd) throws Exception{
		RTBServer.stopped = true;
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = mapper.writeValueAsString(cmd);
		responseQueue.add(jsonString);
	}

	/**
	 * Start the bidder.
	 * @param node. JsonNode - the JSON of the command.
	 */
	public void startBidder(Map<String,Object> cmd) throws Exception  {
		RTBServer.stopped = false;
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = mapper.writeValueAsString(cmd);
		responseQueue.add(jsonString);
	}

	/**
	 * Set the throttle percentage.
	 * @param node. JsoNode - JSON  of the command.
	 */
	public void setPercentage(JsonNode node) {
		responseQueue.add("Response goes here");
	}
	
	/**
	 * THe echo command and its response.
	 * @param echo. Map. The echo command.
	 * @throws Exception. Throws Exception on REDIS errors.
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
	 * @param s String. The JSON of the message
	 */
	public void sendLog(int logLevel, String msg) {
		if (loggerQueue != null && logLevel <= this.logLevel) {
			loggerQueue.add(msg);
		}
	}
	
	/**
	 * Record a bid in REDIS
	 * @param br BidRequest. The bid request that we made.
	 */
	public void recordBid(BidResponse br) {
		Map m = new HashMap();
		m.put("ADM",br.admAsString);
		m.put("PRICE",""+br.price);
		System.out.println("BR:"+br.oidStr);
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
			Controller.getInstance().responseQueue.add(error.toString());
			error.printStackTrace();
		}
	}

	@Override
	public void onPMessage(String arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPSubscribe(String arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPUnsubscribe(String arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSubscribe(String arg0, int arg1) {
		// TODO Auto-generated method stub

	}

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

	public Publisher(Jedis conn, String channel) {
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

	public LogPublisher(Jedis conn, String channel) {
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

