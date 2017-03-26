package com.xrtb.common;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.xrtb.bidder.Controller;
import com.xrtb.bidder.SelectedCreative;
import com.xrtb.exchanges.Nexage;
import com.xrtb.exchanges.adx.AdxCreativeExtensions;
import com.xrtb.nativeads.assets.Entity;
import com.xrtb.nativeads.creative.Data;
import com.xrtb.nativeads.creative.NativeCreative;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;
import com.xrtb.pojo.Impression;
import com.xrtb.pojo.Video;
import com.xrtb.probe.Probe;
import com.xrtb.tools.MacroProcessing;

/**
 * An object that encapsulates the 'creative' (the ad served up and it's
 * attributes). The creative represents the physical object served up by the
 * bidder to the mobile device. The creative contains the image url, the pixel
 * url, and the referring url. The creative will them be used to create the
 * components of the RTB 2 bid
 * 
 * @author Ben M. Faul
 *
 */
public class Creative {
	/** The forward URL used with this creative */
	public String forwardurl;
	/** The encoded version of the forward url used by this creative */
	private transient String encodedFurl;
	/* The image url used by this creative */
	public String imageurl;
	/** The encoded image URL used by this creative */
	private transient String encodedIurl;
	/** The impression id of this creative */
	public String impid;
	/** The width of this creative */
	public Integer w;
	/** The height of this creative */
	public Integer h;
	/** sub-template for banner */
	public String subtemplate;
	/** Private/preferred deals */
	public List<Deal> deals;
	/** String representation of w */
	transient public String strW;
	/** String representation of h */
	transient public String strH;
	/** String representation of price */
	transient public String strPrice;
	/** Attributes used with a video */
	public List<Node> attributes = new ArrayList<Node>();
	/** Input ADM field */
	public List<String> adm;
	/** The encoded version of the adm as a single string */
	public transient String encodedAdm;
	/** currency of this creative */
	public String currency = null;
	/** Extensions needed by SSPs */
	public Map<String,String> extensions = null;

	/** if this is a video creative (NOT a native content video) its protocol */
	public Integer videoProtocol;
	/**
	 * if this is a video creative (NOT a native content video) , the duration
	 * in seconds
	 */
	public Integer videoDuration;
	/** If this is a video (Not a native content, the linearity */
	public Integer videoLinearity;
	/** The videoMimeType */
	public String videoMimeType;
	/**
	 * vast-url, a non standard field for passing an http reference to a file
	 * for the XML VAST
	 */
	public String vasturl;
	/** The price associated with this creative */
	public double price = .01;

	// /////////////////////////////////////////////
	/** Native content assets */
	public NativeCreative nativead;

	/**
	 * Don't use the template, use exactly what is in the creative for the ADM
	 */
	public boolean adm_override = false;

	/** If this is an Adx type creative, here is the payload */
	public AdxCreativeExtensions adxCreativeExtensions;

	@JsonIgnore
	public transient StringBuilder smaatoTemplate = null;
	// //////////////////////////////////////////////////

	/** The macros this particular creative is using */
	@JsonIgnore
	public transient List<String> macros = new ArrayList();

	/** Cap specification */
	public String capSpecification;
	/** Cap frequency count */
	public int capFrequency = 0;
	/** Cap timeout in HOURS */
	public String capTimeout; // is a string, cuz its going into redis

	private String fowrardUrl;

	/**
	 * Empty constructor for creation using json.
	 */
	public Creative() {

	}

	/**
	 * Find a deal by id, if exists, will bid using the deal
	 * 
	 * @param id
	 *            String. The of the deal in the bid request.
	 * @return Deal. The deal, or null, if no deal.
	 */
	public Deal findDeal(String id) {
		if (deals == null || deals.size() == 0)
			return null;
		for (int i = 0; i < deals.size(); i++) {
			Deal d = deals.get(i);
			if (d.id.equals(id)) {
				return d;
			}
		}
		return null;
	}

	/**
	 * Given a list of deals, find out if we have a deal that matches.
	 * 
	 * @param ids
	 *            List. A list of ids.
	 * @return Deal. A matching deal or null if no deal.
	 */
	public Deal findDeal(List<String> ids) {
		if (deals == null || deals.size() == 0)
			return null;
		for (int i = 0; i < ids.size(); i++) {
			Deal d = findDeal(ids.get(i));
			if (d != null)
				return d;
		}
		return null;
	}

	/**
	 * Find a deal by id, if exists, will bid using the deal
	 * 
	 * @param id
	 *            String. The of the deal in the bid request.
	 * @return Deal. The deal, or null, if no deal.
	 */
	public Deal findDeal(long id) {
		if (deals == null || deals.size() == 0)
			return null;
		for (int i = 0; i < deals.size(); i++) {
			Deal d = deals.get(i);
			if (Long.parseLong(d.id) == id) {
				return d;
			}
		}
		return null;
	}

	/**
	 * Does the HTTP encoding for the forward url and image url. The bid will
	 * use the encoded form.
	 */
	void encodeUrl() {
		MacroProcessing.findMacros(macros, forwardurl);
		MacroProcessing.findMacros(macros, imageurl);

		/*
		 * Encode JavaScript tags. Redis <script src=\"a = 100\"> will be
		 * interpeted as <script src="a=100"> In the ADM, this will cause
		 * parsing errors. It must be encoded to produce: <script src=\"a=100\">
		 */
		if (forwardurl != null) {
			if (forwardurl.contains("<script") || forwardurl.contains("<SCRIPT")) {
				if (forwardurl.contains("\"") && (forwardurl.contains("\\\"") == false)) { // has
																							// the
																							// initial
																							// quote,
																							// but
																							// will
																							// not
																							// be
																							// encoded
																							// on
																							// the
																							// output
					forwardurl = forwardurl.replaceAll("\"", "\\\\\"");
				}
			}
		}

		encodedFurl = URIEncoder.myUri(forwardurl);
		encodedIurl = URIEncoder.myUri(imageurl);

		if (adm != null && adm.size() > 0) {
			String s = "";
			for (String ss : adm) {
				s += ss;
			}
			encodedAdm = URIEncoder.myUri(s);
		}

		strW = Integer.toString(w);
		strH = Integer.toString(h);
		strPrice = Double.toString(price);
	}

	/**
	 * Getter for the forward URL, unencoded.
	 * 
	 * @return String. The unencoded url.
	 */
	@JsonIgnore
	public String getForwardUrl() {
		return forwardurl;
	}

	/**
	 * Return the encoded forward url
	 * 
	 * @return String. The encoded url
	 */
	@JsonIgnore
	public String getEncodedForwardUrl() {
		if (encodedFurl == null)
			encodeUrl();
		return encodedFurl;
	}

	/**
	 * Return the encoded image url
	 * 
	 * @return String. The returned encoded url
	 */
	@JsonIgnore
	public String getEncodedIUrl() {
		if (encodedIurl == null)
			encodeUrl();
		return encodedIurl;
	}

	/**
	 * Setter for the forward url, unencoded.
	 * 
	 * @param forwardUrl
	 *            String. The unencoded forwardurl.
	 */
	public void setForwardUrl(String forwardUrl) {
		this.forwardurl = forwardUrl;
	}

	/**
	 * Setter for the imageurl
	 * 
	 * @param imageUrl
	 *            String. The image url to set.
	 */
	public void setImageUrl(String imageUrl) {
		this.imageurl = imageUrl;
	}

	/**
	 * Returns the impression id for this creative (the database key used in
	 * wins and bids).
	 * 
	 * @return String. The impression id.
	 */
	public String getImpid() {
		return impid;
	}

	/**
	 * Set the impression id object.
	 * 
	 * @param impid
	 *            String. The impression id to use for this creative. This is
	 *            merely a databse key you can use to find bids and wins for
	 *            this id.
	 */
	public void setImpid(String impid) {
		this.impid = impid;
	}

	/**
	 * Get the width of the creative, in pixels.
	 * 
	 * @return int. The height in pixels of this creative.
	 */
	public double getW() {
		if (w != null)
			return w;
		return 0;
	}

	/**
	 * Set the width of the creative.
	 * 
	 * @param w
	 *            int. The width of the pixels to set the creative.
	 */
	public void setW(int w) {
		this.w = w;
	}

	/**
	 * Return the height in pixels of this creative
	 * 
	 * @return int. The height in pixels.
	 */
	public double getH() {
		if (h == null)
			return 0;
		return h;
	}

	/**
	 * Set the height of this creative.
	 * 
	 * @param h
	 *            int. Height in pixels.
	 */
	public void setH(int h) {
		this.h = h;
	}

	/**
	 * Set the price on this creative
	 * 
	 * @param price
	 *            double. The price to set.
	 */
	public void setPrice(double price) {
		this.price = price;
	}

	/**
	 * Get the price of this campaign.
	 * 
	 * @return double. The price associated with this creative.
	 */
	public double getPrice() {
		return price;
	}

	/**
	 * Determine if this is a native ad
	 * 
	 * @return boolean. Returns true if this is a native content ad.
	 */
	@JsonIgnore
	public boolean isNative() {
		if (nativead != null)
			return true;
		return false;
	}

	/**
	 * Determine if this creative is video or not
	 * 
	 * @return boolean. Returns true if video.
	 */
	@JsonIgnore
	public boolean isVideo() {
		if (this.videoDuration != null)
			return true;
		return false;
	}

	/**
	 * Encodes the attributes of the node after the node is instantiated.
	 * 
	 * @throws Exception
	 *             on JSON errors.
	 */
	public void encodeAttributes() throws Exception {
		for (Node n : attributes) {
			n.setValues();
		}

		if (nativead != null) {
			nativead.encode();
		}
	}

	/**
	 * Returns the native ad encoded as a String.
	 * 
	 * @param br
	 *            BidRequest. The bid request.
	 * @return String. The encoded native ad.
	 */
	@JsonIgnore
	public String getEncodedNativeAdm(BidRequest br) {
		return nativead.getEncodedAdm(br);
	}

	/**
	 * Process the bid request against this creative.
	 * 
	 * @param br
	 *            BidRequest. Returns true if the creative matches.
	 * @param errorString
	 *            StringBuilder. The string to hold any campaign failure
	 *            messages
	 * @return boolean. Returns true of this campaign matches the bid request,
	 *         ie eligible to bid
	 */
	public SelectedCreative process(BidRequest br, Map<String, String> capSpecs, String adId, StringBuilder errorString , Probe probe) {
		int n = br.getImpressions();
		StringBuilder sb = new StringBuilder();
		Impression imp;
		
		if (isCapped(br, capSpecs)) {
			sb.append("This creative " + this.impid + " is capped for " + capSpecification);
			if (errorString != null) {
				probe.process(br.getExchange(), adId, impid, sb);
				errorString.append(sb);
			}
			return null;
		}

		for (int i=0; i<n;i++) {
			imp = br.getImpression(i);
			SelectedCreative cr = xproc(br,adId,imp,capSpecs,errorString, probe);
			if (cr != null) {
				cr.setImpression(imp);
				return cr;
			}
		}
		return null;
	}
	
	public SelectedCreative xproc(BidRequest br, String adId, Impression imp, Map<String, String> capSpecs, StringBuilder errorString, Probe probe) {
		List<Deal> newDeals = null;
		String dealId = null;
		double xprice = price;
		String impid = this.impid;
		StringBuilder sb;

		if (br.checkNonStandard(this, errorString) != true) {
			return null;
		}

		if (price == 0 && (deals == null || deals.size() == 0)) {
			sb = new StringBuilder(Probe.DEAL_PRICE_ERROR);
			probe.process(br.getExchange(), adId, impid, sb);
			if (errorString != null) {
				errorString.append(Probe.DEAL_PRICE_ERROR);
			}
			return null;
		}
		if (imp.deals != null) {
			probe.process(br.getExchange(), adId, impid, Probe.PRIVATE_AUCTION_LIMITED);
			if ((deals == null || deals.size() == 0) && price == 0) {
				if (errorString != null)
					errorString.append(Probe.PRIVATE_AUCTION_LIMITED);
				return null;
			}

			if (deals != null && deals.size() > 0) {
				/**
				 * Ok, find a deal!
				 */
				newDeals = new ArrayList<Deal>(deals);
				newDeals.retainAll(imp.deals);
				if (newDeals.size() != 0) {
					dealId = newDeals.get(0).id;
					xprice = newDeals.get(0).price;
					Deal brDeal = imp.getDeal(dealId);

					if (brDeal == null && price == 0) {
						probe.process(br.getExchange(), adId, impid, Probe.PRIVATE_AUCTION_LIMITED);
						if (errorString != null)
							errorString.append(Probe.NO_WINNING_DEAL_FOUND);
						return null;
					}

					imp.bidFloor = new Double(brDeal.price);
				} else
					if (price == 0) {
						probe.process(br.getExchange(), adId, impid, Probe.NO_APPLIC_DEAL);
						if (errorString != null)
							errorString.append(Probe.NO_APPLIC_DEAL);
						return null;
					}
			}

		} else {
			if (price == 0)
				return null;
		}
		/*
		 * if (br.privateAuction == 0 && price == 0 && (newDeals == null ||
		 * newDeals.size() == 0)) { if (errorString != null) errorString.
		 * append("This creative is for private auction only, but this is a public auction"
		 * ); return null; }
		 */

		if (imp.bidFloor != null) {
			if (xprice < 0) {
				xprice = Math.abs(xprice) * imp.bidFloor;
			}
			if (imp.bidFloor > xprice) {
				probe.process(br.getExchange(), adId, impid, Probe.BID_FLOOR);
				if (errorString != null) {
					errorString.append(Probe.BID_FLOOR);
		
				return null;
				}
			}
		} else {
			if (xprice < 0)
				xprice = .01; // A fake bid price if no bid floor
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		if (isVideo() && imp.video == null) {
			probe.process(br.getExchange(), adId, impid, Probe.BID_CREAT_IS_VIDEO);
			if (errorString != null)
				errorString.append(Probe.BID_CREAT_IS_VIDEO);
			return null;
		}
		if (isNative() && imp.nativePart == null) {
			probe.process(br.getExchange(), adId, impid, Probe.BID_CREAT_IS_NATIVE);
			if (errorString != null)
				errorString.append(Probe.BID_CREAT_IS_NATIVE);
			return null;
		}
		if ((isVideo() == false && isNative() == false) != (imp.nativePart == null && imp.video == null)) {
			probe.process(br.getExchange(), adId, impid, Probe.BID_CREAT_IS_BANNER);
			if (errorString != null)
				errorString.append(Probe.BID_CREAT_IS_BANNER);
			return null;
		}

		if (isNative()) {
			if (imp.nativePart.layout != 0) {
				if (imp.nativePart.layout != nativead.nativeAdType) {
					probe.process(br.getExchange(), adId, impid, Probe.BID_CREAT_IS_BANNER);
					if (errorString != null)
						errorString.append(Probe.NATIVE_LAYOUT);
					return null;
				}
			}
			if (imp.nativePart.title != null) {
				if (imp.nativePart.title.required == 1 && nativead.title == null) {
					probe.process(br.getExchange(), adId, impid, Probe.NATIVE_TITLE);
					if (errorString != null)
						errorString.append(Probe.NATIVE_TITLE);
					return null;
				}
				if (nativead.title.title.text.length() > imp.nativePart.title.len) {
					probe.process(br.getExchange(), adId, impid, Probe.NATIVE_TITLE_LEN);
					if (errorString != null)
						errorString.append(Probe.NATIVE_TITLE_LEN);
					return null;
				}
			}

			if (imp.nativePart.img != null && nativead.img != null) {
				if (imp.nativePart.img.required == 1 && nativead.img == null) {
					probe.process(br.getExchange(), adId, impid, Probe.NATIVE_WANTS_IMAGE);
					if (errorString != null)
						errorString.append(Probe.NATIVE_WANTS_IMAGE);
					return null;
				}
				if (nativead.img.img.w != imp.nativePart.img.w) {
					probe.process(br.getExchange(), adId, impid, Probe.NATIVE_IMAGEW_MISMATCH);
					if (errorString != null) 
						errorString.append(Probe.NATIVE_IMAGEW_MISMATCH);
					return null;
				}
				if (nativead.img.img.h != imp.nativePart.img.h) {
					probe.process(br.getExchange(), adId, impid, Probe.NATIVE_IMAGEH_MISMATCH);
					if (errorString != null)
						errorString.append(Probe.NATIVE_IMAGEH_MISMATCH);
					return null;
				}
			}

			if (imp.nativePart.video != null) {
				if (imp.nativePart.video.required == 1 || nativead.video == null) {
					probe.process(br.getExchange(), adId, impid, Probe.NATIVE_WANTS_VIDEO);
					if (errorString != null)
						errorString.append(Probe.NATIVE_WANTS_VIDEO);
					return null;
				}
				if (nativead.video.video.duration < imp.nativePart.video.minduration) {
					probe.process(br.getExchange(), adId, impid, Probe.NATIVE_AD_TOO_SHORT);
					if (errorString != null)
						errorString.append(Probe.NATIVE_AD_TOO_SHORT);
					return null;
				}
				if (nativead.video.video.duration > imp.nativePart.video.maxduration) {
					probe.process(br.getExchange(), adId, impid, Probe.NATIVE_AD_TOO_LONG);
					if (errorString != null)
						errorString.append(Probe.NATIVE_AD_TOO_LONG);
					return null;
				}
				if (imp.nativePart.video.linearity != null
						&& imp.nativePart.video.linearity.equals(nativead.video.video.linearity) == false) {
					probe.process(br.getExchange(), adId, impid, Probe.NATIVE_LINEAR_MISMATCH);
					if (errorString != null)
						errorString.append(Probe.NATIVE_LINEAR_MISMATCH);
					return null;
				}
				if (imp.nativePart.video.protocols.size() > 0) {
					if (imp.nativePart.video.protocols.contains(nativead.video.video.protocol)) {
						probe.process(br.getExchange(), adId, impid, Probe.NATIVE_AD_PROTOCOL_MISMATCH);
						if (errorString != null)
							errorString.append(Probe.NATIVE_AD_PROTOCOL_MISMATCH);
						return null;
					}
				}

			}

			for (Data datum : imp.nativePart.data) {
				Integer val = datum.type;
				Entity e = nativead.dataMap.get(val);
				if (datum.required == 1 && e == null) {
					probe.process(br.getExchange(), adId, impid, Probe.NATIVE_AD_PROTOCOL_MISMATCH);
					if (errorString != null)
						errorString.append(Probe.NATIVE_AD_DATUM_MISMATCH);
					return null;
				}
				if (e != null) {
					if (e.value.length() > datum.len) {
						probe.process(br.getExchange(), adId, impid, Probe.NATIVE_AD_PROTOCOL_MISMATCH);
						if (errorString != null)
							errorString.append(Probe.NATIVE_AD_DATUM_MISMATCH);
						return null;
					}
				}

			}

			return new SelectedCreative(this, dealId, xprice, impid);
			// return true;
		}

		if (imp.nativePart == null) {
			if (imp.w == null || imp.h == null) {
				// we will match any size if it doesn't match...		
				if (imp.instl != null && imp.instl.intValue() == 1) {
					Node n = findAttribute("imp.0.instl");
					if (n != null) {
						if (n.intValue() == 0) {
							probe.process(br.getExchange(), adId, impid, Probe.WH_INTERSTITIAL);
							if (errorString != null) {
								errorString.append(Probe.WH_INTERSTITIAL);
								return null;
							}
						}
					} else {
						if (errorString != null) {
							errorString.append(Probe.WH_INTERSTITIAL);
							return null;
						}
					}
				} else if (errorString != null) {
					//errorString.append("No width or height specified\n");
					//return null;
					// ok, let it go.
				}
			} else {

				if (w.intValue() == -1) { // override the values int he creative
											// temporarially with the bid
											// request.
					strW = Integer.toString(imp.w);
					strH = Integer.toString(imp.h);
				} else {
					if (imp.w.doubleValue() != w.doubleValue() || imp.h.doubleValue() != h.doubleValue()) {
						probe.process(br.getExchange(), adId, impid, Probe.WH_MATCH);
						if (errorString != null)
							errorString.append(Probe.WH_MATCH);
						return null;
					}
				}
			}
		}

		/**
		 * Video
		 * 
		 */
		if (imp.video != null) {
			if (imp.video.linearity != -1 && this.videoLinearity != null) {
				if (imp.video.linearity != this.videoLinearity) {
					probe.process(br.getExchange(), adId, impid, Probe.VIDEO_LINEARITY);
					if (errorString != null)
						errorString.append(Probe.VIDEO_LINEARITY);
					return null;
				}
			}
			if (imp.video.minduration != -1) {
				if (this.videoDuration != null) {
					if (!(this.videoDuration.intValue() >= imp.video.minduration)) {
						probe.process(br.getExchange(), adId, impid, Probe.VIDEO_TOO_SHORT);
						if (errorString != null)
							errorString.append(Probe.VIDEO_TOO_SHORT);
						return null;
					}
				}
			}
			if (imp.video.maxduration != -1) {
				if (this.videoDuration != null) {
					if (!(this.videoDuration.intValue() <= imp.video.maxduration)) {
						probe.process(br.getExchange(), adId, impid, Probe.VIDEO_TOO_LONG);
						if (errorString != null)
							errorString.append(Probe.VIDEO_TOO_LONG);
						return null;
					}
				}
			}
			if (imp.video.protocol.size() != 0) {
				if (this.videoProtocol != null) {
					if (imp.video.protocol.contains(this.videoProtocol) == false) {
						probe.process(br.getExchange(), adId, impid, Probe.VIDEO_PROTOCOL);
						if (errorString != null)
							errorString.append(Probe.VIDEO_PROTOCOL);
						return null;
					}
				}
			}
			if (imp.video.mimeTypes.size() != 0) {
				if (this.videoMimeType != null) {
					if (imp.video.mimeTypes.contains(this.videoMimeType) == false) {
						probe.process(br.getExchange(), adId, impid, Probe.VIDEO_MIME);
						if (errorString != null)
							errorString.append(Probe.VIDEO_MIME);
						return null;
					}
				}
			}
		}

		Node n = null;
		/**
		 * Attributes that are specific to the creative (additional to the
		 * campaign
		 */
		try {
			for (int i = 0; i < attributes.size(); i++) {
				n = attributes.get(i);
				if (n.test(br) == false) {
					if (errorString != null)
						errorString.append("CREATIVE MISMATCH: ");
					if (errorString != null) {
						if (n.operator == Node.OR)
							errorString.append("OR failed on all branches\n");
						else
							errorString.append(n.hierarchy);
					}
					sb = new StringBuilder(Probe.CREATIVE_MISMATCH);
					sb.append(n.hierarchy);
					probe.process(br.getExchange(), adId, impid, sb);
					return null;
				}
			}
		} catch (Exception error) {
			// error.printStackTrace();
			if (errorString != null) {
				errorString.append("Internal error in bid request: " + n.hierarchy + " is missing, ");
				errorString.append(error.toString());
				errorString.append("\n");
			}
			return null;
		}

		return new SelectedCreative(this, dealId, xprice, impid);
	}

	boolean isCapped(BidRequest br, Map<String, String> capSpecs) {
		if (capSpecification == null)
			return false;

		String value = null;
		try {
			value = BidRequest.getStringFrom(br.database.get(capSpecification));
			if (value == null)
				return false;
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}

		StringBuilder bs = new StringBuilder("capped_");
		bs.append(impid);
		bs.append(value);
		int k = 0;
		try {
			String cap = bs.toString();
			//System.out.println("---------------------> " + cap);
			capSpecs.put(impid, cap);
			k = Controller.getInstance().getCapValue(cap);
			if (k < 0)
				return false;
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}

		if (k >= capFrequency)
			return true;
		return false;

	}

	/**
	 * Creates a sample of the ADM field, useful for testing your ad markup to
	 * make sure it works.
	 * 
	 * @param camp
	 *            Campaign. The campaign to use with this creative.
	 * @return String. The ad markup HTML.
	 */
	public String createSample(Campaign camp) {
		BidRequest request = new Nexage();

		String page = null;
		String str = null;
		File temp = null;
		
		Impression imp = new Impression();

		imp.w = w;
		imp.h = h;

		BidResponse br = null;

		try {
			if (this.isVideo()) {
				br = new BidResponse(request, imp, camp, this, "123", 1.0, null, 0);
				imp.video = new Video();
				imp.video.linearity = this.videoLinearity;
				imp.video.protocol.add(this.videoProtocol);
				imp.video.maxduration = this.videoDuration + 1;
				imp.video.minduration = this.videoDuration - 1;

				str = br.getAdmAsString();
				/**
				 * Read in the stubbed video page and patch the VAST into it
				 */
				page = new String(Files.readAllBytes(Paths.get("web/videostub.html")), StandardCharsets.UTF_8);
				page = page.replaceAll("___VIDEO___", "http://localhost:8080/vast/onion270.xml");
			} else if (this.isNative()) {

				// br = new BidResponse(request, camp, this,"123",0);
				// request.nativead = true;
				// request.nativePart = new NativePart();
				// str = br.getAdmAsString();

				page = "<html><title>Test Creative</title><body><img src='images/under-construction.gif'></img></body></html>";
			} else {
				br = new BidResponse(request, imp, camp, this, "123", 1.0, null, 0);
				str = br.getAdmAsString();
				page = "<html><title>Test Creative</title><body><xmp>" + str + "</xmp>" + str + "</body></html>";
			}
			page = page.replaceAll("\\{AUCTION_PRICE\\}", "0.2");
			page = page.replaceAll("\\$", "");
			temp = File.createTempFile("test", ".html", new File("www/temp"));
			temp.deleteOnExit();

			Files.write(Paths.get(temp.getAbsolutePath()), page.getBytes());
		} catch (Exception error) {
			error.printStackTrace();
		}
		return "temp/" + temp.getName();
	}

	/**
	 * Find a node of the named hierarchy.
	 * 
	 * @param hierarchy
	 *            String. The hierarchy you are looking for
	 * @return Node. The node with this hierarchy, or, null if not found.
	 */
	public Node findAttribute(String hierarchy) {
		for (Node n : attributes) {
			if (n.hierarchy.equals(hierarchy))
				return n;
		}
		return null;
	}
}
