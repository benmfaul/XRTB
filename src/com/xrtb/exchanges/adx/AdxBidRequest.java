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
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.protobuf.ByteString;
import com.xrtb.bidder.Controller;
import com.xrtb.bidder.RTBServer;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.common.Creative;
import com.xrtb.common.DeviceType;

import com.xrtb.exchanges.adx.RealtimeBidding.BidRequest.AdSlot;
import com.xrtb.exchanges.adx.RealtimeBidding.BidRequest.AdSlot.MatchingAdData;
import com.xrtb.exchanges.adx.RealtimeBidding.BidRequest.AdSlot.MatchingAdData.DirectDeal;
import com.xrtb.exchanges.adx.RealtimeBidding.BidRequest.AdSlot.SlotVisibility;
import com.xrtb.exchanges.adx.RealtimeBidding.BidRequest.BidResponseFeedback;
import com.xrtb.exchanges.adx.RealtimeBidding.BidRequest.Hyperlocal.Point;
import com.xrtb.exchanges.adx.RealtimeBidding.BidRequest.Mobile;
import com.xrtb.exchanges.adx.RealtimeBidding.BidRequest.Device;
import com.xrtb.exchanges.adx.RealtimeBidding.BidRequest.Device.OsVersion;
import com.xrtb.exchanges.adx.RealtimeBidding.BidRequest.Video.VideoFormat;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;
import com.xrtb.pojo.Video;
import com.xrtb.tools.LookingGlass;

interface Command {
	void runCommand(BidRequest br, RealtimeBidding.BidRequest x, ObjectNode root, Map db, String key);
}

public class AdxBidRequest extends BidRequest {

	public static byte e_key[];
	public static byte i_key[];

	static Map<String, Command> methodMap = new HashMap<String, Command>();

	static AdxGeoCodes lookingGlass = (AdxGeoCodes) LookingGlass.symbols.get("@ADXGEO");

	static {

		methodMap.put("device", new Command() {
			public void runCommand(BidRequest br, RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {
				String ip = AdxBidRequest.convertIp(x.getIp());
				String ua = x.getUserAgent();

				ObjectNode device = AdxBidRequest.factory.objectNode();
				device.put("ua", ua);
				device.put("ip", ip);
				root.put("device", device);
				
				Mobile m = x.getMobile();
				if (m != null) {
					ObjectNode app = null;
					if (m.getIsApp() == true) {
						app = (ObjectNode) root.get("app");
						if (app == null) {
							app = AdxBidRequest.factory.objectNode();
						}
						root.put("app", app);
						app.put("name", m.getAppName());
						br.siteName = m.getAppName();
						String apid = m.getAppId();
						app.put("id", apid);
						br.siteId = apid;
					} else {
						app = AdxBidRequest.factory.objectNode();
						root.put("site", app);
					}

					if (m.hasEncryptedHashedIdfa()) {
						ByteString bs = m.getEncryptedHashedIdfa();
						byte[] barry = bs.toByteArray();
						String adId = null;
						try {
							adId = AdxWinObject.decryptIfa(barry);
							device.put("ifa", adId);
						} catch (Exception e) {
							e.printStackTrace();
						}

					}

					if (m.hasEncryptedAdvertisingId()) {
						ByteString bs = m.getEncryptedAdvertisingId();
						String id;
						try {
							id = AdxWinObject.decryptAdvertisingId(bs.toByteArray());
							device.put("ifa", id);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				} else {
					// Site, not mobile
				}

				Device dev = x.getDevice();
				if (device != null) {
					if (dev.hasBrand())
						device.put("make", dev.getBrand());
					if (dev.hasScreenWidth())
						device.put("w", dev.getScreenWidth());
					if (dev.hasScreenHeight())
						device.put("h", dev.getScreenHeight());

	
					if (dev.hasDeviceType()) {
						device.put("devicetype", DeviceType.adxToRtb(dev.getDeviceType().toString()));
					} else
						device.put("devicetype", DeviceType.MobileTablet);

					if (dev.hasModel()) {
						device.put("model", BidRequest.factory.textNode(dev.getModel()));
					}
					if (dev.hasCarrierId()) {
						device.put("carrier", BidRequest.factory.numberNode(dev.getCarrierId()));
					}

					if (dev.hasPlatform()) {
						device.put("os", dev.getPlatform());
					}

					if (dev.hasOsVersion()) {
						OsVersion osv = dev.getOsVersion();
						StringBuilder sb = new StringBuilder();
						if (osv.hasMajor()) {
							sb.append(osv.getMajor());
						}
						if (osv.hasMinor()) {
							sb.append(".");
							sb.append(osv.getMinor());
						}
						if (osv.hasMicro()) {
							sb.append(".");
							sb.append(osv.getMicro());
						}
						device.put("osv", BidRequest.factory.textNode(sb.toString()));
					}
				} else {
					device.put("language", x.getDetectedLanguage(0));
					device.put("devicetype", DeviceType.PersonalComputer);
				}

				ObjectNode geo = BidRequest.factory.objectNode();
				device.put("geo", geo);
				String postal = null;
				if (x.hasPostalCode()) {
					postal = x.getPostalCode();
					geo.put("zip", postal);
				}

				if (x.hasGeoCriteriaId() && lookingGlass != null) {
					Integer geoKey = x.getGeoCriteriaId();
					AdxGeoCode item = lookingGlass.query(geoKey);
					if (item != null) {
						String type = item.type.toLowerCase();
						if (type.equals("city") == false) {
							LookingGlass cz = (LookingGlass) LookingGlass.symbols.get("@ZIPCODES");

							if (cz != null) {
								String[] parts = (String[]) cz.query(postal);
								if (parts != null) {
									geo.put("city", parts[3]);
									geo.put("state", parts[4]);
									geo.put("county", parts[5]);
								}
							}
						} else {
							geo.put(type, item.name);
							geo.put("country", item.iso3);
							if (item.iso3.equals("USA")) {
								LookingGlass cz = (LookingGlass) LookingGlass.symbols.get("@ZIPCODES");
								if (cz != null) {
									String[] parts = (String[]) cz.query(postal);
									if (parts != null) {
										geo.put("state", parts[4]);
										geo.put("county", parts[5]);
									}
								}
							}
						}
						geo.put("country", item.iso3);
					}
				}

			};
		});

		methodMap.put("user", new Command() {
			public void runCommand(BidRequest br, RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {

			};
		});

		methodMap.put("imp", new Command() {
			public void runCommand(BidRequest br, RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {
				int ads = x.getAdslotCount();
				br.video = null;

				boolean isVideo = false;
				if (x.hasVideo()) {
					RealtimeBidding.BidRequest.Video gv = x.getVideo();
					br.video = new Video();
					br.video.maxduration = gv.getMaxAdDuration();
					br.video.minduration = gv.getMinAdDuration();
					List<VideoFormat> formats = gv.getAllowedVideoFormatsList();
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
						xx.put("maxduration", BidRequest.factory.numberNode(br.video.maxduration));
						xx.put("minduration", BidRequest.factory.numberNode(br.video.minduration));
					} else {
						imp.put("banner", xx);
					}

					AdSlot as = x.getAdslot(i);

					MatchingAdData adData = as.getMatchingAdData(0);
					
					/** TBD: Direct deals here */
					if (adData.getDirectDealCount() > 0) {
						ObjectNode pmp = BidRequest.factory.objectNode();
						pmp.put("private_auction",1);
						pmp.put("ext_k", adData.getDirectDealCount());
						ArrayNode array = BidRequest.factory.arrayNode();
						pmp.put("deals", array);
						for (int j=0; j<adData.getDirectDealCount();j++) {
							DirectDeal xxx = adData.getDirectDeal(j);
							ObjectNode deal = BidRequest.factory.objectNode();
							Long id = xxx.getDirectDealId();
							String name = xxx.getDealType().name();
							long fixedCpmMicros = xxx.getFixedCpmMicros();
							deal.put("id", id.toString());
							deal.put("bidfloor",fixedCpmMicros);
							deal.put("ext_name",name);
							array.add(deal);
						}
						imp.put("pmp",pmp);
					}
					////////////////////////////
					
					double min = adData.getMinimumCpmMicros();
					if (min != 0) {
						imp.put("bidfloor", min);
					}

					List<Integer> list = as.getExcludedAttributeList();
					ArrayNode arn = BidRequest.factory.arrayNode();
					for (int j = 0; j < list.size(); j++) {
						arn.add(list.get(j));
					}
					imp.put("battr", arn);

					int n = 0;
					if (as.hasSlotVisibility()) {
						SlotVisibility sv = as.getSlotVisibility();
						n = sv.getNumber();
						switch (n) {
						case 0: // none
							break;
						case 1: // above
							break;
						case 2: // below
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

					double bidFloor = new Double(as.getMatchingAdData(i).getMinimumCpmMicros());
					int adSlotId = as.getId();

					imp.put("bidfloor", BidRequest.factory.numberNode(bidFloor));
					imp.put("id", BidRequest.factory.textNode(Integer.toString(adSlotId)));

				}

			};
		});

		methodMap.put("site", new Command() {
			public void runCommand(BidRequest br, RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {
				if (x.hasMobile())
					return;

			};
		});

		methodMap.put("app", new Command() {
			public void runCommand(BidRequest br, RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {
				if (x.hasMobile() == false)
					return;
			};
		});

		methodMap.put("user", new Command() {
			public void runCommand(BidRequest br, RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {

			};
		});

		methodMap.put("BidRequest.detected_language", new Command() {
			public void runCommand(BidRequest br, RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {
				d.put(key, x.getDetectedLanguage(0));
			};
		});

		methodMap.put("BidRequest.postal_code", new Command() {
			public void runCommand(BidRequest br, RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {
				if (x.hasPostalCode())
					d.put(key, x.getPostalCode());
				else
					d.put(key, null);
			};
		});

		methodMap.put("BidRequest.postal_code_prefix", new Command() {
			public void runCommand(BidRequest br, RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {
				if (x.hasPostalCodePrefix())
					d.put(key, x.getPostalCodePrefix());
				else
					d.put(key, null);
			};
		});

		methodMap.put("BidRequest.google_user_id", new Command() {
			public void runCommand(BidRequest br, RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {
				if (x.hasGoogleUserId())
					d.put(key, x.getGoogleUserId());
				else
					d.put(key, null);
			};
		});

		methodMap.put("BidRequest.seller_network_id", new Command() {
			public void runCommand(BidRequest br, RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {
				if (x.hasSellerNetworkId())
					d.put(key, x.getSellerNetworkId());
				else
					d.put(key, null);
			};
		});

		methodMap.put("BidRequest.publisher_settings_list_id", new Command() {
			public void runCommand(BidRequest br, RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {
				if (x.hasPublisherSettingsListId())
					d.put(key, x.getPublisherSettingsListId());
				else
					d.put(key, null);
			};
		});

		methodMap.put("BidRequest.anonymous_id", new Command() {
			public void runCommand(BidRequest br, RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {
				if (x.hasAnonymousId())
					d.put(key, x.getAnonymousId());
				else
					d.put(key, null);
			};
		});

		methodMap.put("BidRequest.constrained_usage_google_user_id", new Command() {
			public void runCommand(BidRequest br, RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {
				if (x.hasConstrainedUsageGoogleUserId())
					d.put(key, x.getConstrainedUsageGoogleUserId());
				else
					d.put(key, null);
			};
		});

		methodMap.put("BidRequest.geo_criteria_id", new Command() {
			public void runCommand(BidRequest br, RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {
				if (x.hasGeoCriteriaId())
					d.put(key, x.getGeoCriteriaId());
			};
		});

		methodMap.put("BidRequest.mobile.screen_orientation", new Command() {
			public void runCommand(BidRequest br, RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {
				Device dev = x.getDevice();
				if (dev == null)
					return;
				if (dev.hasScreenOrientation()) {
					d.put(key, dev.getScreenOrientation());
				}

			};
		});

		methodMap.put("BidRequest.mobile.is_interstital_request", new Command() {
			public void runCommand(BidRequest br, RealtimeBidding.BidRequest x, ObjectNode root, Map d, String key) {
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

	public int adSlotId;

	public ObjectNode root;

	boolean isApp = false;

	public static final String ADX = "adx";

	/**
	 * The protobuf version of the bid request
	 */
	public RealtimeBidding.BidRequest internal;

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
		return super.interrogate(line);
	}

	// String html_snippet = "%%WINNING_PRICE%%http://localhost:8080/rtb/wins<a
	// href=\"%%CLICK_URL_UNESC%%http%3A%2F%2Fmy.adserver.com%2Fsome%2Fpath%2Fhandleclick%3Fclick%3Dclk\"></a><script
	// src=\"https://ibv.1trnvid.com/app2.js\" data-width=\"320\"
	// data-height=\"480\" data-sid=\"51376\" data-appname=\"Test App\"
	// data-appversion=\"1.0\" data-bundleid=\"test.bundleid\"
	// data-appstoreurl=\"{APPSTORE_URL}\" data-dnt=\"{DNT}\"
	// data-aid=\"{GAID}\" data-idfa=\"{IFA}\" data-lat=\"{LAT}\"
	// data-lon=\"{LON}\" data-custom1=\"\" data-custom2=\"\"
	// data-custom3=\"\"></script>";
	String clickthrough = "http://rtb4free.com/click=1";
	String creativeid = "my-creative-1234ABCD";

	@Override
	public boolean checkNonStandard(Creative creat, StringBuilder sb) {

		return true;
	}

	static int WINS = 0;

	/**
	 * Build the bid response from the bid request, campaign and creatives
	 */
	@Override
	public BidResponse buildNewBidResponse(Campaign camp, Creative creat, double price, String dealId,  int xtime) throws Exception {
		Integer category = null;
		Integer type = null;
		String tracker = null;
		AdxBidResponse response = null;


		/*
		 * if (WINS++ > 5000) { RTBServer.paused = true; }
		 */

		List<Integer> attributes = null;
		if (creat.adxCreativeExtensions != null) {
			category = creat.adxCreativeExtensions.adxCategory;
			type = creat.adxCreativeExtensions.adxVendorType;
			clickthrough = creat.adxCreativeExtensions.adxClickThroughUrl;
			attributes = creat.adxCreativeExtensions.attributes;
			tracker = creat.adxCreativeExtensions.adxTrackingUrl;
		}

		/**
		 * Make sure this creative is not an excluded creative attribute
		 */
		ArrayNode arn = (ArrayNode) interrogate("imp.0.battr");
		List<Integer> battr = null;

		if (arn != null) {
			if (attributes != null && attributes.size() > 0) {
				battr = new ArrayList();
				for (int i = 0; i < arn.size(); i++) {
					battr.add(arn.get(i).asInt());
				}
				if (!battr.retainAll(attributes)) {
					response = new AdxBidResponse();
					response.setNoBid();
					return response;
				}
			}
		}
		
		/**
		 * Make sure this creative is in the allowed product category
		 */
		arn = (ArrayNode) interrogate("bcat");
		List<Integer> bcat = null;
		if (arn != null) {
			if (category != null) {
				bcat = new ArrayList();
				for (int i = 0; i < arn.size(); i++) {
					bcat.add(arn.get(i).asInt());
				}
				if (bcat.contains(category)) {
					response = new AdxBidResponse();
					response.setNoBid();
					return response;
				}
			}
		}
		

		/**
		 * Make sure vendor type this is in the list of allowed vendor types
		 */
		
		arn = (ArrayNode) interrogate("allowedvendortypes");
		List<Integer> vdt = null;
		if (arn != null) {
			if (type != null) {
				vdt = new ArrayList();
				for (int i = 0; i < arn.size(); i++) {
					vdt.add(arn.get(i).asInt());
				}
				if (!vdt.contains(type)) {
					response = new AdxBidResponse();
					response.setNoBid();
					return response;
				}
			}
		}

		response = new AdxBidResponse(this, camp, creat);
		response.br = this;

		response.slotSetId(adSlotId);
		
		if (dealId != null) {	
			Long longid = 1L;
			try {
				longid = Long.parseLong(dealId);
			} catch (Exception error) {
				error.printStackTrace();           // should have been a long...
				price = creat.price;			   // fallback to open auction price
			}
			response.slotSetDealId(longid);
		}
		
		//System.out.println("=============> COST: " + price);
		
		long cost = cost = Math.round(price);
		//if (price < 0) {
		//	price = this.bidFloor * Math.abs(price);
		//	cost = Math.round(price);
		//}

		
		response.slotSetMaxCpmMicros(cost);
		response.adSetHeight(this.h);
		response.adSetWidth(this.w);
		
		response.adAddAgencyId(1);

		response.adAddClickThroughUrl(clickthrough);
		
		response.cost = cost;
		response.utc = System.currentTimeMillis();
		response.oidStr = this.id;
		response.crid = creat.impid;
		response.lat = this.lat;
		response.lon = this.lon;
		response.width = this.w;
		response.height = this.h;
		response.exchange = this.exchange;
		

		if (type != null)
			response.adAddVendorType(type);
		if (category != null)
			response.adAddCategory(category);
		else
			response.adAddCategory(0);

		if (video == null) {
			String html = null;
			try {
				html = response.getTemplate();
			} catch (Exception error) {
				error.printStackTrace();
			}
			response.adtype = "banner";
			response.adSetHtmlSnippet(html);
			response.adSetImpressionTrackingUrl(tracker);
		} else {
			response.adtype = "video";
			response.forwardUrl = creat.adm.get(0);
			response.setVideoUrl(creat.adm.get(0));
		}

		if (creat.adxCreativeExtensions.attributes == null || creat.adxCreativeExtensions.attributes.size() == 0 ) {
			response.addAttribute(0);
		} else {
			for (int i=0; i<creat.adxCreativeExtensions.attributes.size(); i++) {
				response.addAttribute(creat.adxCreativeExtensions.attributes.get(i));
			}
		}
		response.build(xtime);

		//System.out.println("================================ BIDDING ============================");
		//System.out.println(response.toString());
		//System.out.println("======================================================================");
		//System.out.println(response.getInternal());
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

		// System.out.println("========>" + TOTAL);
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

		root.put("bcat", list);
		
		/**
		 * Allowed vendor type, no openRTB analog
		 */
		List<Integer> allowedVendorTypes = ad.getAllowedVendorTypeList();
		if (allowedVendorTypes != null && allowedVendorTypes.size() > 0) {
			list = BidRequest.factory.arrayNode();
			for (Integer x : allowedVendorTypes) {
				list.add(x);
			}
			root.put("allowedvendortypes", list);
		}

		ArrayNode nodes = (ArrayNode) root.get("imp");
		ObjectNode node = (ObjectNode) nodes.get(0);
		if (node.get("video") != null) {
			node = (ObjectNode) node.get("video");
		} else {
			node = (ObjectNode) node.get("banner");
		}
		w = node.get("w").asInt();
		h = node.get("h").asInt();
		if (root.get("app") == null) {
			isApp = false;
			node = (ObjectNode) root.get("site");
			if (node == null) {
				node = BidRequest.factory.objectNode();
				root.put("site", node);
			}
		} else {
			isApp = true;
			node = (ObjectNode) root.get("app");
		}
		if (internal.hasUrl()) {
			if (internal.hasSellerNetworkId())
				node.put("id", Integer.toString(internal.getSellerNetworkId()));
			
			if (isApp) {
				ObjectNode contentNode = null;
				contentNode = BidRequest.factory.objectNode();
				node.put("content", contentNode);
				contentNode.put("url", internal.getUrl());
			} else 
				node.put("url", internal.getUrl());
			
			this.pageurl = internal.getUrl();
		}

		if (internal.hasEncryptedHyperlocalSet()) {
			ByteString bs = internal.getEncryptedHyperlocalSet();
			try {
				byte[] hps = AdxWinObject.decryptHyperLocal(bs.toByteArray());
				RealtimeBidding.BidRequest.HyperlocalSet hyper = RealtimeBidding.BidRequest.HyperlocalSet
						.parseFrom(hps);
				Point p = hyper.getCenterPoint();
				if (p != null) {
					lat = (double) p.getLatitude();
					lon = (double) p.getLongitude();

					ObjectNode geo = (ObjectNode) interrogate("device.geo");
					if (geo == null) {
						geo = BidRequest.factory.objectNode();
						ObjectNode device = (ObjectNode) interrogate("device");
						if (device == null) {
							node = BidRequest.factory.objectNode();
							root.put("device", device);
							device.put("geo", geo);
						}
						device.put("geo", geo);
					} else {
						geo.put("lat", lat);
						geo.put("lon", lon);
					}
					// System.out.println("LAT = " + lat + ", LON = " + lon);
				}
			} catch (Exception error) {
				// Can happen if the keys don't match the keys used to generate
				// the requests file
			}
		}

		TextNode value = (TextNode) interrogate("imp.0.id");
		adSlotId = value.asInt();

		if (lat == null) {
			lat = new Double(0);
			lon = new Double(0);
		}

		DoubleNode impFloor = (DoubleNode) interrogate("imp.0.bidfloor");
		if (impFloor != null) {
			this.bidFloor = new Double(impFloor.doubleValue());
		}

		//System.out.println("========================= INCOMING ====================================");
		//System.out.println(internal);
		//System.out.println("========================= RTB EQUIVALENT ============================");
		//System.out.println(root);
		handleFeedBack();
	}

	static String makeKey(String s) {
		StringBuilder key = new StringBuilder("BidRequest.");
		key.append(s);
		return key.toString();
	}
	
	/**
	 * Handle any feedback messages in the stream
	 */
	void handleFeedBack() {
		int count = internal.getBidResponseFeedbackCount();
		for (int i=0; i<count; i++) {
			BidResponseFeedback r = internal.getBidResponseFeedback(i);
			ByteString id = r.getRequestId();
			String sid = convertToHex(id);
			int code = r.getCreativeStatusCode();
			AdxFeedback afx = new AdxFeedback();
			afx.feedback = sid;
			afx.code = code;
			try {
				if (Configuration.ipAddress == null)
					return;
				
				Controller.getInstance().sendAdxFeedback(afx);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} 
	}

	void internalSetup() {

		List<String> beenThere = new ArrayList();
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String[] codes = key.split("\\.");
			Command c = methodMap.get(codes[0]);
			if (c != null && beenThere.contains(codes[0]) == false) {
				c.runCommand(this, internal, root, database, key);
				beenThere.add(codes[0]);
			}
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
