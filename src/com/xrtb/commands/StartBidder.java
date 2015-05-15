package com.xrtb.commands;

import com.xrtb.bidder.Controller;

/**
 * A class that is used to send the start bidding command to the bidder.
 * @author Ben M. Faul
 *
 */

public class StartBidder extends BasicCommand {
	public StartBidder() {
		super();
		cmd = Controller.START_BIDDER;
	}
	
	public StartBidder(String s) {
		super(s);
		cmd = Controller.START_BIDDER;
	}
}
