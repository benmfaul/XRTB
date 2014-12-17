package com.xrtb.commands;

import com.xrtb.bidder.Controller;

/**
 * A class implementing the 'stop bidding' command.
 * @author Ben M. Faul
 *
 */

public class StopBidder extends BasicCommand {
	public StopBidder() {
		super();
		cmd = Controller.STOP_BIDDER;
	}
}
