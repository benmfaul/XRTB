package com.xrtb.nativeads.creative;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.xrtb.common.URIEncoder;
import com.xrtb.nativeads.assets.Asset;
import com.xrtb.nativeads.assets.Entity;
import com.xrtb.pojo.BidRequest;

public class NativeCreative {
		public List<Asset> assets = new ArrayList<Asset>();
		public Link link;
		public List<String> imptrackers;
		public Integer nativeAdType;
		
		transient public Map<Integer,Entity> dataMap = new HashMap();
		transient public Asset img;
		transient public Asset video;
		transient public Asset title;

		
	public NativeCreative() {
		
	}
	
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
			buf.append(a.toStringBuilder(index));
			if (i + 1 != assets.size())
				buf.append(",");
		}
		buf.append("]}}");

		/*
		 * No escape the string so it can be passed in the adm field
		 */
		return URIEncoder.myUri(buf.toString());
	}
}
