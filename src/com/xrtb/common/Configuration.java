package com.xrtb.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.xrtb.bidder.RTBServer;
import com.xrtb.geo.GeoNode;
import com.xrtb.geo.GeoTag;
import com.xrtb.pojo.BidRequest;

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
	
	/** The Nashhorn shell used by the bidder */
	JJS shell;
	/** The standard HTTP port the bidder uses */
	public int port = 8080;
	/** The url of this bidder */
	public String url;
	/** The log level of the bidding engine */
	public int logLevel = 4;
	/** Set to true to see why the bid response was not bid on */
	public boolean printNoBidReason = false;
	/** The campaign watchdog timer */
	public long timeout = 80;                 
	/** The standard name of this instance */
	public String instanceName = "default";
	/** The exchange seat ids */
	public Map<String,String> seats;
	/** The campaigns used to make bids */
	public List<Campaign> campaignsList = new ArrayList<Campaign>();
	
	/** Standard pixel tracking URL */
	public String pixelTrackingUrl;
	/** Standard win URL */
	public String winUrl;
	/** The redirect URL */
	public String redirectUrl;
	/** The time to live in seconds for REDIS keys */
	public int ttl = 300;
	/** Max number of connections the bidder will support, if exceeded, will NO bid */
	public int maxConnections = 100;
	
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

	/** The host name where the REDIS lives */
	public static String cacheHost = "localhost";
	/** The REDIS TCP port */
	public static int cachePort = 6379;
    
	/**
	 * Private constructor, class has no public constructor.
	 */
	private Configuration() {

	}
	
	/**
	 * Clear the config entries to default state,
	 */
	public void clear() {
		port = 8080;
		url = null;
		logLevel = 4;
		campaignsList.clear();
	}
	
	/**
	 * Read the Java Bean Shell file that initializes this constructor.
	 * @param path. String - The file name containing the Java Bean Shell code.
	 * @throws Exception on file errors.
	 */
	public void initialize(String path) throws Exception {
		
		GeoTag z = new GeoTag();
		z.initTags("data/zip_codes_states.csv",
					"data/unique_geo_zipcodes.txt");
		GeoNode.tag = z;
		
		
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		String str = Charset.defaultCharset().decode(ByteBuffer.wrap(encoded)).toString();
		
		Map<?, ?> m = gson.fromJson(str,Map.class);
		instanceName = (String)m.get("instance");
		seats = new HashMap<String, String>();
		
		/**
		 * Create the seats id map, and create the bin and win handler classes for each exchange
		 */
		List<Map> seatsList = (List<Map>)m.get("seats");
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
		
		m = (Map)m.get("app");
		Map verbosity = (Map)m.get("verbosity");
		if (verbosity != null) {
			logLevel = ((Double)verbosity.get("level")).intValue();
			printNoBidReason = (Boolean)verbosity.get("nobid-reason");
		}
		
		campaignsList.clear();
		
		List<Map> list = (List<Map>)m.get("campaigns");
		if (list != null) {
			for (Map  x : list) {
				String ss = gson.toJson(x);
				addCampaign(ss);
			}
		}
		
		
		String value = null;
		Double dValue = 0.0;
		Boolean bValue = false;
		Map r = (Map)m.get("redis");
		if ((value=(String)r.get("host")) != null)
			cacheHost = value;
		if ((value=(String)r.get("bidchannel")) != null)
			BIDS_CHANNEL = value;
		if ((value=(String)r.get("winchannel")) != null)
			WINS_CHANNEL = value;
		if ((value=(String)r.get("requests")) != null)
			REQUEST_CHANNEL = value;
		if ((value=(String)r.get("logger")) != null)
			LOG_CHANNEL = value;
		if ((value=(String)r.get("clicks")) != null)
			CLICKS_CHANNEL = value;
		if ((dValue=(Double)r.get("port")) != null)
			cachePort = dValue.intValue();

			
		pixelTrackingUrl = (String)m.get("pixel-tracking-url");
		winUrl = (String)m.get("winurl");
		redirectUrl = (String)m.get("redirect-url");
		if (m.get("ttl") != null) {
			Double d = (Double)m.get("ttl");
			ttl = d.intValue();
		}
		if (m.get("connections") != null) {
			Double d = (Double)m.get("connections");
			maxConnections = d.intValue();
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
	 * @return Configuration. The instance of this singleton.
	 */
	public static Configuration getInstance() {
		if (theInstance == null) {
			synchronized (Configuration.class) {
				if (theInstance == null) {
					theInstance = new Configuration();
					try {
						theInstance.shell = new JJS();
					} catch (Exception error) {
						
					}
				}
			}
		}
		return theInstance;
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
	 * 
	 * @param id String. The id of the campaign to delete
	 * @return boolean. Returns true if the campaign was found, else returns false.
	 */
	public boolean deleteCampaign(String id) {
		Iterator<Campaign> it = campaignsList.iterator();
		while(it.hasNext()) {
			Campaign c = it.next();
			if (c.adId.equals(id)) {
				campaignsList.remove(c);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Add a campaign to the list of campaigns we are running.
	 * @param c Campaign. The campaign to add into the accounting.
	 * @throws Exception if the encoding of the attributes fails.
	 */
	public void addCampaign(Campaign c) throws Exception  {
		c.encodeCreatives();
		c.encodeAttributes();
		campaignsList.add(c);
	}
	
	
	/**
	 * Add a campaign to the campaigns list using the String representation of the JSON.
	 * @param json String. The JSON of the campaign.
	 * @throws Exception if the addition of this campaign fails.
	 */
	public void addCampaign(String json) throws Exception  {
		Campaign camp = gson.fromJson(json, Campaign.class);
		addCampaign(camp);
	}
}
