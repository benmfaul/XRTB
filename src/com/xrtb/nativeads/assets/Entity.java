package com.xrtb.nativeads.assets;

import java.util.ArrayList;
import java.util.List;

public class Entity {
	public String text;			// title
	public String value;		// data 
	public String url;			// link	
	public String fallback;
	public Integer w;
	public Integer h;
	public Integer type;		// used with data, index into the type
	public Integer duration;
	public List<String>clicktrackers;
	
	public Entity() {
		
	}
	
	public StringBuilder toStringBuilder(int index,int type) {
		StringBuilder sb = new StringBuilder();
		sb.append("{\"id\":");
		sb.append(index);
		sb.append(",");
		switch(type) {
		case Asset.LINK:
			sb.append("\"link\":{");
			sb.append("\"url\":\"");
			sb.append(url);
			sb.append("\"}");
			break;
		case Asset.TITLE:
			sb.append("\"title\":{");
			sb.append("\"text\":\"");
			sb.append(text);
			sb.append("\"}");
			break;
		case Asset.IMAGE:
			sb.append("\"img\":{");
			sb.append("\"url\":\"");
			sb.append(url);
			sb.append("\",\"w\":");
			sb.append(w);
			sb.append(",\"h\":");
			sb.append(h);
			sb.append("}");
			break;
		case Asset.DATA:
			sb.append("\"data\":{");
			sb.append("\"value\":\"");
			sb.append(value);
			sb.append("\"}");
			break;
		case Asset.VIDEO:
			sb.append("\"video\":\"NOT IMPLEMENTED YET\"}");
			break;
		}
		sb.append("}");
		return sb;
	}
}