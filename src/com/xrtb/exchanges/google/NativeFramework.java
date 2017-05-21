package com.xrtb.exchanges.google;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.openrtb.OpenRtb.NativeRequest.Asset;
import com.google.openrtb.OpenRtb.NativeRequest.Asset.Data;
import com.xrtb.pojo.BidRequest;

public final class NativeFramework {

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
				doData(a,node) ||
				doLink(a,node))
			return;
		System.out.println("No native construct was available on this asset: " + a);
	}
	
	public static boolean doTitle(Asset a, ObjectNode node) {
		if (!a.hasTitle())
			return false;
		
		//node.put("text", "TBD";				// TBD: GOogle has a lot of trash on this object, when it is just a goddamned string
	
		return true;
	}
	
	public static boolean doImg(Asset a, ObjectNode node) {
		if (!a.hasImg())
			return false;
		
		return true;
	}
	
	public static boolean doVideo(Asset a, ObjectNode node) {
		if (!a.hasVideo())
			return false;
	
		return true;
	}
	
	public static boolean doData(Asset a, ObjectNode node) {
		if (!a.hasData()) 
			return true;
		
		Data d = a.getData();
		
		return true;

	}
	
	public static boolean doLink(Asset a, ObjectNode node) {

		// apparently not supported by Google

		return false;
	}
	
}
