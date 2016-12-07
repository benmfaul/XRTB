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
 * 
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
	
	public  void setNoBid () throws Exception {
		this.exchange = AdxBidRequest.ADX;
		internal = RealtimeBidding.BidResponse.newBuilder()
			.setProcessingTimeMs(1)
			.build();
	}
	
	@Override
	public String getTemplate() throws Exception {
		StringBuilder sb = new StringBuilder();
		String str = Configuration.getInstance().masterTemplate.get(exchange);
		if (str == null)
			throw new Exception("No configured template for: " + exchange);
		sb = new StringBuilder(str);

		System.out.println("ADID: " + camp.adId);
		macroSubs(sb);
		MacroProcessing.replace(creat.macros, br, creat, camp.adId, sb);
		admAsString = sb.toString();
		return sb.toString();
	}
	
	public void macroSubs(StringBuilder sb) {
		String lat = "0.0";
		String lon = "0.0";
		if (br.lat != null && br.lat != 0.0) {
			lat = br.lat.toString();
			lon = br.lon.toString();
		}
		Configuration config = Configuration.getInstance();
		replaceAll(sb, "{creative_forward_url}", creat.forwardurl);

		try {
			MacroProcessing.replace(creat.macros, br, creat, adid, sb);
			MacroProcessing.replace(Configuration.getInstance().macros, br, creat, adid, sb);
		} catch (Exception e) {

			e.printStackTrace();
		}
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
		adList.add(adBuilder.build());
	
		internal = RealtimeBidding.BidResponse.newBuilder()
				.setProcessingTimeMs(n)
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
	public void adSetHtmlSnippet(String snippet) {
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
	}
	
	public void setVideoUrl(String snippet) {
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
			content = DbTools.mapper.writer().withDefaultPrettyPrinter().writeValueAsString(seatBid);
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
	
}

class SeatBid {
	public String seat;
	public List<Bid> bid = new ArrayList();
	public String id;
	public String bidid;
	public String protobuf;
	
	public SeatBid(AdxBidResponse parent) {
		if (parent.br.video != null) {
			System.out.println("VIDEO");
		}
		AdxBidRequest bx = (AdxBidRequest)parent.br;
		Bid x = new Bid();
		x.id = Integer.toString(bx.adSlotId);
		x.adId = parent.camp.adId;
		x.price = parent.cost / 1000;			// PRICE BID ON ADX is Micros, not Millis, we expect Millis
		x.adm = parent.admAsString;
		x.crid = parent.creat.impid;
		bid.add(x);
	}
}

class Bid {
	public String impid;
	public String id;
	public double price;
	public String adId;
	public String nurl;
	public String cid;
	public String crid;
	public String adomain;
	public String adm;
}
