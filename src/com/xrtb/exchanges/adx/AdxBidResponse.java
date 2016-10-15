package com.xrtb.exchanges.adx;

import java.io.ByteArrayOutputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.xrtb.common.Campaign;
import com.xrtb.common.Creative;
import com.xrtb.exchanges.adx.RealtimeBidding.BidRequest.AdSlot.Builder;
import com.xrtb.exchanges.adx.RealtimeBidding.BidResponse.Ad;
import com.xrtb.exchanges.adx.RealtimeBidding.BidResponse.Ad.AdSlot;
import com.xrtb.exchanges.adx.RealtimeBidding.BidResponse.Ad.AdSlotOrBuilder;
import com.xrtb.pojo.BidResponse;


public class AdxBidResponse extends BidResponse {

	@JsonIgnore
	private transient RealtimeBidding.BidResponse internal;
	
	@JsonIgnore
	private transient com.xrtb.exchanges.adx.RealtimeBidding.BidResponse.Ad.AdSlot.Builder  slotBuilder;
	
	@JsonIgnore
	private transient com.xrtb.exchanges.adx.RealtimeBidding.BidResponse.Ad.Builder adBuilder;
	
	@JsonIgnore
	private transient List<Ad> adList = new ArrayList();
	
	public  void setNoBid () throws Exception {
		this.exchange = AdxBidRequest.ADX;
		internal = RealtimeBidding.BidResponse.newBuilder()
			.setProcessingTimeMs(1)
			.build();
	}
	
	// https://developers.google.com/ad-exchange/rtb/response-guide
	
	public AdxBidResponse() throws Exception {
		
		slotBuilder = RealtimeBidding.BidResponse.Ad.AdSlot.newBuilder();

		adBuilder = RealtimeBidding.BidResponse.Ad.newBuilder();	
		exchange = AdxBidRequest.ADX;
		
	}
	
	public AdxBidResponse(AdxBidRequest br, Campaign camp, Creative creat) {
		this.camp = camp;
		this.creat = creat;
		this.br = br;
		
		slotBuilder = RealtimeBidding.BidResponse.Ad.AdSlot.newBuilder();
		adBuilder = RealtimeBidding.BidResponse.Ad.newBuilder();
		exchange = AdxBidRequest.ADX;
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
		adBuilder.setHtmlSnippet(snippet);
		this.forwardUrl = snippet;
	}
	
	public void adSetWidth(int width) {
		adBuilder.setWidth(width);
		this.width = width;
	}
	
	public void adSetHeight(int height) {
		adBuilder.setHeight(height);
		this.height = height;
	}
	
	/////////////////////////////////////////
	
	@Override
	public String toString()  {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try {
			internal.writeTo(bout);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return bout.toString();
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
