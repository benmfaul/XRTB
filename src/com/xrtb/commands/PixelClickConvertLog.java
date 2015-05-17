package com.xrtb.commands;

/**
 * Base class for logging pixel loads, clicks and conversions.
 * @author Ben M. Faul
 *
 */
public class PixelClickConvertLog {
	public String instance;
	public String payload;
	public long time;
	public int type;
	public static final int PIXEL = 0;
	public static final int CLICK = 1;
	public static final int CONVERT = 2;
	
	public PixelClickConvertLog() {
		
	}
}
