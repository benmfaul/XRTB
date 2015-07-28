package com.xrtb.common;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A class that implements a campaign. Provide the campaign with evaluation
 * Nodes (a stack) and a bid request, and this campaign will determine if the
 * bid request in question matches this campaign.
 * @author Ben M. Faul
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Campaign implements Comparable {
	
	/** The id (name) of the campaign */
	public String adId = "default-campaign";
	/** The default price associated with the campaign */
	public double price = 0.01;
	/** The default ad domain */
	public String adomain = "default-domain";
	/** An empty template for the exchange formatted message */
	public Map template = new HashMap();
	/** The list of constraint nodes for this campaign */
	public List<Node> attributes = new ArrayList<Node>();
	/** The list of creatives for this campaign */
	public List<Creative> creatives = new ArrayList();
	/** Start and end date for this campaign */
	public List<Integer> date = new ArrayList();
	
	
	/**
	 * Empty constructor, simply takes all defaults, useful for testing.
	 */
	public Campaign() {

	}
	
	public Campaign(String data) throws Exception {
		Gson gson = new Gson();
		Campaign camp = gson.fromJson(data, Campaign.class);
		this.price = camp.price;
		this.adomain = camp.adomain;
		this.template = camp.template;
		this.attributes = camp.attributes;
		this.creatives = camp.creatives;
		this.date = camp.date;
		this.adId = camp.adId;
		encodeCreatives();
		encodeAttributes();
	}
	
	/**
	 * Find the node with the specified hierarchy string.
	 * @param str String. The hierarchy we are looking for.
	 * @return Node. The node with this hierarchy, might be null if not exists.
	 */
	public Node getAttribute(String str) {
		for (Node n : attributes) {
			if (n.equals(str))
				return n;
		}
		return null;
	}
	
	
	/**
	 * Creates a copy of this campaign
	 * @return Campaign. A campaign that is an exact clone of this one
	 * @throws Exception on JSON parse errors.
	 */
	public Campaign copy() throws Exception {
		Gson g = new GsonBuilder().setPrettyPrinting().create();
		String str = g.toJson(this);

		Campaign x = g.fromJson(str,Campaign.class);
		x.encodeAttributes();
		return x;
	}
	
	/**
	 * Constructor with pre-defined node.
	 * @param id. String - the id of this campaign.
	 * @param nodes nodes. List - the list of nodes to add.
	 */
	public Campaign(String id, List<Node> nodes) {
		this.adId = id;
		this.attributes.addAll(nodes);
	}
	
	/**
	 * Enclose the URL fields. GSON doesn't pick the 2 encoded fields up, so you have to make sure you encode them.
	 * This is an important step, the WIN processing will get mangled if this is not called before the campaign is used.
	 * Configuration.getInstance().addCampaign() will call this for you.
	 */
	public void encodeCreatives() throws Exception {
		for (Creative c : creatives) {
			c.encodeUrl();
			c.encodeAttributes();
		}
	}
	
	/**
	 * Encode the values of all the attributes, instantiating from JSON does not do this, it's an incomplete serialization
	 * Always call this if you add a campaign without using Configuration.getInstance().addCampaign();
	 * @throws Exception if the attributes of the node could not be encoded.
	 */
	public void encodeAttributes() throws Exception {
		for (int i=0;i<attributes.size();i++) {
			Node n = attributes.get(i);
			n.setValues();
		}
	}
	
	/**
	 * Add an evaluation node to the campaign.
	 * @param node. Node - the evaluation node to be added to the set.
	 */
	public void add(Node node) {
		attributes.add(node);
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
		if (this.adId.equals(other.adId))
			return 1;
		
		return 0;
	}
	
	/**
	 * Returns this object as a JSON string
	 * @return String. The JSON representation of this object.
	 */
	public String toJson() {
		Gson g = new GsonBuilder().setPrettyPrinting().create();
		return g.toJson(this);
	}
}
