package com.xrtb.common;

import com.xrtb.bidder.RTBServer;
import com.xrtb.pojo.BidRequest;
import com.xrtb.rate.Limiter;
import com.xrtb.shared.FrequencyGoverner;
import com.xrtb.tools.DbTools;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A class that implements a campaign. Provide the campaign with evaluation
 * Nodes (a stack) and a bid request, and this campaign will determine if the
 * bid request in question matches this campaign.
 * @author Ben M. Faul
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Campaign implements Comparable {
	
	/** Set to true if this is an Adx campaign. Can't mix Adx and regular campaigns */
	public boolean isAdx;
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

	/** The spend rate of the campaign, default is $1/minute/second in micros. */
	public long assignedSpendRate = 16667;

	public FrequencyCap frequencyCap = null;

	/** The actual spend rate of the campaign, affected by the number of bidders in the system */
	public transient long effectiveSpendRate;

    private SortNodesFalseCount nodeSorter = new SortNodesFalseCount();
	/**
	 * Empty constructor, simply takes all defaults, useful for testing.
	 */
	public Campaign() {

	}
	
	public Campaign(String data) throws Exception {
		
		Campaign camp = DbTools.mapper.readValue(data, Campaign.class);
		this.isAdx = camp.isAdx;
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
     * Sort the selection criteria in descending order of number of times false was selected.
     * Then, after doing that, zero the counters.
     */
    public void sortNodes() {
        Collections.sort(attributes, nodeSorter);

        for (int i = 0; i<attributes.size();i++) {
            attributes.get(i).clearFalseCount();
        }

        for (int i=0;i<creatives.size();i++) {
            creatives.get(i).sortNodes();
        }
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
	 * Is this campaign capped on the item in this bid request?
	 * @param br BidRequest. The bid request to query.
	 * @param capSpecs Map. The current cap spec.
	 * @return boolean. Returns true if the IP address is capped, else false.
	 */
	public boolean isCapped(BidRequest br, Map<String, String> capSpecs) {
		if (frequencyCap == null)
			return false;
		return frequencyCap.isCapped(br,capSpecs,adId);
	}

	/**
	 * Determine if this bid request + campaign is frequency Governed.
	 * @param br BidReuestcount . The bid request to check for governance.
	 * @return boolean. Returns true if this campaign has bid on the same user/synthkey in the last 1000 ms.
	 */
	public boolean isGoverned(BidRequest br) {
		if (RTBServer.frequencyGoverner == null || FrequencyGoverner.silent.get())
			return false;

		return RTBServer.frequencyGoverner.contains(adId,br);
	}
	/**
	 * Return the Lucene query string for this campaign's attributes
	 * @return String. The lucene query.
	 */
	
	@JsonIgnore
	public String getLucene() {
		return getLuceneFromAttrs(attributes);
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
		Creative c = this.getCreative(crid);
		if (c == null)
			return null;

		String pre = "((-_exists_: imp.bidfloor) OR imp.bidfloor :<=" + c.price + ") AND ";
		if (c.isNative()) {
			
		} else
		if (c.isVideo()) {
			pre += "imp.video.w: " + c.w + " AND imp.video.h: " + c.h + " AND imp.video.maxduration:< " + c.videoDuration;
			pre += " AND imp.video.mimes: *" + c.videoMimeType + "* AND imp.video.protocols: *" + c.videoProtocol + "*";
		} else {
			pre += "imp.banner.w: " + c.w + " AND imp.banner.h: " + c.h;
		}
		
		String str = getLucene();
		String rest = getLuceneFromAttrs(c.attributes);
		
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
	 * @param id String - the id of this campaign.
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
			category = new ArrayList();
		}
		
		if (category.size()>0) {
			String str = "\"cat\":" + DbTools.mapper.writer().withDefaultPrettyPrinter().writeValueAsString(category);
			encodedIab = new StringBuilder(str);
		}

		Limiter.getInstance().addCampaign(this);
		establishSpendRate();
	}

	/**
	 * Calculate the effective spend rate. It is equal to assigned spend rate by the number of members. Then
	 * call the Limiter to fix the rate limiter access for it.
	 *
	 * This is called when the campaign is instantiated (via the encode attributes method, or whenever the
	 * bidder determines there has been a change in the number of bidders in the bid farm
	 */
	public void establishSpendRate() {
		int k = RTBServer.biddersCount;
		if (k == 0)
			k = 1;
		effectiveSpendRate = assignedSpendRate / k;
		Limiter.getInstance().setSpendRate(adId, effectiveSpendRate);
	}

	/**
	 * Add an evaluation node to the campaign.
	 * @param node Node - the evaluation node to be added to the set.
	 */
	public void add(Node node) {
		attributes.add(node);
	}

	/**
	 * The compareTo method to ensure that multiple campaigns
	 * don't exist with the same id.
	 * @param o Object. The object to compare with.
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

	/**
	 * Answers the question, can this campaign use the named exchange?
	 * @param exchange String. The name of the exchange
	 * @return boolean. Returns true if we can use the exchange, otherwise returns false.
	 */
	public boolean canUseExchange(String exchange) {
		boolean canUse = false;
		for (Node node : attributes) {
			if (node.bidRequestValues.contains("exchange")) {
				canUse = false;
				Object obj = node.value;
				if (obj instanceof String) {
					String str = (String)obj;
					if (str.equals(exchange)) {
						canUse = true;
					} else {
						canUse = false;
					}
				} else
				if (obj instanceof List) {
					List<String> list = (List<String>)obj;
					if (list.contains(exchange))
						canUse = true;
					else
						canUse = false;
				}
			}
		}
		return canUse;
	}
}
