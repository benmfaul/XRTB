package com.xrtb.exchanges.google;

import java.io.ByteArrayOutputStream;

import java.io.FileInputStream;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

import com.google.openrtb.OpenRtb.APIFramework;
import com.google.openrtb.OpenRtb.AdUnitId;
import com.google.openrtb.OpenRtb.BannerAdType;
import com.google.openrtb.OpenRtb.BidRequest.App;
import com.google.openrtb.OpenRtb.BidRequest.Content;
import com.google.openrtb.OpenRtb.BidRequest.Device;
import com.google.openrtb.OpenRtb.BidRequest.Geo;
import com.google.openrtb.OpenRtb.BidRequest.Imp;
import com.google.openrtb.OpenRtb.BidRequest.Imp.Banner;
import com.google.openrtb.OpenRtb.BidRequest.Imp.Native;
import com.google.openrtb.OpenRtb.BidRequest.Imp.Pmp;
import com.google.openrtb.OpenRtb.BidRequest.Imp.Pmp.Deal;
import com.google.openrtb.json.OpenRtbJsonFactory;
import com.google.openrtb.json.OpenRtbJsonReader;
import com.google.openrtb.OpenRtb.BidRequest.Imp.Video;
import com.google.openrtb.OpenRtb.BidRequest.Publisher;
import com.google.openrtb.OpenRtb.BidRequest.Site;
import com.google.openrtb.OpenRtb.BidRequest.User;
import com.google.openrtb.OpenRtb.CreativeAttribute;
import com.google.openrtb.OpenRtb.NativeRequest;
import com.google.openrtb.OpenRtb.NativeRequest.Asset;
import com.google.openrtb.OpenRtb.Protocol;
import com.google.protobuf.ProtocolStringList;

import com.xrtb.bidder.RTBServer;
import com.xrtb.common.Campaign;
import com.xrtb.common.Creative;
import com.xrtb.exchanges.adx.Base64;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.Impression;

/**
 * A class that creates a BidRequest from openRTB protobuf that supports most of openRTB.
 * @author Ben M. Faul
 *
 */

public class GoogleBidRequest extends BidRequest {

	public static byte e_key[];
	public static byte i_key[];
	
	// Not a bid request, sometimes google returns no bytes on a read.
	transient boolean notABidRequest = false;
	// The internal JSON form
	ObjectNode root;
	// The internal protobuf form.
	transient private com.google.openrtb.OpenRtb.BidRequest internal;

	
	/**
	 * Simple constructor
	 */
	public GoogleBidRequest() {
		impressions = new ArrayList<Impression>();
	}
	
	/**
	 * Interrogate the bid request
	 */
	@Override
	public Object interrogate(String line) {
		return super.interrogate(line);
	}
	
	/**
	 * Return the internal protobuf representation
	 * @return
	 */
	@JsonIgnore
	public com.google.openrtb.OpenRtb.BidRequest getInternal() {
		return internal;
	}
	
	/**
	 * Build a protobuf version of the response
	 */
	public GoogleBidResponse buildNewBidResponse(Impression imp, Campaign camp, Creative creat,
			double price, String dealId,  int xtime) throws Exception {
		
		return new GoogleBidResponse(this,imp,camp,creat,id, price,dealId,xtime);
	}
	
	/**
	 * Return the no bid code.
	 * 
	 * @return int. The return code.
	 */
	@Override
	public int returnNoBidCode() {
		return RTBServer.NOBID_CODE;
	}
	
	/**
	 * Given the input stream, parse and make the class.
	 * @param in InputStream. The content of the POST
	 * @throws Exception on protobuf, json or I/O errors.
	 */
	public GoogleBidRequest(InputStream in) throws Exception {
		int nRead;
		byte [] data = new byte[1024];
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    	while ((nRead = in.read(data, 0, data.length)) != -1) {
    		  buffer.write(data, 0, nRead);
    	}
    	
    	data = buffer.toByteArray();
    	if (data.length == 0) {
    		notABidRequest = true;
    		return;
    	}
    	
    	//System.out.println(new String(Base64.encodeBase64(data)));
		//System.out.println("LENGTH = " + data.length);
    	try {
    		internal = com.google.openrtb.OpenRtb.BidRequest.parseFrom(data);
    	} catch (Exception e) {
    		//System.out.println("======================== UNEXPECTED ======================");
    		//System.out.println("Error: " + e.toString());
    		//System.out.println("==========================================================");
    		throw e;
    	}
    	//internal = com.google.openrtb.OpenRtb.BidRequest.parseFrom(in);
		doInternal();
	}
	
	/**
	 * Given a protobuf Google bid request object, json-ize it. Used for testing.
	 * @param br com.google.openrtb.OpenRtb.BidRequest. The bid request in protobuf.
	 * @throws Exception on JSON errors.
	 */
	public GoogleBidRequest(com.google.openrtb.OpenRtb.BidRequest br) throws Exception {
		internal = br;
		doInternal();
	}
	
	@Override
	public boolean notABidRequest() {
		return notABidRequest;
	}
		
	/**
	 * Take the internal protobuf and convert to JSON.
	 * @throws Exception on JSON or protobuf errors.
	 */
	void doInternal() throws Exception {
		impressions = new ArrayList<Impression>();
		root = BidRequest.factory.objectNode();
		
		// Add this to the log
		byte[] bytes = internal.toByteArray();
		String str = new String(Base64.encodeBase64(bytes));
		root.put("protobuf", str);
		
		root.put("at",internal.getAt().getNumber());
		ProtocolStringList list = internal.getBadvList();
		root.put("badv", getAsStringList(BidRequest.factory.arrayNode(), list));
		if (internal.hasTmax()) root.put("tmax", internal.getTmax());
		
		root.put("id", internal.getId());
		makeSiteOrApp();
		makeDevice();
		makeImpressions();
		makeUser();
		
		
		rootNode = (JsonNode)root;
		setup();
		
	}
	
	/**
	 * Make a user object
	 */
	
	void makeUser() {
		if (!internal.hasUser())
			return;
		
		User u = internal.getUser();
		ObjectNode node = BidRequest.factory.objectNode();
		if (u.hasBuyeruid()) node.put("buyeruid", u.getBuyeruid());
		if (u.hasCustomdata()) node.put("customdata",u.getCustomdata());
		if (u.hasGender()) node.put("gender", u.getGender());
		if (u.hasGeo()) addGeo(node,u.getGeo());
		if (u.hasId()) node.put("id", u.getId());
		if (u.hasKeywords()) node.put("keywords", u.getKeywords());
		if (u.hasYob()) node.put("yob", u.getYob());
		
		root.put("user",node);
		
	}
	
	/**
	 * Add the geo object
	 * @param node ObjectNode. The parent JSON node
	 * @param g Geo. The protobuf geo encoding.
	 */
	void addGeo(ObjectNode node, Geo g) {
		ObjectNode geo = BidRequest.factory.objectNode();
		node.put("geo", geo);
		if (g.hasCountry()) geo.put("country",g.getCountry());
		if (g.hasType()) geo.put("type",g.getType().getNumber());
		if (g.hasLat()) geo.put("lat",g.getLat());
		if (g.hasLon()) geo.put("lon",g.getLon());
		if (g.hasCity()) geo.put("city",g.getCity());
		if (g.hasRegion()) geo.put("region",g.getRegion());
		if (g.hasMetro()) geo.put("metro",g.getMetro());
		if (g.hasUtcoffset()) geo.put("utcoffset",g.getUtcoffset());
		if (g.hasZip()) geo.put("zip", g.getZip());
	}
	
	/**
	 * Make a device object
	 */
	void makeDevice() {

		if (!internal.hasDevice())
			return;
		
		Device d = internal.getDevice();
		ObjectNode node = BidRequest.factory.objectNode();
		if (d.hasIp()) node.put("ip",d.getIp());
		if (d.hasLanguage()) node.put("language", d.getLanguage());
		if (d.hasOs()) node.put("os",d.getOs());
		if (d.hasOsv()) node.put("osv",d.getOsv());
		if (d.hasCarrier()) node.put("carrier",d.getCarrier());
		if (d.hasConnectiontype()) node.put("connectiontype",d.getConnectiontype().getNumber());
		if (d.hasDidmd5()) node.put("didmd5",d.getDidmd5());
		if (d.hasDidsha1()) node.put("didsha1",d.getDidsha1());
		if (d.hasDpidsha1())node.put("dpidsha1",d.getDpidsha1());
		if (d.hasDnt()) node.put("dnt",d.getDnt());
		if (d.hasDevicetype()) node.put("devicetype", d.getDevicetype().getNumber());
		if (d.hasUa()) node.put("ua", d.getUa());
		if (d.hasJs()) node.put("js", d.getJs());
		if (internal.getDevice().hasGeo())  addGeo(node,d.getGeo());
		root.put("device", node);
		
	}
	
	/**
	 * Make either the site or app object.
	 */
	void makeSiteOrApp() {
		ObjectNode node = BidRequest.factory.objectNode();
		if (internal.hasSite()) {
			Site s = internal.getSite();
			root.put("site", node);
			
			if (s.hasId()) node.put("id", s.getId());
			if (s.hasName()) node.put("name", s.getName());
			node.put("cat", getAsStringList(BidRequest.factory.arrayNode(),s.getCatList()));
			if (s.hasKeywords()) node.put("keywords",s.getKeywords());
			if (s.hasMobile()) { 
				if (s.getMobile())
					node.put("mobile", 1);
				else
					node.put("mobile", 0);
			}
			if (s.hasPage()) node.put("page",s.getPage());
			if (s.hasDomain()) node.put("domain", s.getDomain());
			if (s.hasRef()) node.put("ref", s.getRef());
			if (s.hasSearch()) node.put("search", s.getSearch());
			if (s.hasPrivacypolicy()) node.put("privacypolicy", s.getPrivacypolicy());
			if (s.hasPublisher()) {
				Publisher p = s.getPublisher();
				ObjectNode pub = BidRequest.factory.objectNode();
				pub.put("id",p.getId());
				pub.put("name", p.getId());
				if (p.hasDomain()) pub.put("domain",p.getDomain());
				node.put("publisher", pub);
			}
		} else {
			App  a = internal.getApp();
			root.put("app", node);
			if (a.hasId()) node.put("id", a.getId());
			if (a.hasName()) node.put("name", a.getName());
			node.put("cat", getAsStringList(BidRequest.factory.arrayNode(),a.getCatList()));

			if (a.hasKeywords()) node.put("keywords",a.getKeywords());
			if (a.hasContent()) {
				Content c = a.getContent();
				ObjectNode content = BidRequest.factory.objectNode();
				node.put("content",content);
				if (c.hasAlbum()) content.put("album", c.getAlbum());
				if (c.hasArtist()) content.put("artist", c.getArtist());
				if (c.hasContentrating()) content.put("contentrating", content);
				if (c.hasContext22()) content.put("context22", c.getContext22());
				if (c.hasEmbeddable()) content.put("embeddable", c.getEmbeddable());
				if (c.hasEpisode()) content.put("episode", c.getEpisode());
				if (c.hasSourcerelationship()) content.put("src",c.getSourcerelationship());
				if (c.hasGenre()) content.put("genre", c.getGenre());
				if (c.hasIsrc()) content.put("isrc", c.getIsrc());
				if (c.hasKeywords()) content.put("keyword", c.getKeywords());
				if (c.hasLanguage()) content.put("language", c.getLanguage());
				if (c.hasLen()) content.put("len", c.getLen());
				if (c.hasLivestream()) content.put("livestream",c.getLivestream());
				content.put("cat", getAsStringList(BidRequest.factory.arrayNode(),c.getCatList()));
				if (c.hasSeason()) content.put("season", c.getSeason());
				if (c.hasSeries()) content.put("series", c.getSeries());
				if (c.hasTitle()) content.put("title", c.hasTitle());
				if (c.hasUrl()) content.put("url", c.getUrl());
				if (c.hasUserrating()) content.put("userrating", c.getUserrating());
			}
			if (a.hasBundle()) node.put("bundle", internal.getApp().getBundle());
			if (a.hasDomain()) node.put("domain", internal.getApp().getDomain());
			node.put("cat", getAsStringList(BidRequest.factory.arrayNode(),internal.getApp().getCatList()));
			if (a.hasPrivacypolicy()) node.put("privacypolicy", internal.getApp().getPrivacypolicy());
			if (internal.getApp().hasPublisher()) {
				Publisher p = a.getPublisher();
				ObjectNode pub = BidRequest.factory.objectNode();
				pub.put("id",internal.getApp().getPublisher().getId());
				pub.put("name", internal.getApp().getPublisher().getId());
				if (p.hasDomain()) pub.put("domain", p.getDomain());
				node.put("publisher", pub);
			}
		}
	
	}
	
	/**
	 * Make JSON based impressions.
	 */
	void makeImpressions() {
		ArrayNode array = BidRequest.factory.arrayNode();
		root.put("imp",array);
		for (int i=0;i<internal.getImpCount();i++) {
			Imp imp = internal.getImp(i);
			ObjectNode impx = null;
			
			if (imp.hasAudio()) {
				doAudio(array,imp, i);
			}
			if (imp.hasBanner()) {
				impx = doBanner(array,imp.getBanner(),i);
			}
			if (imp.hasNative()) {
				impx = doNative(array,imp.getNative(), i);
			}
			if (imp.hasVideo()) {
				impx = doVideo(array,imp.getVideo(), i);
			}
		
			if (imp.hasClickbrowser()) impx.put("clickbrowser", imp.getClickbrowser());
			if (imp.hasInstl()) {
				boolean t = imp.getInstl();
				if (t)
					impx.put("instl", 1);
				else
					impx.put("instl", 0);
			}
			if (imp.hasBidfloor()) impx.put("bidfloor",imp.getBidfloor());
			if (imp.hasBidfloorcur()) impx.put("bidfloorcur", imp.getBidfloorcur());
			if (imp.hasDisplaymanager()) impx.put("displaymanager",imp.getDisplaymanager());
			if (imp.hasTagid()) impx.put("tagid", imp.getTagid());

			if (imp.hasPmp()) doPmp(imp.getPmp(),impx);
		}
	}
	
	/**
	 * Make a list of deals
	 * @param pmp Pmp. Private market place on protobuf.
	 * @param impx ObjectNode. The JSON equivalent.
	 */
	void doPmp(Pmp pmp, ObjectNode impx) {
		ObjectNode node = BidRequest.factory.objectNode();
		ArrayNode lx = BidRequest.factory.arrayNode();
		impx.put("pmp",node);
		
		if (pmp.hasPrivateAuction()) 
			node.put("private_auction", pmp.getPrivateAuction());
		else
			node.put("private_auction", 0);
		
		if (pmp.getDealsCount() > 0) {
			List<Deal> list = pmp.getDealsList();
			for (int i=0;i<list.size();i++) {
				ObjectNode d = BidRequest.factory.objectNode();
				Deal deal = list.get(i);
				if (deal.hasAt()) d.put("at", deal.getAt().getNumber());
				if (deal.hasBidfloor()) d.put("bidfloor", deal.getBidfloor());
				if (deal.hasBidfloorcur()) d.put("bidfloorcur", deal.getBidfloorcur());
				d.put("id", deal.getId());
				lx.add(d);
			}
		}
		node.put("deals", lx);
	}
	
	void doAudio(ArrayNode array, Imp imp, int i) {
		
	}
	
	/**
	 * Make a banner impression.
	 * @param array ArrayNode. Holds the impressions.
	 * @param b Banner. The protobuf banner.
	 * @return ObjectNode. The banner as JSON.
	 */
	ObjectNode doBanner(ArrayNode array, Banner b, int i) {
		ObjectNode node = BidRequest.factory.objectNode();
		ObjectNode banner = BidRequest.factory.objectNode();
		banner.put("banner",node);
			
		banner.put("id", Integer.toString((i+1)));
		
		if (b.hasH()) node.put("h", b.getH());
		if (b.hasHmax()) node.put("hmax",b.getHmax());
		if (b.hasHmin()) node.put("hmin", b.getHmin());
		if (b.hasPos()) node.put("pos", b.getPos().getNumber());
		if (b.hasTopframe()) node.put("topframe", b.getTopframe());
		if (b.hasW()) node.put("w", b.getW());
		if (b.hasWmax()) node.put("wmax",b.getWmax());
		if (b.hasWmin()) node.put("wmin", b.getWmin());
		if (b.getBattrCount() > 0) {
			ArrayNode a = BidRequest.factory.arrayNode();
			node.put("battr", getAsAttributeList(a, b.getBattrList()));
		}
		if (b.getApiCount() > 0) {
			ArrayNode a = BidRequest.factory.arrayNode();
			node.put("api", getAsAttributeListAPI(a, b.getApiList()));
		}
		if (b.getBtypeCount() > 0) {
			ArrayNode a = BidRequest.factory.arrayNode();
			node.put("btype", getAsAttributeListBanner(a, b.getBtypeList()));
		}
		if (b.getMimesCount() > 0) {
			ArrayNode a = BidRequest.factory.arrayNode();
			node.put("mimes", getAsStringList(a, b.getMimesList()));
		}
		
		array.add(banner);
		return banner;
	}
	
	/**
	 * Make a video impression.
	 * @param array ArrayNode. Holds the impressions.
	 * @param b Banner. The protobuf banner.
	 * @return ObjectNode. The video as JSON.
	 */
	static ObjectNode doVideo(ArrayNode array, Video v, int i) {
		ObjectNode node = BidRequest.factory.objectNode();
		ObjectNode video = BidRequest.factory.objectNode();
		video.put("video", node);
		
		String impid = Integer.toString(i+1);
		video.put("id",impid);
		
		if (v.hasH()) node.put("h", v.getH());
		if (v.hasW()) node.put("w", v.getW());
		if (v.hasPos()) node.put("pos", v.getPos().getNumber());
		if (v.getApiCount() > 0) {
			ArrayNode a = BidRequest.factory.arrayNode();
			node.put("api", getAsAttributeListAPI(a, v.getApiList()));
		}
		if (v.getBattrCount() > 0) {
			ArrayNode a = BidRequest.factory.arrayNode();
			node.put("battr", getAsAttributeList(a, v.getBattrList()));
		}
		if (v.getMimesCount() > 0) {
			ArrayNode a = BidRequest.factory.arrayNode();
			node.put("mimes", getAsStringList(a, v.getMimesList()));
		}
		
		if (v.hasProtocol()) video.put("protocol", v.getProtocol().getNumber());
		if (v.getProtocolsCount() > 0) {
			ArrayNode a = BidRequest.factory.arrayNode();
			node.put("protocols", getAsAttributeListProtocols(a,v.getProtocolsList()));
		}
		
		if (v.hasBoxingallowed()) node.put("boxingallowed", v.getBoxingallowed());
		if (v.hasLinearity()) node.put("linearity", v.getLinearity().getNumber());
		if (v.hasMaxbitrate()) node.put("maxbitrate", v.getMaxbitrate());
		if (v.hasMinbitrate()) node.put("minbitrate",v.getMinbitrate());
		if (v.hasMinduration()) node.put("minduration", v.getMinduration());
		if (v.hasMaxduration()) node.put("maxduration", v.getMaxduration());
		if (v.hasMaxextended()) node.put("maxextended", v.getMaxextended());
		
		array.add(video);
		return video;
	}
	
	static ObjectNode doNative(ArrayNode array, Native n, int i) {
		ObjectNode node = BidRequest.factory.objectNode();
		ObjectNode nat = BidRequest.factory.objectNode();
		
		nat.put("native", node);
		if (n.hasRequest()) node.put("request", n.getRequest());
		if (n.hasVer()) node.put("ver", n.getVer());
		
		ArrayNode a = BidRequest.factory.arrayNode();
		node.put("api", getAsAttributeListAPI(a, n.getApiList()));
		ArrayNode b = BidRequest.factory.arrayNode();
		node.put("battr", getAsAttributeList(b, n.getBattrList()));
		if (n.hasRequestNative()) {
			NativeRequest nr = n.getRequestNative();
			if (nr.hasContext()) {
				node.put("context",nr.getContext().getNumber());
			}
			if (nr.hasContextsubtype()) {
				node.put("contextsubtype", nr.getContextsubtype().getNumber());
			}
			if (nr.hasPlcmtcnt()) {
				node.put("plcmttype",nr.getPlcmtcnt());
			}
			if (nr.hasPlcmttype()) {
				node.put("plcmttype", nr.getPlcmttype().ordinal());
			}
			if (nr.hasAdunit()) {
				AdUnitId au = nr.getAdunit();
				node.put("adunit", au.getNumber());
			}
			if (nr.hasSeq()) {
				node.put("seq", nr.getSeq());
			}
			
			List<Asset> list = nr.getAssetsList();
			a = BidRequest.factory.arrayNode();
			node.put("assets",a);
			for (Asset asset : list) {
				NativeFramework.makeAsset(asset,a);
			}
			
		}
		return nat;
	}
	
	
	/**
	 * Return a list of creative attributes in JSON form
	 * @param node ArrayNode. The node we will add to.
	 * @param list List. A list of creative attributes.
	 * @return ArrayNode. The node we passed in.
	 */
	static ArrayNode getAsAttributeList(ArrayNode node, List<CreativeAttribute> list ) {
		for (int i=0; i<list.size();i++) {
			node.add(list.get(i).getNumber());
		}
		return node;
	}
	
	/**
	 * Return a list of API frameworks in JSON form
	 * @param node ArrayNode. The node we will add to.
	 * @param list List. A list of API frameworks.
	 * @return ArrayNode. The node we passed in.
	 */
	static ArrayNode getAsAttributeListAPI(ArrayNode node, List<APIFramework> list ) {
		for (int i=0; i<list.size();i++) {
			node.add(list.get(i).getNumber());
		}
		return node;
	}
	
	/**
	 * Return a list of Banner Ad types in JSON form
	 * @param node ArrayNode. The node we will add to.
	 * @param list List. A list of banner ad types.
	 * @return ArrayNode. The node we passed in.
	 */
	static ArrayNode getAsAttributeListBanner(ArrayNode node, List<BannerAdType> list ) {
		for (int i=0; i<list.size();i++) {
			node.add(list.get(i).getNumber());
		}
		return node;
	}
	
	/**
	 * Return a list of protocol numbers in JSON form
	 * @param node ArrayNode. The node we will add to.
	 * @param list List. A list of protocol numbers.
	 * @return ArrayNode. The node we passed in.
	 */
	static ArrayNode getAsAttributeListProtocols(ArrayNode node, List<Protocol> list ) {
		for (int i=0; i<list.size();i++) {
			node.add(list.get(i).getNumber());
		}
		return node;
	}
	
	/**
	 * Return a list of protocol strings in JSON form
	 * @param node ArrayNode. The node we will add to.
	 * @param list List. A list of protocol strings.
	 * @return ArrayNode. The node we passed in.
	 */
	protected static ArrayNode getAsStringList(ArrayNode node, ProtocolStringList list) {
		for (int i=0; i<list.size();i++) {
			node.add(list.get(i));
		}
		return node;
	}
	
	/**
	 * The configuration requires an e_key and an i_key
	 */
	@Override
	public void handleConfigExtensions(Map extension)  {
		String key = (String) extension.get("e_key");
		GoogleWinObject.encryptionKeyBytes = e_key = javax.xml.bind.DatatypeConverter.parseBase64Binary(key);
		key = (String) extension.get("i_key");
		GoogleWinObject.integrityKeyBytes = i_key = javax.xml.bind.DatatypeConverter.parseBase64Binary(key);
	}
	
	/**
	 * Makes sure the Google billing_id is available on the creative
	 * @param creat Creative. The creative in question.
	 * @param errorString StringBuilder. The error handling string. Add your error here if not null.
	 * @returns boolean. Returns true if the Exchange and creative are compatible.
	 */
	@Override
	public boolean checkNonStandard(Creative creat, StringBuilder errorString) {
		if (creat.extensions == null || creat.extensions.get("billing_id") == null) {
			if (errorString != null) {
				errorString.append(creat.impid);
				errorString.append(" ");
				errorString.append("Missing extensions for Google");
			}
			return false;
		}
		return true;
	}
	
	public static GoogleBidRequest fromRTBFile(String initialFile) throws Exception {
		InputStream targetStream = new FileInputStream(initialFile);	
		OpenRtbJsonFactory jf = OpenRtbJsonFactory.create();
			
		MyReader reader = new MyReader(jf);
		com.google.openrtb.OpenRtb.BidRequest r = reader.readBidRequest(targetStream);
			
		GoogleBidRequest google = new GoogleBidRequest(r);
		
		return google;
	}
}

class MyReader extends OpenRtbJsonReader {

	protected MyReader(OpenRtbJsonFactory factory) {
		super(factory);
		// TODO Auto-generated constructor stub
	}
	
}