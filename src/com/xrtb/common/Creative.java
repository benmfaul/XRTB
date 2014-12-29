package com.xrtb.common;

/**
 * An object that encapsulates the 'creative' (the ad served up and it's attributes)
 * @author bfaul
 *
 */
public class Creative {
	/** The forward URL used with this creative */
    public String forwardurl;
    /** The encoded version of the forward url used by this creative */
    public String encodedFurl;
    /* The image url used by this creative */
    public String imageurl;
    /** The encoded image URL used by this creative */
    public String encodedIurl;
    /** The impression id of this creative */
    public String impid;
    /** The width of this creative */
    public double w;
    /** The height of this creative */
    public double h;
    
    /**
     * Empty constructor for creation using json.
     */
    public Creative() {
    	
    }
    
    /**
     * Does the HTTP encoding for the forward url and image url. The bid will use the encoded form.
     */
    void encodeUrl() {
    //	encodedFurl = URIEncoder.encodeURI(forwardUrl);
    //	encodedIurl = URIEncoder.encodeURI(imageUrl);
    	
    	encodedFurl = URIEncoder.myUri(forwardurl);
    	encodedIurl = URIEncoder.myUri(imageurl);
    }

	public String getForwardUrl() {
		return forwardurl;
	}

	public void setForwardUrl(String forwardUrl) {
		this.forwardurl = forwardUrl;
	}

	public String getImageUrl() {
		return imageurl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageurl = imageUrl;
	}

	/**
	 * Returns the impression id for this creative (the database key used in wins and bids).
	 * @return String. The impression id.
	 */
	public String getImpid() {
		return impid;
	}

	/**
	 * Set the impression id object.
	 * @param impid String. The impression id to use for this creative. This is merely a 
	 * databse key you can use to find bids and wins for this id.
	 */
	public void setImpid(String impid) {
		this.impid = impid;
	}

	/**
	 * Get the width of the creative, in pixels.
	 * @return int. The height in pixels of this creative.
	 */
	public double getW() {
		return w;
	}

	/**
	 * Set the width of the creative.
	 * @param w int. The width of the pixels to set the creative.
	 */
	public void setW(double w) {
		this.w = w;
	}

	/**
	 * Return the height in pixels of this creative
	 * @return int. The height in pixels.
	 */
	public double getH() {
		return h;
	}

	/**
	 * Set the height of this creative.
	 * @param h int. Height in pixels.
	 */
	public void setH(double h) {
		this.h = h;
	}

}
