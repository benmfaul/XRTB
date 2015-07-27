package com.xrtb.pojo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.devicemap.data.Device;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.DoubleNode;
import org.codehaus.jackson.node.IntNode;
import org.codehaus.jackson.node.MissingNode;
import org.codehaus.jackson.node.TextNode;

import com.xrtb.bidder.Controller;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.common.Creative;
import com.xrtb.common.Node;
import com.xrtb.geo.Solution;

public class BidRequest {

	/** The JACKSON objectmapper that will be used by the BidRequest. */
	transient static ObjectMapper mapper = new ObjectMapper();
	/** The jackson based JSON root node */
	transient JsonNode rootNode = null;

	transient Map<String, Object> database = new HashMap();

	public String exchange;
	public String id;
	public Double w;
	public Double h;
	public String siteId;
	public Double lat;
	public Double lon;
	public boolean video = false;

	/** extension for device in user agent */
	transient public Device deviceExtension;
	/** extension object for geo city, state, county, zip */
	public Solution geoExtension;

	static List<String> keys = new ArrayList();
	static Map<String, List<String>> mapp = new HashMap();

	static boolean RTB4FREE;

	public static void compile() {
		RTB4FREE = false;
		keys.clear();
		mapp.clear();
		List<Campaign> list = Configuration.getInstance().campaignsList;
		for (Campaign c : list) {
			System.out.println(c.adomain);
			for (Node node : c.attributes) {
				System.out.println(node.hierarchy);
				if (mapp.containsKey(keys) == false) {
					keys.add(node.hierarchy);
					mapp.put(node.hierarchy, node.bidRequestValues);
				}
			}
			for (Creative creative : c.creatives) {          // video creatives have attributes
				for (Node node : creative.attributes) {
					System.out.println(node.hierarchy);
					if (mapp.containsKey(keys) == false) {
						keys.add(node.hierarchy);
						mapp.put(node.hierarchy, node.bidRequestValues);
					}
				}
			}
		}
		
		addMap("site.id");
		addMap("imp.0.banner.w");
		addMap("imp.0.banner.h");
		addMap("imp.0.video.w");
		addMap("imp.0.video.h");
		/**
		 * These are needed to for device attribution and geocode
		 */
		addMap("device.geo.lat");
		addMap("device.geo.lon");
		addMap("device.ua");


	}
	
	public BidRequest() {
		
	}
	
	public BidRequest(String in) throws Exception   {
		String content = new String(Files.readAllBytes(Paths.get(in)));
		rootNode = mapper.readTree(content);
		setup();
	}

	public BidRequest(InputStream in) throws Exception ,
			IOException {
		rootNode = mapper.readTree(in);
		setup();
	}

	void setup() throws Exception {
		String item = "id";
		try {
		id = rootNode.path("id").getTextValue();
		for (String key : keys) {
			List list = mapp.get(key);
			compileList(key, list);
		}

		DoubleNode n = (DoubleNode)database.get("device.geo.lat");
		if (n != null) {
			item = "lat";
			lat = n.getDoubleValue();
			item = "lon";
			lon = ((DoubleNode)database.get("device.geo.lon")).getDoubleValue();
		}
		
		siteId = ((TextNode)getNode("site.id")).getTextValue();
		
		IntNode in = (IntNode)getNode("imp.0.banner.w");
		if (in != null) {
			item = "imp.0.banner.w";
			w = in.getDoubleValue();
			item = "imp.0.banner.h";
			h = ((IntNode)database.get("imp.0.banner.h")).getDoubleValue();
			video = false;
		} else {
			item = "imp.0.video.w";
			w = ((IntNode)getNode("imp.0.video.w")).getDoubleValue();
			item = "imp.0.video.w";
			h = ((IntNode)getNode("imp.0.video.h")).getDoubleValue();
			video = true;
		}
		handleRtb4FreeExtensions();
		} catch (Exception error) {
			Controller.getInstance().sendLog(2,"BidRequest:setup():error","missing bid request item: " + item);
			throw new Exception("Missing required bid request item: " + item);
		}
	
	}
	
	public Object getNode(String what) {
		Object o = database.get(what);
		if (o instanceof MissingNode)
			return null;
		return o;
	}
	
	void handleRtb4FreeExtensions() {
		
		/**
		 * Now deal with RTB4FREE Extensions
		 */
		TextNode text = (TextNode) database.get("device.ua");
		if (Configuration.getInstance().deviceMapper != null) {
			deviceExtension = Configuration.getInstance().deviceMapper
					.classifyDevice(text.getTextValue());
			geoExtension = Configuration.getInstance().geoTagger.getSolution(
					lat, lon);
		}
	}
	
	static void addMap(String line) {
		String[] parts = line.split("\\.");
		List<String> strings = new ArrayList();
		for (int i = 0; i < parts.length; i++) {
			strings.add(parts[i]);
		}
		keys.add(line);
		mapp.put(line,strings);
	}

	void compileList(String key, List<String> list) {

		/**
		 * Synthetic values, the geocode and the device attrribution 
		 */
		if (list.get(0).equals("rtb4free")) {
			if (list.get(1).equals("geocode")) {
				if (geoExtension == null)
					return;
				
				if (list.get(2).equals("city")) {
					database.put(key, geoExtension.city);
				}
				else
				if (list.get(2).equals("state")) {
					database.put(key, geoExtension.state);
				}
				if (list.get(2).equals("county")) {
					database.put(key, geoExtension.county);
				}
				else 
				if (list.get(2).equals("code")) {
					database.put(key, geoExtension.code);
				}
				return;
			}

			if (list.get(1).equals("device")) {
				String str = null;
				try {
					str = deviceExtension.getAttribute(list.get(2));
					Double dbl = Double.parseDouble(str);
					database.put(key,dbl);
				} catch (Exception error) {
					
				}
				return;
			}
			
		} else {
			
			/**
			 * Standard RTB here
			 */
			
			JsonNode node = (JsonNode)walkTree(list); 
			database.put(key, node);
		}
	}

	// //////////////////

	/**
	 * Interrogate an entity in the JSON using dotted format. Example:
	 * {a:{b:c:1}}to find c: "a.b.c" Example: {a:{b:[{x:1},{x:2}]} to find x[1]:
	 * "a.b.1.x"
	 * 
	 * @param line
	 *            . String. The string defining the dotted name.
	 * @return Object. Returns the object at the 'line' location or null if it
	 *         doesn't exist.
	 */
	public Object interrogate(String line) {
		Object obj = database.get(line);
		if (obj == null) { // not in database, so let's query the JSON node
			String[] parts = line.split("\\.");
			List<String> list = new ArrayList();
			for (int i = 0; i < parts.length; i++) {
				list.add(parts[i]);
			}
			obj = walkTree(list);

		}
		return obj;
	}
	
	Object walkTree(List<String> list) {
		JsonNode node = rootNode.get(list.get(0));
		if (node == null)
			return null;
		for (int i = 1; i < list.size(); i++) {
			String o = list.get(i);
			if ((o.charAt(0) >= '0' && o.charAt(0) <= '9') == false) {
				node = node.path((String) o);
				if (node == null)
					return null;;
			} else {
				node = node.get(o.charAt(0) - '0');
			}
		}
		return node;
	}

	/**
	 * Returns this object as a JSON string.
	 * 
	 * @return String. he JSON form of this class.
	 */
	public String toString() {
		if (rootNode == null)
			return null;

		return rootNode.toString();
	}

	/**
	 * Return the id request id.
	 * 
	 * @return String. The hashid of this bid request.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Parse the incoming bid request.
	 * 
	 * @return boolean. Returns true if the Nexage specific parsing of the bid
	 *         was successful.
	 */
	public boolean parse() {
		// do normal rtb
		return parseSpecial();
	}

	/**
	 * Override to do whatever special parsing tour exchange requires.
	 * 
	 * @return boolean. Returns true if it parsed ok, else false on ill-formed
	 *         JSON.
	 */
	public boolean parseSpecial() {
		return true;
	}
	
	public BidRequest copy(InputStream in)  throws Exception {
		return null;
	}

}
