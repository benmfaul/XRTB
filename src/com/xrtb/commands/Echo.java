package com.xrtb.commands;
import java.util.List;

import com.xrtb.bidder.Controller;
import com.xrtb.common.Campaign;

/**
 * This is the echo command and response format. It provides basic statistical info plus
 * all of the campaigns currently loaded in the system.
 * @author Ben M. Faul
 *
 */
public class Echo extends BasicCommand {
	public List<Campaign> campaigns;
	public int percentage;
	public boolean stopped;
	public long bid;
	public long nobid;
	
	public Echo() {
		super();
		cmd = Controller.ECHO;
		status = "ok";
	}
}
