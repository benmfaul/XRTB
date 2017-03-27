package com.xrtb.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.xrtb.common.Deal;
import com.xrtb.nativeads.creative.Data;
import com.xrtb.nativeads.creative.Img;
import com.xrtb.nativeads.creative.NativeVideo;
import com.xrtb.nativeads.creative.Title;

/**
 * A class that implements the Impression object.
 * @author Ben M. Faul
 *
 */
public class Impression {

	protected static List<String> keys = new ArrayList<String>();
	
	/** The compiled list of database values */
	protected static Map<String, List<String>> mapp = new HashMap<String, List<String>>();
	
	/** The bid request values are mapped into a hashmap for fast lookup by campaigns */
	public transient Map<String, Object> database = new HashMap<String, Object>();

	/** Root node of the bid request */
	transient protected JsonNode rootNode;

	/** The root node of the impression */
	transient protected JsonNode rnode;

	/** Private auction flag */
	public int privateAuction = 0;

	/** Private and preferred deals */
	public List<Deal> deals;

	/** Interstitial flag */
	public Integer instl = 0;

	/** the impression id */
	public String impid = null;

	/** A video object */
	public Video video = null;

	/** native ad extension */
	public transient NativePart nativePart;

	/** The bid floor in this bid request's impression, if present */
	public Double bidFloor;

	/** Is this a native ad bid request */
	public boolean nativead = false;

	/** the width requested */
	public Integer w;

	/** The height requested */
	public Integer h;

	/**
	 * Compiles the builtin attributes
	 */
	public static void compileBuiltIns() {
		addMap("instl");
		addMap("banner");
		addMap("banner.w");
		addMap("banner.h");
		addMap("video");
		addMap("video.w");
		addMap("video.h");
		addMap("video.mimes");
		addMap("video.protocol");
		addMap("video.minduration");
		addMap("video.maxduration");
		addMap("native.layout");
		addMap("bidfloor");
		addMap("ipmp");
	}

	/**
	 * Adds a compiled key.
	 * @param line String. Dot separated attribute
	 */
	public static void addMap(String line) {
		String[] parts = line.split("\\.");
		List<String> strings = new ArrayList<String>();
		for (int i = 0; i < parts.length; i++) {
			strings.add(parts[i]);
		}
		keys.add(line);
		mapp.put(line, strings);
	}

	/**
	 * Default constructor
	 */
	public Impression() {

	}

	/**
	 * Impression constructor from the Json/
	 * @param rootNode JsonNode. The json object of the parent object.
	 * @param rnode JsonNode. The json object of this impression.
	 * @throws Exception on Json parsing errors.
	 */
	public Impression(JsonNode rootNode, JsonNode rnode) throws Exception {
		this.rootNode = rootNode;
		this.rnode = rnode;

		JsonNode test;

		if ((test = rnode.get("pmp")) != null) {
			ObjectNode x = (ObjectNode) test;
			JsonNode y = x.path("private_auction");
			if (y != null) {
				privateAuction = y.asInt();
			}
			y = x.path("deals");
			if (privateAuction == 1) {
				if (y != null) {
					ArrayNode nodes = (ArrayNode) y;
					deals = new ArrayList<Deal>();
					for (int i = 0; i < nodes.size(); i++) {
						ObjectNode node = (ObjectNode) nodes.get(i);
						String id = node.get("id").asText();
						double price = node.get("bidfloor").asDouble(0.0);
						Deal deal = new Deal(id, price);
						deals.add(deal);
					}
				}
			}
		}

		if ((test = rnode.get("instl")) != null) {
			JsonNode x = (JsonNode) test;
			instl = x.asInt();
		}

		if ((test = rnode.get("id")) != null) {
			JsonNode x = (JsonNode) test;
			impid = x.asText();
		}

		if ((test = rnode.get("bidfloor")) != null) {
			if (test instanceof IntNode) {
				IntNode x = (IntNode) test;
				bidFloor = x.asDouble();
			} else {
				DoubleNode dd = (DoubleNode) test;
				bidFloor = dd.asDouble();
			}
		}

		if (rnode.get("banner") != null)
			doBanner();
		else if (rnode.get("video") != null)
			doVideo();
		else
			doNative();
	}

	/**
	 * Handle the banner impression.
	 */
	void doBanner() {
		JsonNode test;
		JsonNode banner = rnode.get("banner");
		test = banner.get("w");
		if (test != null) {
			w = test.intValue();
			test = banner.get("h");
			if (test != null)
				h = test.intValue();
			nativead = false;
		}
	}

	/**
	 * Handle the video impression.
	 */
	void doVideo() {
		JsonNode test;
		JsonNode rvideo = rnode.get("video");
		test = rvideo.get("w");
		if (test != null) {
			w = test.intValue();
			test = rvideo.get("h");
			h = test.intValue();
		}
		video = new Video();
		test = rvideo.get("linearity");
		if (test != null && !(test instanceof MissingNode)) {
			video.linearity = test.intValue();
		}
		test = rvideo.get("minduration");
		if (test != null && !(test instanceof MissingNode)) {
			video.minduration = test.intValue();
		}
		test = rvideo.get("maxduration");
		if (test != null && !(test instanceof MissingNode)) {
			video.maxduration = test.intValue();
		}
		test = rvideo.get("protocol");
		if (test != null && !(test instanceof MissingNode)) {
			video.protocol.add(test.intValue());
		}
		test = rvideo.get("protocols");
		if (test != null && !(test instanceof MissingNode)) {
			ArrayNode array = (ArrayNode) test;
			for (JsonNode member : array) {
				video.protocol.add(member.intValue());
			}
		}
		test = rvideo.get("mimes");
		if (test != null && !(test instanceof MissingNode)) {
			ArrayNode array = (ArrayNode) test;
			for (JsonNode member : array) {
				video.mimeTypes.add(member.textValue());
			}
		}
		nativead = false;
	}

	/**
	 * Handle the native impression
	 */
	void doNative() {
		JsonNode node = rnode.get("native");
		if (node != null) {
			JsonNode child = null;
			nativead = true;
			nativePart = new NativePart();
			child = node.path("layout");
			if (child != null) {
				nativePart.layout = child.intValue();
			}
			ArrayNode array = (ArrayNode) node.path("assets");

			for (JsonNode x : array) {
				child = x.path("title");
				if (child instanceof MissingNode == false) {
					nativePart.title = new Title();
					nativePart.title.len = child.path("len").intValue();
					if (x.path("required") instanceof MissingNode == false) {
						nativePart.title.required = x.path("required").intValue();
					}
				}
				child = x.path("img");
				if (child instanceof MissingNode == false) {
					nativePart.img = new Img();
					nativePart.img.w = child.path("w").intValue();
					nativePart.img.h = child.path("h").intValue();
					if (x.path("required") instanceof MissingNode == false) {
						nativePart.img.required = x.path("required").intValue();
					}
					if (child.path("mimes") instanceof MissingNode == false) {
						if (child.path("mimes") instanceof TextNode) {
							nativePart.img.mimes.add(child.get("mimes").asText());
						} else {
							array = (ArrayNode) child.path("mimes");
							for (JsonNode nx : array) {
								nativePart.img.mimes.add(nx.textValue());
							}
						}
					}
				}

				child = x.path("video");
				if (child instanceof MissingNode == false) {
					nativePart.video = new NativeVideo();
					nativePart.video.linearity = child.path("linearity").intValue();
					nativePart.video.minduration = child.path("minduration").intValue();
					nativePart.video.maxduration = child.path("maxduration").intValue();
					if (x.path("required") instanceof MissingNode == false) {
						nativePart.video.required = x.path("required").intValue();
					}
					if (child.path("mimes") instanceof MissingNode == false) {
						array = (ArrayNode) child.path("protocols");
						for (JsonNode nx : array) {
							nativePart.video.protocols.add(nx.textValue());
						}
					}
				}

				child = x.path("data");
				if (child instanceof MissingNode == false) {
					Data data = new Data();
					if (x.path("required") instanceof MissingNode == false) {
						data.required = x.path("required").intValue();
					}
					if (child.path("len") instanceof MissingNode == false) {
						data.len = child.path("len").intValue();
					}
					data.type = child.path("type").intValue();
					nativePart.data.add(data);
				}
			}
		}
	}

	/**
	 * Set a new bid floor
	 * 
	 * @param d
	 *            double. The new value.
	 */
	public void setBidFloor(double d) {
		/*
		 * Object test = null; ArrayNode n = (ArrayNode) rootNode.get("imp");
		 * JsonNode n1 = n.get(0); ObjectNode parent = (ObjectNode) n1;
		 * parent.put("bidfloor", d);
		 * 
		 * String key = "imp.0.bidfloor";
		 * 
		 * // recompile List list = mapp.get(key); compileList(key, list);
		 * 
		 * bidFloor = new Double(d);
		 */
	}

	/**
	 * Return a deal by its ID
	 * 
	 * @param id
	 *            String. The id of the deal
	 * @return Deal. The deal of this id
	 */
	public Deal getDeal(String id) {
		if (deals == null)
			return null;
		for (int i = 0; i < deals.size(); i++) {
			Deal d = deals.get(i);
			if (d.id.equals(id))
				return d;
		}
		return null;
	}

	public Object getNode(String what) {
		Object o = database.get(what);
		if (o == null || o instanceof MissingNode) {
			return null;
		}
		return o;
	}

	public Object interrogate(String line) {

		Object obj = database.get(line);
		if (obj == null) { // not in database, so let's query the JSON node
			String[] parts = line.split("\\.");
			List<String> list = new ArrayList<String>();
			for (int i = 0; i < parts.length; i++) {
				list.add(parts[i]);
			}
			obj = walkTree(list);

		}
		return obj;
	}

	/**
	 * Walk the Json object for a value contained from a list of attributes.
	 * @param list List. The list of attribute names. banner.w becomes "banner", "w" for example.
	 * @return Object. The Json Object of that attrbute.
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
					return null;
				;
			} else {
				node = node.get(o.charAt(0) - '0');
			}
		}
		return node;
	}
}
