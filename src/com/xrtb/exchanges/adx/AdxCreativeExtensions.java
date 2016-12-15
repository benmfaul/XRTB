package com.xrtb.exchanges.adx;

import java.util.List;

/**
 * 
 * A class to hold Adx extensions for creative definitions.
 * @author Ben M. Fsaul
 *
 */
public class AdxCreativeExtensions {

	/** Adx category, if present */
	public Integer adxCategory;
	/** Adx vendor type, if present */
	public Integer adxVendorType;
	/** Adx clickthrough url, if this is a banner ad */
	public String adxClickThroughUrl;
	/** Attributes of the creative, if supplied */
	public List<Integer> attributes;
	/** Impression tracking url for banners */
	public String adxTrackingUrl;
	
	public AdxCreativeExtensions() {
		
	}
}
