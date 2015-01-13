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
	static Gson gson = new Gson();
	static Configuration theInstance;
	
	JJS shell;
	public int port = 8080;
	public String url;
	public static int logLevel = 4;
	public static boolean printNoBidReason = false;
	public long timeout = 80;                     // campaign selector in ms
	public static String instanceName = "default";
	public Map<String,String> seats;
	public List<Campaign> campaignsList = new ArrayList<Campaign>();
	
	public String pixelTrackingUrl;
	public String winUrl;
	public String redirectUrl;
	
	/**
	 * REDIS LOGGING INFO
	 */
	public static String BIDS_CHANNEL = null;
	public static String WINS_CHANNEL = null;
	public static String REQUEST_CHANNEL = null;
	public static String LOG_CHANNEL = null;
	public static String CLICKS_CHANNEL = null;

	public static String cacheHost = "localhost";
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
		for (Map  x : list) {
			String ss = gson.toJson(x);
			addCampaign(ss);
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
