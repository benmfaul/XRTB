package com.xrtb.nativeads.assets;

/**
 * A class that defines the assets used in a native ad bid request and used in the campaign definitions
 * @author Ben M. Faul
 *
 */

public class Asset  {
	/** A data asset */
	public static final int DATA = 0;
	/** An image asset */
	public static final int IMAGE = 1;
	/* A link asset */
	public static final int LINK = 2;
	/* A title asset */
	public static final int TITLE = 3;
	/* A video asset */
	public static final int VIDEO = 4;
	/** The value of the title */
	public Entity title;
	/** A link entity means there is a call to action associated with the other entity in this part */
	public Entity link;
	/** A img entity, will be non null of this is an IMAGE asset */
	public Entity img;
	/** A data entity, will be non null if this ia a data asset */
	public Entity data;
	/** A video entity, will be non null if this is a video entity */
	public Entity video;
	
	/**
	 * Empty constuctor used by Jackson when the campaign is created.
	 */
	public Asset() {
		
	}
	
	/**
	 * Return the entity that belongs to this asset.
	 * @return Entity. The entity of this asset
	 */
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
	
	/**
	 * Return the name of the asset entity.
	 * @return String. The name of the entity as the JSON element.
	 */
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
	
	/**
	 * If this is a data asset, return 'type'. Otherwise returns null.
	 * @return String. Returns 'type' if this is a data asset.
	 */
	public String getDataKey() {
		if (data == null)
			return null;
		return "type";
	}
	
	/**
	 * Return the data 'type' if a data seet, else returns -1.
	 * @return int. The data type defined by the RTB ad native spec.
	 */
	public int getDataType() {
		if (data != null)
			return data.type;
		return -1;
	}
	
	/**
	 * Return the type of asset.
	 * @return int. Returns the asset type as integer flag.
	 */
	public int getType() {
		if (title != null)
			return TITLE;
		if (img != null)
			return IMAGE;
		if (data != null)
			return DATA;
		if (video != null)
			return VIDEO;
		return -1;
	}
	
	/**
	 * Used to form the bid response for a native ad.
	 * Returns the String (builder) representation of this asset, using the index number provided (which matches
	 * the index used in the bid request).
	 * @param index
	 * @return
	 */
	public StringBuilder toStringBuilder(int index) {
		switch(getType()) {
		case TITLE:
			return title.toStringBuilder(index,TITLE);
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
