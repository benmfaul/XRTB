package com.xrtb.pojo;

import java.io.ByteArrayOutputStream;

import java.io.InputStream;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.xrtb.bidder.Controller;
import com.xrtb.bidder.RTBServer;
import com.xrtb.bidder.SelectedCreative;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.common.Creative;
import com.xrtb.common.ForensiqLog;
import com.xrtb.common.Node;
import com.xrtb.common.URIEncoder;

import com.xrtb.geo.Solution;
import com.xrtb.tools.HexDump;

/**
 * Implements the OpenRTB 2.3 bid request object.
 * 
 * @author Ben M. Faul
 *
 */
public class BidRequest {

	private static ExchangeCounts ec = new ExchangeCounts();

	transient protected static final JsonNodeFactory factory = JsonNodeFactory.instance;

	/** The JACKSON objectmapper that will be used by the BidRequest. */
	protected transient ObjectMapper mapper = new ObjectMapper();

	/** The jackson based JSON root node */
	transient protected JsonNode rootNode = null;
	/** Indicates this bid request's response uses an encoded adm field */
	transient public boolean usesEncodedAdm = true;
	/** The bid request values are mapped into a hashmap for fast lookup by campaigns */
	public transient Map<String, Object> database = new HashMap();

	/** The exchange this request came from */
	private String exchange;
	/** the bid request id */
	public String id;

	/** the bid request site id */
	public String siteId;
	/** the bid request site domain */
	public String siteDomain;
	/** the site name */
	public String siteName;
	/** the latitude of the request */
	public Double lat;
	/** the longitude of the request */
	public Double lon;
	/** Is this a video bid request? */

	/** Forensiq fraud record */
	public ForensiqLog fraudRecord;

	// The impressions objects;
	protected List<Impression> impressions;

	/** extension object for geo city, state, county, zip */
	public Solution geoExtension;
	/**
	 * These are the keys found in the union of all campaigns (ie bid request
	 * items that have constraints
	 */
	protected static List<String> keys = new ArrayList();
	/** The compiled list of database values */
	protected static Map<String, List<String>> mapp = new HashMap();
	/**
	 * Indicates there is an ext.rrtb4free object present in the bid request,
	 * used by our own private exchange
	 */
	static boolean RTB4FREE;

	transient public boolean blackListed = false;

	transient public static Set<String> blackList;

	/**
	 * Was the forensiq score too high? Will be false if forensiq is not used
	 */
	public boolean isFraud = false;

	/** The pageurl of the request */
	public String pageurl = "";
	// The type field, used in logging
	public String type = "requests";

	/**
	 * Take the union of all campaign attributes and place them into the static
	 * mapp. This way the JSON is queried once and the query becomes the key,
	 * and the JSON value becomes the map value. With multiple campaigns it is
	 * important to not be traversing the JSON tree for each campaign.
	 * 
	 * The compiled attributes are stored in mapp. In setup, the compiled list
	 * of key/values is then put in the 'database' object for the bidrequest.
	 */
	public synchronized static void compile() throws Exception {
		RTB4FREE = false;

		/**
		 * Stop the bidder, if it is running
		 */
		stopBidder();

		keys.clear();
		mapp.clear();
		List<Campaign> list = Configuration.getInstance().campaignsList;
		for (int i = 0; i < list.size(); i++) {
			Campaign c = list.get(i);
			Controller.getInstance().sendLog(5, "BidRequest:compile", ("Compiling for domain: : " + c.adomain));
			for (int j = 0; j < c.attributes.size(); j++) {
				Node node = c.attributes.get(j);
				if (mapp.containsKey(keys) == false) {
					Controller.getInstance().sendLog(5, "BidRequest:compile",
							("Compile unit: " + c.adomain + ":" + node.hierarchy) + ", values: "
									+ node.bidRequestValues);

					if (node.hierarchy.equals("") == false) {
						keys.add(node.hierarchy);
						mapp.put(node.hierarchy, node.bidRequestValues);
					} else {
						if (node.operator != Node.OR) {
							startBidder();
							throw new Exception("Malformed OR processing in campaign " + c.adId);
						}
						List<Node> nodes = (List<Node>) node.value;
						for (int nc = 0; nc < nodes.size(); nc++) {
							Object x = nodes.get(nc);
							Node n = null;
							if (x instanceof LinkedHashMap) {
								Map map = (Map) x;
								n = new Node(map);

							} else
								n = (Node) x;

							n.setValues();
							if (mapp.get(n.hierarchy) == null) {
								keys.add(n.hierarchy);
								mapp.put(n.hierarchy, n.bidRequestValues);
							}
						}
					}
				}
			}
			for (Creative creative : c.creatives) { // Handle creative specific
													// attributes
				Controller.getInstance().sendLog(5, "BidRequest:compile",
						"Compiling creatives for: " + c.adomain + ":" + creative.impid);

				for (Node node : creative.attributes) {

					if (mapp.containsKey(keys) == false) {

						Controller.getInstance().sendLog(5, "BidRequest:compile",
								("Compile unit: " + c.adomain + ":" + creative.impid + ":" + node.hierarchy)
										+ ", values: " + node.bidRequestValues);

						if (mapp.get(node.hierarchy) == null) {
							keys.add(node.hierarchy);
							mapp.put(node.hierarchy, node.bidRequestValues);
						}
					}
				}

				// Now frequency caps */
				if (creative.capSpecification != null) {
					String spec = creative.capSpecification;
					if (mapp.containsKey(spec) == false) {
						addMap(spec);
					}
				}
			}
		}

		compileBuiltIns();

		/**
		 * Restart the bidder
		 */
		startBidder();
	}

	private static boolean needsRestart = false;
	private static boolean compilerBusy = false;

	public static boolean compilerBusy() {
		return compilerBusy;
	}

	private static void stopBidder() {
		compilerBusy = true;

		if (RTBServer.stopped)
			return;

		needsRestart = true;
		RTBServer.stopped = true;
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		;
	}

	private static void startBidder() {
		compilerBusy = false;
		if (needsRestart)
			RTBServer.stopped = false;
	}

	public static void compileBuiltIns() {
		addMap("site.id");
		addMap("site.domain");
		addMap("site.name");
		addMap("site.page");
		addMap("site.content.url");

		addMap("app.id");
		addMap("app.domain");
		addMap("app.name");
		addMap("app.content.url");

		addMap("imp.0.id");
		addMap("imp.0.instl");
		addMap("imp.0.banner");
		addMap("imp.0.banner.w");
		addMap("imp.0.banner.h");
		addMap("imp.0.video");
		addMap("imp.0.video.w");
		addMap("imp.0.video.h");
		addMap("imp.0.video.mimes");
		addMap("imp.0.video.protocol");
		addMap("imp.0.video.minduration");
		addMap("imp.0.video.maxduration");
		addMap("imp.0.native.layout");
		addMap("imp.0.bidfloor");
		addMap("imp.0.pmp");
		/**
		 * These are needed to for device attribution and geocode
		 */
		addMap("device.geo.lat");
		addMap("device.geo.lon");
		addMap("device.ua");
		addMap("device.geo.country");
	}

	/**
	 * Default constructor
	 */
	public BidRequest() {

	}

	/**
	 * Create a bid request from a file .
	 * 
	 * @param in
	 *            String. The name of the file to read.
	 * @throws Exception
	 *             on file and json processing errors.
	 */
	public BidRequest(String in) throws Exception {
		String content = new String(Files.readAllBytes(Paths.get(in)));
		rootNode = mapper.readTree(content);
		setup();
	}

	public BidRequest(StringBuilder sb) throws Exception {
		rootNode = mapper.readTree(sb.toString());
		setup();
	}

	/**
	 * Create a bid from an input stream.
	 * 
	 * @param in
	 *            InputStream. The stream to read the JSON from
	 * @throws Exception
	 *             on stream and JSON processing errors.
	 */
	public BidRequest(InputStream in) throws Exception {
		rootNode = mapper.readTree(in);
		setup();
	} 

	public BidRequest(InputStream in, String exchange)  {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		String text = null;
		try {
			int nRead;
			byte[] data = new byte[4096];

			while ((nRead = in.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}

			buffer.flush();
			text = buffer.toString("UTF-8");
			rootNode = mapper.readTree(text);
			setup();
		} catch (Exception error) {
			byte [] bytes =  buffer.toByteArray();
			System.err.println("Error: Bad data from Exchange: " + exchange + ", : " + text);
			HexDump.dumpHexData(System.err, "Hex Dump Follows", bytes, bytes.length);
			blackListed = true;
			id = "";
		}

	}

	/**
	 * Return a bid response of the appropriate type, normally it is a simple
	 * BidResponse, but for non openRTB you may need to use a different
	 * response.
	 * 
	 * @param camp
	 *            Campagign. The campaign used to create the response.
	 * @param creat
	 *            Creative. The creative used to make the response.
	 * @param xtime
	 *            int. The time it took to process the bid request
	 * @return BidResponse. The actual bidResaponse object
	 * @throws Exception
	 *             on JSON parsing errors.
	 */
	public BidResponse buildNewBidResponse(Impression imp, Campaign camp, Creative creat, double price, String dealId, int xtime)
			throws Exception {
		return new BidResponse(this, imp, camp, creat, id, price, dealId, xtime);
	}
	
	public BidResponse buildNewBidResponse(Impression imp, List<SelectedCreative> list,  int xtime)
			throws Exception {
		return new BidResponse(this, imp,list, xtime);
	}

	/**
	 * Return's the bid response no bid JSON or other (protoc in Adx for
	 * example).
	 * 
	 * @param reason
	 *            String. The reason you are returning no bid.
	 * @return String. The reason code.
	 */
	public static String returnNoBid(String reason) {
		return reason;
	}

	/**
	 * Return the no bid code. Note, for Adx. you have to return 200
	 * 
	 * @return
	 */
	public int returnNoBidCode() {
		return RTBServer.NOBID_CODE;
	}

	public void writeNoBid(HttpServletResponse response, long time) throws Exception {
		response.setStatus(RTBServer.NOBID_CODE);
	}

	/**
	 * Return the application type this bid request/response uses
	 * 
	 * @return String. The content type to return.
	 */
	public String returnContentType() {
		return "application/json;charset=utf-8";
	}

	/**
	 * Sets up the database of values of the JSON, from the mapped keys in the
	 * campaigns. THis traverses the JSON once, and stores the required values
	 * needed by campaigns once.
	 * 
	 * @throws Exception
	 *             on JSON processing errors.
	 */
	protected void setup() throws Exception {
		id = rootNode.path("id").textValue();
		if (id == null) {
			throw new Exception("Required field 'id' is missing or wrong type");
		}

		IntNode in = null;
		Object test = null;
		StringBuilder item = new StringBuilder("id"); // a fast way to keep up
														// with required fields
														// Im looking for
		try {
			for (int i = 0; i < keys.size(); i++) {
				String key = keys.get(i);
				List list = mapp.get(key);
				if (list.size() != 0)
					compileList(key, list);
			}

			// ////////////////////////////////////////////////////////////////////
			if ((test = getNode("site.id")) != null)
				siteId = ((TextNode) test).textValue();
			else {
				test = getNode("app.id");
				if (test != null) {
					siteId = ((TextNode) test).textValue();
				}
			}

			if ((test = getNode("site.domain")) != null)
				siteDomain = ((TextNode) test).textValue();
			else {
				test = getNode("app.domain");
				if (test != null) {
					siteDomain = ((TextNode) test).textValue();
				}
			}

			// ///////////////
			if (siteDomain != null && blackList != null) {
				if (blackList.contains(siteDomain)) {
					blackListed = true;
					return;
				}
			}

			if ((test = getNode("site.name")) != null)
				siteName = ((TextNode) test).textValue();
			else {
				test = getNode("app.name");
				if (test != null) {
					siteName = ((TextNode) test).textValue();
				}
			}

			////////////////// Fill in pageurl info ////////////////
			if ((test = getNode("site.content.url")) != null) {
				pageurl = ((TextNode) test).textValue();
			} else if ((test = getNode("site.page")) != null) {
				pageurl = ((TextNode) test).textValue();
			} else {
				test = getNode("app.content.url");
				if (test != null) {
					pageurl = ((TextNode) test).textValue();
				}
			}
			///////////////////////////////////////////////////////

			// ////////////////////////////////////////////////////

			if ((test = database.get("device.geo.lat")) != null && test instanceof MissingNode == false) {

				try {
					lat = getDoubleFrom(test);
					test = database.get("device.geo.lon");
					if (test != null)
						lon = getDoubleFrom(test);
				} catch (Exception error) {

				}

			}

			//////////////////////////////////////////////////////////////////
			//
			// Handle the impressions
			//
			ArrayNode imps = (ArrayNode)rootNode.get("imp");
			impressions = new ArrayList();
			for (int i=0;i<imps.size();i++) {
				JsonNode obj = imps.get(i);
				Impression imp = new Impression(rootNode,obj);
				impressions.add(imp);
			}
			
			handleRtb4FreeExtensions();
		} catch (Exception error) { // This is an error in the protocol
			error.printStackTrace();
			if (Configuration.isInitialized() == false)
				return;
			// error.printStackTrace();
			// String str = rootNode.toString();
			// System.out.println(str);
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			error.printStackTrace(pw);
			String str = sw.toString();
			String lines[] = str.split("\n");
			StringBuilder sb = new StringBuilder();
			sb.append("Abmormal processing of bid ");
			sb.append(id);
			sb.append(", ");
			if (lines.length > 0)
				sb.append(lines[0]);
			if (lines.length > 1)
				sb.append(lines[1]);
			Controller.getInstance().sendLog(4, "BidRequest:setup():error", sb.toString());
		}

	}

	/**
	 * Return a double, whether it's integer or not.
	 * 
	 * @param o
	 *            Obhect. The json object.
	 * @return double. Returns the value as a double.
	 * @throws Exception
	 *             if the object is not a number.
	 */
	public static double getDoubleFrom(Object o) throws Exception {
		double x = 0;
		if (o instanceof DoubleNode) {
			DoubleNode dn = (DoubleNode) o;
			x = dn.doubleValue();
		} else if (o instanceof MissingNode) {
			throw new Exception("Missing value from: " + o.toString());
		} else {
			IntNode dn = (IntNode) o;
			x = dn.doubleValue();
		}
		return x;
	}

	/**
	 * Given a JSON bject, return it's string representation.
	 * 
	 * @param o
	 *            Object. The object to interpret.
	 * @return
	 * @throws Exception
	 */
	public static String getStringFrom(Object o)  {
		if (o == null)
			return null;
		JsonNode js = (JsonNode) o;
		return js.asText();
	}

	public boolean forensiqPassed() throws Exception {

		if (Configuration.forensiq == null) {
			return true;
		}

		Object node = null;
		String ip = null, ua = null, url = null, seller;
		ip = findValue(this, "device.ip");
		ua = findValue(this, "device.ua");
		url = findValue(this, "site.page");
		seller = findValue(this, "site.name");
		if (seller == null)
			seller = siteDomain;

		if (ua != null)
			ua = URIEncoder.myUri(ua);
		if (url != null)
			url = URIEncoder.myUri(url);

		try {
			fraudRecord = Configuration.forensiq.bid("display", ip, url, ua, seller, "xxx");
		} catch (Exception e) {
			if (Configuration.forensiq.bidOnError)
				return true;
			throw e;
		}
		if (fraudRecord == null)
			return true;
		fraudRecord.id = id;
		fraudRecord.domain = siteDomain;
		fraudRecord.exchange = exchange;
		return false;
	}

	String findValue(BidRequest br, String what) {
		Object node = null;
		if ((node = br.interrogate(what)) != null) {
			if (node instanceof MissingNode == false) {
				JsonNode n = (JsonNode) node;
				return n.asText();
			}
		}
		return null;
	}

	/**
	 * Given a key, return the value of the bid request of that key that is now
	 * stored in the database.
	 * 
	 * @param what
	 *            String. The key to use for the retrieval.
	 * @return Object. The value of that key.
	 */
	public Object getNode(String what) {
		Object o = database.get(what);
		if (o == null || o instanceof MissingNode) {
			return null;
		}
		return o;
	}

	/**
	 * Handle any rtb4free extensions - like the specialized geo
	 */
	void handleRtb4FreeExtensions() {
		/**
		 * Now deal with RTB4FREE Extensions
		 */
		if (lat == null || lon == null)
			return;

		if (database.get("device.ua") instanceof MissingNode == false) {
			if (Configuration.getInstance().geoTagger != null)
				geoExtension = Configuration.getInstance().geoTagger.getSolution(lat, lon);
		}
	}

	/**
	 * Add a constraint key to the mapp.
	 * 
	 * @param line
	 *            String. The Javascript notation of the constraint.
	 */
	public static void addMap(String line) {
		String[] parts = line.split("\\.");
		List<String> strings = new ArrayList();
		for (int i = 0; i < parts.length; i++) {
			strings.add(parts[i]);
		}
		keys.add(line);
		mapp.put(line, strings);
	}

	/**
	 * Compile the JSON values into the database from the list of constraint
	 * keys. This is what queries the JSON and places it into the database
	 * object.
	 * 
	 * @param key
	 *            String. The key name.
	 * @param list
	 *            List. The constraint keys (eg device.geo.lat becomes
	 *            ['device','geo','lat']).
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
				} else if (list.get(2).equals("state")) {
					database.put(key, geoExtension.state);
				}
				if (list.get(2).equals("county")) {
					database.put(key, geoExtension.county);
				} else if (list.get(2).equals("code")) {
					database.put(key, geoExtension.code);
				}
				return;
			}

		} else {

			/**
			 * Standard RTB here
			 */

			JsonNode node = (JsonNode) walkTree(list);
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

		if (line.equals("exchange"))
			return exchange;

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
	 * Walk the JSON tree using the list. The list contains the object names.
	 * Foe example, device.geo.lat is stored in the list as
	 * ['device','geo','lat']. The JSON tree's device node is found, then in
	 * device, the geo node is found, and then the 'lat' node is then found in
	 * geo.
	 * 
	 * @param list
	 *            String. The list of JSON node names.
	 * @return Object. The object found at 'x.y.z'
	 */
	
	Object walkTree(List<String> list) {
		JsonNode node = rootNode.get(list.get(0));
		if (node == null)
			return null;
		
		if (list.contains("tagid")) {
			System.out.println("Here");;
		}
		for (int i = 1; i < list.size(); i++) {
			String o = list.get(i);
			if (!(o.charAt(0) >= '0' && o.charAt(0) <= '9') && o.charAt(0) != '*') {
				node = node.path((String) o);
				if (node == null)
					return null;
				;
			} else {
				if (o.charAt(0) == '*') {
					ArrayList values = new ArrayList();
					ArrayNode nodes = (ArrayNode)node;
					for (int count = 0; count < nodes.size(); count++) {
						JsonNode subnode = nodes.get(count);
						for (int k = i + 1; k < list.size(); k++) {
							String key = list.get(k);
							subnode = subnode.path(key);
						}
						values.add(subnode.textValue());
					}
					return values;
				} else
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
	 * Override this to create a copy of the BidRequest that derives from thos
	 * class.
	 * 
	 * @param in
	 *            InputStream. The stream containing the JSON of the request.
	 * @return BidRequest. The new object
	 * @throws Exception
	 *             on JSON or InputStream processing errors.
	 */
	public BidRequest copy(InputStream in) throws Exception {
		throw new Exception("copy constructor for Exchange handler is not implemented");
	}

	/**
	 * Returns the asset id in the bid request of the requested index
	 * 
	 * @param what
	 *            String. The name of the link we are looking for
	 * @return int. Returns the index in the asset object. If not found, returns
	 *         -1
	 */
	public int getNativeAdAssetIndex(String type, String subtype, int value) {
		JsonNode nat = rootNode.path("imp");
		if (nat == null || nat.isArray() == false)
			return -1;
		ArrayNode array = (ArrayNode) nat;
		JsonNode node = array.get(0).path("native").path("assets");
		ArrayNode nodes = (ArrayNode) node;
		for (int i = 0; i < nodes.size(); i++) {
			JsonNode asset = nodes.get(i);
			JsonNode n = asset.path(type);
			JsonNode id = asset.path("id");
			if (n instanceof MissingNode == false) {
				if (subtype != null) {
					n = n.path(subtype);
					if (n != null) {
						if (n.intValue() == value)
							return id.intValue();
					}
				} else {
					return id.intValue();
				}
			}
		}
		return -1;
	}

	/**
	 * Return the original root node, useful for dumping to string for later
	 * examination.
	 * 
	 * @return JsonNode. The original root node of the request.
	 */
	public JsonNode getOriginal() {
		return rootNode;
	}



	public boolean checkNonStandard(Creative creat, StringBuilder sb) {
		return true;
	}
	
	/**
	 * Set the exchange field.
	 * @param exchange String. The name of the exchange
	 */
	public void setExchange(String exchange) {
		this.exchange = exchange;
	}
	
	/**
	 * Get the exchange name
	 * @return String. The name of the exchange for this request.
	 */
	public String getExchange() {
		return exchange;
	}
	
	/**
	 * Add an impression.
	 * @param imp Impression. The impression to add.
	 */
	public void addImpression(Impression imp) {
		impressions.add(imp);
	}
	
	/**
	 * Get the number of impressions from the request.
	 * @return int. The number of impressions found.
	 */
	public int getImpressions() {
		if (impressions == null)
			return 0;
		return impressions.size();
	}
	
	/**
	 * Return the nth impression.
	 * @param n int. The impression to return.
	 * @return
	 */
	public Impression getImpression(int n) {
		if (impressions == null || impressions.size() == 0)
			return null;
		return impressions.get(n);
	}

	/**
	 * Handle any specific configurations, used by child classes (Exchange).
	 * 
	 * @param m
	 *            Map. The extensions map,
	 * @throws Exception
	 *             on parsing errors.
	 */
	public void handleConfigExtensions(Map m)  {

	}
	
	/**
	 * Override this method to indicate this is not a bid request. Like AppNexus and their hokey /ready flag.
	 * @return boolean Return true of this isn't a bid request.
	 */
	public boolean notABidRequest() {
		return false;
	}
	
	/**
	 * Override this method to return the code the non bid request return is supposed to be.
	 * @return
	 */
	public int getNonBidReturnCode() {
		return 200;
	}
	
	/**
	 * Override this method to return the data response the non bid request return is supposed to be.
	 * @return
	 */
	public String getNonBidRespose() {
		return "";
	}

	public static void incrementWins(String exchange) {
		if (exchange == null)
			return;
		ec.incrementWins(exchange);
	}

	public void incrementBids() {
		if (exchange == null)
			return;
		ec.incrementBid(exchange);
	}

	public void incrementRequests() {
		if (exchange == null)
			return;
		ec.incrementRequest(exchange);
	}

	public void incrementErrors() {
		if (exchange == null)
			return;
		ec.incrementError(exchange);
	}

	public static List<Map> getExchangeCounts() {
		return ec.getList();
	}
}
