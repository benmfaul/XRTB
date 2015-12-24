package com.xrtb.bidder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.redisson.Redisson;
import org.redisson.RedissonClient;
import org.redisson.core.MessageListener;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.xrtb.commands.AddCampaign;
import com.xrtb.commands.BasicCommand;
import com.xrtb.commands.ClickLog;
import com.xrtb.commands.ConvertLog;
import com.xrtb.commands.Echo;
import com.xrtb.commands.LogMessage;
import com.xrtb.commands.PixelClickConvertLog;
import com.xrtb.commands.PixelLog;
import com.xrtb.commands.ShutdownNotice;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.db.User;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;
import com.xrtb.pojo.NobidResponse;
import com.xrtb.pojo.WinObject;

/**
 * A class for handling REDIS based commands to the RTB server. The Controller
 * open REDIS channels to the requested channels to handle commands, and logging
 * channels for log messages, win notifications, bid requests and bids. The idea
 * is to transmit all this information through REDIS so that you can\ build your
 * own database, accounting, and analytic processes outside of the bidding
 * engine.
 * 
 * Another job of the Controller is to create the REDIS cache. There could be
 * multiple bidders running in the infrastructure, but handling a win
 * notification requires that you have information about the original bid. This
 * means the system receiving the notification may not be the same system that
 * made the bid. The bid is stored in the cache as a map so the win handling
 * system can handle the win, even though it did not actually make the bid.
 * 
 * @author Ben M. Faul
 *
 */
public enum Controller {

	INSTANCE;

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
	/** The notice that bidder is terminating */
	public static final int SHUTDOWNNOTICE = 7;
	/** Set the no bid reason flag */
	public static final int NOBIDREASON = 8;

	/** The REDIS channel for sending commands to the bidders */
	public static final String COMMANDS = "commands";
	/** The REDIS channel the bidder sends command responses out on */
	public static final String RESPONSES = "responses";

	/** Publisher for commands */
	static Publisher commandsQueue;
	/** The JEDIS object for creating bid hash objects */
	static Jedis bidCache;

	/** The loop object used for reading commands */
	static CommandLoop loop;

	/** The queue for posting responses on */
	static Publisher responseQueue;
	/** Queue used to send wins */
	static Publisher winsQueue;
	/** Queue used to send bids */
	static Publisher bidQueue;
	/** Queue used to send nobid responses */
	static Publisher nobidQueue;
	/** Queue used for requests */
	static Publisher requestQueue;
	/** Queue for sending log messages */
	static LogPublisher loggerQueue;
	/** Queue for sending clicks */
	static ClicksPublisher clicksQueue;
	/** Formatter for printing log messages */
	static SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS");

	/* The configuration object used bu the controller */
	static Configuration config = Configuration.getInstance();
	
	private static  String password = "yabbadabbadoo";

	/**
	 * Private construcotr with specified hosts
	 * 
	 * @throws Exception
	 *             on REDIS errors.
	 */
	public static Controller getInstance() throws Exception {
		/** the cache of bid adms */

		if (bidCache == null) {
			bidCache = new Jedis(Configuration.cacheHost);
			bidCache.connect();
			if (Configuration.password != null)
				bidCache.auth(Configuration.password);

			commandsQueue = new Publisher(config.redisson, COMMANDS);
			commandsQueue.getChannel().addListener(new CommandLoop());

			// System.out.println("============= COMMAND LOOP ESTABLIISHED =================");

			responseQueue = new Publisher(config.redisson, RESPONSES);

			if (config.REQUEST_CHANNEL != null)
				requestQueue = new Publisher(config.redisson,
						config.REQUEST_CHANNEL);
			if (config.WINS_CHANNEL != null)
				winsQueue = new Publisher(config.redisson, config.WINS_CHANNEL);
			if (config.BIDS_CHANNEL != null)
				bidQueue = new Publisher(config.redisson, config.BIDS_CHANNEL);
			if (config.NOBIDS_CHANNEL != null)
				nobidQueue = new Publisher(config.redisson, config.NOBIDS_CHANNEL);
			if (config.LOG_CHANNEL != null)
				loggerQueue = new LogPublisher(config.redisson,
						config.LOG_CHANNEL);
			if (config.CLICKS_CHANNEL != null) {
				clicksQueue = new ClicksPublisher(config.redisson,
						config.CLICKS_CHANNEL);
			}
		}

		return INSTANCE;
	}
	
	/**
	 * Simplest form of the add campaign
	 * 
	 * @param c
	 *            Campaign. The campaign to add.
	 * @throws Exception
	 *             on redis errors.
	 */
	public void addCampaign(Campaign c) throws Exception {
		Configuration.getInstance().deleteCampaign(c.name, c.adId);
		Configuration.getInstance().addCampaign(c);
	}

	/**
	 * Add a campaign from REDIS
	 * 
	 * @param c
	 *            BasiCommand. The command to add
	 * @throws Exception
	 *             on REDIS errors.
	 */
	public void addCampaign(BasicCommand c) throws Exception {
		System.out.println("ADDING " + c.name + "/" + c.target);
		Campaign camp = WebCampaign.getInstance().db.getCampaign(c.name,
				c.target);
		// System.out.println("========================");
		BasicCommand m = null;
		if (camp == null) {
			m = new BasicCommand();
			m.to = c.from;
			m.from = Configuration.getInstance().instanceName;
			m.id = c.id;
			m.type = c.type;
			m.status = "Error";
			m.msg = "Campaign load failed, could not find " + c.name + "/"
					+ c.target;
			responseQueue.add(m);
		} else {
			Configuration.getInstance().deleteCampaign(camp.name, camp.adId);
			Configuration.getInstance().addCampaign(camp);
			m = new BasicCommand();
			m.to = c.from;
			m.from = Configuration.getInstance().instanceName;
			m.id = c.id;
			m.type = c.type;
			m.msg = "Campaign " + camp.name + "/" + camp.adId + " loaded ok";
			m.name = "AddCampaign Response";
			responseQueue.add(m);
		}
		System.out.println(m.msg);
	}

	/**
	 * Delete a campaign.
	 * 
	 * @param id
	 *            String. The Map of this command.
	 * @throws Exception
	 *             if there is a JSON parse error.
	 */
	public void deleteCampaign(String name, String id) throws Exception {
		Configuration.getInstance().deleteCampaign(name, id);
	}

	/**
	 * From REDIS delete campaign
	 * 
	 * @param cmd
	 *            BasicCommand. The delete command
	 */
	public void deleteCampaign(BasicCommand cmd) throws Exception {
		boolean b = Configuration.getInstance().deleteCampaign(cmd.name,
				cmd.target);
		BasicCommand m = new BasicCommand();
		if (!b) {
			m.msg = "error, no such campaign " + cmd.name + "/" + cmd.target;
			m.status = "error";
		} else
			m.msg = "Campaign deleted: " + cmd.name + "/" + cmd.target;
		m.to = cmd.from;
		m.from = Configuration.getInstance().instanceName;
		m.id = cmd.id;
		m.type = cmd.type;
		m.name = "DeleteCommand Response";
		responseQueue.add(m);
		
		if (cmd.name == null) {
			Configuration.getInstance().campaignsList.clear();
			this.sendLog(1, "deleteCampaign", "All campaigns cleared by "
					+ cmd.from);
		} else
		this.sendLog(1, "deleteCampaign", cmd.msg + " by "
				+ cmd.from);
	}

	/**
	 * Stop the bidder from REDIS
	 * 
	 * @param cmd
	 *            BasicCommand. The command as a map.
	 * @throws Exception
	 *             if there is a JSON parsing error.
	 */
	public void stopBidder(BasicCommand cmd) throws Exception {
		RTBServer.stopped = true;
		BasicCommand m = new BasicCommand();
		m.msg = "stopped";
		m.to = cmd.from;
		m.from = Configuration.getInstance().instanceName;
		m.id = cmd.id;
		m.type = cmd.type;
		m.name = "StopBidder Response";
		responseQueue.add(m);
		this.sendLog(1, "stopBidder", "Bidder stopped by command from "
				+ cmd.from);
	}

	/**
	 * Start the bidder from REDIS
	 * 
	 * @param cmd
	 *            BasicCmd. The command.
	 * @throws Exception
	 *             if there is a JSON parsing error.
	 */
	public void startBidder(BasicCommand cmd) throws Exception {
		RTBServer.stopped = false;
		BasicCommand m = new BasicCommand();
		m.msg = "running";
		m.to = cmd.from;
		m.from = Configuration.getInstance().instanceName;
		m.id = cmd.id;
		m.type = cmd.type;
		m.name = "StartBidder Response";
		responseQueue.add(m);
		this.sendLog(1, "startBidder", "Bidder started by command from "
				+ cmd.from);
	}

	/**
	 * Set the throttle percentage from REDIS
	 * 
	 * @param node
	 *            . JsoNode - JSON of the command. TODO: this needs
	 *            implementation.
	 */
	public void setPercentage(JsonNode node) {
		responseQueue.add(new BasicCommand());
	}

	/**
	 * THe echo command and its response.
	 * 
	 * @param cmd
	 *            BasicCommand. The command used
	 * @throws Exception
	 *             if there is a JSON parsing error.
	 */
	public void echo(BasicCommand cmd) throws Exception {
		Echo m = RTBServer.getStatus();
		m.to = cmd.from;
		m.from = Configuration.getInstance().instanceName;
		m.id = cmd.id;
		m.name = "Echo Response";
		responseQueue.add(m);
	}
	
	/**
	 * Send a shutdown notice to all concerned!
	 * @throws Exception on Redisson errors.
	 */
	public void sendShutdown() throws Exception {
		ShutdownNotice cmd = new ShutdownNotice(Configuration.getInstance().instanceName);
		responseQueue.writeFast(cmd);
	}

	public void setLogLevel(BasicCommand cmd) throws Exception {
		int old = Configuration.getInstance().logLevel;
		Configuration.getInstance().logLevel = Integer.parseInt(cmd.target);
		Echo m = RTBServer.getStatus();
		m.to = cmd.from;
		m.from = Configuration.getInstance().instanceName;
		m.id = cmd.id;
		m.msg = "Log level changed from " + old + " to " + cmd.target;
		m.name = "SetLogLevel Response";
		responseQueue.add(m);
		this.sendLog(1, "setLogLevel", m.msg + ", by " + cmd.from);
	}
	
	public void setNoBidReason(BasicCommand cmd) throws Exception {
		boolean old = Configuration.getInstance().printNoBidReason;
		Configuration.getInstance().printNoBidReason = Boolean.parseBoolean(cmd.target);
		Echo m = RTBServer.getStatus();
		m.to = cmd.from;
		m.from = Configuration.getInstance().instanceName;
		m.id = cmd.id;
		m.msg = "Print no bid reason level changed from " + old + " to " + cmd.target;
		m.name = "SetNoBidReason Response";
		responseQueue.add(m);
		this.sendLog(1, "setNoBidReason", m.msg + ", by " + cmd.from);
	}

	/*
	 * The not handled response to the command entity. Used when an unrecognized
	 * command is sent.
	 * 
	 * @param cmd. BasicCommand - the error message.
	 * 
	 * @throws Exception if there is a JSON parsing error.
	 */
	public void notHandled(BasicCommand cmd) throws Exception {
		Echo m = RTBServer.getStatus();
		m.msg = "error, unhandled event";
		m.status = "error";
		m.to = cmd.from;
		m.from = Configuration.getInstance().instanceName;
		m.id = cmd.id;
		m.name = "Unhandled Response";
		responseQueue.add(m);
	}

	/**
	 * Sends an RTB request out on the appropriate REDIS queue
	 * 
	 * @param br
	 *            BidRequest. The request
	 */

	public void sendRequest(BidRequest br) {
		if (requestQueue != null)
			requestQueue.add(br);
	}

	/**
	 * Sends an RTB bid out on the appropriate REDIS queue
	 * 
	 * @param bid
	 *            BidResponse. The bid
	 */
	public void sendBid(BidResponse bid) {
		if (bidQueue != null)
			bidQueue.add(bid);
	}
	
	/**
	 * Channel to send no bid information
	 * @param nobid NobidResponse. Info about the no bid
	 */
	public void sendNobid(NobidResponse nobid) {
		if (nobidQueue != null)
			nobidQueue.add(nobid);
	}

	/**
	 * Sends an RTB win out on the appropriate REDIS queue
	 * 
	 * @param hash
	 *            String. The bid id.
	 * @param cost
	 *            String. The cost component of the win.
	 * @param lat
	 *            String. The latitude component of the win.
	 * @param lon
	 *            String. The longitude component of the win.
	 * @param adId
	 *            String. The campaign adid of this win.
	 * @param cridId
	 * 			  String. The creative id of this win.
	 * @param pubId
	 *            String. The publisher id component of this win/
	 * @param image
	 *            String. The image part of the win.
	 * @param forward
	 *            String. The forward URL of the win.
	 * @param price
	 *            String. The bid price of the win.
	 * @param adm
	 * 			  String. the adm that was returned on the win notification. If null, it means nothing was returned.
	 */
	public void sendWin(String hash, String cost, String lat, String lon,
			String adId, String cridId, String pubId, String image, String forward,
			String price, String adm) {
		if (winsQueue != null)
			winsQueue.add(new WinObject(hash, cost, lat, lon, adId, cridId, pubId,
					image, forward, price, adm));
	}

	/**
	 * Sends a log message on the appropriate REDIS queue
	 * 
	 * @param level
	 *            int. The log level of this message.
	 * @param field
	 *            String. An identification field for this message.
	 * @param msg
	 *            String. The JSON of the message
	 */
	public void sendLog(int level, String field, String msg) {
		if (config.logLevel > 0 && (level > config.logLevel))
			return;

		if (loggerQueue == null)
			return;

		LogMessage ms = new LogMessage(level, config.instanceName, field, msg);
		if (config.logLevel < 0) {
			if (Math.abs(config.logLevel) >= level)
				System.out.format("[%s] - %d - %s - %s - %s\n",
						sdf.format(new Date()), ms.sev, ms.source, ms.field,
						ms.message);
		}
		loggerQueue.add(ms);
	}

	/**
	 * Send click info.
	 * 
	 * @param target
	 *            String. The URI of this click data
	 */
	public void publishClick(String target) {
		if (clicksQueue != null) {
			ClickLog log = new ClickLog(target);
			clicksQueue.add(log);
		}
	}

	/**
	 * Send pixel info. This fires when the ad actually loads into the users web
	 * page.
	 * 
	 * @param target
	 *            String. The URI of this pixel data
	 */
	public void publishPixel(String target) {
		if (clicksQueue != null) {
			PixelLog log = new PixelLog(target);
			clicksQueue.add(log);
		}
	}

	/**
	 * Send pixel info. This fires when the ad actually loads into the users web
	 * page.
	 * 
	 * @param target
	 *            String. The URI of this pixel data
	 */
	public void publishConvert(String target) {
		if (clicksQueue != null) {
			ConvertLog log = new ConvertLog(target);
			clicksQueue.add(log);
		}
	}

	/**
	 * Record a bid in REDIS
	 * 
	 * @param br
	 *            BidResponse. The bid response that we made earlier.
	 * @throws Exception
	 *             on redis errors.
	 */
	public void recordBid(BidResponse br) throws Exception {
		Map m = new HashMap();

		synchronized (bidCache) {
			Pipeline p = bidCache.pipelined();
			m.put("ADM", br.getAdmAsString());
			m.put("PRICE", "" + br.creat.price);
			try {
				p.hmset(br.oidStr, m);
				p.expire(br.oidStr, Configuration.getInstance().ttl);
				p.exec();
			} catch (Exception error) {

			} finally {
				p.sync();
			}
		}
	}

	/**
	 * Remove a bid object from the cache.
	 * 
	 * @param hash
	 *            String. The bid object id.
	 */
	public void deleteBidFromCache(String hash) {
		synchronized (bidCache) {
			Map m = new HashMap();
			Pipeline p = bidCache.pipelined();
			try {
				p.del(hash);
				p.exec();
			} catch (Exception error) {

			} finally {
				p.sync();
			}
		}
	}

	/**
	 * Retrieve previously recorded bid data
	 * 
	 * @param oid
	 *            String. The object id of the bid.
	 * @return Map. A map of the returned data, will be null if not found.
	 */
	public Map getBidData(String oid) {
		Map m = null;
		Response r = null;

		synchronized (bidCache) {
			Pipeline p = bidCache.pipelined();
			try {
				r = p.hgetAll(oid);

				p.exec();
			} catch (Exception error) {

			} finally {
				p.sync();
			}

			m = (Map) r.get();
		}
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
	 * 
	 * @param arg0
	 *            . String - the channel of this message.
	 * @param arg1
	 *            . String - the JSON encoded message.
	 */
	@Override
	public void onMessage(String arg0, BasicCommand item) {

		try {
			if (item.to != null && (item.to.equals("*") == false)) {
				boolean mine = Configuration.getInstance().instanceName
						.matches(item.to);
				if (item.to.equals("") == false && !mine) {
					Controller.getInstance().sendLog(5,
							"Controller:onMessage:" + item,
							"Message was not for me: " + item);
					return;
				}
			}
		} catch (Exception error) {
			try {
				Echo m = new Echo();
				m.from = Configuration.getInstance().instanceName;
				m.to = item.from;
				m.id = item.id;
				m.status = "error";
				m.msg = error.toString();
				Controller.getInstance().responseQueue.add(m);
				Controller.getInstance().sendLog(1,
						"Controller:onMessage:" + item,
						"Error: " + error.toString());
				return;
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

		}

		try {
			switch (item.cmd) {
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
				item.msg = error.toString();
				item.to = item.from;
				item.from = Configuration.getInstance().instanceName;
				item.status = "error";
				Controller.getInstance().responseQueue.add(item);
				error.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			error.printStackTrace();
		}

	}

}

/**
 * A type of Publisher, but used specifically for clicks logging, contains the
 * instance name and the current time in EPOCH.
 * 
 * @author Ben M. Faul
 *
 */
class ClicksPublisher extends Publisher {

	/**
	 * Constructor for clicls publisher class.
	 * 
	 * @param conn
	 *            Jedis. The REDIS connection.
	 * @param channel
	 *            String. The topic name to publish on.
	 */
	public ClicksPublisher(RedissonClient redisson, String channel)
			throws Exception {
		super(redisson, channel);
	}

	/**
	 * Process, pixels, clicks, and conversions
	 */
	@Override
	public void run() {
		PixelClickConvertLog event = null;
		while (true) {
			try {
				if ((event = (PixelClickConvertLog) queue.poll()) != null) {
					logger.publishAsync(event);
				}
				Thread.sleep(1);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
	}

}
