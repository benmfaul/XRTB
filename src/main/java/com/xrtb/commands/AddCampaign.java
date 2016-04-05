package com.xrtb.commands;

import com.xrtb.bidder.Controller;
import com.xrtb.common.Campaign;

/**
 * A class that is used to encapsulate a REDIS command for adding a campaign to the bidder.
 * GSON will be used to create the structure.
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

	public AddCampaign(String to, String name, String id) {
		super(to);
		cmd = Controller.ADD_CAMPAIGN;
		status = "ok";
		this.owner = name;
		target = id;
		msg = "A new campaign is being added to the system: " + name +"/" + target;
	}
}
