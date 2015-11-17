package com.xrtb.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.xrtb.nativeads.assets.Asset;
import com.xrtb.nativeads.assets.Entity;
import com.xrtb.nativeads.creative.Data;
import com.xrtb.nativeads.creative.Link;
import com.xrtb.nativeads.creative.NativeCreative;
import com.xrtb.pojo.BidRequest;

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
	public Double w;
	/** The height of this creative */
	public Double h;
	/** Attributes used with a video */
	public List<Node> attributes = new ArrayList<Node>();
	/** Input ADM field */
	public List<String> adm;
	/** The encoded version of the adm as a single string */
	public transient String encodedAdm;
	/** currency of this creative */
	public String currency = "USD";
	/** if this is a video creative (NOT a native content video) its protocol */
	public Integer videoProtocol;
	/** if this is a video creative (NOT a native content video) , the duration in seconds */
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

	// //////////////////////////////////////////////////

	/**
	 * Empty constructor for creation using json.
	 */
	public Creative() {

	}

	/**
	 * Does the HTTP encoding for the forward url and image url. The bid will
	 * use the encoded form.
	 */
	void encodeUrl() {
		// encodedFurl = URIEncoder.encodeURI(forwardUrl);
		// encodedIurl = URIEncoder.encodeURI(imageUrl);

		encodedFurl = URIEncoder.myUri(forwardurl);
		encodedIurl = URIEncoder.myUri(imageurl);

		if (adm != null && adm.size() > 0) {
			String s = "";
			for (String ss : adm) {
				s += ss;
			}
			encodedAdm = URIEncoder.myUri(s);
		}
	}

	/**
	 * Getter for the forward URL, unencoded.
	 * 
	 * @return String. The unencoded url.
	 */
	public String getForwardUrl() {
		return forwardurl;
	}

	/**
	 * Return the encoded forward url
	 * 
	 * @return String. The encoded url
	 */
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
	 * Getter for the image url.
	 * 
	 * @return String. Returns the imageUrl.
	 */
	public String getImageUrl() {
		return imageurl;
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
	public void setW(double w) {
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
	public void setH(double h) {
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
	public boolean isVideo() {
		if (this.videoDuration != null)
			return true;
		return false;
	}

	public void encodeAttributes() throws Exception {
		for (Node n : attributes) {
			n.setValues();
		}
		
		if (nativead != null) {
			nativead.encode();
		}
	}

	public String getEncodedNativeAdm(BidRequest br) {
		return nativead.getEncodedAdm(br);
	}

	public boolean process(BidRequest br, StringBuilder errorString) {
		if (isVideo() && br.video == null) {
			errorString.append("Creative is video, request is not");
			return false;
		}
		if (isNative() && br.nativePart == null) {
			errorString.append("Creative is native content, request is not");
			return false;
		}
		if ((isVideo() == false && isNative() == false) != (br.nativePart == null && br.video == null)) {
			errorString.append("Creative is banner, request is not");
			return false;
		}

		if (isNative()) {
			if (br.nativePart.layout != 0) {
				if (br.nativePart.layout != nativead.nativeAdType) {
					errorString.append("Native ad layouts don't match");
					return false;
				}
			}
			if (br.nativePart.title != null) {
				if (br.nativePart.title.required == 1 && nativead.title == null) {
					errorString.append("Native ad request wants a title, creative has none.");
					return false;
				}
				if (nativead.title.title.text.length() > br.nativePart.title.len) {
					errorString.append("Native ad title length is too long");
					return false;
				}
			}
			
			if (br.nativePart.img != null && nativead.img != null) {
				if (br.nativePart.img.required == 1 && nativead.img == null) {
					errorString.append("Native ad request wants an img, creative has none.");
					return false;
				}
				if (nativead.img.img.w != br.nativePart.img.w) {
					errorString.append("Native ad img widths dont match");
					return false;
				}
				if (nativead.img.img.h != br.nativePart.img.h) {
					errorString.append("Native ad img heoghts dont match");
					return false;
				}
			}
			
			if (br.nativePart.video != null) {
				if (br.nativePart.video.required == 1 ||  nativead.video == null) {
					errorString.append("Native ad request wants a video, creative has none.");
					return false;
				}
				if (nativead.video.video.duration < br.nativePart.video.minduration) {
					errorString.append("Native ad video duration is < what request wants");
					return false;
				}
				if (nativead.video.video.duration > br.nativePart.video.maxduration) {
					errorString.append("Native ad video duration is > what request wants");
					return false;
				}
				if (br.nativePart.video.linearity != null &&
						br.nativePart.video.linearity != nativead.video.video.linearity) {
					errorString.append("Native ad video linearity doesn't match the ad");
					return false;
				}
				if (br.nativePart.video.protocols.size() > 0) {
					if (br.nativePart.video.protocols.contains(nativead.video.video.protocol)) {
						errorString.append("Native ad video protocol doesn't match the ad");
						return false;
					}
				}
						
			}
			
			for (Data datum : br.nativePart.data) {
				Integer val = datum.type;
				Entity e = nativead.dataMap.get(val);
				if (datum.required == 1 && e == null) {
					errorString.append("Native ad data item of type " + datum.type + " not present in ad");
					return false;
				}
				if (e != null) {
					if (e.value.length() > datum.len) {
						errorString.append("Native ad data item of type " + datum.type + " length is too long for request");
						return false;
					}
				}
				
			}
					
			return true;
		}

		if (br.nativePart == null && br.w.doubleValue() != w.doubleValue() || br.h.doubleValue() != h.doubleValue()) {
			errorString.append("Creative  w or h attributes dont match");
			return false;
		}
		
		/**
		 * Video
		 * 
		 */
		if (br.video != null) {
			if (br.video.linearity != -1 && this.videoLinearity != null) {
				if (br.video.linearity != this.videoLinearity) {
					errorString.append("Video Creative  linearity attributes dont match");
					return false;
				}
			}
			if (br.video.minduration != -1) {
				if (this.videoDuration != null) {
					if (!(this.videoDuration.intValue() >= br.video.minduration)) {
						errorString.append("Video Creative min duration not long enough.");
						return false;
					}
				}
			}
			if (br.video.maxduration != -1) {
				if (this.videoDuration != null) {
					if (!(this.videoDuration.intValue() <= br.video.maxduration)) {
						errorString.append("Video Creative duration is too long.");
						return false;
					}
				}
			}
			if (br.video.protocol.size() != 0) {
				if (this.videoProtocol != null) {
					if (br.video.protocol.contains(this.videoProtocol) == false) {
						errorString.append("Video Creative protocols don't match");
						return false;
					}
				}
			}
			if (br.video.mimeTypes.size() != 0) {
				if (this.videoMimeType != null) {
					if (br.video.mimeTypes.contains(this.videoMimeType) == false) {
						errorString.append("Video Creative mime types don't match");
						return false;
					}
				}
			}
		}

		Node n = null;
		/** Atributes that are specific to the creative (additional to the campaign */
		try {
			for (int i = 0; i < attributes.size(); i++) {
				n = attributes.get(i);
				if (n.test(br) == false) {
					errorString.append("CREATIVE MISMATCH: ");
					errorString.append(n.hierarchy);
					return false;
				}
			}
		} catch (Exception error) {
			// error.printStackTrace();
			errorString.append("Internal error in bid request: "
					+ n.hierarchy + " is missing, ");
			errorString.append(error.toString());
			return false;
		}

		return true;
	}
}

