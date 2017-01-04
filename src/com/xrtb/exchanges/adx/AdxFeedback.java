package com.xrtb.exchanges.adx;

/**
 * A class to use in logging feedback messages from adx.
 * @author Ben M. Faul
 *
 */
public class AdxFeedback {

	/** The id of the feedback messages (relates to a bid id) */
	public String feedback;
	/** The reason the bid was filtered */
	public int code;
	
	/** 
	 * Constructor for jackson.
	 */
	public AdxFeedback() {
		
	}
}
