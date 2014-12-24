package com.xrtb.commands;

import com.xrtb.bidder.Controller;

/**
 * A class that is used to delete a campaign (by the 'id')
 * @author Ben M. Faul
 *
 */
public class DeleteCampaign extends BasicCommand {
	/** The name of the campaign to delete */
	public String campaign;
	/**
	 * Default constructor for GSON
	 */
	public DeleteCampaign() {
		
	}
	
	public DeleteCampaign(String id) {
		super();
		campaign = id;
		cmd = Controller.DEL_CAMPAIGN;
		status = "ok";
	}
}
