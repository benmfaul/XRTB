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
}
