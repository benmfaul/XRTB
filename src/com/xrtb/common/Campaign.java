package com.xrtb.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.xrtb.pojo.BidRequest;

/**
 * A class that implements a campaign. Provide the campaign with evaluation
 * nodes (a stack) and a bid request, and this campaign will determine if the
 * bid request in question matches this campaign.
 * @author Ben M. Faul
 *
 */

public class Campaign implements Comparable {
	
	public String id = "default-campaign";
	public double price = 0.01;
	public String adomain = "default-domain";
	public Map template = new HashMap();
	public BidRequest br;
	public List<Node> nodes = new ArrayList<Node>();
	public List<Creative> creatives = new ArrayList();
	
	
	/**
	 * Empty constructor, simply takes all defaults, useful for testing.
	 */
	public Campaign() {

	}
	
	/**
	 * Creates a campaign from map data passed.
	 * @param data Map. Contains key value pairs for the campaign constraints.
	 * @throws Exception. Throws an exception on null pointers - when key/value pair is missing.
	 */
	public Campaign(Map data) throws Exception {
		setup(data);
	}
	
	/**
	 * Sets up the campaign with map data.
	 * @param data Map. The campaign defined as a map
	 */
	private void setup(Map data) {
		id = (String)data.get("campaign-adId");
		price = (Double)data.get("campaign-price");
		adomain = (String) data.get("campaign-adomain");
		template = (Map) data.get("campaign-adm-template");
		List<Map> list = (List)data.get("campaign-attributes");
		List<Map> cr = (List)data.get("campaign-creatives");
		
		for (Map x : cr) {
			Creative create = new Creative();
			create.forwardUrl = (String)x.get("forwardurl");
			create.w = (double) x.get("w");
			create.h = (double) x.get("h");
			create.imageUrl = x.get("imageurl").toString();
			create.impid = x.get("impid").toString();
			create.encodeUrl();
			creatives.add(create);
		}
		
		for (Map<String,Object> m : list) {
			String hier = null;
			for (String key : m.keySet()) {
			    hier = key;
			}
			m = (Map)m.get(hier);
			String op = (String)m.get("op");
			Object value = null;
			if (m.get("value")  != null)
				value = m.get("value");
			else
				value = m.get("values");
			Node node = new Node(hier, hier ,op,value);
			nodes.add(node);
		}
	}
	
	/**
	 * Constructor with a string representation of the campaign.
	 * @param id. String - the campaign as a string, will be converted to map, then the campaign is set up.
	 */
	public Campaign(String data) throws Exception  {
		Gson g = new Gson();
		Map m = g.fromJson(data, Map.class);
		setup(m);
	}
	
	/**
	 * Constructor with pre-defined node.
	 * @param id. String - the id of this campaign.
	 * @param nodes nodes. List<Mode> - the list of nodes to add.
	 */
	public Campaign(String id, List<Node> nodes) {
		this.id = id;
		this.nodes.addAll(nodes);
	}
	
	/**
	 * Add an evaluation node to the campaign.
	 * @param node. Node - the evaluation node to be added to the set.
	 */
	public void add(Node node) {
		nodes.add(node);
	}

	/**
	 * The compareTo method to ensure that multiple campaigns
	 * don't exist with the same id.
	 * @param o. Object. The object to compare with.
	 * @return int. Returns 1 if the ids match, otherwise 0.
	 */
	@Override
	public int compareTo(Object o) {
		Campaign other = (Campaign)o;
		if (this.id.equals(other.id))
			return 1;
		
		return 0;
	}
	
	public String toJson() {
		Gson g = new Gson();
		return g.toJson(this);
	}
}
