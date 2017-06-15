package com.xrtb.fraud;

/**
 * A class that represents Fraud logs
 * @author Ben M. Faul
 *
 */
public class FraudLog {
	// The time the log was generated
	public long timestamp = System.currentTimeMillis();
	// The source of truth
	public String source;
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
	// The organization
	public String organization;
	// The time it took to generate in.
	public long xtime;


	/**
	 * Default constructor used for JSON serialization.
	 */
	public FraudLog() {
		
	}
}
