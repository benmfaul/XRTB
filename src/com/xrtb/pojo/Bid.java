package com.xrtb.pojo;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import com.google.gson.Gson;

/**
 * POJO for a bid object. Use the HTTP payload of the bid and convert it into this object. This is used for
 * debugging and in the JUNIT tests. This class is used to conveniently build a bid object from a string.
 * This is a GSON based de-serializer.
 */
public class Bid {
	/** The GSON encoder */
	transient static Gson gson = new Gson();
	/** The id of the bid */
	public String id;
	/** The impression id of the bid */
	public String impid;
	/** The price of the bid */
	public double price;
	/** the ad id of the bid */
	public String adid;
	/** The nurl field of the bid */
	public String nurl;
	/** The cid field of the bid */
	public String cid;
	/** The crid field of the bid */
	public String crid;
	/** The image url of the bid */
	public String iurl;

	/**
	 * Empty constructor for GSON
	 */
	public Bid() {

	}
	
	/**
	 * Create a bid object from the HTTP data.
	 * @param s String. The payload of an HTTP bid response
	 */
	public Bid(String s) {
		Map map = gson.fromJson(s,Map.class);
		List list = (List)map.get("seatbid");
		map = (Map)list.get(0);
		
		list = (List)map.get("bid");
		map = (Map)list.get(0);
		
		id = (String)map.get("id");
		impid = (String)map.get("impid");
		price = (Double)map.get("price");
		adid = (String)map.get("adid");
		nurl = (String)map.get("nurl");
		cid = (String)map.get("cid");
		crid = (String)map.get("crid");
		iurl = (String)map.get("iurl");
	}
	
	/**
	 * Return JSON representation of the object.
	 */
	@Override
	public String toString() {
		return gson.toJson(this);
	}
}
