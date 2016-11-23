package com.xrtb.exchanges.adx;

import java.io.InputStream;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.protobuf.ByteString;
import com.google.protobuf.ProtocolStringList;
import com.xrtb.bidder.RTBServer;
import com.xrtb.common.Campaign;
import com.xrtb.common.Creative;
import com.xrtb.exchanges.adx.RealtimeBidding.BidRequest.AdSlot;
import com.xrtb.exchanges.adx.RealtimeBidding.BidRequest.Mobile;
import com.xrtb.exchanges.adx.RealtimeBidding.BidRequest.Mobile.DeviceOsVersion;
import com.xrtb.exchanges.adx.RealtimeBidding.BidRequest.Video.VideoFormat;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;
import com.xrtb.pojo.Video;

interface Command {
    void runCommand(RealtimeBidding.BidRequest x, Map db, String key);
}

public class AdxBidRequest extends BidRequest {
	
	public static byte e_key [];
	public static byte i_key [];
	
	static Map<String, Command> methodMap = new HashMap<String, Command>();
	
	static {
		
		methodMap.put("BidRequest.detected_language", new Command() {
            public void runCommand(RealtimeBidding.BidRequest x, Map d, String key) { 
            	d.put(key, x.getDetectedLanguage(0));
            };
        });
		
		methodMap.put("BidRequest.postal_code", new Command() {
            public void runCommand(RealtimeBidding.BidRequest x, Map d, String key) { 
            	if (x.hasPostalCode()) 
            		d.put(key, x.getPostalCode()); 
            	else
            		d.put(key, null);
            };
        });
		
		methodMap.put("BidRequest.postal_code_prefix", new Command() {
            public void runCommand(RealtimeBidding.BidRequest x, Map d, String key) { 
            	if (x.hasPostalCodePrefix()) 
            		d.put(key, x.getPostalCodePrefix()); 
            	else
            		d.put(key, null);
            };
        });
		
		methodMap.put("BidRequest.google_user_id", new Command() {
            public void runCommand(RealtimeBidding.BidRequest x, Map d, String key) { 
            	if (x.hasGoogleUserId()) 
            		d.put(key, x.getGoogleUserId()); 
            	else
            		d.put(key, null);
            };
        });
		
		methodMap.put("BidRequest.seller_network_id", new Command() {
            public void runCommand(RealtimeBidding.BidRequest x, Map d, String key) { 
            	if (x.hasSellerNetworkId()) 
            		d.put(key, x.getSellerNetworkId()); 
            	else
            		d.put(key, null);
            };
        });
		
		methodMap.put("BidRequest.publisher_settings_list_id", new Command() {
            public void runCommand(RealtimeBidding.BidRequest x, Map d, String key) { 
            	if (x.hasPublisherSettingsListId()) 
            		d.put(key, x.getPublisherSettingsListId()); 
            	else
            		d.put(key, null);
            };
        });
		
		methodMap.put("BidRequest.anonymous_id", new Command() {
            public void runCommand(RealtimeBidding.BidRequest x, Map d, String key) { 
            	if (x.hasAnonymousId()) 
            		d.put(key, x.getAnonymousId()); 
            	else
            		d.put(key, null);
            };
        });
		
		methodMap.put("BidRequest.constrained_usage_google_user_id", new Command() {
            public void runCommand(RealtimeBidding.BidRequest x, Map d, String key) { 
            	if (x.hasConstrainedUsageGoogleUserId()) 
            		d.put(key, x.getConstrainedUsageGoogleUserId()); 
            	else
            		d.put(key, null);
            };
        });
		
		methodMap.put("BidRequest.geo_criteria_id", new Command() {
            public void runCommand(RealtimeBidding.BidRequest x, Map d, String key) { 
            	if (x.hasGeoCriteriaId())
            		d.put(key, x.getGeoCriteriaId());
            };
        });
		
		methodMap.put("BidRequest.mobile.platform", new Command() {
            public void runCommand(RealtimeBidding.BidRequest x, Map d, String key) { 
            	Mobile m = x.getMobile();
            	if (m == null)
            		return;
            	if (m.hasPlatform()) {
            		d.put(key, m.getPlatform());
            	}
            	
            };
        });
		
		methodMap.put("BidRequest.mobile.mobile_device_type", new Command() {
            public void runCommand(RealtimeBidding.BidRequest x, Map d, String key) { 
            	Mobile m = x.getMobile();
            	if (m == null)
            		return;
            	if (m.hasMobileDeviceType()) {
            		d.put(key, m.getMobileDeviceType());
            	}
            	
            };
        });
		
		methodMap.put("BidRequest.mobile.screen_orientation", new Command() {
            public void runCommand(RealtimeBidding.BidRequest x, Map d, String key) { 
            	Mobile m = x.getMobile();
            	if (m == null)
            		return;
            	if (m.hasScreenOrientation()) {
            		d.put(key, m.getScreenOrientation());
            	}
            	
            };
        });
		
		methodMap.put("BidRequest.mobile.brand", new Command() {
            public void runCommand(RealtimeBidding.BidRequest x, Map d, String key) { 
            	Mobile m = x.getMobile();
            	if (m == null)
            		return;
            	if (m.hasBrand()) {
            		d.put(key, m.getBrand());
            	} 	
            };
        });
		
		methodMap.put("BidRequest.mobile.is_app", new Command() {
            public void runCommand(RealtimeBidding.BidRequest x, Map d, String key) { 
            	Mobile m = x.getMobile();
            	if (m == null)
            		return;
            	if (m.hasIsApp()) {
            		d.put(key, m.getIsApp());
            	} 	
            };
        });
		
		methodMap.put("BidRequest.mobile.is_interstital_request", new Command() {
            public void runCommand(RealtimeBidding.BidRequest x, Map d, String key) { 
            	Mobile m = x.getMobile();
            	if (m == null)
            		return;
            	if (m.hasIsInterstitialRequest()) {
            		d.put(key, m.getIsInterstitialRequest());
            	}
            	
            };
        });
		
		methodMap.put("BidRequest.mobile.os_version.os_version_major", new Command() {
            public void runCommand(RealtimeBidding.BidRequest x, Map d, String key) { 
            	Mobile m = x.getMobile();
            	if (m == null)
            		return;
            	DeviceOsVersion  v = m.getOsVersion();
            	if (v == null)
            		return;
            	if (v.hasOsVersionMajor()) {
            		d.put(key, v.getOsVersionMajor());
            	}
            	
            };
        });
		
		methodMap.put("BidRequest.mobile.os_version.os_version_minor", new Command() {
            public void runCommand(RealtimeBidding.BidRequest x, Map d, String key) { 
            	Mobile m = x.getMobile();
            	if (m == null)
            		return;
            	DeviceOsVersion  v = m.getOsVersion();
            	if (v == null)
            		return;
            	if (v.hasOsVersionMinor()) {
            		d.put(key, v.getOsVersionMinor());
            	}
            	
            };
        });
		
		methodMap.put("BidRequest.mobile.os_version.os_version_micro", new Command() {
            public void runCommand(RealtimeBidding.BidRequest x, Map d, String key) { 
            	Mobile m = x.getMobile();
            	if (m == null)
            		return;
            	DeviceOsVersion  v = m.getOsVersion();
            	if (v == null)
            		return;
            	if (v.hasOsVersionMicro()) {
            		d.put(key, v.getOsVersionMicro());
            	}
            	
            };
        });
		
	}
	
	List<Integer> excludedCategories = new ArrayList<Integer>();
	List<Integer> allowedVendorTypeList = new ArrayList<Integer>();
	List<Integer> excludedAttributesList = new ArrayList<Integer>();
	
	int adSlotId;
	
	ObjectNode root;
	
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
	
	@Override
	public boolean checkNonStandard(Creative creat, StringBuilder sb) {
		Integer category = (Integer)creat.extensions.get("category");
		Integer type = (Integer)creat.extensions.get("vendorType");
		List<Integer> attrs = (List<Integer>)creat.extensions.get("attributes");
		
		if (category != null && excludedCategories.contains(category)) {
			if (sb != null) {
				sb.append("Excluded categories contains this category: ");
				sb.append(category);
			}
			return false;
		}
		
		if (allowedVendorTypeList != null && allowedVendorTypeList.size() != 0) {
			if (sb != null) {
				sb.append("Allowed vendor types does not contain this vendorType: ");
				sb.append(type);
			}
			return false;
		}
		
		if (attrs != null && excludedAttributesList != null && excludedAttributesList.size() != 0 ) {
			List<Integer> copy = new ArrayList(attrs);
			copy.retainAll(excludedAttributesList);
			if (copy.size() > 0)
				return false;
			return true;
		}
		return true;
	}
	
	/**
	 * Build the bid response from the bid request, campaign and creatives
	 */
	@Override
	public BidResponse buildNewBidResponse(Campaign camp, Creative creat, int xtime) throws Exception {
		Integer category = (Integer)creat.extensions.get("category");
		Integer type = (Integer)creat.extensions.get("vendorType");
		AdxBidResponse response = null;
		
		response = new AdxBidResponse(this,camp,creat);

		response.slotSetId(adSlotId);
		response.slotSetMaxCpmMicros((int)creat.getPrice());
		response.adSetHeight(this.h);
		response.adSetWidth(this.w);
		
		response.adAddClickThroughUrl(clickthrough);;
		
		if ((type = (Integer)creat.extensions.get("vendorType")) != null)
			response.adAddVendorType(type);
		if (category != null)
				response.adAddCategory(type);
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
	 * Write the nobid associated with this bid request
	 */
	@Override
	public void writeNoBid(HttpServletResponse response,  long time) throws Exception {
		//new AdxBidResponse((int)time).writeTo(response);
		AdxBidResponse resp = new AdxBidResponse();
		resp.setNoBid();
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
	
	static int TOTAL = 1;
	/**
	 * Constructor using input stream
	 * @param in InputStream.  Content of the body as input stream
	 * @throws Exception on I/O or parsing errors.
	 */
	public AdxBidRequest(InputStream in) throws Exception {
		
		root = BidRequest.factory.objectNode();
		 
		internal = RealtimeBidding.BidRequest.parseFrom(in);
		
		System.out.println("========>" + TOTAL);
		TOTAL++;
		int ads = internal.getAdslotCount();
		ByteString id = internal.getId();
		String ip = convertIp(internal.getIp());
		this.id = convertToHex(internal.getId());
		String ua = internal.getUserAgent();

		ObjectNode device = AdxBidRequest.factory.objectNode();
		device.put("ua", ua);
		root.put("device", device);
		
		ObjectNode user = BidRequest.factory.objectNode();
		root.put("user", user);
		
		root.put("id", BidRequest.factory.textNode(this.id));
		device.put("ip", ip);
		
		database.put("exchange", ADX);
		
		ArrayNode impressions = BidRequest.factory.arrayNode();
		root.put("imp",impressions);
		
		boolean isApp = false;
		

		if (internal.hasMobile()) {
			Mobile m = internal.getMobile();

			if (m.hasPlatform()) {
				device.put("model", BidRequest.factory.textNode(m.getPlatform()));
			}
			if (m.hasCarrierId()) {
				device.put("carrier", BidRequest.factory.numberNode(m.getCarrierId()));
			}
			if (m.hasOsVersion()) {
				DeviceOsVersion d = m.getOsVersion();
				StringBuilder sb = new StringBuilder();
				if (d.hasOsVersionMajor()) {
					sb.append(d.getOsVersionMajor());
				}
				if (d.hasOsVersionMinor()) {
					sb.append(".");
					sb.append(d.getOsVersionMinor());
				}
				if (d.hasOsVersionMicro()) {
					sb.append(".");
					sb.append(d.getOsVersionMicro());
				}
				device.put("osv", BidRequest.factory.textNode(sb.toString()));
				if (m.hasIsApp()) 
					isApp = m.getIsApp();
			}
		}
		
		ObjectNode appOrSite = BidRequest.factory.objectNode();
		if (isApp) 
			root.put("app", appOrSite);
		else
			root.put("site", appOrSite);

		boolean isVideo = false;
		if (internal.hasVideo()) {
			if (internal.hasVideo()) {
				RealtimeBidding.BidRequest.Video  gv = internal.getVideo();
				this.video = new Video();
				video.maxduration = gv.getMaxAdDuration();
				video.minduration = gv.getMinAdDuration();
				List<VideoFormat> formats = gv.getAllowedVideoFormatsList();
				for (VideoFormat v : formats) {
					System.out.println("FORMAR: " + v.toString());
				}
			}
			isVideo = true;
		}
		
		for (int i=0; i<ads;i++) {
			ObjectNode imp = BidRequest.factory.objectNode();
			impressions.add(imp);
			
			ObjectNode x = BidRequest.factory.objectNode();
			if (isVideo) {
				imp.put("video", x);
				x.put("maxduration", BidRequest.factory.numberNode(video.maxduration));
				x.put("minduration", BidRequest.factory.numberNode(video.minduration));
			} else {
				imp.put("banner", x);
			}
			
			AdSlot as = internal.getAdslot(i);
			try {
				this.w = as.getWidth(i);
				this.h = as.getHeight(i);

				
			} catch (Exception error) {
				this.w = -1;
				this.h = -1;
			}
			x.put("w", BidRequest.factory.numberNode(this.w));
			x.put("h", BidRequest.factory.numberNode(this.h));
			
			this.bidFloor = new Double(as.getMatchingAdData(i).getMinimumCpmMicros()/1000000);
			this.adSlotId = as.getId();
			
			imp.put("bidfloor", BidRequest.factory.numberNode(this.bidFloor));
			imp.put("id", BidRequest.factory.textNode(Integer.toString(this.adSlotId)));
				
			excludedCategories = as.getExcludedSensitiveCategoryList(); 			
			allowedVendorTypeList = as.getAllowedVendorTypeList();		
			excludedAttributesList = as.getExcludedAttributeList();
			
			database.put("BidRequest.AdSlot.excluded_attribute",as.getExcludedAttributeList());
			database.put("BidRequest.AdSlot.allowed_vendor_type",as.getAllowedVendorTypeList());
			database.put("BidRequest.AdSlot.matching_ad_data[adgroup_id]",as.getMatchingAdData(i).getAdgroupId());
			
			Map m = as.getAllFields();
			System.out.println("XXXXXXXXXXXXXXX\n" + m + "\nXXXXXXXXXXXXXXX");
		}
		
		
		if (internal.hasEncryptedHyperlocalSet()) {
			ByteString bs = internal.getEncryptedHyperlocalSet();
		}
		
		ProtocolStringList list = internal.getDetectedLanguageList();
		ArrayNode ar = BidRequest.factory.arrayNode();
		List<String> slist = new ArrayList<String>();
		for (String s : list) {
			ar.add(s);
		}
		
		user.put("detected_language",ar);
		ObjectNode geo = BidRequest.factory.objectNode();
		device.put("geo", geo);
		if (internal.hasPostalCode()) {
			geo.put("zip", BidRequest.factory.textNode(internal.getPostalCode()));
		}
		
		if (internal.hasIsTest()) 
			root.put("is_test", BidRequest.factory.booleanNode(internal.getIsTest()));
		
		if (internal.hasGoogleUserId())
			user.put("google_user_id", BidRequest.factory.textNode(internal.getGoogleUserId()));
		if (internal.hasCookieAgeSeconds())
			user.put("cookie_age_seconds", BidRequest.factory.numberNode(internal.getCookieAgeSeconds()));
		
		System.out.println(internal);
		byte [] bytes = internal.toByteArray();
		String str = new String(Base64.encodeBase64(bytes));
		System.out.println("=======================\n" + str  + "\n=======================");
		
		internalSetup();
	}
	
	static String makeKey(String s) {
		StringBuilder key =  new StringBuilder("BidRequest.");
		key.append(s);
		return key.toString();
	}
	
	void internalSetup() {

		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			if (key.startsWith("BidRequest")) {
				methodMap.get(key).runCommand(internal, database, key);  
			}
		}
		
		rootNode = (JsonNode)root;
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
	
	@Override
	public void handleConfigExtensions(Map extension) throws Exception {
		String key = (String)extension.get("e_key");
		AdxWinObject.encryptionKeyBytes = e_key = javax.xml.bind.DatatypeConverter.parseBase64Binary(key);
		key = (String)extension.get("i_key");
		AdxWinObject.integrityKeyBytes = i_key = javax.xml.bind.DatatypeConverter.parseBase64Binary(key);
	}
	
}
