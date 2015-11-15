package com.xrtb.nativeads.creative;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that encapsulates an IMG asset in a bid request for a native ad.
 * @author Ben M. Faul
 *
 */

public class Img {
	/** The required flag as defined in the RTB Native Ad spec */
	public int required = 0;
	/** The type flag as defined in the RTB Native Ad spec */
	public int type;
	/** The  width as defined in the RTB Native Ad spec */
	public int w;
	/** The  height as defined in the RTB Native Ad spec */
	public int h;
	/** The mime types supported as defined in the RTB Native Ad spec */
	public List<String> mimes = new ArrayList<String>();
}
