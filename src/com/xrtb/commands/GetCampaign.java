package com.xrtb.commands;

import com.xrtb.bidder.Controller;

/**
 * A class that is used to encapsulate a 0MQ command for adding a campaign to the bidder.
 * Jackson will be used to create the structure.
 * @author Ben M. Faul
 *
 */
public class GetCampaign extends BasicCommand {

	/**
	 * Empty constructor for gson
	 */
	public GetCampaign() {
		super();
		cmd = Controller.GET_CAMPAIGN;
		msg = "Return a campaign JSON";
	}

	/**
	 * Add a campaign to the system.
	 * @param to String. The bidder that will execute the command.
	 * @param name String. The name of the owner of the campaign.
	 * @param id String id. The campaign adid to load.
	 */
	public GetCampaign(String to, String name, String id) {
		super(to);
		cmd = Controller.GET_CAMPAIGN;
		status = "ok";
		this.owner = name;
		target = id;
		msg = "JSON returned for: " + name +"/" + id;
	}
}
