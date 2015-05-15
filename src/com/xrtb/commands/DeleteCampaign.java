package com.xrtb.commands;

import com.xrtb.bidder.Controller;

/**
 * A class that is used to delete a campaign (by the 'id')
 * @author Ben M. Faul
 *
 */
public class DeleteCampaign extends BasicCommand {
	/**
	 * Default constructor for GSON
	 */
	public DeleteCampaign() {
		super();
		cmd = Controller.DEL_CAMPAIGN;
		status = "ok";
	}
	
	public DeleteCampaign(String to, String id) {
		super(to);
		target = id;
		cmd = Controller.DEL_CAMPAIGN;
		status = "ok";
	}
}
