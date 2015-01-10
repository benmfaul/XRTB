package com.xrtb.pojo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.DoubleNode;
import org.codehaus.jackson.node.IntNode;
import org.codehaus.jackson.node.TextNode;

/**
 * A class that encapsulates an RTB2 bid request. Only standard RTB conversions
 * are done in this class. Exchanges can introduce their own JSON into the bid
 * request, so all the special parsing is done in a class that extends the
 * BidRequest. Note, the bid request data items are stored in a JACKSON object.
 * <p>
 * Some basic parts of the request like w, h, lat, lon, exchange, and site-id of the
 * request are stored as fields. 
 * <p>
 * For other data items in the bid request, use the interrogate() method of the base class 
 * is used to retrieve the object values from the class.
 */
public class BidRequest {
	/** The RTB bid hash */
	public String id;
	/** The latitude of the mobile user */
	public Double lat;
	/** The longitude of the mobile user */
	public Double lon;
	/** The width of the requested ad */
	public Double w;
	/* The height of the requested ad */
	public Double h;
	/** The name of the exchange that transmitted this request */
	public String exchange;
	/** The id of the site that is requesting the bid */
	public String siteId;

	/** The JACKSON objectmapper that will be used by the BidRequest. */
	static ObjectMapper mapper = new ObjectMapper();
	/** The jackson based JSON root node */
	JsonNode rootNode = null;

	/** A device.geo.lat string array used for interrogating that part of the object */
	static List<String> deviceGeoLat = new ArrayList();
	/** A device.geo.lon string array used for interrogating that part of the object */
	static List<String> deviceGeoLon = new ArrayList();
	/** A site.id string array used for interrogating that part of the object */
	static List<String> idSite = new ArrayList();
	/** A imp.0.banner.h string array used for interrogating that part of the object */
	static List<String> impH = new ArrayList();
	/** A imp.0.banner.w string array used for interrogating that part of the object */
	static List<String> impW = new ArrayList();
	static {
		deviceGeoLat.add("device");
		deviceGeoLat.add("geo");
		deviceGeoLat.add("lat");

		deviceGeoLon.add("device");
		deviceGeoLon.add("geo");
		deviceGeoLon.add("lon");

		idSite.add("site");
		idSite.add("id");

		impH.add("imp");
		impH.add("0");
		impH.add("banner");
		impH.add("h");

		impW.add("imp");
		impW.add("0");
		impW.add("banner");
		impW.add("w");
	}

	/**
	 * Default constructor, useful for testing.
	 */
	public BidRequest() {

	}

	/**
	 * Creates a bid request from a file, useful for debugging.
	 * 
	 * @param in
	 *            String. The name of the file to read.
	 * @throws JsonProcessingException
	 *             on parse errors.
	 * @throws IOException
	 *             on file reading errors
	 */
	public BidRequest(String in) throws JsonProcessingException, IOException {
		String content = new String(Files.readAllBytes(Paths.get(in)));
		rootNode = mapper.readTree(content);
		setup();
	}

	/**
	 * Setup the bid request after receiving the input.
	 */
	private void setup() {
		JsonNode node = rootNode.path("id");
		id = node.getTextValue();
		Object test = interrogate(deviceGeoLat);

		if (test != null) {
			DoubleNode n = (DoubleNode) test;
			lat = n.getDoubleValue();
		}
		test = interrogate(deviceGeoLon);
		if (test != null) {
			DoubleNode n = (DoubleNode) test;
			lon = n.getDoubleValue();
		}
		test = interrogate(idSite);
		if (test != null) {
			TextNode n = (TextNode) test;
			siteId = n.getTextValue();
		}
		DoubleNode n = null;
		test = interrogate(impW);
		if (test != null) {
			if (test instanceof IntNode) {
				IntNode inn = (IntNode) test;
				w = inn.getDoubleValue();
			} else {
				DoubleNode inn = (DoubleNode) test;
				w = inn.getDoubleValue();
			}
		}

		test = interrogate(impH);
		if (test != null) {
			if (test instanceof IntNode) {
				IntNode inn = (IntNode) test;
				h = inn.getDoubleValue();
			} else {
				DoubleNode inn = (DoubleNode) test;
				h = inn.getDoubleValue();
			}
		}
	}

	/**
	 * Constructor for use by HTTP handler.
	 * 
	 * @param in
	 *            . InputStream - the input stream of the incoming json of the
	 *            request.
	 * @throws JsonProcessingException
	 *             on parse errors.
	 * @throws IOException
	 *             on file reading errors
	 */
	public BidRequest(InputStream in) throws JsonProcessingException,
			IOException {
		rootNode = mapper.readTree(in);
		setup();
	}

	/**
	 * Interrogate an entity in the JSON using dotted format. Example:
	 * {a:{b:c:1}}to find c: "a.b.c" Example: {a:{b:[{x:1},{x:2}]} to find x[1]:
	 * "a.b.1.x"
	 * @deprecated This is a slower version of the interrogate method.
	 * @param line
	 *            . String. The string defining the dotted name.
	 * @return Object. Returns the object at the 'line' location or null if it
	 *         doesn't exist.
	 */
	public Object interrogate(String line) {
		String[] parts = line.split("\\.");
		List<String> strings = new ArrayList();
		for (int i = 0; i < parts.length; i++) {
			strings.add(parts[i]);
		}
		return interrogate(strings);
	}

	/**
	 * Interrogate the request using dotted name form, each entry is a name in
	 * the hierarchy.
	 * 
	 * @param parts
	 *            . List- the list of strings making up the name in hierarchical
	 *            form.
	 * @return Object. Returns the object at the 'line' location or null if it
	 *         doesn't exist.
	 */
	public Object interrogate(List<String> parts) {
		try {
			JsonNode x = rootNode;
			for (String n : parts) {
				if (n.matches("[+-]?\\d*(\\.\\d+)?")) {
					int num = Integer.parseInt(n);
					x = x.get(num);
				} else {
					if (x == null)
						return null;
					x = x.get(n);
				}
			}
			return x;
		} catch (Exception err) {

		}
		return null;
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

	public BidRequest copy(InputStream is) throws JsonProcessingException,
			IOException {
		return null;
	}
}
