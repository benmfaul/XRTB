package com.xrtb.commands;

import com.xrtb.bidder.Controller;

/**
 * A class that is used to encapsulate a 0MQ command for adding a campaign to the bidder.
 * Jackson will be used to create the structure.
 * @author Ben M. Faul
 *
 */
public class ListCampaignsResponse extends ListCampaigns {

	/**
	 * Empty constructor for gson
	 */
	public ListCampaignsResponse() {
		super();
		cmd = Controller.LIST_CAMPAIGNS_RESPONSE;
		msg = "Campaigns are listed";
	}

	/**
	 * Add a campaign to the system.
	 * @param to String. The bidder that will execute the command.
	 * @param id String id. The campaign adid to load.
	 */
	public ListCampaignsResponse(String to, String name, String id) {
		super(to);
		cmd = Controller.LIST_CAMPAIGNS_RESPONSE;
		status = "ok";
		target = id;
		msg = "The following campagigns are running: " + target;
	}
}
