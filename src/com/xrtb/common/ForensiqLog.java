package com.xrtb.common;

/**
 * A class that represents ForensiqIQ logs
 * @author Ben M. Faul
 *
 */
public class ForensiqLog {
	// The IP address of the check.
	public String ip;
	// The page url of the bid request.
	public String url;
	// The user agent of the request.
	public String ua;
	// The seller id in the request.
	public String seller;
	// The exchange where the bid was placed.
	public String exchange;
	// The bid id of the request.
	public String id;
	// The computed risk this was a bot.
	public double risk;
	// The domain found in the bid request.
	public String domain;


	/**
	 * Default constructor used for JSON serialization.
	 */
	public ForensiqLog() {
		
	}
}
