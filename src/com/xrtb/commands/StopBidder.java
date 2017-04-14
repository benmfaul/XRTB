package com.xrtb.commands;

import com.xrtb.bidder.Controller;

/**
 * A class implementing the 'stop bidding' command.
 * @author Ben M. Faul
 *
 */

public class StopBidder extends BasicCommand {
	
	/**
	 * Default constructor.
	 */
	public StopBidder() {
		super();
		cmd = Controller.STOP_BIDDER;
		msg = "Stop Bidder command issued";
		name = "StopBidder";
	}
	
	/**
	 * Stops the assigned bidder. If "" or * all of them will stop.
	 * @param to String. The bidder to stop.
	 */
	public StopBidder(String to) {
		super(to);
		cmd = Controller.STOP_BIDDER;
		name = "StopBidder";
		msg = "Stop Bidder command issued";
	}
}
