package com.xrtb.commands;

import com.xrtb.bidder.Controller;
import com.xrtb.common.Campaign;

/**
 * A class that is used to encapsulate a 0MQ command for adding a campaign to the bidder.
 * Jackson will be used to create the structure.
 * @author Ben M. Faul
 *
 */
public class AddCampaign extends BasicCommand {
			
	/**
	 * Empty constructor for gson
	 */
	public AddCampaign() {
		super();
		cmd = Controller.ADD_CAMPAIGN;
		msg = "A new campaign is being added to the system";
	}

	/**
	 * Add a campaign to the system.
	 * @param to String. The bidder that will execute the command.
	 * @param id String id. The campaign adid to load.
	 */
	public AddCampaign(String to, String id) {
		super(to);
		cmd = Controller.ADD_CAMPAIGN;
		status = "ok";
		target = id;
		msg = "A new campaign is being added to the system: " + name +"/" + target;
	}
}
