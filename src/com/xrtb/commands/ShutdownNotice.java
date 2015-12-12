package com.xrtb.commands;

import com.xrtb.bidder.Controller;

public class ShutdownNotice extends BasicCommand {
	/**
	 * Empty constructor 
	 */
	public ShutdownNotice() {
		this.cmd = Controller.SHUTDOWNNOTICE;
		msg = "Shutdown notice from this bidder";
		name = "ShutDownNotice";
	}
	
}
