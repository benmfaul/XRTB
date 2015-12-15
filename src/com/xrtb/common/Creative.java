package com.xrtb.common;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xrtb.exchanges.Nexage;
import com.xrtb.nativeads.assets.Asset;
import com.xrtb.nativeads.assets.Entity;
import com.xrtb.nativeads.creative.Data;
import com.xrtb.nativeads.creative.Link;
import com.xrtb.nativeads.creative.NativeCreative;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;
import com.xrtb.pojo.NativePart;
import com.xrtb.pojo.Video;

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
	public String currency = null; //

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
		encodedFurl = URIEncoder.myUri(forwardurl);
		encodedIurl = URIEncoder.myUri(imageurl);

		if (adm != null && adm.size() > 0) {
			String s = "";
			for (String ss : adm) {
				s += ss;
			}
			encodedAdm = URIEncoder.myUri(s);
		}

		strW = "" + w;
		strH = "" + h;
		strPrice = "" + price;
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

	public void encodeAttributes() throws Exception {
		for (Node n : attributes) {
			n.setValues();
		}

		if (nativead != null) {
			nativead.encode();
		}
	}

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
	public boolean process(BidRequest br, StringBuilder errorString) {
		if (isVideo() && br.video == null) {
			if (errorString != null)
				errorString.append("Creative is video, request is not");
			return false;
		}
		if (isNative() && br.nativePart == null) {
			if (errorString != null)
				errorString
						.append("Creative is native content, request is not");
			return false;
		}
		if ((isVideo() == false && isNative() == false) != (br.nativePart == null && br.video == null)) {
			if (errorString != null)
				errorString.append("Creative is banner, request is not");
			return false;
		}

		if (isNative()) {
			if (br.nativePart.layout != 0) {
				if (br.nativePart.layout != nativead.nativeAdType) {
					if (errorString != null)
						errorString.append("Native ad layouts don't match");
					return false;
				}
			}
			if (br.nativePart.title != null) {
				if (br.nativePart.title.required == 1 && nativead.title == null) {
					if (errorString != null)
						errorString
								.append("Native ad request wants a title, creative has none.");
					return false;
				}
				if (nativead.title.title.text.length() > br.nativePart.title.len) {
					if (errorString != null)
						errorString
								.append("Native ad title length is too long");
					return false;
				}
			}

			if (br.nativePart.img != null && nativead.img != null) {
				if (br.nativePart.img.required == 1 && nativead.img == null) {
					if (errorString != null)
						errorString
								.append("Native ad request wants an img, creative has none.");
					return false;
				}
				if (nativead.img.img.w != br.nativePart.img.w) {
					if (errorString != null)
						errorString.append("Native ad img widths dont match");
					return false;
				}
				if (nativead.img.img.h != br.nativePart.img.h) {
					if (errorString != null)
						errorString.append("Native ad img heoghts dont match");
					return false;
				}
			}

			if (br.nativePart.video != null) {
				if (br.nativePart.video.required == 1 || nativead.video == null) {
					if (errorString != null)
						errorString
								.append("Native ad request wants a video, creative has none.");
					return false;
				}
				if (nativead.video.video.duration < br.nativePart.video.minduration) {
					if (errorString != null)
						errorString
								.append("Native ad video duration is < what request wants");
					return false;
				}
				if (nativead.video.video.duration > br.nativePart.video.maxduration) {
					if (errorString != null)
						errorString
								.append("Native ad video duration is > what request wants");
					return false;
				}
				if (br.nativePart.video.linearity != null
						&& br.nativePart.video.linearity != nativead.video.video.linearity) {
					if (errorString != null)
						errorString
								.append("Native ad video linearity doesn't match the ad");
					return false;
				}
				if (br.nativePart.video.protocols.size() > 0) {
					if (br.nativePart.video.protocols
							.contains(nativead.video.video.protocol)) {
						if (errorString != null)
							errorString
									.append("Native ad video protocol doesn't match the ad");
						return false;
					}
				}

			}

			for (Data datum : br.nativePart.data) {
				Integer val = datum.type;
				Entity e = nativead.dataMap.get(val);
				if (datum.required == 1 && e == null) {
					if (errorString != null)
						errorString.append("Native ad data item of type "
								+ datum.type + " not present in ad");
					return false;
				}
				if (e != null) {
					if (e.value.length() > datum.len) {
						if (errorString != null)
							errorString.append("Native ad data item of type "
									+ datum.type
									+ " length is too long for request");
						return false;
					}
				}

			}

			return true;
		}

		if (br.nativePart == null && br.w.doubleValue() != w.doubleValue()
				|| br.h.doubleValue() != h.doubleValue()) {
			if (errorString != null)
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
					if (errorString != null)
						errorString
								.append("Video Creative  linearity attributes dont match");
					return false;
				}
			}
			if (br.video.minduration != -1) {
				if (this.videoDuration != null) {
					if (!(this.videoDuration.intValue() >= br.video.minduration)) {
						if (errorString != null)
							errorString
									.append("Video Creative min duration not long enough.");
						return false;
					}
				}
			}
			if (br.video.maxduration != -1) {
				if (this.videoDuration != null) {
					if (!(this.videoDuration.intValue() <= br.video.maxduration)) {
						if (errorString != null)
							errorString
									.append("Video Creative duration is too long.");
						return false;
					}
				}
			}
			if (br.video.protocol.size() != 0) {
				if (this.videoProtocol != null) {
					if (br.video.protocol.contains(this.videoProtocol) == false) {
						if (errorString != null)
							errorString
									.append("Video Creative protocols don't match");
						return false;
					}
				}
			}
			if (br.video.mimeTypes.size() != 0) {
				if (this.videoMimeType != null) {
					if (br.video.mimeTypes.contains(this.videoMimeType) == false) {
						if (errorString != null)
							errorString
									.append("Video Creative mime types don't match");
						return false;
					}
				}
			}
		}

		Node n = null;
		/**
		 * Atributes that are specific to the creative (additional to the
		 * campaign
		 */
		try {
			for (int i = 0; i < attributes.size(); i++) {
				n = attributes.get(i);
				if (n.test(br) == false) {
					if (errorString != null)
						errorString.append("CREATIVE MISMATCH: ");
					if (errorString != null)
						errorString.append(n.hierarchy);
					return false;
				}
			}
		} catch (Exception error) {
			// error.printStackTrace();
			if (errorString != null)
				errorString.append("Internal error in bid request: "
						+ n.hierarchy + " is missing, ");
			if (errorString != null)
				errorString.append(error.toString());
			return false;
		}

		return true;
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

		request.w = w;
		request.h = h;
		
		BidResponse br = null; 
		
		try {
			if (this.isVideo()) {
				br = new BidResponse(request, camp, this, "123");
				request.video = new Video();
				request.video.linearity = this.videoLinearity;
				request.video.protocol.add(this.videoProtocol);
				request.video.maxduration= this.videoDuration + 1;
				request.video.minduration = this.videoDuration - 1;
				
				str = br.getAdmAsString();
				/**
				 * Read in the stubbed vide page and patch the VAST into it
				 */
				page = new String(Files.readAllBytes(Paths.get("web/videostub.html")),StandardCharsets.UTF_8);
				page = page.replaceAll("___VIDEO___", "http://localhost:8080/vast/onion270.xml");
			} else
			if (this.isNative()) {	
				page = "<html><title>Test Creative</title><body><img src='images/under-construction.gif'></img></body></html>";
			} else {
				br = new BidResponse(request, camp, this, "123");
				str = br.getAdmAsString();
				page = "<html><title>Test Creative</title><body><xmp>" + str + "</xmp>" + str + "</body></html>";
			}
			temp = File.createTempFile("test", ".html", new File("www/temp"));
			temp.deleteOnExit();

			Files.write(Paths.get(temp.getAbsolutePath()), page.getBytes());
		} catch (Exception error) {
			error.printStackTrace();
		}
		return "temp/" + temp.getName();
	}
}
