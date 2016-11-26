package com.xrtb.common;

import java.lang.reflect.Constructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.xrtb.tools.DbTools;
import com.xrtb.tools.NashHorn;

/**
 * A class that implements a campaign. Provide the campaign with evaluation
 * Nodes (a stack) and a bid request, and this campaign will determine if the
 * bid request in question matches this campaign.
 * @author Ben M. Faul
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Campaign implements Comparable {
	
	/** points back to the name of the owner of the campaign */
	public String owner = null;
	/** The id of the campaign */
	public String adId = "default-campaign";
	/** The campaign name */
	public String name;
	/** The default ad domain */
	public String adomain = "default-domain";
	/** The list of constraint nodes for this campaign */
	public List<Node> attributes = new ArrayList<Node>();
	/** The list of creatives for this campaign */
	public List<Creative> creatives = new ArrayList();
	/** Start and end date for this campaign */
	public List<Integer> date = new ArrayList();
	/** IAB Categories */
	public List<String> category;
	/** encoded IAB category */
	public transient StringBuilder encodedIab;	
	/** Should you do forensiq fingerprinting for this campaign? */
	public boolean forensiq = false;
	
	/**
	 * Empty constructor, simply takes all defaults, useful for testing.
	 */
	public Campaign() {

	}
	
	public Campaign(String data) throws Exception {
		
		Campaign camp = DbTools.mapper.readValue(data, Campaign.class);
		this.adomain = camp.adomain;
		this.attributes = camp.attributes;
		this.creatives = camp.creatives;
		this.date = camp.date;
		this.adId = camp.adId;
		this.name = camp.name;
		this.owner = camp.owner;
		this.forensiq = camp.forensiq;
		if (camp.category != null)
			this.category = camp.category;
		
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
	 * Return the Lucene query string for this campaign's attributes
	 * @return String. The lucene query.
	 */
	
	@JsonIgnore
	public String getLucene() {
		String str = getLuceneFromAttrs(attributes);
		return str;
	}
	
	String getLuceneFromAttrs(List<Node> attributes) {
		String str = "";
		
		List<String> strings = new ArrayList();
		for (int i=0; i < attributes.size(); i++) {
			Node x = attributes.get(i);
			String s = x.getLucene();
			if (s != null && s.length() > 0)
				strings.add(s);
		}
		
		for (int i=0; i<strings.size();i++) {
			String s = strings.get(i);
			str += s;
			if (i + 1 < strings.size())
				str += " AND ";
		}
		
		return str;
	}
	
	/**
	 * Return the lucene query string for the named creative.
	 * @param crid String. The creative id.
	 * @return String. The lucene string for this query.
	 */
	@JsonIgnore
	public String getLucene(String crid) {
		String pre = null;
		Creative c = this.getCreative(crid);
		if (c == null)
			return null;
		
		if (c.isNative()) {
			
		} else
		if (c.isVideo()) {
			pre = "imp.video.w: " + c.w + " AND imp.video.h: " + c.h + " AND imp.video.maxduration < " + c.videoDuration;
			pre += " AND imp.video.mimes: *" + c.videoMimeType + "* AND imp.video.protocols: *" + c.videoProtocol + "*";
		} else {
			pre = "imp.banner.w: " + c.w + " AND imp.banner.h: " + c.h;
		}
		
		String str = getLucene();
		String rest = getLuceneFromAttrs(c.attributes);
		
		
		if (pre == null)
			return "";
		
		if (str == null || str.length() == 0)  {
			return pre + " AND " + rest;
		}
		
		if (rest == null || rest.length() == 0)
			return pre + " AND " + str;
		
		return pre + " AND " + str + " AND " + rest;
	}
	
	/**
	 * Get a creative of this campaign.
	 * @param crid: String. The creative id.
	 * @return Creative. The creative or null;
	 */
	public Creative getCreative(String crid) {
		for (Creative c : creatives) {
			if (c.impid.equals(crid)) {
				return c;
			}
		}
		return null;
	}
	
	/**
	 * Creates a copy of this campaign
	 * @return Campaign. A campaign that is an exact clone of this one
	 * @throws Exception on JSON parse errors.
	 */
	public Campaign copy() throws Exception {

		String str =  DbTools.mapper.writer().writeValueAsString(this);
		Campaign x = DbTools.mapper.readValue(str, Campaign.class);
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
		
		if (category == null) {
			category = new ArrayList();    // ol
		}
		
		if (category.size()>0) {
			String str = "\"cat\":" + DbTools.mapper.writer().withDefaultPrettyPrinter().writeValueAsString(category);
			encodedIab = new StringBuilder(str);
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
		try {
			return DbTools.mapper.writer().withDefaultPrettyPrinter().writeValueAsString(this);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
