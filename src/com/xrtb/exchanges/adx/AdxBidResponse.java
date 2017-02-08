package com.xrtb.exchanges.adx;

import java.io.ByteArrayOutputStream;


import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.common.Creative;

import com.xrtb.exchanges.adx.RealtimeBidding.BidResponse.Ad;
import com.xrtb.exchanges.adx.RealtimeBidding.BidResponse.Ad.AdSlot;

import com.xrtb.pojo.BidResponse;
import com.xrtb.tools.DbTools;
import com.xrtb.tools.MacroProcessing;

/**
 * An Ad Exchange Bid Response. Used to creat the logging record and the response to the bidder.
 * https://developers.google.com/ad-exchange/rtb/response-guide
 * @author Ben M. Faul
 *
 */

public class AdxBidResponse extends BidResponse {

	@JsonIgnore
	private transient RealtimeBidding.BidResponse internal;
	
	@JsonIgnore
	private transient com.xrtb.exchanges.adx.RealtimeBidding.BidResponse.Ad.AdSlot.Builder  slotBuilder;
	
	@JsonIgnore
	private transient com.xrtb.exchanges.adx.RealtimeBidding.BidResponse.Ad.Builder adBuilder;
	
	@JsonIgnore
	private transient List<Ad> adList = new ArrayList();
	
	private transient String adomain;
	
	private transient boolean isNoBid = false;
	
	public  void setNoBid () throws Exception {
		isNoBid = true;
		this.exchange = AdxBidRequest.ADX;
		internal = RealtimeBidding.BidResponse.newBuilder()
			.setProcessingTimeMs(1)
			.build();
	}
	
	// https://developers.google.com/ad-exchange/rtb/response-guide
	
	public AdxBidResponse(byte[] bytes) throws Exception {
		exchange = AdxBidRequest.ADX;
		internal = RealtimeBidding.BidResponse.parseFrom(bytes);
	}
	
	public AdxBidResponse() throws Exception {
		
		slotBuilder = RealtimeBidding.BidResponse.Ad.AdSlot.newBuilder();

		adBuilder = RealtimeBidding.BidResponse.Ad.newBuilder();	
		exchange = AdxBidRequest.ADX;
		
	}
	
	public AdxBidResponse(AdxBidRequest br, Campaign camp, Creative creat) {
		this.camp = camp;
		this.creat = creat;
		this.br = br;
		adid = camp.adId;
		
		slotBuilder = RealtimeBidding.BidResponse.Ad.AdSlot.newBuilder();
		adBuilder = RealtimeBidding.BidResponse.Ad.newBuilder();
		exchange = AdxBidRequest.ADX;
		adomain = camp.adomain;
	}
	
	public AdxBidResponse build(int n) {
		AdSlot  adSlot = slotBuilder.build();
		Ad ad = adBuilder.addAdslot(adSlot).build();	

		adBuilder.setBuyerCreativeId(camp.adId + ":" + creat.impid);
		adList.add(adBuilder.build());
	
		internal = RealtimeBidding.BidResponse.newBuilder()
				.setProcessingTimeMs(10+n)
				.addAllAd(adList)
				.build();
		this.xtime = n;
		
		byte[] bytes = internal.toByteArray();
		protobuf = new String(Base64.encodeBase64(bytes));
		return this;
	}
	
	/////////////////////////////////////////

	public void slotSetMaxCpmMicros(long x) {
		slotBuilder.setMaxCpmMicros(x);
		this.cost = x;
	}
	
	public void slotSetDealId(long id) {
		slotBuilder.setDealId(id);
	}
	
	public void slotSetId(int id) {
		slotBuilder.setId(id);
		this.impid = Integer.toString(id);
	}
	
	///////////////////////////////////////
	
	public void adAddClickThroughUrl(String ct) {
		adBuilder.addClickThroughUrl(ct);
	}
	public void adAddVendorType(int type) {
		adBuilder.addVendorType(type);
	}
	public void adAddCategory(int cat) {
		adBuilder.addCategory(cat);
	}
	
	public void adAddAgencyId(int id) {
		adBuilder.setAgencyId(id);
	}
	
	public void addAttribute(int id) {
		adBuilder.addAttribute(id);
	}
	
	public void adSetImpressionTrackingUrl(String str) {
		StringBuilder sb = new StringBuilder(str);
		MacroProcessing.findMacros(creat.macros,str);
		try {
			MacroProcessing.replace(creat.macros, br, creat, adid, sb);
			adBuilder.addImpressionTrackingUrl(sb.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String adSetHtmlSnippet(String snippet) {
		if (creat.macros.size()==0)
			MacroProcessing.findMacros(creat.macros,snippet);
		StringBuilder sb = new StringBuilder(snippet);
		try {
			MacroProcessing.replace(creat.macros, br, creat, adid, sb);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		admAsString = sb.toString();
		adBuilder.setHtmlSnippet(admAsString);
		forwardUrl = admAsString;
		return admAsString;
	}
	
	public String setVideoUrl(String snippet) {
		StringBuilder sb = new StringBuilder(snippet);
		if (creat.macros.size()==0)
			MacroProcessing.findMacros(creat.macros,snippet);
		try {
			MacroProcessing.replace(creat.macros, br, creat, adid, sb);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		admAsString = sb.toString();
		adBuilder.setVideoUrl(admAsString);
		return admAsString;
	}
	
	public void adSetWidth(int width) {
		adBuilder.setWidth(width);
		this.width = width;
	}
	
	public void adSetHeight(int height) {
		adBuilder.setHeight(height);
		this.height = height;
	}
	
	public com.xrtb.exchanges.adx.RealtimeBidding.BidResponse getInternal() {
		return internal;
	}
	
	/////////////////////////////////////////
	
	@Override
	public String toString()  {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		byte[] bytes = internal.toByteArray();
		protobuf = new String(Base64.encodeBase64(bytes));
		
		SeatBid seatBid = new SeatBid(this);
		seatBid.seat = Configuration.getInstance().seats.get(exchange);
		seatBid.id = br.id;
		seatBid.protobuf = protobuf;
		
		String content = null;
		try {
			content = DbTools.mapper.writer().writeValueAsString(seatBid);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return content;
	}
	
	public void toFile(FileOutputStream f) throws Exception {
		internal.writeTo(f);
	}
	
	@Override
	public void writeTo(HttpServletResponse response) throws Exception {
		response.setContentType("application/octet-string");
		internal.writeTo(response.getOutputStream());
	}
	
	@Override
	public void writeTo(HttpServletResponse response, String x) throws Exception {
		internal.writeTo(response.getOutputStream());
	}
	
	/**
	 * Returns whether the response is actually a no bid.
	 */
	@Override
	public boolean isNoBid() {
		return isNoBid;
	}
}

class SeatBid {
	public String seat;
	public List<Bid> bid = new ArrayList();
	public String id;
	public String bidid;
	public String protobuf;
	
	public SeatBid(AdxBidResponse parent) {
		AdxBidRequest bx = (AdxBidRequest)parent.br;
		Bid x = new Bid();

		x.bidid = x.id = Integer.toString(bx.adSlotId);
		x.adId = parent.camp.adId;
		x.price = parent.cost;			// PRICE BID ON ADX is Micros, not Millis, we expect Millis
		x.adm = parent.admAsString;
		x.crid = parent.creat.impid;
		bid.add(x);
	}
}

class Bid {
	public String impid;
	public String id;
	public String bidid;
	public double price;
	public String adId;
	public String nurl;
	public String cid;
	public String crid;
	public String adomain;
	public String adm;
}
