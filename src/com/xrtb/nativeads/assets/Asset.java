package com.xrtb.nativeads.assets;

public class Asset {
	public static final int DATA = 0;
	public static final int IMAGE = 1;
	public static final int LINK = 2;
	public static final int TITLE = 3;
	public static final int VIDEO = 4;
	public Entity title;
	public Entity link;
	public Entity img;
	public Entity data;
	public Entity video;
	
	public Asset() {
		
	}
	
	public Entity getEntity() {
		switch(getType()) {
		case TITLE:
			return title;
		case LINK:
			return link;
		case IMAGE:
			return img;
		case DATA:
			return data;
		case VIDEO:
			return video;
		}
		return null;
	}
	
	public String getEntityName() {

			switch(getType()) {
			case TITLE:
				return "title";
			case LINK:
				return "link";
			case IMAGE:
				return "img";
			case DATA:
				return "data";
			case VIDEO:
				return "video";
			}
			return null;
	}
	
	public String getDataKey() {
		if (data == null)
			return null;
		return "type";
	}
	
	public int getDataType() {
		if (data != null)
			return data.type;
		return -1;
	}
	
	public int getType() {
		if (title != null)
			return TITLE;
		if (link != null)
			return LINK;
		if (img != null)
			return IMAGE;
		if (data != null)
			return DATA;
		if (video != null)
			return VIDEO;
		return -1;
	}
	
	public StringBuilder toStringBuilder(int index) {
		switch(getType()) {
		case TITLE:
			return title.toStringBuilder(index,TITLE);
		case LINK:
			return link.toStringBuilder(index,LINK);
		case IMAGE:
			return img.toStringBuilder(index,IMAGE);
		case DATA:
			return data.toStringBuilder(index,DATA);
		case VIDEO:
			return video.toStringBuilder(index,VIDEO);
		}
		return null;
	}
}
