package com.xrtb.nativeads.creative;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.xrtb.common.URIEncoder;
import com.xrtb.nativeads.assets.Asset;
import com.xrtb.nativeads.assets.Entity;
import com.xrtb.pojo.BidRequest;

/**
 * The native part of a creative.
 * @author Ben M. Faul
 *
 */
public class NativeCreative {
		/** The assets that belong to this creative */
		public List<Asset> assets = new ArrayList<Asset>();
		/** The link asset for this creative */
		public Link link;
		/** The impression trackers used by this creative */
		public List<String> imptrackers;
		/** The native ad type of this creative */
		public Integer nativeAdType;
		
		/** The data asset of this creative. Transient because in the campaign it is defined with all the assets in 
		 * an  array, but we will pull out img, video, link, and title  so we can quickly find them later. Only
		 * data assets are in the array after it is encoded.
		 */
		transient public Map<Integer,Entity> dataMap = new HashMap();
		/** The img asset of this creative */
		transient public Asset img;
		/** The video asset of this creative. Transient because in the campaign it is defined with the assets array, but
		 * we will pull it out when the campaign is created so we can quickly find it later
		 */
		transient public Asset video;
		/** The title asset of this creative. Transient because in the campaign it is defined with the assets array, but
		 * we will pull it out when the campaign is created so we can quickly find it later
		 */
		transient public Asset title;

		
    /**
     * An empty constructor for use by Jackson
     */
	public NativeCreative() {
		
	}
	
	/**
	 * Pulls the image, video, link, and title assets out of the array and assigns them to objects. The
	 * data assets are assigned to a hashmap by type for fast lookup O(1) in the selection of creatives.
	 */
	public void encode() {
		for (Asset a : assets) {
			if (a.title != null)
				title = a;
			else
			if (a.img != null)
				img = a;
			else
			if (a.video != null)
				video = a;
			else {
				dataMap.put(a.getDataType(), a.data);
			}
		}
	}
	
	/**
	 * Create the encoded native ADM from the assets of the creative matches with the assets of the bid request.
	 * @param br BidRequest. The bid request of this transaction
	 * @return String. The URI encoded string to use in the ADM field.
	 */
	public String  getEncodedAdm(BidRequest br) {
		StringBuilder buf = new StringBuilder();
		// /////////////////////////////////////// Move to initialiation !
		// //////////////////////////////
		buf.append("{\"native\":{\"ver\":1,");
		buf.append("\"link\":");
		buf.append(link.getStringBuilder());
		buf.append(",\"assets\":[");
		// ///////////////////////////////////////////

		int index = -1; // index of the asset in the bid request
		for (int i = 0; i < assets.size(); i++) {
			Asset a = assets.get(i);
			index = br.getNativeAdAssetIndex(a.getEntityName(), a.getDataKey(),
					a.getDataType());
			
			/**
			 * If -1 is returned, then the creative has a native ad component that the bid request 
			 * didn't ask for. We presume this is ok and will bid, since the creative did have all
			 * the other required pieces.
			 */
			if (i != -1) {
				buf.append(a.toStringBuilder(index));
				if (i + 1 != assets.size())
					buf.append(",");
			}
		}
		buf.append("]}}");

		/*
		 * No escape the string so it can be passed in the adm field
		 */
		return URIEncoder.myUri(buf.toString());
	}
}
