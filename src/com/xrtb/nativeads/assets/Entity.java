package com.xrtb.nativeads.assets;

/**
 * Defines the entity part of a native adasset.
 */
import java.util.List;

public class Entity {
	/** The text field, used when this is a title */
	public String text;
	/** The value field, used when this is a data asset */		
	public String value;	
	/** The value when used as a video or image asset */
	public String url;			
	/** The fallback url when used as a link asset */
	public String fallback;
	/** The width if this an image asset */
	public Integer w;
	/** The height, if this is used as an image asset */
	public Integer h;
	/** The index type if this is a data asset */
	public Integer type;	
	/** The duration in seconds, of a video asset */
	public Integer duration;
	/** The linearity of a video asset */
	public int linearity;
	/** The protocol of the video asset */
	public String protocol;
	/** The clicktrackers used in a link asset */
	public List<String>clicktrackers;
	
	/**
	 * The empty constructor used by Jackson to create the entity from the campaign.
	 */
	public Entity() {
		
	}
	
	/**
	 * Encodes this entity into the String (builder) representation used in the ADM field of the +response 
	 * @param index int. The index of the asset in the bid request.
	 * @param type int. The type of asset
	 * @return StringBuilder. Tghe string representation of the entity for use int he bid response.
	 */
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
			sb.append("\"video\":{");
			sb.append("\"vasttag\":\"");
			sb.append(value);
			sb.append("\"}");
			break;
		}
		sb.append("}");
		return sb;
	}
}