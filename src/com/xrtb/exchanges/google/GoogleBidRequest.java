package com.xrtb.exchanges.google;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.openrtb.OpenRtb.BidRequest.Imp;
import com.google.protobuf.ProtocolStringList;
import com.xrtb.bidder.RTBServer;
import com.xrtb.common.Campaign;
import com.xrtb.common.Creative;
import com.xrtb.exchanges.adx.RealtimeBidding;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;
import com.xrtb.pojo.Impression;


public class GoogleBidRequest extends BidRequest {

	public static byte e_key[];
	public static byte i_key[];
	
	ObjectNode root;
	com.google.openrtb.OpenRtb.BidRequest internal;

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
	
	public BidResponse buildNewBidResponse(Impression imp, Campaign camp, Creative creat, double price, String dealId,  int xtime) throws Exception {
		return null;
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
	
	public GoogleBidRequest(InputStream in) throws Exception {
		internal = com.google.openrtb.OpenRtb.BidRequest.parseFrom(in);
		impressions = new ArrayList<Impression>();
		root = BidRequest.factory.objectNode();
		
		root.put("id",internal.getId());
		root.put("at",internal.getAt().getNumber());
		ProtocolStringList list = internal.getBadvList();
		root.put("badv", getAsStringList(BidRequest.factory.arrayNode(), list));
		root.put("tmax", internal.getTmax());
		
		makeSiteOrApp();
		makeDevice();
		
	}
	
	void makeDevice() {

		if (!internal.hasDevice())
			return;
		
		ObjectNode node = BidRequest.factory.objectNode();
		node.put("ip", internal.getDevice().getIp());
		node.put("language", internal.getDevice().getLanguage());
		node.put("os",internal.getDevice().getOs());
		node.put("osv",internal.getDevice().getOsv());
		node.put("carrier",internal.getDevice().getCarrier());
		node.put("connectiontype",internal.getDevice().getConnectiontype().getNumber());
		node.put("didmd5",internal.getDevice().getDidmd5());
		node.put("didsha1",internal.getDevice().getDidsha1());
		node.put("dnt",internal.getDevice().getDnt());
		node.put("devicetype", internal.getDevice().getDevicetype().getNumber());
		if (internal.getDevice().hasGeo()) {
			ObjectNode geo = BidRequest.factory.objectNode();
			node.put("geo", geo);
			geo.put("country",internal.getDevice().getGeo().getCountry());
			geo.put("type",internal.getDevice().getGeo().getType().getNumber());
			geo.put("lat",internal.getDevice().getGeo().getLat());
			geo.put("lon",internal.getDevice().getGeo().getLon());
			geo.put("city",internal.getDevice().getGeo().getCity());
			geo.put("region",internal.getDevice().getGeo().getRegion());
			geo.put("metro",internal.getDevice().getGeo().getMetro());
			geo.put("utcoffset",internal.getDevice().getGeo().getUtcoffset());
		}
		root.put("device", node);
		
	}
	
	void makeSiteOrApp() {
		ObjectNode node = BidRequest.factory.objectNode();
		if (internal.hasSite()) {
			root.put("site", node);
			node.put("id", internal.getSite().getId());
			node.put("name", internal.getSite().getName());
			node.put("cat", getAsStringList(BidRequest.factory.arrayNode(),internal.getSite().getCatList()));
			node.put("keywords",internal.getSite().getKeywords());
			node.put("page", internal.getSite().getPage());
			node.put("ref", internal.getSite().getRef());
			node.put("search", internal.getSite().getSearch());
			node.put("cat", getAsStringList(BidRequest.factory.arrayNode(),internal.getSite().getCatList()));
			node.put("privacypolicy", internal.getApp().getPrivacypolicy());
			if (internal.getSite().hasPublisher()) {
				ObjectNode pub = BidRequest.factory.objectNode();
				pub.put("id",internal.getSite().getPublisher().getId());
				pub.put("name", internal.getSite().getPublisher().getId());
				node.put("publisher", pub);
			}
		} else {
			root.put("app", node);
			node.put("id", internal.getApp().getId());
			node.put("name", internal.getApp().getName());
			node.put("cat", getAsStringList(BidRequest.factory.arrayNode(),internal.getApp().getCatList()));
			node.put("keywords",internal.getApp().getKeywords());
			node.put("bundle", internal.getApp().getBundle());
			node.put("domain", internal.getApp().getDomain());
			node.put("cat", getAsStringList(BidRequest.factory.arrayNode(),internal.getApp().getCatList()));
			node.put("privacypolicy", internal.getApp().getPrivacypolicy());
			if (internal.getApp().hasPublisher()) {
				ObjectNode pub = BidRequest.factory.objectNode();
				pub.put("id",internal.getApp().getPublisher().getId());
				pub.put("name", internal.getApp().getPublisher().getId());
				node.put("publisher", pub);
			}
		}
	
	}
	
	void makeImpressions() {
		ArrayNode array = BidRequest.factory.arrayNode();
		root.put("imp",array);
		for (int i=0;i<internal.getImpCount();i++) {
			Imp imp = internal.getImp(i);
		}
	}
	
	ArrayNode getAsStringList(ArrayNode node, ProtocolStringList list) {
		for (int i=0; i<list.size();i++) {
			node.add(list.get(i));
		}
		return node;
	}
}
