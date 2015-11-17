package com.xrtb.pojo;


import java.util.ArrayList;
import java.util.List;

/**
 * A class that comnprises the video part of a regular ad specified in the bid request
 * @author Ben M. Faul
 */

public class Video {
	/** The required field as set forth in the rtb native a spec */
	public int required = 0;
	/** The min duration as set forth in the rtb native a spec */
	public int minduration = -1;
	/** The max duration field as set forth in the rtb native a spec */
	public int maxduration = -1;
	/** The linearity field as set forth in the rtb native a spec */
	public int linearity = -1;
	/** The protocol supported as set forth in the rtb native a spec */
	public List<Integer> protocol = new ArrayList<Integer>();
	/** The mine types I like */
	public List<String> mimeTypes = new ArrayList<String>();
}
