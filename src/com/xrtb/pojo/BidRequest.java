package com.xrtb.pojo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;
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

import com.xrtb.bidder.Controller;
import com.xrtb.common.Configuration;

/**
 * A class that encapsulates an RTB2 bid request. Exchanges extend this overriding 
 * parse special to handle any exchange specific stuff.
 */
public class BidRequest {
	
	// These items are pulled from the bid request, but there are many others. If you need them
	// call the interrogate method.
	public String id;
	public Double lat;
	public Double lon;
	public Double w;
	public Double h;
	public String exchange;
	public String siteId;
	
	ObjectMapper mapper = new ObjectMapper();
	JsonNode rootNode = null;
	
	/**
	 * Default constructor, useful for testing.
	 */
	public BidRequest() {
		
	}
	
	public BidRequest(String in) throws JsonProcessingException, IOException {
		String content = new String(Files.readAllBytes(Paths.get(in)));
		rootNode = mapper.readTree(content);
		setup();
	}
	
	private void setup() {
		JsonNode node = rootNode.path("id");
		id = node.getTextValue();
		Object test = interrogate("device.geo.lat");
		if (test != null) {
			DoubleNode n = (DoubleNode)test;
			lat = n.getDoubleValue();
		}
		test = interrogate("device.geo.lon");
		if (test != null) {
			DoubleNode n = (DoubleNode)test;
			lon = n.getDoubleValue();
		}
		test = interrogate("site.id");
		if (test != null) {
			TextNode n = (TextNode)test;
			siteId = n.getTextValue();
		}
		DoubleNode n = null;
		test = interrogate("imp.0.banner.w");
		if (test != null) {
			if (test instanceof IntNode) {
				IntNode inn = (IntNode)test;
				w = inn.getDoubleValue();
			} else {
				DoubleNode inn = (DoubleNode)test;
				w = inn.getDoubleValue();
			}
		}
		
		test = interrogate("imp.0.banner.h");
		if (test != null) {
		if (test instanceof IntNode) {
			IntNode inn = (IntNode)test;
			h = inn.getDoubleValue();
		} else {
			DoubleNode inn = (DoubleNode)test;
			h = inn.getDoubleValue();
		}
		}
	}
	
	
	/**
	 * Constructor for use by HTTP handler.
	 * @param in. InputStream - the input stream of the incoming json of the request.
	 * @throws JsonProcessingException. Throws on JSON parsing errors.
	 * @throws IOException. Throws on I/O errors reading JSON.
	 */
	public BidRequest(InputStream in) throws JsonProcessingException, IOException {
		rootNode = mapper.readTree(in);
		setup();
	}
	
	/**
	 * Interrogate an entity in the JSON using dotted format.
	 * Example: {a:{b:c:1}}to find c: "a.b.c"
	 * Example: {a:{b:[{x:1},{x:2}]} to find x[1]: "a.b.1.x"
	 * @param line. String. The string defining the dotted name.
	 * @return Object. Returns the object at the 'line' location or null if it doesn't exist.
	 */
	public Object interrogate(String line) {
		String [] parts = line.split("\\.");
		List<String> strings = new ArrayList();
		for (int i=0;i < parts.length;i++) {
			strings.add(parts[i]);
		}
		return interrogate(strings);
	}

	/**
	 * Interrogate the request using dotted name form, each entry is a name in the hierarchy.
	 * @param parts. List<String> - the list of strings making up the name in hierarchical form.
	 * @return Object. Returns the object at the 'line' location or null if it doesn't exist.
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
	 */
	public String toString() {
		if (rootNode == null)
			return null;
		
		return rootNode.toString();
	}
	
	/**
	 * Return the vid request id.
	 * @return
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Parse the incoming bid request.
	 * @return
	 */
	public boolean parse() {
		// do normal rtb
		return parseSpecial();
	}
	
	/**
	 * Override to do whatever special parsing tour exchange requires.
	 * @return boolean. Returns true if it parsed ok, else false on ill-formed JSON.
	 */
	public boolean parseSpecial() {
		return true;
	}
}
