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

	transient RealtimeBidding.BidResponse internal;
	transient com.xrtb.exchanges.adx.RealtimeBidding.BidResponse.Ad.AdSlot.Builder  slotBuilder;
	transient com.xrtb.exchanges.adx.RealtimeBidding.BidResponse.Ad.Builder adBuilder;
	transient List<Ad> adList = new ArrayList();
	
	public AdxBidResponse(long time) {
		this.exchange = AdxBidRequest.ADX;
		internal = RealtimeBidding.BidResponse.newBuilder()
			.setProcessingTimeMs((int)time)
			.build();
	}
	
	// https://developers.google.com/ad-exchange/rtb/response-guide
	
	public AdxBidResponse() throws Exception {
		
		slotBuilder = RealtimeBidding.BidResponse.Ad.AdSlot.newBuilder();

		adBuilder = RealtimeBidding.BidResponse.Ad.newBuilder();	
		exchange = AdxBidRequest.ADX;
		
	/*	AdSlot slot = RealtimeBidding.BidResponse.Ad.AdSlot.newBuilder()
				.setId(132)
				.setMaxCpmMicros(150000)
				.build();
	
		Ad ad = RealtimeBidding.BidResponse.Ad.newBuilder()
				.addClickThroughUrl(clickthrough)
				.addVendorType(113)
				.addCategory(3)
				.setHtmlSnippet(html_snippet)
				.addAdslot(slot)
				.build(); */
			
		//list.add(ad);
		/*internal = RealtimeBidding.BidResponse.newBuilder()
				.setProcessingTimeMs(32)
				.addAllAd(list)
				.build();
				
		System.out.println(internal.getSerializedSize());
		System.out.println(internal); */
	}
	
	public AdxBidResponse(AdxBidRequest br, Campaign camp, Creative creat) {
		this.camp = camp;
		this.creat = creat;
		this.br = br;
		
		slotBuilder = RealtimeBidding.BidResponse.Ad.AdSlot.newBuilder();
		adBuilder = RealtimeBidding.BidResponse.Ad.newBuilder();
		exchange = AdxBidRequest.ADX;
	}
	
	public AdxBidResponse build() {
		AdSlot  adSlot = slotBuilder.build();
		Ad ad = adBuilder.addAdslot(adSlot).build();	
		adList.add(adBuilder.build());
	
		internal = RealtimeBidding.BidResponse.newBuilder()
				.setProcessingTimeMs(32)
				.addAllAd(adList)
				.build();
		return this;
	}
	
	/////////////////////////////////////////

	public void slotSetMaxCpmMicros(long x) {
		slotBuilder.setMaxCpmMicros(x);
	}
	
	public void slotSetId(int id) {
		slotBuilder.setId(id);
	}
	
	public AdSlot getAdSlot() {
		return internal.getAd(0).getAdslot(0);
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
	}
	
	public void adSetWidth(int width) {
		adBuilder.setWidth(width);
	}
	
	public void adSetHeight(int height) {
		adBuilder.setHeight(height);
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
