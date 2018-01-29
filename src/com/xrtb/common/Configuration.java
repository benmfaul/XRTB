package com.xrtb.common;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


import com.aerospike.redisson.AerospikeHandler;
import com.aerospike.redisson.RedissonClient;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.GetObjectTaggingRequest;
import com.amazonaws.services.s3.model.GetObjectTaggingResult;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.Tag;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.xrtb.bidder.DeadmanSwitch;
import com.xrtb.bidder.RTBServer;
import com.xrtb.bidder.WebCampaign;
import com.xrtb.blocks.Bloom;
import com.xrtb.blocks.Cuckoo;
import com.xrtb.blocks.LookingGlass;
import com.xrtb.blocks.NavMap;
import com.xrtb.blocks.SimpleMultiset;
import com.xrtb.blocks.SimpleSet;
import com.xrtb.db.DataBaseObject;
import com.xrtb.db.Database;
import com.xrtb.db.User;
import com.xrtb.exchanges.adx.AdxGeoCodes;
import com.xrtb.exchanges.appnexus.Appnexus;
import com.xrtb.fraud.ForensiqClient;
import com.xrtb.fraud.FraudIF;
import com.xrtb.fraud.MMDBClient;
import com.xrtb.geo.GeoTag;
import com.xrtb.pojo.BidRequest;
import com.xrtb.tools.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The singleton class that makes up the Configuration object. A configuration
 * is a JSON file that describes the campaigns and operational parameters needed
 * by the bidding engine.
 * 
 * All classes needing config data retrieve it here.
 * 
 * @author Ben M. Faul
 *
 */

public class Configuration {

	/** Keep a sleazy map of the campaigns around for quick lookup */
	static Map <String,Map<String,String>>handyMap = new HashMap();

	/** Log all requests */
	public static final int REQUEST_STRATEGY_ALL = 0;
	/** Log only requests with bids */
	public static final int REQUEST_STRATEGY_BIDS = 1;
	/** Log only requests with wins */
	public static final int REQUEST_STRATEGY_WINS = 2;

	/** The singleton instance */
	static volatile Configuration theInstance;

	public static String ipAddress = null;

	/** Geotag extension object */
	public GeoTag geoTagger = new GeoTag();
	/** The Nashhorn shell used by the bidder */
	JJS shell;
	/**
	 * The standard HTTP port the bidder uses, note this commands from the
	 * command line -p
	 */
	public int port = 8080;
	/** The standard HTTPS port the bidder runs on, if SSL is configured */
	public int sslPort = 8081;
	/** shard key for this bidder, comes from the command line -s */
	public String shard = "";
	/** The url of this bidder */
	public String url;
	/** The log level of the bidding engine */
	public int logLevel = 4;
	/** Set to true to see why the bid response was not bid on */
	public boolean printNoBidReason = false;
	/** The campaign watchdog timer */
	public long timeout = 80;
	/** The standard name of this instance */
	public static String instanceName = "default";
	/** The exchange seat ids used in bid responses */
	public Map<String, String> seats;
	/** the configuration item defining seats and their endpoints */
	public List<Map> seatsList;
	/** The blocking files */
	public List<Map> filesList;
	/** The campaigns used to make bids */
	public List<Campaign> campaignsList = new ArrayList<Campaign>();
	/** An empty template for the exchange formatted message */
	public Map template = new HashMap();
	/** Standard pixel tracking URL */
	public String pixelTrackingUrl;
	/** Standard win URL */
	public String winUrl;
	/** The redirect URL */
	public String redirectUrl;
	/** The time to live in seconds for REDIS keys */
	public int ttl = 300;
	/** the list of initially loaded campaigns */
	public List<Map> initialLoadlist;

	/** Macros found in the templates */
	public List<String> macros = new ArrayList();
	/** The templates by by their exchange name */
	public Map<String, String> masterTemplate = new HashMap();
	/** Filename this originated from */
	public String fileName;
	/** The SSL Information, if SSL is supplied */
	public SSL ssl;
	/** The root password, passed in the Campaigns/payday.json file */
	public String password;

	// The Jedis pool, if it is used
	public MyJedisPool jedisPool;

	public static AmazonS3Client s3;
	public static String s3_bucket;

	/**
	 * HTTP admin port, usually same as bidder, but set this for a different
	 * port for admin functions
	 */
	public int adminPort = 0;
	/** Tell whether the port is supposed to be SSL or not, default is not */
	public boolean adminSSL = false;

	/** Test bid request for fraud */
	public static FraudIF forensiq;

	/**
	 * ZEROMQ LOGGING INFO
	 *getInternalAddress
	 */
	/** The channel that raw requests are written to */
	public String BIDS_CHANNEL = null;
	/** The channel that wins are written to */
	public String WINS_CHANNEL = null;
	/** The channel the bid requests are written to */
	public String REQUEST_CHANNEL = null;
	/** The channel the bid requests are written to for unilogger */
	public String UNILOGGER_CHANNEL = null;
	/** The channel clicks are written to */
	public String CLICKS_CHANNEL = null;
	/** The channel nobids are written to */
	public String NOBIDS_CHANNEL = null;
	/** The channel to output forensiq data */
	public String FORENSIQ_CHANNEL = null;
	/** The channel to send status messages */
	public String PERF_CHANNEL = null;
	/** The channel the bidder sends command responses out on */
	public static String RESPONSES = null;
	// Channel that reports reasons
	public static String REASONS_CHANNEL = null;
	/** Zeromq command port */
	public static String commandsPort;
	/** Whether to allow multiple bids per response */
	public static boolean multibid = false;

	/** Logging strategy for logs */
	public static int requstLogStrategy = REQUEST_STRATEGY_ALL;

	/** Zookeeper instance */
	public static ZkConnect zk;

	public List<String> commandAddresses = new ArrayList();

	public static final int STRATEGY_HEURISTIC = 0;
	public static final int STRATEGY_MAX_CONNECTIONS = 1;

	/** The host name where the aerospike lives */
	public String cacheHost = null;
	/** The aerospike TCP port */
	public int cachePort = 3000;
	/** Max number of aerospike connections */
	public int maxconns = 300;
	/** Pause on Startup */
	public boolean pauseOnStart = false;
	/** a copy of the config verbosity object */
	public Map verbosity;
	/** A copy of the the geotags config */
	public Map geotags;
	/** Deadman switch */
	public DeadmanSwitch deadmanSwitch;

	/** Logging object */
	static final Logger logger = LoggerFactory.getLogger(Configuration.class);

	///////////////////////////////////////////////////////////////////////
	//
	// NASHHORN BASED CORRECTIONS FROM THE TEMPLATE FOR SMAATO
	//
	// These are read by the SmaatoBidResponse, and are set
	// when the campaign is created
	//
	///////////////////////////////////////////////////////////////////////
	/**
	 * These are filled in from the templates
	 */
	@JsonIgnore
	transient public String SMAATOclickurl = "";
	@JsonIgnore
	transient public String SMAATOimageurl = "";
	@JsonIgnore
	transient public String SMAATOtooltip = "";
	@JsonIgnore
	transient public String SMAATOadditionaltext = "";
	@JsonIgnore
	transient public String SMAATOpixelurl = "";
	@JsonIgnore
	transient public String SMAATOtext = "";
	@JsonIgnore
	transient public String SMAATOscript = "";

	public RedissonClient redisson;

	/**
	 * Private constructor, class has no public constructor.
	 */
	private Configuration() throws Exception {

	}

	public static void reset() {
		theInstance = null;
	}

	/**
	 * Clear the config entries to default state,
	 */
	public void clear() {
		shard = "";
		port = 8080;
		url = null;
		logLevel = 4;
		campaignsList.clear();
	}

	public void initialize(String fileName) throws Exception {
		this.fileName = fileName;
		initialize(fileName, "", 8080, 8081);
	}

	/**
	 * Initialize the system from the JSON or Aerospike configuration file.
	 * 
	 * @param path String - The file name containing the Java Bean Shell code.
	 * @param shard Stromg. The shard name
	 * @param port int. The port the web access listens on
	 * @param sslPort  int. The port the SSL listens on.
	 * @throws Exception
	 *             on file errors.
	 */
	public void initialize(String path, String shard, int port, int sslPort) throws Exception {
		this.fileName = path;

		
		/******************************
		 * System Name
		 *****************************/
		this.shard = shard;
		this.port = port;
		this.sslPort = sslPort;

		java.net.InetAddress localMachine = null;
		String useName = null;
		try {
			localMachine = java.net.InetAddress.getLocalHost();
			ipAddress = localMachine.getHostAddress();
			useName = localMachine.getHostName();
		} catch (Exception error) {
			useName = getIpAddress();
		}

		if (shard == null || shard.length() == 0)
			instanceName = useName + ":" + port;
		else
			instanceName = shard + ":" + useName + ":" + port;

		/**
		 * Set up tem p files
		 */
		Files.createDirectories(Paths.get("www/temp")); // create the temp
														// directory in www so
														// preview campaign will
														// work

		/*********************************************
		 * USE ZOOKEEPER, AEROSPIKE OR FILE CONFIG
		 *********************************************/
		String str = null;
		if (path.startsWith("zookeeper")) {
			String parts[] = path.split(":");
			logger.info("Zookeeper: {}",""+parts);
			zk = new ZkConnect(parts[1]);
			zk.join(parts[2], "bidders", instanceName);
			str = zk.readConfig(parts[2] + "/bidders");
		} else if (path.startsWith("aerospike")) {
			String parts[] = path.split(":");
			logger.info("Zookeeper: {}",""+parts);;
			String aerospike = parts[1];
			String configKey = parts[2];
			AerospikeHandler spike = AerospikeHandler.getInstance(aerospike, 3000, 300);
			redisson = new RedissonClient(spike);
			Database.getInstance(redisson);
			str = redisson.get(configKey);
			if (str == null) {
				throw new Exception("Aerospike configuration at " + path + " not available.");
			}
			logger.info("Zookeeper: {}",str);
		} else {
			byte[] encoded = Files.readAllBytes(Paths.get(path));
			str = Charset.defaultCharset().decode(ByteBuffer.wrap(encoded)).toString();

			str = Configuration.substitute(str);
			System.out.println(str);
		}

		Map<?, ?> m = DbTools.mapper.readValue(str, Map.class);
		/*******************************************************************************/

		seats = new HashMap<String, String>();
		if (m.get("lists") != null) {
			filesList = (List) m.get("lists");
			initializeLookingGlass(filesList);
		}

		if (m.get("s3") != null) {
			Map<String, String> ms3 = (Map) m.get("s3");
			String accessKey = ms3.get("access_key_id");
			String secretAccessKey = ms3.get("secret_access_key");
			String region = ms3.get("region");
			s3_bucket = ms3.get("bucket");

			s3 = new AmazonS3Client(new BasicAWSCredentials(accessKey, secretAccessKey));
			ObjectListing listing = s3.listObjects(new ListObjectsRequest().withBucketName(s3_bucket));

			try {
				processDirectory(s3, listing, s3_bucket);
			} catch (Exception error) {
				System.err.println("ERROR IN AWS LISTING: " + error.toString());
			}
		} 
		/**
		 * SSL
		 */

		if (m.get("ssl") != null) {
			Map x = (Map) m.get("ssl");
			ssl = new SSL();
			ssl.setKeyManagerPassword = (String) x.get("setKeyManagerPassword");
			ssl.setKeyStorePassword = (String) x.get("setKeyStorePassword");
			ssl.setKeyStorePath = (String) x.get("setKeyStorePath");
		}
		/**
		 * Create the seats id map, and create the bin and win handler classes
		 * for each exchange
		 */
		seatsList = (List<Map>) m.get("seats");
		for (int i = 0; i < seatsList.size(); i++) {
			Map x = seatsList.get(i);

			String seatId = (String) x.get("id");
			String className = (String) x.get("bid");
			int k = className.indexOf("=");
			String parts[] = new String[2];
			String uri = className.substring(0, k);
			className = className.substring(k + 1);
			String[] options = null;

			/**
			 * set up any options on the class string
			 */
			if (className.contains("&")) {
				parts = className.split("&");
				className = parts[0].trim();
				options = parts[1].split(",");
				for (int ind = 0; ind < options.length; ind++) {
					options[ind] = options[ind].trim();
				}
			}

			String[] tags = uri.split("/");
			String exchange = tags[tags.length - 1];

			String name = (String) x.get("name");
			if (name == null)
				name = exchange;

			String id = (String) x.get("id");
			seats.put(name, id);

			try {
				Class<?> c = Class.forName(className);
				BidRequest br = (BidRequest) c.newInstance();
				if (br == null) {
					throw new Exception("Could not make new instance of: " + className);
				}
				Map extension = (Map) x.get("extension");
				if (x != null)
					br.handleConfigExtensions(extension);
				/**
				 * Handle generic-ized exchanges
				 */
				if (className.contains("Generic")) {
					br.setExchange(exchange);
					br.usesEncodedAdm = true;
				}

				RTBServer.exchanges.put(uri, br);

				if (parts[0] != null) {
					for (int ind = 1; ind < parts.length; ind++) {
						String option = parts[ind];
						String[] tuples = option.split("=");
						switch (tuples[0]) {
						case "usesEncodedAdm":
							br.usesEncodedAdm = true;
							break;
						case "!usesEncodedAdm":
							br.usesEncodedAdm = false;
							break;
						case "rlog":
							Double rlog = Double.parseDouble(tuples[1]);
							ExchangeLogLevel.getInstance().setExchangeLogLevel(name, rlog.intValue());
							break;
						case "useStrings":
							break;
						case "!useStrings":
							break;
						case "!usesPiggyBackWins":
							break;
						case "usesPiggyBackWins":
							BidRequest.setUsesPiggyBackWins(name);
							break;
						default:
							System.err.println("Unknown request: " + tuples[0] + " in definition of " + className);
						}
					}
				}

				/**
				 * Appnexus requires additional support for ready, pixel and
				 * click
				 */
				if (className.contains("Appnexus")) {
					RTBServer.exchanges.put(uri + "/ready", new Appnexus(Appnexus.READY));
					RTBServer.exchanges.put(uri + "/pixel", new Appnexus(Appnexus.PIXEL));
					RTBServer.exchanges.put(uri + "/click", new Appnexus(Appnexus.CLICK));
					RTBServer.exchanges.put(uri + "/delivered", new Appnexus(Appnexus.DELIVERED));
					Appnexus.seatId = seatId;
				}

			} catch (Exception error) {
				System.err.println("Error configuring exchange: " + name + ", error = ");
				throw error;
			}
		}

		/**
		 * Create forensiq
		 */
		Map fraud = (Map) m.get("fraud");
		if (fraud != null) {
			if (m.get("forensiq") != null) {
				logger.info("*** Fraud detection is set to Forensiq");
				Map f = (Map) m.get("forensiq");
				String ck = (String) f.get("ck");
				Integer x = (Integer) f.get("threshhold");
				if (!(x == 0 || ck == null || ck.equals("none"))) {
					ForensiqClient fx = ForensiqClient.build(ck);

					if (fraud.get("endpoint") != null) {
						fx.endpoint = (String) fraud.get("endpoint");
					}
					if (fraud.get("bidOnError") != null) {
						fx.bidOnError = (Boolean) fraud.get("bidOnError");
					}
					if (f.get("connections") != null)
						ForensiqClient.getInstance().connections = (int) (Integer) fraud.get("connections");
					forensiq = fx;
				}
			} else {
				logger.info("*** Fraud detection is set to MMDB");
				String db = (String) fraud.get("db");
				if (db == null) {
					throw new Exception("No fraud db specified for MMDB");
				}
				MMDBClient fy;
				try {
					fy = MMDBClient.build(db);
				} catch (Error error) {
					throw error;
				}
				if (fraud.get("bidOnError") != null) {
					fy.bidOnError = (Boolean) fraud.get("bidOnError");
				}
				if (fraud.get("watchlist") != null) {
					fy.setWatchlist((List<String>) fraud.get("watchlist"));
				}
				forensiq = fy;
			}
		} else {
			logger.info("*** NO Fraud detection");
		}

		/**
		 * Deal with the app object
		 */
		m = (Map) m.get("app");

		password = (String) m.get("password");

		if (m.get("threads") != null) {
			RTBServer.threads = (Integer) m.get("threads");
		}

		if (m.get("multibid") != null) {
			multibid = (Boolean) m.get("multibid");
		}

		if (m.get("adminPort") != null) {
			adminPort = (Integer) m.get("adminPort");
		}
		if (m.get("adminSSL") != null) {
			adminSSL = (Boolean) m.get("adminSSL");
		}

		String strategy = (String) m.get("strategy");
		if (strategy != null && strategy.equals("heuristic"))
			RTBServer.strategy = STRATEGY_HEURISTIC;
		else
			RTBServer.strategy = STRATEGY_MAX_CONNECTIONS;

		verbosity = (Map) m.get("verbosity");
		if (verbosity != null) {
			logLevel = (Integer) verbosity.get("level");
			printNoBidReason = (Boolean) verbosity.get("nobid-reason");
		}

		template = (Map) m.get("template");
		if (template == null) {
			throw new Exception("No template defined");
		}
		encodeTemplates();
		encodeTemplateStubs();

		geotags = (Map) m.get("geotags");
		if (geotags != null) {
			String states = (String) geotags.get("states");
			String codes = (String) geotags.get("zipcodes");
			geoTagger.initTags(states, codes);
		}

		Boolean bValue = false;
		bValue = (Boolean) m.get("stopped");
		if (bValue != null && bValue == true) {
			RTBServer.stopped = true;
			pauseOnStart = true;
		}

		Map redis = (Map) m.get("redis");
		if (redis != null) {
			Integer rsize = (Integer) redis.get("pool");
			if (rsize == null)
				rsize = 64;

			String host = (String) redis.get("host");
			Integer rport = (Integer) redis.get("port");
			if (rport == null)
				rport = 6379;

			// JedisPoolConfig poolConfig = new JedisPoolConfig();;
			// configJedis.setMaxTotal(rsize);
			// configJedis.setMaxWaitMillis(10);

			// poolConfig.setMaxIdle(4000);
			// Tests whether connections are dead during idle periods
			// poolConfig.setTestWhileIdle(true);
			// poolConfig.setMaxTotal(4000);
			// poolConfig.setMaxWaitMillis(30);

			// jedisPool = new JedisPool(poolConfig,host,rport);

			MyJedisPool.host = host;
			MyJedisPool.port = rport;
			jedisPool = new MyJedisPool(1000, 1000, 5);

			logger.info("*** JEDISPOOL = {}/{}/{} {}",jedisPool,host,rport,rsize);
		}

		Map zeromq = (Map) m.get("zeromq");
		
		if (zeromq == null) {
			throw new Exception("Zeromq is mot configured!");	
		}

		String value = null;
		Double dValue = 0.0;
		bValue = false;

		Map r = (Map) m.get("aerospike");
		if (r != null) {
			if ((value = (String) r.get("host")) != null)
				cacheHost = value;
			if (r.get("port") != null)
				cachePort = (Integer) r.get("port");
			if (r.get("maxconns") != null)
				maxconns = (Integer) r.get("maxconns");
			AerospikeHandler.getInstance(cacheHost, cachePort, maxconns);
			redisson = new RedissonClient(AerospikeHandler.getInstance());
			Database.getInstance(redisson);
			logger.info("*** Aerospike connection set to: {}. port: {}. connections: {}, handlers: {}",cacheHost,cachePort,maxconns,AerospikeHandler.getInstance().getCount());

			String key = (String) m.get("deadmanswitch");
			if (key != null) {
				deadmanSwitch = new DeadmanSwitch(redisson, key);
			}
		} else {
			redisson = new RedissonClient();
			Database db = Database.getInstance(redisson);
			readDatabaseIntoCache("database.json");
			readBlackListIntoCache("blacklist.json");
		}

		/**
		 * Zeromq
		 */
		if ((value = (String) zeromq.get("bidchannel")) != null)
			BIDS_CHANNEL = value;
		if ((value = (String) zeromq.get("nobidchannel")) != null)
			NOBIDS_CHANNEL = value;
		if ((value = (String) zeromq.get("winchannel")) != null)
			WINS_CHANNEL = value;
		if ((value = (String) zeromq.get("requests")) != null)
			REQUEST_CHANNEL = value;
		if ((value = (String) zeromq.get("unilogger")) != null)
			UNILOGGER_CHANNEL = value;
		if ((value = (String) zeromq.get("clicks")) != null)
			CLICKS_CHANNEL = value;
		if ((value = (String) zeromq.get("fraud")) != null)
			FORENSIQ_CHANNEL = value;
		if ((value = (String) zeromq.get("responses")) != null) 
			RESPONSES = value;		
		if ((value = (String) zeromq.get("status")) != null)
			PERF_CHANNEL = value;
		if ((value = (String) zeromq.get("reasons")) != null)
			REASONS_CHANNEL = value;

		Map xx = (Map) zeromq.get("subscribers");
		List<String> list = (List) xx.get("hosts");
		commandsPort = (String) xx.get("commands");
		for (String host : list) {
			String address = "tcp://" + host + ":" + commandsPort + "&commands";
			commandAddresses.add(address);
		}

		if (zeromq.get("requeststrategy") != null) {
			Object obj = zeromq.get("requeststrategy");
			if (obj instanceof String) {
				strategy = (String) zeromq.get("requeststrategy");
				if (strategy.equalsIgnoreCase("all") || strategy.equalsIgnoreCase("requests"))
					requstLogStrategy = REQUEST_STRATEGY_ALL;
				if (strategy.equalsIgnoreCase("bids"))
					requstLogStrategy = REQUEST_STRATEGY_BIDS;
				if (strategy.equalsIgnoreCase("WINS"))
					requstLogStrategy = REQUEST_STRATEGY_WINS;
			} else {
				if (obj instanceof Integer) {
					int level = (Integer) obj;
					ExchangeLogLevel.getInstance().setStdLevel(level);
				} else if (obj instanceof Double) {
					Double perc = (Double) obj;
					ExchangeLogLevel.getInstance().setStdLevel(perc.intValue());
				}
			}
		}
		/********************************************************************/

		campaignsList.clear();

		pixelTrackingUrl = (String) m.get("pixel-tracking-url");
		winUrl = (String) m.get("winurl");
		redirectUrl = (String) m.get("redirect-url");
		if (m.get("ttl") != null) {
			ttl = (Integer) m.get("ttl");
		}

		initialLoadlist = (List<Map>) m.get("campaigns");

		for (Map<String, String> camp : initialLoadlist) {
			if (camp.get("id") != null) {
				addCampaign(camp.get("name"), camp.get("id"));
			} else {
				logger.error("Configuration, *** ERRORS DETECTED IN INITIAL LOAD OF CAMPAIGNS *** ");
			}
		}

		if (cacheHost == null)
			logger.warn("*** NO AEROSPIKE CONFIGURED, USING CACH2K INSTEAD *** ");

		if (winUrl.contains("localhost")) {
			logger.warn("Configuration",
					"*** WIN URL IS SET TO LOCALHOST, NO REMOTE ACCESS WILL WORK FOR WINS ***");
		}
	}

	/**
	 * Substutute the macros and environment variables found in the the string.
	 * @param address String. The address being queries/
	 * @return String. All found environment vars will be substituted.
	 * @throws Exception on parsing errors.
	 */
	public static String substitute(String address) throws Exception {

		while(address.contains("$HOSTNAME"))
			address = GetEnvironmentVariable(address,"$HOSTNAME",Configuration.instanceName);
		while(address.contains("$BROKERLIST"))
			address = GetEnvironmentVariable(address,"$BROKERLIST","localhost[9092]");
		while(address.contains("$PUBSUB"))
			address = GetEnvironmentVariable(address,"$PUBSUB","localhost");

		while(address.contains("$WIN"))
			address = GetEnvironmentVariable(address,"$WIN","localhost");
		while(address.contains("$PIXEL"))
			address = GetEnvironmentVariable(address,"$PIXEL","localhost");
		while(address.contains("$VIDEO"))
			address = GetEnvironmentVariable(address,"$VIDEO","localhost");
		while(address.contains("$BID"))
			address = GetEnvironmentVariable(address,"$BID","localhost");

		while(address.contains("$IFACE-0"))
			address = GetEnvironmentVariable(address,"$IFACE-0","eth0");
		while(address.contains("$IFACE-1"))
			address = GetEnvironmentVariable(address,"$IFACE-1","eth1");
		while(address.contains("$IFACE-2"))
			address = GetEnvironmentVariable(address,"$IFACE-2","eth2");
		while(address.contains("$IFACE-3"))
			address = GetEnvironmentVariable(address,"$IFACE-3","eth3");
		while(address.contains("$IFACE-4"))
			address = GetEnvironmentVariable(address,"$IFACE-4","eth4");

		while(address.contains("$BRAND"))
			address = GetEnvironmentVariable(address,"$BRAND","RTB4FREE - JAVA Based RTB Bidder");

		while(address.contains("$IPADDRESS"))
			address = GetIpAddressFromInterface(address);

		if(address.contains("_PLAYGROUND_")) {
			String s = getPlaygroundAddress();
			address = address.replaceAll("_PLAYGROUND_:8080",s);
		}

		return address;
	}

	public static String getPlaygroundAddress() throws Exception {
		String addr = Performance.getInternalAddress("eth1");
		java.net.InetAddress localMachine = null;
		String useName = null;
		try {
			localMachine = java.net.InetAddress.getLocalHost();
			ipAddress = localMachine.getHostAddress();
			useName = localMachine.getHostName();
		} catch (Exception error) {
			useName = getIpAddress();
		}
		String theName = "ip" + addr + "-";
		theName = theName.replaceAll("\\.","-");
		theName = theName + "8080.direct.labs.play-with-docker.com";

		return theName;
	}

	/**
	 * Retrieve a variable from the environment variables
	 * @param address String. The address string to change.
	 * @param varName String. The name of the environment variable, begins with $
	 * @return String. The address string modified.
	 */
	public static String GetEnvironmentVariable(String address, String varName) {
		if (address.contains(varName)) {
			String sub = varName.substring(1);
			Map<String, String> env = System.getenv();
			if (env.get(sub) != null)
				address = address.replace(varName, env.get(sub));
		}
		return address;
	}

	/**
	 * Retrieve a variable from the environment variables, and if it exists, use that, else use the alternate.
	 * @param address String. The address string to change.
	 * @param varName String. The name of the environment variable, begins with $
	 * @param altName String. The name to use if the environment variables is not defined.
	 * @return String. The address string modified.
	 */
	public static String GetEnvironmentVariable(String address, String varName, String altName) {
		String test = GetEnvironmentVariable(address,varName);
		if (altName != null && test.equals(address))
			test = address.replace(varName, altName);
		return test;
	}

	/**
	 * Get the first IP address from a specified interface, in the form $IPADRESS#IFACE-NAME#
	 * @param address String. The address we are looking at
	 * @return String. The first occurrance of $IPADDRESS#XXX# will be substituted, if found
	 * @throws Exception on parsing errors.
	 */
	public static String GetIpAddressFromInterface(String address) throws Exception {
		int i = address.indexOf("$IPADDRESS");
		if (i<0)
			return address;

		if (address.charAt(i+10)=='#') {
			String chunk = address.substring(i+12);
			int j = chunk.indexOf("#");
			if (j < 0)
				address = address.replace("$IPADDRESS",Performance.getInternalAddress());
			else {
				String key = address.substring(i,i+13+j);
				String [] parts = key.split("#");
				address = address.replace(key,Performance.getInternalAddress(parts[1]));
			}
		} else {
			address = address.replace("$IPADDRESS",Performance.getInternalAddress());
		}
		return address;
	}

	public String requstLogStrategyAsString() {
		switch (requstLogStrategy) {
		case REQUEST_STRATEGY_ALL:
			return "all";
		case REQUEST_STRATEGY_BIDS:
			return "bids";
		case REQUEST_STRATEGY_WINS:
			return "wins";
		default:
		}
		return "all";
	}

	public void processDirectory(AmazonS3Client s3, ObjectListing listing, String bucket) throws Exception {
		for (S3ObjectSummary objectSummary : listing.getObjectSummaries()) {
			long size = objectSummary.getSize();
			logger.info("*** Processing S3 {}, size: {}",objectSummary.getKey(),size);
			S3Object object = s3.getObject(new GetObjectRequest(bucket, objectSummary.getKey()));

			String bucketName = object.getBucketName();
			String keyName = object.getKey();

			GetObjectTaggingRequest request = new GetObjectTaggingRequest(bucketName, keyName);
			GetObjectTaggingResult result = s3.getObjectTagging(request);
			List<Tag> tags = result.getTagSet();
			String type = null;
			String name = null;

			if (tags.isEmpty()) {
				System.err.println("Error: " + keyName + " has no tags");
			} else {
				for (Tag tag : tags) {
					String key = tag.getKey();
					String value = tag.getValue();

					if (key.equals("type")) {
						type = value;
					}

					if (key.equals("name")) {
						name = value;
					}
				}

				if (name == null)
					throw new Exception("Error: " + keyName + " is missing a name tag");
				if (name.contains(" "))
					throw new Exception("Error: " + keyName + " has a name attribute with a space in it");
				if (type == null)
					throw new Exception("Error: " + keyName + " has no type tag");

				if (!name.startsWith("$"))
					name = "$" + name;

				readData(type, name, object, size);
			}
		}
	}
	
	public static String readData(String fileName) throws Exception {
		String message = "";
		int i = fileName.indexOf(".");
		if (i == -1)
			throw new Exception("Filename is missing type field");
		String type = fileName.substring(i);
		NavMap map;
		SimpleMultiset set;
		SimpleSet sset;
		Bloom b;
		Cuckoo c;
		switch(type) {
		case "range":
			map = new NavMap(fileName,fileName,false);
			message = "Added NavMap " + fileName + ": from file, has " + map.size() + " members";
			break;
		case "cidr":
			map = new NavMap(fileName,fileName,true);
			message = "Added NavMap " + fileName + ": from file, has " + map.size() + " members";
			break;
		case "bloom":
			b = new Bloom(fileName,fileName);
			message = "Initialize Bloom Filter: " + fileName + " from file, members = " + b.getMembers();
			break;
		case "cuckoo":
			c = new Cuckoo(fileName,fileName);
			break;
		case "multiset":
			set = new SimpleMultiset(fileName, fileName);
			message = "Initialize Multiset " + fileName + " from file, entries = " + set.getMembers();
			break;
		case "set":
			sset = new SimpleSet(fileName, fileName);
			message = "Initialize Multiset " + fileName + " from file, entries = " + sset.size();
			break;
			
		default:
			message = "Unknown type: " + type;
		}
		logger.info("*** {}",message);
		return message;
	}

	public static String readData(String type, String name, S3Object object, long size) throws Exception {
		String message = "";
		switch (type) {
		case "range":
		case "cidr":
			NavMap map = new NavMap(name, object);
			message = "Added NavMap " + name + ": has " + map.size() + " members";
			break;
		case "set":
			SimpleSet set = new SimpleSet(name, object);
			message = "Initialize Set: " + name + " from S3, entries = " + set.size();
			break;
		case "bloom":
			Bloom b = new Bloom(name, object, size);
			message = "Initialize Bloom Filter: " + name + " from S3, members = " + b.getMembers();
			break;

		case "cuckoo":
			Cuckoo c = new Cuckoo(name, object, size);
			message = "Initialize Cuckoo Filter: " + name + " from S3, entries = " + c.getMembers();
			break;
		case "multiset":
			SimpleMultiset ms = new SimpleMultiset(name, object);
			message = "Initialize Multiset " + name + " from S3, entries = " + ms.getMembers();
			break;
		default:
			message = "Unknown type: " + type;
		}
		logger.info("*** {}",message);
		return message;
	}

	public int requstLogStrategyAsInt(String x) {
		switch (x) {
		case "all":
			return REQUEST_STRATEGY_ALL;
		case "bids":
			return REQUEST_STRATEGY_BIDS;
		case "wins":
			return REQUEST_STRATEGY_WINS;
		}
		return REQUEST_STRATEGY_ALL;
	}

	public void initializeLookingGlass(List<Map> list) throws Exception {
		for (Map m : list) {
			String fileName = (String) m.get("filename");
			String name = (String) m.get("name");
			String type = (String) m.get("type");
			if (name.startsWith("@") == false)
				name = "@" + name;
			if (type.contains("NavMap") || type.contains("RangeMap")) {
				new NavMap(name, fileName, false); // file uses ranges
			} else if (type.contains("CidrMap")) { // file uses CIDR blocks
				new NavMap(name, fileName, true);
			} else if (type.contains("AdxGeoCodes")) {
				new AdxGeoCodes(name, fileName);
			} else if (type.contains("LookingGlass")) {
				new LookingGlass(name, fileName);
			} else {
				// Ok, load it by class name
				Class cl = Class.forName(type);
				Constructor<?> cons = cl.getConstructor(String.class, String.class);
				cons.newInstance(name, fileName);
			}
			logger.info("*** Configuration Initialized {} with {}",name, fileName);
		}
	}

	/**
	 * Purpose is to test if the Cache2k system is usable with the win URL
	 * specified in the configuration file.
	 * 
	 * @throws Exception
	 *             if the Win URL is not set to this instance.
	 */
	public void testWinUrlWithCache2k() throws Exception {
		String test = null;
		if (redisson.isCache2k()) { // WIN URL MUST RESOLVE TO YOUR OWN INSTANCE
									// IF THIS IS CACHE2!
			HttpPostGet hp = new HttpPostGet();
			String[] parts = winUrl.split("/");
			test = "http://" + parts[2] + "/info";
			test = hp.sendGet(test, 5000, 5000);
			if (test == null) {
				throw new Exception("Info on " + test + " failed!");
			}
			Map m = DbTools.mapper.readValue(test, Map.class);
			test = (String) m.get("from");
			if (test.equals(instanceName) == false) {
				throw new Exception("Win URL must resolve this instance if using Cache2K!, instead it is: " + test
						+ ", expecting " + instanceName);
			}
		}
	}

	/**
	 * Used to load ./database.json into Cache2k. This is used when aerospike is
	 * not present. This instance will handle its own cache, and do its own win
	 * processing.
	 * 
	 * @param fname
	 *            String. The file name of the database.
	 * @throws Exception
	 *             on file or cache2k errors.
	 */
	private static void readDatabaseIntoCache(String fname) throws Exception {
		String content = new String(Files.readAllBytes(Paths.get(fname)), StandardCharsets.UTF_8);

		content = substitute(content);
		logger.info(content);
		Database db = Database.getInstance();

		List<User> users = DbTools.mapper.readValue(content,
				DbTools.mapper.getTypeFactory().constructCollectionType(List.class, User.class));
		for (User u : users) {
			db.addUser(u);
		}
	}

	/**
	 * Reads the blacklist into cache2k when aerospike is not being used.
	 * 
	 * @param fname
	 *            String. The name of the blacklist.
	 * @throws Exception
	 *             on file I/O or cache2k errors.
	 */
	private static void readBlackListIntoCache(String fname) throws Exception {
		String content = new String(Files.readAllBytes(Paths.get(fname)), StandardCharsets.UTF_8);
		logger.info(content);
		List<String> list = DbTools.mapper.readValue(content, List.class);
		DataBaseObject shared = DataBaseObject.getInstance();
		shared.addToBlackList(list);
	}

	/**
	 * Return the instance of Configuration, and if necessary, instantiates it
	 * first.
	 * 
	 * @param fileName
	 *            String. The name of the initialization file.
	 * @return Configuration. The instance of this singleton.
	 * @throws Exception
	 *             on JSON errors.
	 */
	public static Configuration getInstance(String fileName) throws Exception {
		if (theInstance == null) {
			synchronized (Configuration.class) {
				if (theInstance == null) {
					theInstance = new Configuration();
					theInstance.initialize(fileName);
					try {
						theInstance.shell = new JJS();
					} catch (Exception error) {

					}
				} else
					theInstance.initialize(fileName);
			}
		}
		return theInstance;
	}

	/**
	 * Get an instance of the configuration object, using the specified config
	 * file, shard name and http poty
	 * 
	 * @param fileName
	 *            String. The filename of the configuration file.
	 * @param shard
	 *            String. The shard name for this instance.
	 * @param port
	 *            int. The HTTP port byumber
	 * @return Configuration singleton.
	 * @throws Exception
	 *             on file errors and JSON errors.
	 */
	public static Configuration getInstance(String fileName, String shard, int port, int sslPort) throws Exception {
		if (theInstance == null) {
			synchronized (Configuration.class) {
				if (theInstance == null) {
					theInstance = new Configuration();
					try {
						theInstance.initialize(fileName, shard, port, sslPort);
						theInstance.shell = new JJS();
					} catch (Exception error) {
						error.printStackTrace();
					}
				} else
					theInstance.initialize(fileName);
			}
		}
		return theInstance;
	}

	/**
	 * Handle specialized encodings, like those needed for Smaato
	 */
	public void encodeTemplates() throws Exception {
		Map m = (Map) template.get("exchange");
		if (m == null)
			return;
		Set set = m.keySet();
		Iterator<String> it = set.iterator();
		while (it.hasNext()) {
			String key = it.next();
			String value = (String) m.get(key);

			MacroProcessing.findMacros(macros, value);

			if (key.equalsIgnoreCase("smaato") || key.equalsIgnoreCase("smaaato")) {
				encodeSmaato(value);
			}
		}

		MacroProcessing.findMacros(macros, "{creative_ad_width} {creative_ad_height}");
	}

	/**
	 * For each of the seats, find out which template to use
	 */
	void encodeTemplateStubs() {
		Map m = (Map) template.get("exchange");
		String defaultStr = (String) template.get("default");

		Iterator<String> sr = seats.keySet().iterator();
		while (sr.hasNext()) {
			String key = sr.next();
			String value = (String) m.get(key);
			if (value == null)
				masterTemplate.put(key, defaultStr);
			else
				masterTemplate.put(key, value);

		}

	}

	/**
	 * Encode the smaato campaign variables.
	 * 
	 * @param value
	 *            String. The string of javascript to execute.
	 * @throws Exception
	 *             on JavaScript errors.
	 */
	private void encodeSmaato(String value) throws Exception {
		NashHorn scripter = new NashHorn();
		scripter.setObject("c", this);
		String[] parts = value.split(";");

		// If it starts with { then it's not the campaign will encode smaato
		// itself
		if (value.startsWith("{"))
			return;

		for (String part : parts) {
			part = "c.SMAATO" + part.trim();
			part = part.replaceAll("''", "\"");
			scripter.execute(part);
		}
	}

	/**
	 * Return the configuration instance.
	 * 
	 * @return The instance.
	 */
	public static Configuration getInstance() {
		if (theInstance == null)
			throw new RuntimeException("Please initialize the Configuration instance first.");
		return theInstance;
	}

	/**
	 * Is the configuration object initialized.
	 * 
	 * @return boolean. Returns true of initialized, else returns false.
	 */
	public static boolean isInitialized() {
		if (theInstance == null)
			return false;
		return true;

	}

	/**
	 * Returns an input stream from the file of the given name.
	 * 
	 * @param fname
	 *            String. The fully qualified file name.
	 * @return InputStream. The stream to read from.
	 * @throws Exception
	 *             on file errors.
	 */
	public static InputStream getInputStream(String fname) throws Exception {
		File f = new File(fname);
		return new FileInputStream(f);
	}

	/**
	 * Delete a user and all the campaigns. Causes a full reload
	 * 
	 * @param owner
	 *            String. The user deleting this user, the user itself or root.
	 * @param name
	 *            String. The name of the user being deleted.
	 * @return boolean. Returns true if the user was deleted, else returns
	 *         false.
	 * @throws Exception
	 *             on database errors.
	 */
	public boolean deleteUser(String owner, String name) throws Exception {
		User u = Database.getInstance().getUser(owner);
		if (u == null)
			return false;

		Database.getInstance().deleteUser(name);
		return true;
	}

	/**
	 * This deletes a campaign from the campaignsList (the running commands)
	 * this does not delete from the database Unless it is a cache2k system.
	 * 
	 * @param owner String. The 'owner of the campaign - the user.
	 * @param name
	 *            String. The id of the campaign to delete
	 * @return boolean. Returns true if the campaign was found, else returns
	 *         false.
	 */
	public boolean deleteCampaign(String owner, String name) throws Exception {
		boolean delta = false;
		if ((owner == null || owner.length() == 0)) {
			campaignsList.clear();
			handyMap.clear();
			return true;
		}

		List<Campaign> deletions = new ArrayList();
		Iterator<Campaign> it = campaignsList.iterator();
		while (it.hasNext()) {
			Campaign c = it.next();
			if (owner.equals("root") || c.owner.equals(owner)) {
				if (name.equals("*") || c.adId.equals(name)) {
					deletions.add(c);
					delta = true;
					if (name.equals("*") == false)
						break;
				}
			}
		}

		for (Campaign c : deletions) {
			campaignsList.remove(c);
			if (redisson.isCache2k()) {
				Database db = Database.getInstance();
				db.deleteCampaign(owner, name);
			}
		}
		recompile();
		return delta;
	}

	/**
	 * This deletes a campaign's creative from the campaignsList (the running
	 * commands) this does not delete from the database unless it is a Cache2k
	 * (not Aerospike) based system.
	 *
	 * @param owner String. The owner of the campaign, associated with a user.
	 * @param name
	 *            String. The id of the campaign to delete a creative from.
	 * @param crid String. The creative id being deleted.
	 * @throws Exception
	 *             if campaign can't be found
	 */
	public void deleteCampaignCreative(String owner, String name, String crid) throws Exception {

		Iterator<Campaign> it = campaignsList.iterator();
		while (it.hasNext()) {
			Campaign c = it.next();
			if (c.owner.equals(owner) && c.adId.equals(name)) {
				for (Creative cr : c.creatives) {
					if (cr.impid.equals(crid)) {
						c.creatives.remove(cr);
						// recompile(); -> recompile on the next add or delete
						// campaign.
						if (redisson.isCache2k()) {
							Database db = Database.getInstance();
							db.editCampaign(owner, c);
						}
						return;
					}
				}
				throw new Exception("No such creative found");
			}
		}
		throw new Exception("No such campaign found");
	}

	/**
	 * Recompile the bid attributes we will parse from bid requests, based on
	 * the aggregate of all campaign bid constraints.
	 */
	public void recompile() throws Exception {
		int percentage = RTBServer.percentage.intValue(); // save the current
															// throttle
		// RTBServer.percentage = new AtomicLong(0); // throttle the bidder to 0
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		} // Wait for the working campaigns to drain
		BidRequest.compile(); // modify the Map of bid request components.
		// RTBServer.percentage = new AtomicLong(percentage); // restore the old
		// percentage
	}

	/**
	 * Add a campaign to the list of campaigns we are running. Does not add to
	 * Aerospike.
	 * 
	 * @param c
	 *            Campaign. The campaign to add into the accounting.
	 * @throws Exception
	 *             if the encoding of the attributes fails.
	 */
	public void addCampaign(Campaign c) throws Exception {
		if (c == null)
			return;

		Map<String,String> entry = new HashMap();
		handyMap.put(c.adId,entry);

		for (int i = 0; i < campaignsList.size(); i++) {
			Campaign test = campaignsList.get(i);
			if (test.adId.equals(c.adId) && test.owner.equals(c.owner)) {
				campaignsList.remove(i);
				break;
			}
		}

		for (Creative cr : c.creatives) {
			String type = "unknown";
			if (cr.isVideo())
				type = "video";
			else if (cr.isNative())
				type = "native";
			else
				type = "banner";
			entry.put(cr.impid,type);
		}

		c.encodeCreatives();
		c.encodeAttributes();
		campaignsList.add(c);

		recompile();
	}

	/**
	 * A horrible hack to find out the ad type.
	 * @param adid String. The ad id.
	 * @param crid String. The creative id.
	 * @return String. Returns the type, or, null if anything goes wrong.
	 */
	public String getAdType(String adid, String crid) {
		Map<String,String> creative =  handyMap.get(adid);;
		if (creative == null)
			return "unknown";
		return creative.get(crid);
	}

	/**
	 * Efficiently add a list of campaigns to the system
	 * 
	 * @param owner
	 *            String. The owner (user) of the campaign.
	 * @param campaigns
	 *            String[]. The array of campaign adids to load.
	 * @throws Exception
	 *             on Database errors.
	 */
	public void addCampaignsList(String owner, String[] campaigns) throws Exception {

		List<Integer> removals = new ArrayList();
		for (String adid : campaigns) {
			Campaign camp = WebCampaign.getInstance().db.getCampaign(owner, adid);
			if (camp != null) {
				for (int i = 0; i < campaignsList.size(); i++) {
					Campaign test = campaignsList.get(i);
					if (test.adId.equals(adid)) {
						campaignsList.remove(i);
						break;
					}
				}

				camp.encodeCreatives();
				camp.encodeAttributes();
				campaignsList.add(camp);
			} else {
				logger.warn("ERROR: no such camaign: {}",adid);
			}
		}
		recompile();
	}

	/**
	 * Is the identified campaign running?
	 * 
	 * @param owner
	 *            String. The campaign owner
	 * @param name
	 *            String. The campaign adid.
	 * @return boolean. Rewturns true if it is loaded, else false.
	 */
	public boolean isRunning(String owner, String name) {
		for (Campaign c : campaignsList) {
			if (c.owner.equals(owner) && c.adId.equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a list of all the campaigns that are running
	 * 
	 * @return List. The list of campaigns, byadIds, that are running.
	 */
	public List<String> getLoadedCampaignNames() {
		List<String> list = new ArrayList();
		for (Campaign c : campaignsList) {
			list.add(c.adId);
		}
		return list;
	}

	/**
	 * Add a campaign to the campaigns list using the shared map database of
	 * campaigns
	 * 
	 * @param owner
	 *            String. The owner/user of the campaign.
	 * @param name String. The name of the campaign.
	 * @throws Exception
	 *             if the addition of this campaign fails.
	 */
	public void addCampaign(String owner, String name) throws Exception {
		List<Campaign> list = Database.getInstance().getCampaigns(owner);
		if (list == null) {
			logger.error("Requested load of campaigns failed because this user does not exist: {}",owner);
		} else {
			for (Campaign c : list) {
				if (c.adId.matches(name)) {
					deleteCampaign(owner, name);
					addCampaign(c);
					logger.info(
							"Loaded  {}/{}",name + "/" + c.adId);
				}
			}
		}
	}

	/**
	 * Return your IP address by posting to api.externalip.net
	 * 
	 * @return String. The IP address of this instance.
	 */
	public static String getIpAddress() {
		URL myIP;
		try {
			myIP = new URL("http://api.externalip.net/ip/");

			BufferedReader in = new BufferedReader(new InputStreamReader(myIP.openStream()));
			return in.readLine();
		} catch (Exception e) {
			try {
				myIP = new URL("http://myip.dnsomatic.com/");

				BufferedReader in = new BufferedReader(new InputStreamReader(myIP.openStream()));
				return in.readLine();
			} catch (Exception e1) {
				try {
					myIP = new URL("http://icanhazip.com/");

					BufferedReader in = new BufferedReader(new InputStreamReader(myIP.openStream()));
					return in.readLine();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}

		return null;
	}
}
