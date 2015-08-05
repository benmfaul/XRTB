package com.xrtb.bidder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.codehaus.jackson.JsonNode;
import org.redisson.Redisson;
import org.redisson.core.MessageListener;
import org.redisson.core.RTopic;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import com.xrtb.commands.AddCampaign;
import com.xrtb.commands.BasicCommand;
import com.xrtb.commands.ClickLog;
import com.xrtb.commands.ConvertLog;
import com.xrtb.commands.DeleteCampaign;
import com.xrtb.commands.Echo;
import com.xrtb.commands.LogMessage;
import com.xrtb.commands.PixelClickConvertLog;
import com.xrtb.commands.PixelLog;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.db.User;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;
import com.xrtb.pojo.WinObject;

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
	/** The set log level command */
	public static final int SETLOGLEVEL = 6;
	
	/** The REDIS channel for sending commands to the bidders */
	public static final String COMMANDS = "commands";
	/** The REDIS channel the bidder sends command responses out on */
	public static final String RESPONSES = "responses";
	
	/** Publisher for commands */
	Publisher commandsQueue;
	/** The JEDIS object for creating bid hash objects */
	Jedis bidCache;
	
	/** The loop object used for reading commands */
	CommandLoop loop;

	/** The queue for posting responses on */
	Publisher responseQueue;
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
	/** Formatter for printing log messages */
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
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

		
		commandsQueue = new Publisher(config.redisson,COMMANDS);
		commandsQueue.getChannel().addListener(new CommandLoop());
		
		responseQueue = new Publisher(config.redisson,RESPONSES);
		
		if (config.REQUEST_CHANNEL != null)
			requestQueue = new Publisher(config.redisson,config.REQUEST_CHANNEL);
		if (config.WINS_CHANNEL != null)
			winsQueue = new Publisher(config.redisson,config.WINS_CHANNEL);
		if (config.BIDS_CHANNEL != null)
			bidQueue = new Publisher(config.redisson,config.BIDS_CHANNEL);
		if (config.LOG_CHANNEL != null)
			loggerQueue = new LogPublisher(config.redisson,config.LOG_CHANNEL);
	    if (config.CLICKS_CHANNEL != null) {
	    	clicksQueue = new ClicksPublisher(config.redisson,config.CLICKS_CHANNEL);
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
	 * Simplest form of the add campaign
	 * @param c Campaign. The campaign to add.
	 * @throws Exception on redis errors.
	 */
	public void addCampaign(Campaign c) throws Exception {
		Configuration.getInstance().deleteCampaign(c.adId);
		Configuration.getInstance().addCampaign(c);			
		AddCampaign cmd = new AddCampaign(null,c.adId);
	}
	
	/**
	 * Add a campaign from REDIS
	 * @param c BasiCommand. The command to add
	 * @throws Exception on REDIS errors.
	 */
	public void addCampaign(BasicCommand c) throws Exception {
		Campaign camp = WebCampaign.getInstance().db.getCampaign(c.target);
		Configuration.getInstance().deleteCampaign(camp.adId);
		Configuration.getInstance().addCampaign(camp);
		responseQueue.add(c);
	}

	/**
	 * Delete a campaign.
	 * @param id String. The Map of this command.
	 * @throws Exception if there is a JSON parse error.
	 */
	public void deleteCampaign(String id) throws Exception {
		Configuration.getInstance().deleteCampaign(id);
	}
	
	/**
	 * From REDIS delete campaign
	 * @param cmd BasicCommand.  The delete command
	 */
	public void deleteCampaign(BasicCommand cmd) {
		boolean b = Configuration.getInstance().deleteCampaign(cmd.target);
		if (!b)
			cmd.status = "error, no such campaign " + cmd.target;
		responseQueue.add(cmd);
	}

	/**
	 * Stop the bidder from REDIS
	 * @param cmd BasicCommand. The command as a map.
	 * @throws Exception if there is a JSON parsing error.
	 */
	public void stopBidder(BasicCommand cmd) throws Exception{
		RTBServer.stopped = true;
		cmd.msg = "stopped";
		responseQueue.add(cmd);
	}

	/**
	 * Start the bidder from REDIS
	 * @param cmd BasicCmd. The command.
	 * @throws Exception if there is a JSON parsing error.
	 */
	public void startBidder(BasicCommand cmd) throws Exception  {
		RTBServer.stopped = false;
		cmd.msg = "running";
		responseQueue.add(cmd);
	}

	/**
	 * Set the throttle percentage from REDIS
	 * @param node. JsoNode - JSON  of the command.
	 * TODO: this needs implementation.
	 */
	public void setPercentage(JsonNode node) {
		responseQueue.add(new BasicCommand());
	}
	
	/**
	 * THe echo command and its response.
	 * @param cmd BasicCommand. The command used
	 * @throws Exception if there is a JSON parsing error.
	 */
	public void echo(BasicCommand cmd) throws Exception  {
		Echo m = RTBServer.getStatus();
		m.to = cmd.to;
		m.id = cmd.id;
		responseQueue.add(m);
	}
	
	public void setLogLevel(BasicCommand cmd) throws Exception {
		int old = Configuration.getInstance().logLevel;
		Configuration.getInstance().logLevel = Integer.parseInt(cmd.target);
		Echo m = RTBServer.getStatus();
		m.to = cmd.to;
		m.id = cmd.id;
		m.status = "Log level changed from " + old + " to " + cmd.target;
		this.sendLog(1,"loglevel",m.status);
		responseQueue.add(m);
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
		responseQueue.add(m);
	}
	
	/**
	 * Sends an RTB request out on the appropriate REDIS queue
	 * @param br BidRequest. The request
	 */
	
	public void sendRequest(BidRequest br) {
		if (requestQueue != null)
			requestQueue.add(br);
	}
	
	
	/**
	 * Sends an RTB bid out on the appropriate REDIS queue
	 * @param bid BidResponse. The bid
	 */
	public void sendBid(BidResponse bid) {
		if (bidQueue != null)
			bidQueue.add(bid);
	}
	
	/**
	 * Sends an RTB win out on the appropriate REDIS queue
	 * @param hash String. The bid id.
	 * @param cost String. The cost component of the win.
	 * @param lat String. The latitude component of the win.
	 * @param lon String. The longitude component of the win.
	 * @param adId String. The campaign adid of this win.
	 * @param pubId String. The publisher id component of this win/
	 * @param image String. The image part of the win.
	 * @param forward String. The forward URL of the win.
	 * @param price String. The bid price of the win.
	 */
	public void sendWin(String hash,String cost,String lat,
			String lon, String adId,String pubId,String image, 
			String forward,String price) {
		if (winsQueue != null)
			winsQueue.add(new WinObject(hash, cost, lat,
			 lon,  adId, pubId, image, 
			 forward, price));
	}
	
	/**
	 * Sends a log message on the appropriate REDIS queue
	 * @param level int. The log level of this message.
	 * @param field String. An identification field for this message.
	 * @param msg String. The JSON of the message
	 */
	public void sendLog(int level, String field, String msg) {
		if (config.logLevel >  0 && (level > config.logLevel))
			return;
	    
		if (loggerQueue == null)
			return;
		
		LogMessage ms = new LogMessage(level,config.instanceName,field,msg);
		if (config.logLevel < 0) {
			if (Math.abs(config.logLevel) >= level)
				System.out.format("[%s] - %d - %s - %s - %s\n",sdf.format(new Date()),ms.sev,ms.source,ms.field,ms.message);
		} else {
			loggerQueue.add(ms);
		}
	}
	
	/**
	 * Send click info.
	 * @param target String. The URI of this click data
	 */
	public void publishClick(String target) {
		if (clicksQueue != null) {
			ClickLog log = new ClickLog(target);
			clicksQueue.add(log);
		}
	}
	
	/**
	 * Send pixel info. This fires when the ad actually loads into the users web page.
	 * @param target String. The URI of this pixel data
	 */
	public void publishPixel(String target) {
		if (clicksQueue != null) {
			PixelLog log = new PixelLog(target);
			clicksQueue.add(log);
		}
	}
	
	/**
	 * Send pixel info. This fires when the ad actually loads into the users web page.
	 * @param target String. The URI of this pixel data
	 */
	public void publishConvert(String target) {
		if (clicksQueue != null) {
			ConvertLog log = new ConvertLog(target);
			clicksQueue.add(log);
		}
	}
	
	/**
	 * Record a bid in REDIS
	 * @param br BidResponse. The bid response that we made earlier.
	 * @throws Exception on redis errors.
	 */
	public void recordBid(BidResponse br) throws Exception {
		Map m = new HashMap();
		Pipeline p = bidCache.pipelined();
		m.put("ADM",br.getAdmAsString());
		m.put("PRICE",""+br.price);
		try {
			p.hmset(br.oidStr,m);
			p.expire(br.oidStr, Configuration.getInstance().ttl);
			p.exec();
		} catch (Exception error) {
			
		} finally {
			p.sync();
		}
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
		//System.out.println(item);
		if (item.from != null && item.from.equals(Configuration.getInstance().instanceName))  {     // don't process your own commands.
			//System.out.println("DIDNT ACCEPT< IT WAS FROM ME!");
			return; 
		}
		
		ConcurrentMap<String,User>  map = config.redisson.getMap("users-database");
		try {
			switch(item.cmd) {
			case Controller.ADD_CAMPAIGN:
				Controller.getInstance().addCampaign(item);
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
			case Controller.SETLOGLEVEL:
				Controller.getInstance().setLogLevel(item);
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
 * A type of Publisher, but used specifically for clicks logging, contains the instance name
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
	public ClicksPublisher(Redisson redisson, String channel) throws Exception {
		super(redisson,channel);
	}
	
	/**
	 * Process, pixels, clicks, and conversions
	 */
	@Override
	public void run() {
		PixelClickConvertLog event = null;
		while(true) {
			try {
				if ((event = (PixelClickConvertLog)queue.poll()) != null) {
					logger.publish(event);
				}
				Thread.sleep(1);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
	}
	
}
