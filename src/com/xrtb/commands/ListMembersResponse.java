package com.xrtb.commands;


import com.xrtb.bidder.Controller;

/**
 * A class that is used to encapsulate a 0MQ command for adding a campaign to the bidder.
 * Jackson will be used to create the structure.
 * @author Ben M. Faul
 *
 */
public class ListMembersResponse extends ListCampaigns {

	/**
	 * Empty constructor for gson
	 */
	public ListMembersResponse() {
		super();
		cmd = Controller.LIST_MEMBERS_RESPONSE;
		msg = "Member bidders are listed";
	}

	/**
	 * Add a campaign to the system.
	 * @param to String. The bidder that will execute the command.
	 * @param id String id. The campaign adid to load.
	 */
	public ListMembersResponse(String to, String name, String id) {
		super(to);
		cmd = Controller.LIST_CAMPAIGNS_RESPONSE;
		status = "ok";
		target = id;
		msg = "The following members are running: " + target;
	}
}
