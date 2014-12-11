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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.gson.Gson;

/**
 * The singleton class that makes up the Configuration object. A configuration is a JSON file that describes the campaigns and operational
 * parameters needed by the bidding engine.
 * 
 * All classes needing config data retrieve it here.
 * @author Ben M. Faul
 *
 */

public class Configuration {
	static Configuration theInstance;
	
	JJS shell;
	public List<String> exchanges = new ArrayList();
	public int port = 8080;
	public String url;
	public int logLevel = 4;
	public long timeout = 80;                     // campaign selector in ms
	public static String instanceName = "default";
	public Map<String,String> seats;
	public Set<Campaign> campaigns = new TreeSet();
	public List<Campaign> campaignsList = new ArrayList();
	
	public String pixelTrackingUrl;
	public String winUrl;
	public String redirectUrl;
	
	public static LoggerIF winLogger;
	public static LoggerIF requestLogger;
	public static LoggerIF bidLogger;
	
	/**
	 * Private constructor, class has no public constructor.
	 */
	private Configuration() {

	}
	
	/**
	 * Clear the config entries to default state,
	 */
	public void clear() {
		exchanges.clear();
		port = 8080;
		url = null;
		logLevel = 4;
		campaigns.clear();
	}
	
	/**
	 * Read the Java Bean Shell file that initializes this constructor.
	 * @param path. String - The file name containing the Java Bean Shell code.
	 * @throws Exception. Throws errors on I/O errors, or JAVA runtime errors initializing the object.
	 */
	public void initialize(String path) throws Exception {
		Gson gson = new Gson();
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		String str = Charset.defaultCharset().decode(ByteBuffer.wrap(encoded)).toString();
		
		Map m = gson.fromJson(str,Map.class);
		instanceName = (String)m.get("instance");
		seats = (Map)m.get("seats");
		
		m = (Map)m.get("app");
		List<Map> list = (List)m.get("campaigns");
		for (Map x : list) {
			Campaign c = new Campaign(x);
			campaigns.add(c);
			campaignsList.add(c);
		}
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
	 * @return
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
	 * @throws Exception. Throws exceptions if can't open file, or it doesn't exist.
	 */
	public static InputStream getInputStream(String fname) throws Exception {
		File f = new File(fname);
		FileInputStream fis = new FileInputStream(f);
		return fis;
	}
	
}
