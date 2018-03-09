package com.xrtb.exchanges.google;

import com.xrtb.pojo.BidRequest;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.openrtb.OpenRtb.BidRequest.Imp.Video;
import com.google.openrtb.OpenRtb.ImageAssetType;
import com.google.openrtb.OpenRtb.NativeRequest.Asset;
import com.google.openrtb.OpenRtb.NativeRequest.Asset.Data;
import com.google.openrtb.OpenRtb.NativeRequest.Asset.Image;
import com.google.openrtb.OpenRtb.NativeRequest.Asset.Title;

/**
 * Native ad decomposition for Google.
 * @author Ben M. Faul
 *
 */
public final class NativeFramework {

	/**
	 * Make an asset from the google asset construct defined by 'a'.
	 * @param a Asset. The protobuf defined asset.
	 * @param array ArrayNode. The array list to add the native asset after decoding.
	 */
	public static void makeAsset(Asset a, ArrayNode array) {
		ObjectNode node = BidRequest.factory.objectNode();
		array.add(node);
		if (a.hasId()) {
			node.put("id", a.getId());
		}
		if (a.hasRequired()) {
			if (a.getRequired())
				node.put("required", 1);
			else
				node.put("required", 0);
		}
		if (doTitle(a, node) ||
				doImg(a,node) ||
				doVideo(a,node) ||
				doData(a,node))
			return;
		System.out.println("No native construct was available on this asset: " + a);
	}
	
	/**
	 * Process a title.
	 * @param a Asset. Tge google defined asset.
	 * @param node ObjectNode. The object that will contain this asset.
	 * @return boolean. True if this was a title.
	 */
	public static boolean doTitle(Asset a, ObjectNode node) {
		if (!a.hasTitle())
			return false;
		
		ObjectNode title = BidRequest.factory.objectNode();
		Title n = a.getTitle();
		if (n.hasLen()) 
			title.put("len", n.getLen());
		node.set("title", title);
		return true;
	}
	
	/**
	 * Process an image.
	 * @param a Asset. The google defined asset.
	 * @param node ObjectNode. The object that will contain this asset.
	 * @return boolean. True if this was an image.
	 */
	public static boolean doImg(Asset a, ObjectNode node) {
		if (!a.hasImg())
			return false;
		
		Image image = a.getImg();
		ObjectNode n = BidRequest.factory.objectNode();
		
		if (image.hasW())
			n.put("w", image.getW());
		if (image.hasWmin())
			n.put("wmin", image.getWmin());
		if (image.hasH())
			n.put("h", image.getH());
		if (image.hasHmin())
			n.put("hmin", image.getHmin());
		if (image.hasType()) {
			ImageAssetType iat = image.getType();
			n.put("type", iat.getNumber());
		}
		
		ArrayNode mimes = BidRequest.factory.arrayNode();
		n.set("mimes", GoogleBidRequest.getAsStringList(mimes, image.getMimesList()));

		node.set("img", n);
		return true;
	}
	
	/**
	 * Process a video asset.
	 * @param asset Asset. The google defined asset.
	 * @param n ObjectNode. The object that will contain this asset.
	 * @return boolean. True if this was a video.
	 */
	public static boolean doVideo(Asset asset, ObjectNode n) {
		if (!asset.hasVideo())
			return false;
		
		Video v = asset.getVideo();
		ObjectNode node = BidRequest.factory.objectNode();
		
		if (v.hasH()) node.put("h", v.getH());
		if (v.hasW()) node.put("w", v.getW());
		if (v.hasPos()) node.put("pos", v.getPos().getNumber());
		if (v.getApiCount() > 0) {
			ArrayNode a = BidRequest.factory.arrayNode();
			node.set("api", GoogleBidRequest.getAsAttributeListAPI(a, v.getApiList()));
		}
		if (v.getBattrCount() > 0) {
			ArrayNode a = BidRequest.factory.arrayNode();
			node.set("battr", GoogleBidRequest.getAsAttributeList(a, v.getBattrList()));
		}
		if (v.getMimesCount() > 0) {
			ArrayNode a = BidRequest.factory.arrayNode();
			node.set("mimes", GoogleBidRequest.getAsStringList(a, v.getMimesList()));
		}
		
		if (v.getProtocolsCount() > 0) {
			ArrayNode a = BidRequest.factory.arrayNode();
			node.set("protocols", GoogleBidRequest.getAsAttributeListProtocols(a,v.getProtocolsList()));
		}
		
		if (v.hasBoxingallowed()) node.put("boxingallowed", v.getBoxingallowed());
		if (v.hasLinearity()) node.put("linearity", v.getLinearity().getNumber());
		if (v.hasMaxbitrate()) node.put("maxbitrate", v.getMaxbitrate());
		if (v.hasMinbitrate()) node.put("minbitrate",v.getMinbitrate());
		if (v.hasMinduration()) node.put("minduration", v.getMinduration());
		if (v.hasMaxduration()) node.put("maxduration", v.getMaxduration());
		if (v.hasMaxextended()) node.put("maxextended", v.getMaxextended());
	
		n.set("video", node);
		return true;
	}
	
	/**
	 * Process a data asset.
	 * @param a Asset. The google defined asset.
	 * @param node ObjectNode. The object that will contain this asset.
	 * @return boolean. True if this was a data asset.
	 */
	public static boolean doData(Asset a, ObjectNode node) {
		if (!a.hasData()) 
			return true;
		
		ObjectNode n = BidRequest.factory.objectNode();
		Data d = a.getData();
		if (d.hasType()) 
			n.put("type", d.getType().getNumber());
		if (d.hasLen())
			n.put("len", d.getLen());
		
		node.set("data", n);
		return true;

	}
	
}
