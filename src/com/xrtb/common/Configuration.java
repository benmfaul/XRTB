package com.xrtb.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
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
import java.util.concurrent.atomic.AtomicLong;

import org.apache.devicemap.DeviceMapClient;
import org.apache.devicemap.DeviceMapFactory;
import org.apache.devicemap.loader.LoaderOption;
import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.RedissonClient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;
import com.xrtb.bidder.Controller;
import com.xrtb.bidder.DeadmanSwitch;
import com.xrtb.bidder.RTBServer;
import com.xrtb.bidder.WebCampaign;
import com.xrtb.commands.BasicCommand;
import com.xrtb.commands.LogMessage;
import com.xrtb.geo.GeoTag;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.Forensiq;
import com.xrtb.pojo.ForensiqClient;
import com.xrtb.tools.NashHorn;

/**
 * The singleton class that makes up the Configuration object. A configuration is a JSON file that describes the campaigns and operational
 * parameters needed by the bidding engine.
 * 
 * All classes needing config data retrieve it here.
 * @author Ben M. Faul
 *
 */

public class Configuration {
	/** JSON parser for decoding configuration parameters */
	static Gson gson = new Gson();
	/** The singleton instance */
	static volatile Configuration theInstance;
	
	/** The Apache DeviceMapper object */
	public DeviceMapClient deviceMapper = DeviceMapFactory.getClient(LoaderOption.JAR);
	/** Geotag extension object */
	public GeoTag geoTagger = new GeoTag();
	/** The Nashhorn shell used by the bidder */
	JJS shell;
	/** The standard HTTP port the bidder uses, note this commands from the command line -p */
	public int port = 8080;
	/** shard key for this bidder, comes from the command line -s */
	public String shard = "";
	/** The url of this bidder */
	public String url;
	/** The log level of the bidding engine  */
	public int logLevel = 4;
	/** Set to true to see why the bid response was not bid on */
	public boolean printNoBidReason = false;
	/** The campaign watchdog timer */
	public long timeout = 80;                 
	/** The standard name of this instance */
	public String instanceName = "default";
	/** The exchange seat ids used in bid responses*/
	public Map<String,String> seats;
	/** the configuration item defining seats and their endpoints */
	public List<Map> seatsList;
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
	
	public static String password;
	
	/** Test bid request for fraud */
	public static ForensiqClient forensiq;
	
	/**
	 * REDIS LOGGING INFO
	 *
	 */
	/** The channel that raw requests are written to */
	public String BIDS_CHANNEL = null;
	/** The channel that wins are written to */
	public String WINS_CHANNEL = null;
	/** The channel the bid requests are written to */
	public String REQUEST_CHANNEL = null;
	/** The channel where log messages are written to */
	public String LOG_CHANNEL = null;
	/** The channel clicks are written to */
	public String CLICKS_CHANNEL = null;
	/** The channel nobids are written to */
	public String NOBIDS_CHANNEL = null;
	/** The channel to output forenasiq data */
	public String FORENSIQ_CHANNEL = null;
	
	public static final int STRATEGY_HEURISTIC 			= 0;
	public static final int STRATEGY_MAX_CONNECTIONS 	= 1;

	/** The host name where the REDIS lives */
	public static String cacheHost = "localhost";
	/** The REDIS TCP port */
	public static int cachePort = 6379;
	/** Pause on Startup */
	public static boolean pauseOnStart = false;
	/** a copy of the config verbosity object */
	public static Map verbosity;
	/** A copy of the the geotags config */
	public static Map geotags;
	/** Deadman switch */
	public static DeadmanSwitch deadmanSwitch;
	
	
	
	///////////////////////////////////////////////////////////////////////
	//
	//     NASHHORN BASED CORRECTIONS FROM THE TEMPLATE FOR SMAATO
	//
	//		These are read by the SmaatoBidResponse, and are set
	//      when the campaign is created
	//
	///////////////////////////////////////////////////////////////////////
	/**
	 * These are filled in from the templates
	 */
	@JsonIgnore
	transient public String SMAATOclickurl="";
	@JsonIgnore
	transient public String SMAATOimageurl="";
	@JsonIgnore
	transient public String SMAATOtooltip="";
	@JsonIgnore
	transient public String SMAATOadditionaltext="";
	@JsonIgnore
	transient public String SMAATOpixelurl=""; 
	@JsonIgnore
	transient public String SMAATOtext="";
	@JsonIgnore
	transient public String SMAATOscript="";
	
    
	/**
	 * Redisson shared memory over redis
	 * 
	 */
	/** Redisson configuration object */
	public Config redissonConfig = new Config();
	/** Redisson object */
	public RedissonClient redisson;
   
	/**
	 * Private constructor, class has no public constructor.
	 * @param fileName String. The filename of the configuration data.
	 */
	private Configuration() throws Exception {

	}

	public static void reset() {
		theInstance = null;
	}
	
	public static String setPassword()  {
		try {
			password = new String(Files.readAllBytes(Paths.get(".passwords")),StandardCharsets.UTF_8);
			password = password.replaceAll("\n","");
		} catch (Exception error) {
			password = null;
		}
		return password;
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
		setPassword();
		initialize(fileName,"",8080);
	}
	/**
	 * Read the Java Bean Shell file that initializes this constructor.
	 * @param path. String - The file name containing the Java Bean Shell code.
	 * @throws Exception on file errors.
	 */
	public void initialize(String path, String shard, int port) throws Exception {
		setPassword();
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		String str = Charset.defaultCharset().decode(ByteBuffer.wrap(encoded)).toString();
		
		Map<?, ?> m = gson.fromJson(str,Map.class);

		seats = new HashMap<String, String>();
		
		this.shard = shard;
		this.port = port;
		
		/**
		 * Create the seats id map, and create the bin and win handler classes for each exchange
		 */
		seatsList = (List<Map>)m.get("seats");
		for (int i=0;i<seatsList.size();i++) {
			Map x = seatsList.get(i);
			String name = (String)x.get("name");
			String id = (String)x.get("id");
			seats.put(name,id);
			
			String className = (String)x.get("bid");
			String parts [] = className.split("=");
			String uri = parts[0];
			className = parts[1];
			Class<?> c = Class.forName(className);
			BidRequest br = (BidRequest)c.newInstance();
			RTBServer.exchanges.put(uri,br);
		}
		
		if (m.get("forensiq") != null) {
			Map f = (Map)m.get("forensiq");
			String ck = null;
			if (f.get("ck") != null) {
				ck = (String)f.get("ck");
			}
			
			forensiq = ForensiqClient.build(ck);
			if (f.get("threshhold") != null) {
				Double x = (Double)f.get("threshhold");
				forensiq.threshhold = x.intValue();
			}
			if (f.get("endpoint") != null) {
				forensiq.endpoint = (String)f.get("endpoint");
			}
			if (f.get("bidOnError") != null) {
				forensiq.bidOnError = (Boolean)f.get("bidOnError");
			}
			if (f.get("connections") != null) {
				ForensiqClient.getInstance().connections = (Integer)f.get("connections");
			}
		}
		
		
		m = (Map)m.get("app");
		
		if (m.get("threads")!=null) {
			RTBServer.threads = (Integer)m.get("threads");
		}
	
		String strategy = (String)m.get("strategy");
		if (strategy != null && strategy.equals("heuristic"))
			RTBServer.strategy = STRATEGY_HEURISTIC;
		else
			RTBServer.strategy = STRATEGY_MAX_CONNECTIONS;
		
		verbosity = (Map)m.get("verbosity");
		if (verbosity != null) {
			logLevel = ((Double)verbosity.get("level")).intValue();
			printNoBidReason = (Boolean)verbosity.get("nobid-reason");
		}
		
		template = (Map)m.get("template");
		if (template == null) {
			throw new Exception("No template defined");
		}
		encodeTemplates();
		
		geotags = (Map)m.get("geotags");
		if (geotags != null) {
			String states = (String)geotags.get("states");
			String codes = (String)geotags.get("zipcodes");
			geoTagger.initTags(states,codes);
		}
		
		Boolean bValue = false;
		bValue = (Boolean)m.get("stopped");
		if (bValue != null && bValue == true) {
			RTBServer.stopped = true;
			pauseOnStart = true;
		}
		
		String value = null;
		Double dValue = 0.0;
		bValue = false;
		Map r = (Map)m.get("redis");
		if ((value=(String)r.get("host")) != null)
			cacheHost = value;
		if ((value=(String)r.get("bidchannel")) != null)
			BIDS_CHANNEL = value;
		if ((value=(String)r.get("nobidchannel")) != null)
			NOBIDS_CHANNEL = value;
		if ((value=(String)r.get("winchannel")) != null)
			WINS_CHANNEL = value;
		if ((value=(String)r.get("requests")) != null)
			REQUEST_CHANNEL = value;
		if ((value=(String)r.get("logger")) != null)
			LOG_CHANNEL = value;
		if ((value=(String)r.get("clicks")) != null)
			CLICKS_CHANNEL = value;
		if ((value=(String)r.get("forensiq")) != null)
			FORENSIQ_CHANNEL = value;
		if ((dValue=(Double)r.get("port")) != null)
			cachePort = dValue.intValue();

		if (password != null) {
			redissonConfig.useSingleServer()
        		.setAddress(cacheHost+":"+((int)cachePort))
        		.setPassword(password)
        		.setConnectionPoolSize(128);
		} else {
			redissonConfig.useSingleServer()
    		.setAddress(cacheHost+":"+((int)cachePort))
    		.setConnectionPoolSize(128);
		}
		redisson = Redisson.create(redissonConfig);
		
		String key = (String)m.get("deadmanswitch");
		if (key != null) {
			deadmanSwitch = new DeadmanSwitch(Configuration.cacheHost,
					Configuration.cachePort, key);
		}
		
		campaignsList.clear();
		
			
		pixelTrackingUrl = (String)m.get("pixel-tracking-url");
		winUrl = (String)m.get("winurl");
		redirectUrl = (String)m.get("redirect-url");
		if (m.get("ttl") != null) {
			Double d = (Double)m.get("ttl");
			ttl = d.intValue();
		}
	
		String useName = null;
		java.net.InetAddress localMachine = null;
		try {
			localMachine = java.net.InetAddress.getLocalHost();
			useName = localMachine.getHostName();
		} catch (Exception error) {
			useName = getIpAddress();
		}
		if (shard == null || shard.length()==0)
			instanceName = useName + ":" + port;
		else
			instanceName = shard + ":" + useName + ":" + port;
		
		initialLoadlist = (List<Map>)m.get("campaigns");
		
		for (Map<String,String> camp :initialLoadlist) {		
			addCampaign(camp.get("name"),camp.get("id"));
		}
	}
	
	/**
	 * TODO: Needs work.
	 */
	@Override
	public String toString() {
		for (Field f : getClass().getDeclaredFields()) {

		    System.out.println(f);
		}
		return "na";
	}

	/**
	 * Return the instance of Configuration, and if necessary, instantiates it first.
	 * @param fileName String. The name of the initialization file.
	 * @return Configuration. The instance of this singleton.
	 * @throws Exception on JSON errors.
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
	
	public static Configuration getInstance(String fileName, String shard, int port) throws Exception {
		if (theInstance == null) {
			synchronized (Configuration.class) {
				if (theInstance == null) {
					theInstance = new Configuration();
					theInstance.initialize(fileName, shard, port);
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
	 * Handle specialized encodings, like those needed for Smaato
	 */
	public void encodeTemplates() throws Exception {
		Map m = (Map)template.get("exchange");
		if (m == null)
			return;
		Set set= m.keySet();
		Iterator<String> it = set.iterator();
		while(it.hasNext()) {
			String key = it.next();
			String value = (String)m.get(key);
			if (key.equalsIgnoreCase("smaato")) {
				encodeSmaato(value);			
			}
		}
	}
	

	
	/**
	 * Encode the smaato campaign variables.
	 * @param value String. The string of javascript to execute.
	 * @throws Exception on JavaScript errors.
	 */
	private void encodeSmaato(String value) throws Exception {
			NashHorn scripter = new NashHorn();
			scripter.setObject("c", this);
			String [] parts = value.split(";");
			for (String part : parts) {
				part =  "c.SMAATO" + part.trim();
				part = part.replaceAll("''", "\"");
				scripter.execute(part);
			}
	}
	
	
	/**
	 * Return the configuration instance.
	 * @return The instance.
	 */
	public static Configuration getInstance()  {
		if (theInstance == null)
			throw new RuntimeException("Please initialize the Configuration instance first.");
		return theInstance;
	}
	
	public static boolean isInitialized() {
		if (theInstance == null)
			return false;
		return true;
		
	}
	
	/**
	 * Returns an input stream from the file of the given name.
	 * @param fname String. The fully qualified file name.
	 * @return InputStream. The stream to read from.
	 * @throws Exception on file errors.
	 */
	public static InputStream getInputStream(String fname) throws Exception {
		File f = new File(fname);
		FileInputStream fis = new FileInputStream(f);
		return fis;
	}
	
	/**
	 * This deletes a campaign from the campaignsList (the running commands) this does not delete from the database
	 * @param id String. The id of the campaign to delete
	 * @return boolean. Returns true if the campaign was found, else returns false.
	 */
	public boolean deleteCampaign(String owner, String name) throws Exception {
		boolean delta = false;
		if ((owner == null || owner.length()==0)) {
			campaignsList.clear();
			return true;
		}
		
		List<Campaign> deletions = new ArrayList();
		Iterator<Campaign> it = campaignsList.iterator();
		while(it.hasNext()) {
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
		}
		recompile();
		return delta;
	}
	
	/**
	 * This deletes a campaign's creative from the campaignsList (the running commands) this does not delete from the database
	 * @param id String. The id of the campaign to delete
	 * @throws Exception if campaign can't be found
	 */
	public void deleteCampaignCreative(String owner, String name, String crid) throws Exception {
		
		
		Iterator<Campaign> it = campaignsList.iterator();
		while(it.hasNext()) {
			Campaign c = it.next();
			if (c.owner.equals(owner) && c.adId.equals(name)) {       
				for (Creative cr : c.creatives) {
					if (cr.impid.equals(crid))  {
						c.creatives.remove(cr);
						// recompile();    -> recompile on the next add or delete campaign.
						return;
					}
				}	
				throw new Exception("No such creative found");
			}
		}
		throw new Exception("No such campaign found");
	}
	
	/**
	 * Recompile the bid attributes we will parse from bid requests, based on the aggregate of all
	 * campaign bid constraints.
	 */
	public void recompile() throws Exception  {
		int percentage = RTBServer.percentage.intValue();		// save the current throttle
	//	RTBServer.percentage = new AtomicLong(0);					// throttle the bidder to 0
		try { Thread.sleep(1000); } catch (InterruptedException e) {}	// Wait for the working campaigns to drain
		BidRequest.compile();						// modify the Map of bid request components.
	//	RTBServer.percentage = new AtomicLong(percentage);			// restore the old percentage
	}
	
	/**
	 * Add a campaign to the list of campaigns we are running. Does not add to REDIS.
	 * @param c Campaign. The campaign to add into the accounting.
	 * @throws Exception if the encoding of the attributes fails.
	 */
	public void addCampaign(Campaign c) throws Exception  {
		if (c == null)
			return;
		
		for (int i=0;i<campaignsList.size();i++) {
			Campaign test = campaignsList.get(i);
			if (test.adId.equals(c.adId) && test.owner.equals(c.owner)) {
				campaignsList.remove(i);
				break;
			}
		}
		
		c.encodeCreatives();
		c.encodeAttributes();
		campaignsList.add(c);
		
		recompile();
	}
	
	/** 
	 * Is the identified campaign running?
	 * @param owner String. The campaign owner
	 * @param name String. The campaign adid.
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
	 * Add a campaign to the campaigns list using the shared map database of campaigns
	 * @param campId String. The campaign id of what to add.
	 * @throws Exception if the addition of this campaign fails.
	 */
	public void addCampaign(String owner, String name) throws Exception  {
		List<Campaign> list = WebCampaign.getInstance().db.getCampaigns(owner);
		if (list == null) {
			Controller.getInstance().sendLog(1, "initialization:campaign","Requested load of campaigns failed because this user does not exist: " + owner);
		}
		else {
		for (Campaign c : list) {
			if (c.adId.matches(name)) {
				deleteCampaign(owner, name);
				addCampaign(c);
				Controller.getInstance().sendLog(1, "initialization:campaign","Loaded  User/Campaign " + name + "/" + c.adId);
			}
		}
		}
	}
	
	public static String getIpAddress() 
	{ 
	        URL myIP;
	        try {
	            myIP = new URL("http://api.externalip.net/ip/");

	            BufferedReader in = new BufferedReader(
	                    new InputStreamReader(myIP.openStream())
	                    );
	            return in.readLine();
	        } catch (Exception e) 
	        {
	            try 
	            {
	                myIP = new URL("http://myip.dnsomatic.com/");

	                BufferedReader in = new BufferedReader(
	                        new InputStreamReader(myIP.openStream())
	                        );
	                return in.readLine();
	            } catch (Exception e1) 
	            {
	                try {
	                    myIP = new URL("http://icanhazip.com/");

	                    BufferedReader in = new BufferedReader(
	                            new InputStreamReader(myIP.openStream())
	                            );
	                    return in.readLine();
	                } catch (Exception e2) {
	                    e2.printStackTrace(); 
	                }
	            }
	        }

	    return null;
	}
}
