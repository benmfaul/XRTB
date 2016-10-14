package com.xrtb.exchanges.adx;

import java.io.InputStream;


import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.google.protobuf.ByteString;
import com.xrtb.bidder.RTBServer;
import com.xrtb.common.Campaign;
import com.xrtb.common.Creative;
import com.xrtb.exchanges.adx.RealtimeBidding.BidRequest.AdSlot;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;

public class AdxBidRequest extends BidRequest {
	
	int adSlotId;
	public static final String ADX = "adx";
	
	/**
	 * The protobuf version of the bid request
	 */
	RealtimeBidding.BidRequest internal;
	
	/**
	 * Empty constructor
	 */
	public AdxBidRequest()  {

	}
	
	/**
	 * Make bid request from a string version of the protobuf.
	 * @param str String. The string to read.
	 * @throws Exception on parsing errors.
	 */
	public AdxBidRequest(String str) throws Exception {
		
	}
	
	/**
	 * Interrogate the bid request
	 */
	@Override
	public Object interrogate(String line) {
		return database.get(line);
	}
	
	
	String html_snippet = "%%WINNING_PRICE%%http://localhost:8080/rtb/wins<a href=\"%%CLICK_URL_UNESC%%http%3A%2F%2Fmy.adserver.com%2Fsome%2Fpath%2Fhandleclick%3Fclick%3Dclk\"></a><script src=\"https://ibv.1trnvid.com/app2.js\" data-width=\"320\" data-height=\"480\" data-sid=\"51376\" data-appname=\"Test App\" data-appversion=\"1.0\" data-bundleid=\"test.bundleid\" data-appstoreurl=\"{APPSTORE_URL}\" data-dnt=\"{DNT}\" data-aid=\"{GAID}\" data-idfa=\"{IFA}\" data-lat=\"{LAT}\" data-lon=\"{LON}\" data-custom1=\"\" data-custom2=\"\" data-custom3=\"\"></script>";
	String clickthrough = "http://rtb4free.com/click=1";
	String creativeid = "my-creative-1234ABCD";
	
	/**
	 * Build the bid response from the bid request, campaign and creatives
	 */
	@Override
	public BidResponse buildNewBidResponse(Campaign camp, Creative creat, int xtime) throws Exception {
		AdxBidResponse response = new AdxBidResponse(this,camp,creat);
		response.slotSetId(adSlotId);
		response.slotSetMaxCpmMicros((int)creat.getPrice());
		response.adSetHeight(this.h);
		response.adSetWidth(this.w);
		
		response.adAddClickThroughUrl(clickthrough);
		response.adAddVendorType(113);
		response.adAddCategory(3);
		response.adid = creat.impid;
		
		String html = null;
		
		try {
			html = response.getTemplate();
		} catch (Exception error) {
			error.printStackTrace();
		}
		
		
		response.adSetHtmlSnippet(html);
	
		response.build(xtime);
		return response;
	}
	
	/**
	 * Return's the bid response no bid JSON or other (protoc in Adx for example).
	 * @param reason String. The reason you are returning no bid.
	 * @return String. The reason code.
	 */
	@Override
	public String returnNoBid(String reason) {
		return reason;
	}
	
	/**
	 * Write the nobid associated with this bid request
	 */
	@Override
	public void writeNoBid(HttpServletResponse response,  long time) throws Exception {
		//new AdxBidResponse((int)time).writeTo(response);
		AdxBidResponse resp = new AdxBidResponse();
		resp.build(1);
		resp.writeTo(response);
	}
	
	/**
	 * Return the no bid code. Note, for Adx. you have to return 200
	 * @return int. The return code.
	 */
	@Override
	public int returnNoBidCode() {
		return RTBServer.BID_CODE;
	}
	
	/**
	 * Return the application type this bid request/response uses
	 * @return String. The content type to return.
	 */
	@Override
	public String returnContentType() {
		return "application/octet-string";
	}
	
	/**
	 * Constructor using input stream
	 * @param in InputStream.  Content of the body as input stream
	 * @throws Exception on I/O or parsing errors.
	 */
	public AdxBidRequest(InputStream in) throws Exception {
		internal = RealtimeBidding.BidRequest.parseFrom(in);
		int ads = internal.getAdslotCount();
		ByteString id = internal.getId();
		String ip = convertIp(internal.getIp());
		this.id = convertToHex(internal.getId());
		
		database.put("device.ua", internal.getUserAgent());
		database.put("exchange", ADX);
		
		String ua = internal.getUserAgent();
		for (int i=0; i<ads;i++) {
			AdSlot as = internal.getAdslot(i);
			this.w = as.getWidth(i);
			this.h = as.getHeight(i);
			this.bidFloor = new Double(as.getMatchingAdData(i).getMinimumCpmMicros()/1000000);
			this.adSlotId = as.getId();
			
			database.put("BidRequest.AdSlot.excluded_attribute",as.getExcludedAttributeList());
			database.put("BidRequest.AdSlot.allowed_vendor_type",as.getAllowedVendorTypeList());
			database.put("BidRequest.AdSlot.matching_ad_data[adgroup_id]",as.getMatchingAdData(i).getAdgroupId());
			
			Map m = as.getAllFields();
			System.out.println(m);
		}
	}
	
	/**
	 * Convert IP address to dotted decimal (ipv4) and coloned decimal (ipv6)
	 * @param ip ByteString. The bytes to decode
	 * @return String. The ip address form of the byte stream.
	 */
	protected static String convertIp(ByteString ip) {
		ByteBuffer b = ip.asReadOnlyByteBuffer();
		StringBuilder sb = new StringBuilder();
		if (ip.size() == 3) {
			for (int i=0;i<ip.size();i++) {
				sb.append(Integer.toUnsignedString(0xff & b.get(i)));
				sb.append(".");
			}
			sb.append("0");
		} else {
			for (int i=0;i<ip.size();i++) {
				sb.append(Integer.toUnsignedString(0xff & b.get(i)));
				sb.append(":");
			}
			sb.append("0");
		}
		return sb.toString();
	}
	
	/**
	 * Return the hex string
	 * @param ip ByteString. Source to convert
	 * @return String. The string encoded version of the source, as a string og hex digits.
	 */
	protected static String convertToHex(ByteString ip) {
		ByteBuffer b = ip.asReadOnlyByteBuffer();
		StringBuilder sb = new StringBuilder();
		for (int i=0;i<ip.size();i++) {
			sb.append(Integer.toHexString(0xff & b.get(i)));
		}
		return sb.toString();
	}
}
