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
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.DoubleNode;
import org.codehaus.jackson.node.IntNode;
import org.codehaus.jackson.node.MissingNode;
import org.codehaus.jackson.node.TextNode;

import com.xrtb.bidder.Controller;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.common.Creative;
import com.xrtb.common.Node;
import com.xrtb.db.Database;
import com.xrtb.geo.Solution;

public class BidRequest {

	/** The JACKSON objectmapper that will be used by the BidRequest. */
	transient static ObjectMapper mapper = new ObjectMapper();
	/** The jackson based JSON root node */
	transient JsonNode rootNode = null;
	/** The bid request values are mapped into a hashmap for fast lookup by campaigns */
	transient Map<String, Object> database = new HashMap();

	/** The exchange this request came from */
	public String exchange;
	/** the bid request id */
	public String id;
	/** the width requested */
	public Double w;
	/** The height requested */
	public Double h;
	/** the bid request site id */
	public String siteId;
	/** the latitude of the request */
	public Double lat;
	/** the longitude of the request */
	public Double lon;
	/** Is this a video bid request? */
	public boolean video = false;
	/** Is this a native ad bid request */
	public boolean nativead = false;

	/** extension for device in user agent */
	transient public Device deviceExtension;
	/** extension object for geo city, state, county, zip */
	public Solution geoExtension;
	/** These are the keys found in the union of all campaigns (ie bid request items that have constraints */
	static List<String> keys = new ArrayList();
	/** The compiled list of database values */
	static Map<String, List<String>> mapp = new HashMap();
	/** Indicates there is an ext.rrtb4free object present in the bid request, used by our own private exchange */
	static boolean RTB4FREE;

	/**
	 * Take the union of all campaign attributes and place them into the static mapp. This way the JSON
	 * is queried once and the query becomes the key, and the JSON value becomes the map value. With
	 * multiple campaigns it is important to not be traversing the JSON tree for each campaign.
	 * 
	 * The compiled attributes are stored in mapp. In setup, the compiled list of key/values
	 * is then put in the 'database' object for the bidrequest.
	 */
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
		addMap("imp.0.native.layout");
		/**
		 * These are needed to for device attribution and geocode
		 */
		addMap("device.geo.lat");
		addMap("device.geo.lon");
		addMap("device.ua");


	}
	
	/**
	 * Default constructor
	 */
	public BidRequest() {
		
	}
	
	/**
	 * Create a bid request from a file .
	 * @param in String. The name of the file to read.
	 * @throws Exception on file and json processing errors.
	 */
	public BidRequest(String in) throws Exception   {
		String content = new String(Files.readAllBytes(Paths.get(in)));
		rootNode = mapper.readTree(content);
		setup();
	}

	/**
	 * Create a bid from an input stream.
	 * @param in InputStream. The stream to read the JSON from
	 * @throws Exception on stream and JSON processing errors.
	 */
	public BidRequest(InputStream in) throws Exception {
		rootNode = mapper.readTree(in);
		setup();
	}

	/**
	 * Sets up the database of values of the JSON, from the mapped keys in the campaigns.
	 * THis traverses the JSON once, and stores the required values needed by campaigns once.
	 * @throws Exception on JSON processing errors.
	 */
	void setup() throws Exception {
		StringBuilder item = new StringBuilder("id");  // a fast way to keep up with required fields Im looking for 
		try {
		id = rootNode.path("id").getTextValue();
		for (String key : keys) {
			List list = mapp.get(key);
			compileList(key, list);
		}

		DoubleNode n = (DoubleNode)database.get("device.geo.lat");
		if (n != null) {
			item.setLength(0); item.append("lat");
			lat = n.getDoubleValue();
			item.setLength(0); item.append("lon");
			lon = ((DoubleNode)database.get("device.geo.lon")).getDoubleValue();
		}
		
		siteId = ((TextNode)getNode("site.id")).getTextValue();
		
		IntNode in = (IntNode)getNode("imp.0.banner.w");
		if (in != null) {
			item.setLength(0); item.append("imp.0.banner.w");
			w = in.getDoubleValue();
			item.setLength(0); item.append("imp.0.banner.h");
			h = ((IntNode)database.get("imp.0.banner.h")).getDoubleValue();
			video = false;
			nativead = false;
		} else {
			in = (IntNode)getNode("imp.0.video.w");
			if (in != null) {
				item.setLength(0); item.append("imp.0.video.w");
				w = ((IntNode)getNode("imp.0.video.w")).getDoubleValue();
				item.setLength(0); item.append("imp.0.banner.h");
				h = ((IntNode)getNode("imp.0.video.h")).getDoubleValue();
				video = true;
				nativead = false;
			} else {
				item = null;
				if (getNode("imp.0.native.layout") != null) {
					nativead = true;
				} else {
					String str = rootNode.toString();
					Map m = (Map)Database.gson.fromJson(str, Map.class);
					System.err.println(Database.gson.toJson(m));
					Controller.getInstance().sendLog(2,"BidRequest:setup():error","Unknown bid type" + rootNode.toString());
					throw new Exception("Unknown bid request");
				}
			}
		}
		handleRtb4FreeExtensions();
		} catch (Exception error) {
			Controller.getInstance().sendLog(2,"BidRequest:setup():error","missing bid request item: " + item.toString());
			throw new Exception("Missing required bid request item: " + item.toString());
		}
	
	}
	
	/**
	 * Given a key, return the value of the bid request of that key that is now stored in the database.
	 * @param what String. The key to use for the retrieval.
	 * @return Object. The value of that key.
	 */
	public Object getNode(String what) {
		Object o = database.get(what);
		if (o instanceof MissingNode)
			return null;
		return o;
	}
	
	/**
	 * Handle any rtb4free extensions - like the specialized geo
	 */
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
	
	/**
	 * Add a constraint key to the mapp.
	 * @param line String. The Javascript notation of the constraint.
	 */
	static void addMap(String line) {
		String[] parts = line.split("\\.");
		List<String> strings = new ArrayList();
		for (int i = 0; i < parts.length; i++) {
			strings.add(parts[i]);
		}
		keys.add(line);
		mapp.put(line,strings);
	}

	/**
	 * Compile the JSON values into the database from the  list of constraint keys. This is what queries
	 * the JSON and places it into the database object.
	 * @param key String. The key name. 
	 * @param list List. The constraint keys (eg device.geo.lat becomes ['device','geo','lat']).
	 */
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
	
	/**
	 * Walk the JSON tree using the list. The list contains the object names. Foe example, device.geo.lat is 
	 * stored in the list as ['device','geo','lat']. The JSON tree's device node is found, then in device, the
	 * geo node is found, and then the 'lat' node is then found in geo.
	 * @param list String. The list of JSON node names.
	 * @return Object. The object found at 'x.y.z'
	 */
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
	
	/**
	 * Override this to create a copy of the BidRequest that derives from thos class.
	 * @param in InputStream. The stream containing the JSON of the request.
	 * @return BidRequest. The new object
	 * @throws Exception on JSON or InputStream processing errors.
	 */
	public BidRequest copy(InputStream in)  throws Exception {
		return null;
	}
	
	/**
	 * Returns the asset id in the bid request of the requested index
	 * @param what String. The name of the link we are looking for
	 * @return int. Returns the index in the asset object. If not found, returns -1
	 */
	public int getNativeAdAssetIndex(String type,String subtype, int value) {
		JsonNode nat = rootNode.path("native");
		if (nat == null)
			return -1;
		JsonNode node = nat.path("assets");
		if (node.isArray() == false)
			return -1;
		ArrayNode nodes = (ArrayNode)node;
		for (int i=0;i<nodes.size();i++) {
			 JsonNode n = nodes.get(i);
			 n = n.path(type);
			 if (n != null) {
				 if (subtype != null) {
					 n = n.path(subtype);
					 if (n != null) {
						 if (n.getIntValue() == value)
							 return i;
					 }
				 } else {
					 return i;
				 }
			 }
		}
		return -1;
	}
}
