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
 * POJO for a bid object
 */
public class Bid {
	transient static Gson gson = new Gson();
	public String id;
	public String impid;
	public double price;
	public String adid;
	public String nurl;
	public String cid;
	public String crid;
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
