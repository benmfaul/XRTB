package com.xrtb.commands;

import com.xrtb.common.Configuration;

/**
 * A logger for user conversions.
 * @author Ben M. Faul
 *
 */

public class ConvertLog extends PixelClickConvertLog {

	public ConvertLog() {
		super();
		type = CONVERT;
	}
	
	public ConvertLog(String payload) {
		this.payload = payload;
		type = CONVERT;
		instance = Configuration.getInstance().instanceName;
		time = System.currentTimeMillis();
	}
}
