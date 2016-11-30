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
import com.xrtb.common.DeviceType;
import com.xrtb.common.Node;
import com.xrtb.exchanges.adx.RealtimeBidding.BidRequest.AdSlot;
import com.xrtb.exchanges.adx.RealtimeBidding.BidRequest.AdSlot.SlotVisibility;
import com.xrtb.exchanges.adx.RealtimeBidding.BidRequest.Mobile;
import com.xrtb.exchanges.adx.RealtimeBidding.BidRequest.Mobile.DeviceOsVersion;
import com.xrtb.exchanges.adx.RealtimeBidding.BidRequest.Video.VideoFormat;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;
import com.xrtb.pojo.Video;

interface Command {
	void runCommand(RealtimeBidding.BidRequest x, ObjectNode root, Map db, String key);
}

public class AdxBidRequest extends BidRequest {

	public static byte e_key[];
	public static byte i_key[];

	static Map<String, Command> methodMap = new HashMap<String, Command>();

	static {

		methodMap.put("device", new Command() {
			public void runCommand(RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {
				String ip =  AdxBidRequest.convertIp(x.getIp());
				String ua = x.getUserAgent();

				ObjectNode device = AdxBidRequest.factory.objectNode();
				device.put("ua", ua);
				device.put("ip", ip);
				root.put("device", device);
				Mobile m = x.getMobile();

				if (m != null) {
					if (m.hasBrand())
						device.put("make", m.getBrand());
					if (m.hasScreenWidth())
						device.put("w",m.getScreenWidth());
					if (m.hasScreenHeight())
						device.put("h",m.getScreenHeight());
					if (x.getDetectedLanguageList().size() > 0)
						device.put("language",x.getDetectedLanguage(0));
					
					ObjectNode app = null;
					if (m.hasAppName()) {
						app = (ObjectNode)root.get("app");
						if (app == null) {
							app = AdxBidRequest.factory.objectNode();
						}
						root.put("app",app);
						app.put("name", m.getAppName());
					}
					if (m.hasAppId()) {
						app = (ObjectNode)root.get("app");
						if (app == null) {
							app = AdxBidRequest.factory.objectNode();
						}
						String apid = m.getAppId();
						app.put("id",apid);
					}
					
					if (m.hasEncryptedHashedIdfa()) {
						ByteString bs = m.getEncryptedHashedIdfa();
						byte [] barry = bs.toByteArray();
						String adId = null;
						try {
							adId = AdxWinObject.decryptIfa(barry);
							device.put("ifa", adId);
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
					
					if (m.hasMobileDeviceType()) {
						device.put("devicetype",DeviceType.adxToRtb(m.getMobileDeviceType().toString()));
					} else
						device.put("devicetype",DeviceType.MobileTablet);
		
					if (m.hasModel()) {
						device.put("model", BidRequest.factory.textNode(m.getModel()));
					}
					if (m.hasCarrierId()) {
						device.put("carrier", BidRequest.factory.numberNode(m.getCarrierId()));
					}

					if (m.hasPlatform()) {
						device.put("os", m.getPlatform());
					}
					
					if (m.hasOsVersion()) {
						DeviceOsVersion osv = m.getOsVersion();
						StringBuilder sb = new StringBuilder();
						if (osv.hasOsVersionMajor()) {
							sb.append(osv.getOsVersionMajor());
						}
						if (osv.hasOsVersionMinor()) {
							sb.append(".");
							sb.append(osv.getOsVersionMinor());
						}
						if (osv.hasOsVersionMicro()) {
							sb.append(".");
							sb.append(osv.getOsVersionMicro());
						}
						device.put("osv", BidRequest.factory.textNode(sb.toString()));
					}
				} else {
					device.put("language",x.getDetectedLanguage(0));
					device.put("devicetype",DeviceType.PersonalComputer);
				}
				
				ObjectNode geo = BidRequest.factory.objectNode();
				device.put("geo", geo);
				if (x.hasPostalCode()) {
					geo.put("zip", BidRequest.factory.textNode(x.getPostalCode()));
				}

				
			};
		});
		
		

		methodMap.put("user", new Command() {
			public void runCommand(RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {

			};
		});

		methodMap.put("imp", new Command() {
			public void runCommand(RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {
				int ads = x.getAdslotCount();
				Video video = null;
				boolean isVideo = false;
				if (x.hasVideo()) {
					if (x.hasVideo()) {
						RealtimeBidding.BidRequest.Video gv = x.getVideo();
						video = new Video();
						video.maxduration = gv.getMaxAdDuration();
						video.minduration = gv.getMinAdDuration();
						List<VideoFormat> formats = gv.getAllowedVideoFormatsList();
						for (VideoFormat v : formats) {
							System.out.println("FORMAR: " + v.toString());
						}
					}
					isVideo = true;
				}

				ArrayNode impressions = BidRequest.factory.arrayNode();
				root.put("imp", impressions);
				
				for (int i = 0; i < ads; i++) {
					ObjectNode imp = BidRequest.factory.objectNode();
					impressions.add(imp);

					ObjectNode xx = BidRequest.factory.objectNode();
					if (isVideo) {
						imp.put("video", xx);
						xx.put("maxduration", BidRequest.factory.numberNode(video.maxduration));
						xx.put("minduration", BidRequest.factory.numberNode(video.minduration));
					} else {
						imp.put("banner", xx);
					}

					AdSlot as = x.getAdslot(i);
					List<Integer> list= as.getExcludedAttributeList();
					ArrayNode arn = BidRequest.factory.arrayNode();
					for (int j=0;j<list.size();j++) {
						arn.add(list.get(j));
					}
					imp.put("battr", arn);
					
					int n = 0;
					if (as.hasSlotVisibility()) {
						SlotVisibility sv = as.getSlotVisibility();
						n = sv.getNumber();
						switch(n) {
						case 0:   // none
							break;
						case 1:   // above
							break;
						case 2:	  // below
							n = 3;
						default:
							n = 0;
						}
					} else {
						xx.put("pos", n);
					}
					
					int w = 0, h = 0;
					try {
						w = as.getWidth(i);
						h = as.getHeight(i);

					} catch (Exception error) {
						w = -1;
						h = -1;
					}
					xx.put("w", BidRequest.factory.numberNode(w));
					xx.put("h", BidRequest.factory.numberNode(h));

					double bidFloor = new Double(as.getMatchingAdData(i).getMinimumCpmMicros() / 1000000);
					int adSlotId = as.getId();

					imp.put("bidfloor", BidRequest.factory.numberNode(bidFloor));
					imp.put("id", BidRequest.factory.textNode(Integer.toString(adSlotId)));
					
					
				}

			};
		});


		methodMap.put("site", new Command() {
			public void runCommand(RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {
				if (x.hasMobile())
					return;

			};
		});

		methodMap.put("app", new Command() {
			public void runCommand(RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {
				if (x.hasMobile() == false)
					return;
			};
		});

		methodMap.put("user", new Command() {
			public void runCommand(RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {

			};
		});

		methodMap.put("BidRequest.detected_language", new Command() {
			public void runCommand(RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {
				d.put(key, x.getDetectedLanguage(0));
			};
		});

		methodMap.put("BidRequest.postal_code", new Command() {
			public void runCommand(RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {
				if (x.hasPostalCode())
					d.put(key, x.getPostalCode());
				else
					d.put(key, null);
			};
		});

		methodMap.put("BidRequest.postal_code_prefix", new Command() {
			public void runCommand(RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {
				if (x.hasPostalCodePrefix())
					d.put(key, x.getPostalCodePrefix());
				else
					d.put(key, null);
			};
		});

		methodMap.put("BidRequest.google_user_id", new Command() {
			public void runCommand(RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {
				if (x.hasGoogleUserId())
					d.put(key, x.getGoogleUserId());
				else
					d.put(key, null);
			};
		});

		methodMap.put("BidRequest.seller_network_id", new Command() {
			public void runCommand(RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {
				if (x.hasSellerNetworkId())
					d.put(key, x.getSellerNetworkId());
				else
					d.put(key, null);
			};
		});

		methodMap.put("BidRequest.publisher_settings_list_id", new Command() {
			public void runCommand(RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {
				if (x.hasPublisherSettingsListId())
					d.put(key, x.getPublisherSettingsListId());
				else
					d.put(key, null);
			};
		});

		methodMap.put("BidRequest.anonymous_id", new Command() {
			public void runCommand(RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {
				if (x.hasAnonymousId())
					d.put(key, x.getAnonymousId());
				else
					d.put(key, null);
			};
		});

		methodMap.put("BidRequest.constrained_usage_google_user_id", new Command() {
			public void runCommand(RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {
				if (x.hasConstrainedUsageGoogleUserId())
					d.put(key, x.getConstrainedUsageGoogleUserId());
				else
					d.put(key, null);
			};
		});

		methodMap.put("BidRequest.geo_criteria_id", new Command() {
			public void runCommand(RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {
				if (x.hasGeoCriteriaId())
					d.put(key, x.getGeoCriteriaId());
			};
		});

		methodMap.put("BidRequest.mobile.screen_orientation", new Command() {
			public void runCommand(RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {
				Mobile m = x.getMobile();
				if (m == null)
					return;
				if (m.hasScreenOrientation()) {
					d.put(key, m.getScreenOrientation());
				}

			};
		});

		methodMap.put("BidRequest.mobile.is_interstital_request", new Command() {
			public void runCommand(RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {
				Mobile m = x.getMobile();
				if (m == null)
					return;
				if (m.hasIsInterstitialRequest()) {
					d.put(key, m.getIsInterstitialRequest());
				}

			};
		});

	}

	List<Integer> excludedCategories = new ArrayList<Integer>();
	List<Integer> allowedVendorTypeList = new ArrayList<Integer>();
	List<Integer> excludedAttributesList = new ArrayList<Integer>();

	int adSlotId;

	ObjectNode root;

	boolean isApp = false;

	public static final String ADX = "adx";

	/**
	 * The protobuf version of the bid request
	 */
	RealtimeBidding.BidRequest internal;

	/**
	 * Empty constructor
	 */
	public AdxBidRequest() {

	}

	/**
	 * Make bid request from a string version of the protobuf.
	 * 
	 * @param str
	 *            String. The string to read.
	 * @throws Exception
	 *             on parsing errors.
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
	
		return true;
	}

	/**
	 * Build the bid response from the bid request, campaign and creatives
	 */
	@Override
	public BidResponse buildNewBidResponse(Campaign camp, Creative creat, int xtime) throws Exception {
		Integer category = (Integer) creat.extensions.get("category");
		Integer type = (Integer) creat.extensions.get("vendorType");
		AdxBidResponse response = null;

		response = new AdxBidResponse(this, camp, creat);

		response.slotSetId(adSlotId);
		response.slotSetMaxCpmMicros((int) creat.getPrice());
		response.adSetHeight(this.h);
		response.adSetWidth(this.w);

		response.adAddClickThroughUrl(clickthrough);
		;

		if ((type = (Integer) creat.extensions.get("vendorType")) != null)
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
	public void writeNoBid(HttpServletResponse response, long time) throws Exception {
		// new AdxBidResponse((int)time).writeTo(response);
		AdxBidResponse resp = new AdxBidResponse();
		resp.setNoBid();
		resp.writeTo(response);
	}

	/**
	 * Return the no bid code. Note, for Adx. you have to return 200
	 * 
	 * @return int. The return code.
	 */
	@Override
	public int returnNoBidCode() {
		return RTBServer.BID_CODE;
	}

	/**
	 * Return the application type this bid request/response uses
	 * 
	 * @return String. The content type to return.
	 */
	@Override
	public String returnContentType() {
		return "application/octet-string";
	}

	static int TOTAL = 1;

	/**
	 * Constructor using input stream
	 * 
	 * @param in
	 *            InputStream. Content of the body as input stream
	 * @throws Exception
	 *             on I/O or parsing errors.
	 */
	public AdxBidRequest(InputStream in) throws Exception {
		
		root = BidRequest.factory.objectNode();
		internal = RealtimeBidding.BidRequest.parseFrom(in);
		
		internalSetup();

		//System.out.println("========>" + TOTAL);
		TOTAL++;
		
		this.id = convertToHex(internal.getId());
		root.put("id", BidRequest.factory.textNode(this.id));
		database.put("exchange", ADX);
	
		if (internal.hasEncryptedHyperlocalSet()) {
			ByteString bs = internal.getEncryptedHyperlocalSet();
		}

		if (internal.hasIsTest())
			root.put("is_test", BidRequest.factory.booleanNode(internal.getIsTest()));

		byte[] bytes = internal.toByteArray();
		String str = new String(Base64.encodeBase64(bytes));
		root.put("protobuf", str);
		
		//BidRequest.AdSlot.excluded_sensitive_category + BidRequest.AdSlot.excluded_product_category

		AdSlot ad = internal.getAdslot(0);
		List<Integer> cl = ad.getExcludedProductCategoryList();
		List<Integer> cs = ad.getExcludedSensitiveCategoryList();
		ArrayNode list = BidRequest.factory.arrayNode();
		for (Integer x : cl) {
			list.add(x);
		}
		for (Integer x : cs) {
			list.add(x);
		}
		
		root.put("bcat",list);
		
		ArrayNode nodes = (ArrayNode)root.get("imp");
		ObjectNode node = (ObjectNode)nodes.get(0);
		if (node.get("video")!=null) {
			node = (ObjectNode)node.get("video");
		} else {
			node = (ObjectNode)node.get("banner");
		}
		w = node.get("w").asInt();
		h = node.get("h").asInt();
		if (root.get("app")==null) {
			isApp = false;
			node = (ObjectNode)root.get("site");
			if (node == null) {
				 node =  BidRequest.factory.objectNode();
				 root.put("sitei",node);
			}
		}
		else {
			isApp = true;
			node = (ObjectNode)root.get("app");
		}
		if (internal.hasUrl()) {
			if (internal.hasSellerNetworkId()) 
				node.put("id", Integer.toString(internal.getSellerNetworkId()));
			node.put("url", internal.getUrl());
		}
		
		System.out.println(internal);
		System.out.println("=======================================================================");
		System.out.println(root);
		
	}

	static String makeKey(String s) {
		StringBuilder key = new StringBuilder("BidRequest.");
		key.append(s);
		return key.toString();
	}

	void internalSetup() {

		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String [] codes = key.split("\\.");
			Command c = methodMap.get(codes[0]);
			if (c != null)
				c.runCommand(internal, root, database, key);
		}

		rootNode = (JsonNode) root;
	}

	/**
	 * Convert IP address to dotted decimal (ipv4) and coloned decimal (ipv6)
	 * 
	 * @param ip
	 *            ByteString. The bytes to decode
	 * @return String. The ip address form of the byte stream.
	 */
	protected static String convertIp(ByteString ip) {
		ByteBuffer b = ip.asReadOnlyByteBuffer();
		StringBuilder sb = new StringBuilder();
		if (ip.size() == 3) {
			for (int i = 0; i < ip.size(); i++) {
				sb.append(Integer.toUnsignedString(0xff & b.get(i)));
				sb.append(".");
			}
			sb.append("0");
		} else {
			for (int i = 0; i < ip.size(); i++) {
				sb.append(Integer.toUnsignedString(0xff & b.get(i)));
				sb.append(":");
			}
			sb.append("0");
		}
		return sb.toString();
	}

	/**
	 * Return the hex string
	 * 
	 * @param ip
	 *            ByteString. Source to convert
	 * @return String. The string encoded version of the source, as a string og
	 *         hex digits.
	 */
	protected static String convertToHex(ByteString ip) {
		ByteBuffer b = ip.asReadOnlyByteBuffer();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ip.size(); i++) {
			sb.append(Integer.toHexString(0xff & b.get(i)));
		}
		return sb.toString();
	}

	@Override
	public void handleConfigExtensions(Map extension) throws Exception {
		String key = (String) extension.get("e_key");
		AdxWinObject.encryptionKeyBytes = e_key = javax.xml.bind.DatatypeConverter.parseBase64Binary(key);
		key = (String) extension.get("i_key");
		AdxWinObject.integrityKeyBytes = i_key = javax.xml.bind.DatatypeConverter.parseBase64Binary(key);
	}

}
