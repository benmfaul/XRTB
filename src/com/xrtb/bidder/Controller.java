package com.xrtb.bidder;

import java.text.SimpleDateFormat;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aerospike.redisson.AerospikeHandler;
import com.aerospike.redisson.RedissonClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xrtb.blocks.AwsCommander;
import com.xrtb.commands.BasicCommand;
import com.xrtb.commands.ClickLog;
import com.xrtb.commands.ConvertLog;

import com.xrtb.commands.DeleteCreative;
import com.xrtb.commands.Echo;

import com.xrtb.commands.PixelLog;
import com.xrtb.commands.SetPrice;
import com.xrtb.commands.ShutdownNotice;

import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.common.Creative;
import com.xrtb.common.ExchangeLogLevel;
import com.xrtb.db.Database;
import com.xrtb.exchanges.adx.AdxFeedback;
import com.xrtb.fraud.FraudLog;
import com.xrtb.jmq.RTopic;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;
import com.xrtb.pojo.NobidResponse;
import com.xrtb.pojo.WinObject;
import com.xrtb.tools.Performance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class for handling REDIS based commands to the RTB server. The Controller
 * open REDIS channels to the requested channels to handle commands, and logging
 * channels for log messages, win notifications, bid requests and bids. The idea
 * is to transmit all this information through REDIS so that you can\ build your
 * own database, accounting, and analytic processes outside of the bidding
 * engine.
 * 
 * Another job of the Controller is to create the Aerospike cache. There could be
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
	/** Remove a creative */
	public static final int DELETE_CREATIVE = 9;
	/** Remove a user */
	public static final int DELETE_USER = 10;
	/** Add a user */
	public static final int ADD_USER = 11;
	// Get Price
	public static final int GET_PRICE = 12;
	// Set Price
	public static final int SET_PRICE = 13;
	// Add a list of campaigns
	public static final int ADD_CAMPAIGNS_LIST = 14;
	// Add an aws object
	public static final int CONFIGURE_AWS_OBJECT = 15;

	/** The REDIS channel for sending commands to the bidders */
	public static final String COMMANDS = "commands";

	/** The JEDIS object for creating bid hash objects */
	static RedissonClient bidCachePool;

	/** The loop object used for reading commands */
	static CommandLoop loop;

	/** The queue for posting responses on */
	static ZPublisher responseQueue;
	/** Queue used to send wins */
	static ZPublisher winsQueue;
	/** Queue used to send bids */
	static ZPublisher bidQueue;
	/** Queue used to send nobid responses */
	static ZPublisher nobidQueue;
	/** Queue used for requests */
	static ZPublisher requestQueue;
	/** Alternate Queue used for requests when doing unilogging */
	static ZPublisher request2Queue;
	/** Queue for sending log messages */
	static ZPublisher loggerQueue;
	/** Queue for sending clicks */
	static ZPublisher clicksQueue;
	/** Formatter for printing Xforensiqs messages */
	static ZPublisher forensiqsQueue;
	/** Queue for sending stats info */
	static ZPublisher perfQueue;
	// Queue for sending nobid reasons */
	static ZPublisher reasonsQueue;
	/** Formatter for printing log messages */
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	/* The configuration object used bu the controller */
	static Configuration config;

	/** A factory object for making timnestamps */
	static final JsonNodeFactory factory = JsonNodeFactory.instance;
	
	static final ExchangeLogLevel requestLogLevel = ExchangeLogLevel.getInstance();

	static final Logger logger = LoggerFactory.getLogger(Controller.class);
	/**
	 * Private construcotr with specified hosts
	 * 
	 * @throws Exception
	 *             on REDIS errors.
	 */
	public static Controller getInstance() throws Exception {
		
		config = Configuration.getInstance();
		/** the cache of bid adms */

		if (bidCachePool == null) {
			bidCachePool = config.redisson;

			RTopic t = new RTopic(config.commandAddresses);
			t.addListener(new CommandLoop());

			if (config.RESPONSES != null) {
				responseQueue = new ZPublisher(config.RESPONSES);
			}
			if (config.REQUEST_CHANNEL != null) {
				requestQueue = new ZPublisher(config.REQUEST_CHANNEL);
			}
			if (config.UNILOGGER_CHANNEL != null) {
				request2Queue = new ZPublisher(config.UNILOGGER_CHANNEL);
			}
			if (config.PERF_CHANNEL != null) {
				perfQueue = new ZPublisher(config.PERF_CHANNEL);
			}
			if (config.REASONS_CHANNEL != null) {
				reasonsQueue = new ZPublisher(config.REASONS_CHANNEL);
			}
			if (config.WINS_CHANNEL != null) {
				winsQueue = new ZPublisher(config.WINS_CHANNEL);
			}
			if (config.BIDS_CHANNEL != null) {
				bidQueue = new ZPublisher(config.BIDS_CHANNEL);
			}
			if (config.NOBIDS_CHANNEL != null) {
				nobidQueue = new ZPublisher(config.NOBIDS_CHANNEL);
			}
			if (config.CLICKS_CHANNEL != null) {
				clicksQueue = new ZPublisher(config.CLICKS_CHANNEL);
			}
			if (config.FORENSIQ_CHANNEL != null) {
				forensiqsQueue = new ZPublisher(config.FORENSIQ_CHANNEL);
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
		Configuration.getInstance().deleteCampaign(c.owner, c.adId);
		Configuration.getInstance().addCampaign(c);
	}
	
	public void configureAwsObject(BasicCommand c) {
		logger.info("ADDING AWS OBJECT {}/{}",c.owner,c.target);
		BasicCommand m = new BasicCommand();
		m.owner = c.owner;
		m.to = c.from;
		m.from = Configuration.instanceName;
		m.id = c.id;
		m.type = c.type;
		AwsCommander aws = new AwsCommander(c.target);
		if (aws.errored()) {
			m.status = "Error";
			m.msg = "AWS Object load failed: " + aws.getMessage();
			responseQueue.add(m);
		} else {
			m.msg = "AWS Object " + c.target + " loaded ok";
			m.name = "AWS Object Response";
			logger.info("ConfigureAws results: {}", m.msg);
			responseQueue.add(m);
		}
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
		logger.info("ADDING campaign: {}/{}",c.owner, c.target);
		Campaign camp = WebCampaign.getInstance().db.getCampaign(c.owner, c.target);
		BasicCommand m = new BasicCommand();
		m.owner = c.owner;
		m.to = c.from;
		m.from = Configuration.instanceName;
		m.id = c.id;
		m.type = c.type;
		if (camp == null) {
			m.status = "Error";
			m.msg = "Campaign load failed, could not find " + c.owner + "/" + c.target;
			responseQueue.add(m);
		} else {

			Configuration.getInstance().deleteCampaign(camp.owner, camp.adId);
			Configuration.getInstance().addCampaign(camp);

			// System.out.println(camp.toJson());

			m.msg = "Campaign " + camp.owner + "/" + camp.adId + " loaded ok";
			m.name = "AddCampaign Response";
			logger.info("AddCampaign {} by {}", m.msg, c.owner);
			responseQueue.add(m);
		}
		logger.info("Responding with: {}",m.msg);
	}
	
	public void addCampaignsList(BasicCommand c) throws Exception {
		logger.info("ADDING {}/{}",c.owner,c.target);
		String [] campaigns = c.target.split(",");
		
		BasicCommand m = new BasicCommand();
		m.owner = c.owner;
		m.to = c.from;
		m.from = Configuration.instanceName;
		m.id = c.id;
		m.type = c.type;
		Configuration.getInstance().addCampaignsList(c.owner, campaigns);
		responseQueue.add(m);
	}
	
	public void setPrice(SetPrice cmd) throws Exception {
		logger.info("Setting Price {}/{} to {}" + cmd.name,cmd.target,cmd.price);
		String campName = cmd.name;
		String creatName = cmd.target;
		BasicCommand m = new BasicCommand();
		m.owner = cmd.owner;
		m.to = cmd.from;
		m.from = Configuration.instanceName;
		m.id = cmd.id;
		m.type = cmd.type;
		boolean handled = false;
		Double price = cmd.price;
		for (Campaign campaign : Configuration.getInstance().campaignsList) {
			if (campaign.adId.equals(campName)) {
				for (Creative creat : campaign.creatives) {
					if (creat.impid.equals(creatName)) {
						creat.price = price;
						m.msg = "Price set to " + price;
						handled = true;
						Database db = Database.getInstance();
						db.reload();
						
					}
				}
				if (handled == false) {
					m.status = "Error";
					m.msg = "Can't find creative: " + creatName;
					handled = true;
					break;
				}
			}
		}
		if (!handled) {
			m.msg = "Can't find campaign: " + campName;
			m.status = "Error";
		}
		
		m.name = "SetPrice Response";
		responseQueue.add(m);	
	
		logger.info("Responding with {}", m.msg);
	}
	
	public void getPrice(BasicCommand c) throws Exception {
		logger.info("Getting Price {}/{}",c.owner,c.target);
		String parts[] = c.target.split("/");
		BasicCommand m = new BasicCommand();
		m.owner = c.owner;
		m.to = c.from;
		m.from = Configuration.instanceName;
		m.id = c.id;
		m.type = c.type;
		boolean handled = false;
		for (Campaign campaign : Configuration.getInstance().campaignsList) {
			if (campaign.adId.equals(parts[0])) {
				for (Creative creat : campaign.creatives) {
					if (creat.impid.equals(parts[1])) {
						m.price = creat.price;
						handled = true;
						break;
					}
				}
				m.status = "Error";
				m.msg = "Can't find creative: " + parts[1];
				handled = true;
				break;
			}
		}
		if (!handled) {
			m.msg = "Can't find campaign: " + parts[0];
			m.status = "Error";
		}
		
		m.name = "GetPrice Response";
		responseQueue.add(m);
	}

	public void updateStatusZooKeeper(String msg) {
		if (Configuration.zk == null)
			return;
		try {
			Configuration.zk.writeStatus(msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void removeZnode() {
		if (Configuration.zk == null)
			return;
		try {
			Configuration.zk.remove();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Deletes the user, then tells all the other bidders to stop, then reload
	 * all their campaigns.
	 * 
	 * @param owner
	 *            String. The user or root that is deleting the use.
	 * @param name
	 *            String name. The user to delete
	 * @throws Exception
	 *             on database errors from Redisson
	 */
	public void deleteUser(String owner, String name) throws Exception {
		Configuration.getInstance().deleteUser(owner, name);
		Controller.getInstance().deleteCampaign(name, "*"); // delete from
															// bidder;

	}

	/**
	 * Delete a campaign.
	 * 
	 * @param  owner String. The owner (user) of the campaign.
	 * @param name String. The name of the campaign.
	 * @throws Exception
	 *             if there is a JSON parse error.
	 */
	public void deleteCampaign(String owner, String name) throws Exception {
		Configuration.getInstance().deleteCampaign(owner, name);
	}

	public void deleteUser(BasicCommand cmd) throws Exception {
		boolean b = Configuration.getInstance().deleteUser(cmd.owner, cmd.target);
		BasicCommand m = new BasicCommand();
		if (!b) {
			m.msg = "error, no such User " + cmd.target;
			m.status = "error";
		} else
			m.msg = "User deleted: " + cmd.target + " by " + cmd.target;
		m.to = cmd.from;
		m.from = Configuration.instanceName;
		m.id = cmd.id;
		m.type = cmd.type;
		m.name = "DeleteUser Response";
		responseQueue.add(m);
		logger.info("DeleteUser: {} by {}", cmd.msg, cmd.owner);
	}

	/**
	 * From Campaign List in Server delete campaign Note, if this is a cache2k
	 * based system (Not Aerospike) it will delete from the cache2k database
	 * too.
	 * 
	 * @param cmd
	 *            BasicCommand. The delete command
	 */
	public void deleteCampaign(BasicCommand cmd) throws Exception {
		boolean b = Configuration.getInstance().deleteCampaign(cmd.owner, cmd.target);
		BasicCommand m = new BasicCommand();
		if (!b) {
			m.msg = "error, no such campaign " + cmd.owner + "/" + cmd.target;
			m.status = "error";
		} else
			m.msg = "Campaign deleted: " + cmd.owner + "/" + cmd.target;
		m.to = cmd.from;
		m.from = Configuration.instanceName;
		m.id = cmd.id;
		m.type = cmd.type;
		m.name = "DeleteCampaign Response";
		responseQueue.add(m);

		if (cmd.name == null) {
			Configuration.getInstance().campaignsList.clear();
			logger.info("DeleteCampaign: all campaigns cleared by: {}",cmd.from);
		} else
			logger.info("DeleteCampaign {} by {}", cmd.msg,cmd.owner);
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
		m.from = Configuration.instanceName;
		m.id = cmd.id;
		m.type = cmd.type;
		m.name = "StopBidder Response";
		responseQueue.add(m);
		logger.info("StopBidder: by command from {}",cmd.from);
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

		if (Configuration.getInstance().deadmanSwitch != null) {
			if (Configuration.getInstance().deadmanSwitch.canRun() == false) {
				BasicCommand m = new BasicCommand();
				m.msg = "Error, the deadmanswitch is not present";
				m.to = cmd.from;
				m.from = Configuration.instanceName;
				m.id = cmd.id;
				m.type = cmd.type;
				m.name = "StartBidder Response";
				responseQueue.add(m);
				logger.warn("StartBidder, error: attempted start bidder by command from {}, failed deadman switch is thrown",cmd.from);
				return;
			}
		}

		RTBServer.stopped = false;
		BasicCommand m = new BasicCommand();
		m.msg = "running";
		m.to = cmd.from;
		m.from = Configuration.instanceName;
		m.id = cmd.id;
		m.type = cmd.type;
		m.name = "StartBidder Response";
		responseQueue.add(m);
		logger.info("StartBidderm bidder started by command from {}",cmd.from);
	}

	/**
	 * Set the throttle percentage from REDIS
	 * 
	 * @param node
	 *            . JsoNode - JSON of the command. TODO: this needs
	 *            implementation.
	 */
	public void setPercentage(JsonNode node) throws Exception {
		responseQueue.add(new BasicCommand());
	}

	private void load(Map values, Map<String, String> m, String key, Object def) {
		String value = null;
		if (m.get(key) != null) {
			try {
				
				if (def instanceof String) {
					value = m.get(key);
					values.put(key, value);
				} else if (def instanceof Long) {
					value = m.get(key);
					values.put(key, Long.parseLong(value));
				} else if (def instanceof Boolean) {
					value = m.get(key);
					values.put(key, Boolean.parseBoolean(value));
				} else if (def instanceof Integer) {
					value = m.get(key);
					values.put(key, Integer.parseInt(value));
				}
				if (def instanceof Double) {
					value = m.get(key);
					values.put(key, Double.parseDouble(value));
				}
				if (def instanceof List) {
					values.put(key, def);
				}
			} catch (Exception error) {
				System.err.println("---------->" + key + ", " + value);
				values.put(key, 0);
			}
		} else {
			// System.err.println("-----------> Unknown type: " + key + ", "
			// + value);
			values.put(key, def);
		}
	}

	/**
	 * Retrieve a member RTB status from Aerospike
	 * 
	 * @param member
	 *            String. The member's instance name.
	 * @return Map. A Hash,ap of data.
	 */
	public Map getMemberStatus(String member) {
		Map values = new HashMap();
		Map<String, String> m = null;

		try {
			m = bidCachePool.hgetAll(member);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (m != null) {
			load(values, m, "total", new Long(0));
			load(values, m, "request", new Long(0));
			load(values, m, "bid", new Long(0));
			load(values, m, "nobid", new Long(0));
			load(values, m, "win", new Long(0));
			load(values, m, "clicks", new Long(0));
			load(values, m, "pixels", new Long(0));
			load(values, m, "errors", new Long(0));
			load(values, m, "adspend", new Double(0));
			load(values, m, "qps", new Double(0));
			load(values, m, "avgx", new Double(0));
			load(values, m, "fraud", new Long(0));
			load(values, m, "stopped", new Boolean(true));
			load(values, m, "ncampaigns", new Long(0));
			load(values, m, "bid", new Long(0));
			load(values, m, "loglevel", new Long(-3));
			load(values, m, "nobidreason", new Boolean(false));
			load(values, m, "exchanges", m.get("exchanges"));
		}
		return values;
	}

	/**
	 * Record the member stats in Aerospike
	 * 
	 * @param e
	 *            Echo. The status of this campaign.
	 */
	public void setMemberStatus(Echo e) throws Exception {
		String member = Configuration.instanceName;
		Map m = new HashMap();
		m.put("total", "" + e.handled);
		m.put("request", "" + e.request);
		m.put("bid", "" + e.bid);
		m.put("nobid", "" + e.nobid);
		m.put("win", "" + e.win);
		m.put("clicks", "" + e.clicks);
		m.put("pixels", "" + e.pixel);
		m.put("errors", "" + e.error);
		m.put("adspend", "" + e.adspend);
		m.put("qps", "" + e.qps);
		m.put("avgx", "" + e.avgx);
		m.put("fraud", "" + e.fraud);
		m.put("exchanges", BidRequest.getExchangeCounts());

		m.put("time", "" + System.currentTimeMillis());

		m.put("cpu", Performance.getCpuPerfAsString());
		m.put("diskpctfree", Performance.getPercFreeDisk());
		m.put("threads", "" + Performance.getThreadCount());
		m.put("cores", "" + Performance.getCores());

		m.put("stopped", "" + RTBServer.stopped);
		m.put("ncampaigns", "" + Configuration.getInstance().campaignsList.size());
		m.put("loglevel", "" + Configuration.getInstance().logLevel);
		m.put("nobidreason", "" + Configuration.getInstance().printNoBidReason);

		bidCachePool.hmset(member, m, RTBServer.PERIODIC_UPDATE_TIME / 1000 + 15);

	}
	
	public void reportNoBidReasons() {
		if (reasonsQueue != null) { 
			String report = CampaignProcessor.probe.reportCsv();
			if (report.length()==0)
				return;
			reasonsQueue.addString(report);
		}
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
		m.from = Configuration.instanceName;
		m.id = cmd.id;
		m.name = "Echo Response";
		responseQueue.add(m);
	}

	/**
	 * Send a shutdown notice to all concerned!
	 * 
	 * @throws Exception
	 *             on Redisson errors.
	 */
	public void sendShutdown() throws Exception {
		ShutdownNotice cmd = new ShutdownNotice(Configuration.instanceName);
		responseQueue.add(cmd);
	}

	public void setLogLevel(BasicCommand cmd) throws Exception {
		int old = Configuration.getInstance().logLevel;
		Configuration.getInstance().logLevel = Integer.parseInt(cmd.target);
		Echo m = RTBServer.getStatus();
		m.to = cmd.from;
		m.from = Configuration.instanceName;
		m.id = cmd.id;
		m.msg = "Log level changed from " + old + " to " + cmd.target;
		m.name = "SetLogLevel Response";
		responseQueue.add(m);
		logger.info("SetLogLevel {} by {}", m.msg, cmd.from);
	}

	/**
	 * This will whack a creative out of a campaign. This stops the bidding on
	 * it
	 * 
	 * @param cmd
	 *            BasicCommand. The command.
	 * @throws Exception
	 */
	public void deleteCreative(DeleteCreative cmd) throws Exception {
		String owner = cmd.owner;
		String campaignid = cmd.name;
		String creativeid = cmd.target;

		Echo m = RTBServer.getStatus();
		m.owner = cmd.owner;
		m.to = cmd.from;
		m.from = Configuration.instanceName;
		m.id = cmd.id;
		try {
			Configuration.getInstance().deleteCampaignCreative(owner, campaignid, creativeid);
			m.msg = "Delete campaign creative " + owner + "/" + campaignid + "/" + creativeid + " succeeded";
		} catch (Exception error) {
			m.msg = "Delete campaign creative " + owner + "/" + campaignid + "/" + creativeid + " failed, reason: "
					+ error.getMessage();
		}
		m.name = "DeleteCreative Response";
		responseQueue.add(m);
		logger.info("SetLogLevel {} by {}", m.msg , cmd.from);
	}
	
	public List<Map> getBackPressure() {
		List<Map> bp = new ArrayList();
		Map m = null;
		/** The queue for posting responses on */
		if (responseQueue != null) m = responseQueue.getBp();
		if (m != null) bp.add(m);
		
		/** Queue used to send wins */
		if (winsQueue != null) m = winsQueue.getBp();
		if (m != null) bp.add(m);
		
		/** Queue used to send bids */
		if (bidQueue != null) m = bidQueue.getBp();
		if (m != null) bp.add(m);
		
		/** Queue used to send nobid responses */
		if (nobidQueue != null) m = nobidQueue.getBp();
		if (m != null) bp.add(m);
		
		/** Queue used for requests */
		if (requestQueue != null) m = requestQueue.getBp();
		if (m != null) bp.add(m);
		
		/** Queue for sending log messages */
		if (loggerQueue != null) m = loggerQueue.getBp();
		if (m != null) bp.add(m);
		
		/** Queue for sending clicks */
		if (clicksQueue != null) m = clicksQueue.getBp();
		if (m != null) bp.add(m);
		
		/** Formatter for printing forensiqs messages */
		if (forensiqsQueue != null) m = forensiqsQueue.getBp();
		if (m != null) bp.add(m);
		
		/** Queue for sending stats info */
		if (perfQueue != null) m = perfQueue.getBp();
		if (m != null) bp.add(m);
		
		return bp;
	}

	public void setNoBidReason(BasicCommand cmd) throws Exception {
		boolean old = Configuration.getInstance().printNoBidReason;
		Configuration.getInstance().printNoBidReason = Boolean.parseBoolean(cmd.target);
		Echo m = RTBServer.getStatus();
		m.to = cmd.from;
		m.from = Configuration.instanceName;
		m.id = cmd.id;
		m.msg = "Print no bid reason level changed from " + old + " to " + cmd.target;
		m.name = "SetNoBidReason Response";
		responseQueue.add(m);
		logger.info("SetNoBidReason {} by {}", m.msg, cmd.from);
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
		m.from = Configuration.instanceName;
		m.id = cmd.id;
		m.name = "Unhandled Response";
		responseQueue.add(m);
	}
	
	/**
	 * Log summary stats
	 * @param m Map. The map containing the stats.
	 * @throws Exception if Error writing top queue
	 */
	public void sendStats(Map m)  {
		if (perfQueue != null) { 
			perfQueue.add(m);
		}
	}

	/**
	 * Sends an RTB request out on the appropriate Publisher queue. Note, this does not report
	 * to the Unilogger queue.
	 * 
	 * @param br  BidRequest. The request.
	 * @param override boolean. Set to true to log, no matter what the log percentage is set at.
	 * @return boolean. Returns true if it logged, else returns false.
	 */

	public boolean sendRequest(BidRequest br, boolean override) throws Exception {	
		 if (br.notABidRequest())
			 return false;
		 
		 if (!override) {
			 if (!requestLogLevel.shouldLog(br.getExchange())) {
				return false;
			 }
		 }
	 
		 if (requestQueue != null) {
				ObjectNode original = (ObjectNode) br.getOriginal();
				
				// Can happen if this wasn't a real bid
				if (original == null)
					return false;

				ObjectNode ext = (ObjectNode) original.get("ext");
				if (ext != null) {
					ext.put("timestamp", System.currentTimeMillis());
					ext.put("exchange", br.getExchange());
				} else {
					ObjectNode child = factory.objectNode();
					child.put("timestamp", System.currentTimeMillis());
					child.put("exchange", br.getExchange());	
					original.put("ext", child);
				}
				original.put("type", "requests");
				requestQueue.addString(original.toString());

		}
		
		return true;
	}

	/**
	 * Sends an RTB bid out on the appropriate ZeroMQ queue
	 * 
	 * @param bid
	 *            BidResponse. The bid
	 */
	public void sendBid(BidRequest br, BidResponse bid)  {
		if (bid.isNoBid()) // this can happen on Adx, as BidResponse code is
							// always 200, even on nobid
			return;

		////////////// UNIFIED LOGGER ///////////////
		if (request2Queue != null)
			request2Queue.add(br.getOriginal());
		/////////////////////////////////////////////
		
		if (bidQueue != null) 
			bidQueue.add(bid);	
	}

	/**
	 * Channel to send no bid information
	 * 
	 * @param nobid
	 *            NobidResponse. Info about the no bid
	 */
	public void sendNobid(NobidResponse nobid) {
		if (nobidQueue != null)
			nobidQueue.add(nobid);
	}

	/**
	 * Inject a feedback message into the request log
	 * 
	 * @param feedback
	 *            AdxFeedback. A feedback id and message.
	 */
	public void sendAdxFeedback(AdxFeedback feedback) {
		if (requestQueue == null)
			return;
		requestQueue.add(feedback);
	}

	/**
	 * Sends an RTB win out on the appropriate 0MQ queue
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
	 *            String. The creative id of this win.
	 * @param pubId
	 *            String. The publisher id component of this win/
	 * @param image
	 *            String. The image part of the win.
	 * @param forward
	 *            String. The forward URL of the win.
	 * @param price
	 *            String. The bid price of the win.
	 * @param adm
	 *            String. the adm that was returned on the win notification. If
	 *            null, it means nothing was returned.
	 */
	public void sendWin(String hash, String cost, String lat, String lon, String adId, String cridId, String pubId,
			String image, String forward, String price, String adm, String adtype) {
		if (winsQueue != null)
			winsQueue.add(new WinObject(hash, cost, lat, lon, adId, cridId, pubId, image, forward, price, adm, adtype));
	}

	/**
	 * Determine if it is appropriate to log. Use this on debug log messages so
	 * you dont create a lot of objects for the log message, and then it just
	 * gets tossed because of the log level.
	 * 
	 * @param level
	 *            int. The level you want to log at.
	 * @return boolean. Returns true if it will log at this level, else returns
	 *         false.
	 */
	public boolean canLog(int level) {
		int checkLog = config.logLevel;
		if (checkLog < 0)
			checkLog = -checkLog;

		if (level > checkLog)
			return false;

		if (loggerQueue == null)
			return false;

		return true;
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

	public void publishFraud(FraudLog m) {
		if (forensiqsQueue != null) {
			forensiqsQueue.add(m);
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
	 * Record a bid in Aerospike
	 * 
	 * @param br
	 *            BidResponse. The bid response that we made earlier.
	 * @throws Exception
	 *             on Aerospike errors.
	 */
	public void recordBid(BidResponse br)  {

		Map map = new HashMap();
		map.put("ADM", br.getAdmAsString());
		map.put("PRICE", Double.toString(br.cost));
		map.put("adtype", br.adtype);
		if (br.capSpec != null) {
			map.put("SPEC", br.capSpec);
			map.put("EXPIRY", br.creat.capTimeout);
		}
		try {
			bidCachePool.hmset(br.oidStr, map, Configuration.getInstance().ttl);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			AerospikeHandler.reset();
		}

	}

	/**
	 * Return the Cap value
	 * 
	 * @param capSpec
	 *            String key for the count
	 * @return int. The Integer value of the capSpec
	 */
	public int getCapValue(String capSpec) throws Exception {
		String str = bidCachePool.get(capSpec);
		if (str == null)
			return -1;
		try {
			return Integer.parseInt(str);
		} catch (Exception error) {

		}
		return -1;
	}

	/**
	 * Remove a bid object from the cache but return it's adtype for use in creating the win record.
	 * 
	 * @param hash
	 *            String. The bid object id.
	 * @return String. The adtype.
	 */
	public String deleteBidFromCache(String hash) throws Exception {
		Map map = null;
		map = bidCachePool.hgetAll(hash);
		String adtype = null;
		if (map != null) {
			adtype = (String)map.get("adtype");
			String capSpec = (String) map.get("SPEC");
			if (capSpec != null) {
				String s = (String) map.get("EXPIRY");
				int n = Integer.parseInt(s);
				long r = bidCachePool.incr(capSpec);
				if (r == 1) {
					bidCachePool.expire(capSpec, n);
				}
			}
			bidCachePool.del(hash);
		}
		return adtype;
	}

	/**
	 * Retrieve previously recorded bid data
	 * 
	 * @param oid
	 *            String. The object id of the bid.
	 * @return Map. A map of the returned data, will be null if not found.
	 */
	public Map getBidData(String oid) throws Exception {
		return bidCachePool.hgetAll(oid);
	}

}

/**
 * A class to retrieve RTBServer commands from 0MQ.
 * 
 * 
 * @author Ben M. Faul
 *
 */
class CommandLoop implements com.xrtb.jmq.MessageListener<BasicCommand> {
	/**
	 * The thread this command loop uses to process REDIS subscription messages
	 */
	/** The configuration object */
	Configuration config = Configuration.getInstance();

	/**
	 * On a message from 0MQ, handle the command.
	 * 
	 * @param arg0
	 *            . String - the channel of this message.
	 */
	@Override
	public void onMessage(String arg0, BasicCommand item) {

		try {
			if (item.to != null && (item.to.equals("*") == false)) {
				boolean mine = Configuration.instanceName.matches(item.to);
				if (item.to.equals("") == false && !mine) {
					Controller.getInstance().logger.debug( "Controller:onMessage: {}, wasn't for me",item);
					return;
				}
			}
		} catch (Exception error) {
			try {
				Echo m = new Echo();
				m.from = Configuration.instanceName;
				m.to = item.from;
				m.id = item.id;
				m.status = "error";
				m.msg = error.toString();
				Controller.getInstance().responseQueue.add(m);
				Controller.getInstance().logger.debug( "Controller:onMessage: {}, error: {}",item,error.toString());
				return;
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

		}

		try {
			Runnable task = null;
			Thread thread;
			switch (item.cmd) {
			case Controller.GET_PRICE:
				task = () -> {
					try {
						Controller.getInstance().getPrice(item);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				};
				thread = new Thread(task);
				thread.start();
				break;
			case Controller.SET_PRICE:
				task = () -> {
					try {
						Controller.getInstance().setPrice((SetPrice)item);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				};
				thread = new Thread(task);
				thread.start();
				break;
			case Controller.ADD_CAMPAIGN:

				task = () -> {
					try {
						Controller.getInstance().addCampaign(item);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				};
				thread = new Thread(task);
				thread.start();

				break;
				
			case Controller.CONFIGURE_AWS_OBJECT:

				task = () -> {
					try {
						Controller.getInstance().configureAwsObject(item);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				};
				thread = new Thread(task);
				thread.start();

				break;
			
			case Controller.ADD_CAMPAIGNS_LIST:

				task = () -> {
					try {
						Controller.getInstance().addCampaignsList(item);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				};
				thread = new Thread(task);
				thread.start();

				break;
			
			case Controller.DEL_CAMPAIGN:
				task = () -> {
					try {
						Controller.getInstance().deleteCampaign(item);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				};
				thread = new Thread(task);
				thread.start();

				break;
			case Controller.DELETE_USER:
				task = () -> {
					try {
						Controller.getInstance().deleteUser(item);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				};
				thread = new Thread(task);
				thread.start();
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
			case Controller.DELETE_CREATIVE:
				Controller.getInstance().deleteCreative((DeleteCreative) item);
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
