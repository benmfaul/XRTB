package com.xrtb.nativeads.creative;


import java.util.ArrayList;
import java.util.List;

/**
 * A class that comnprises the video part of a native ad specified in the bid request
 * @author Ben M. Faul
 */

public class Video {
	/** The required field as set forth in the rtb native a spec */
	public int required = 0;
	/** The min duration as set forth in the rtb native a spec */
	public int minduration;
	/** The max duration field as set forth in the rtb native a spec */
	public int maxduration;
	/** The linearity field as set forth in the rtb native a spec */
	public Integer linearity;
	/** The protocol supported as set forth in the rtb native a spec */
	public List<String> protocols = new ArrayList<String>();
}
