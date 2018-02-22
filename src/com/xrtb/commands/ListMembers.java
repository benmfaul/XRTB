package com.xrtb.commands;


import com.xrtb.bidder.Controller;

/**
 * A class that is used to encapsulate a 0MQ command for adding a campaign to the bidder.
 * Jackson will be used to create the structure.
 * @author Ben M. Faul
 *
 */
public class ListMembers extends BasicCommand {

	/**
	 * Empty constructor for gson
	 */
	public ListMembers() {
		super();
		cmd = Controller.LIST_MEMBERS;
		msg = "List member bidders.";
	}

	/**
	 * Add a campaign to the system.
	 * @param to String. The bidder that will execute the command.
	 */
	public ListMembers(String to) {
		super(to);
		cmd = Controller.LIST_MEMBERS;
		status = "ok";
	}
}
