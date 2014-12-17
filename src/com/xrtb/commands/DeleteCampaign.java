package com.xrtb.commands;

import com.xrtb.bidder.Controller;

/**
 * A class that is used to delete a campaign (by the 'id')
 * @author Ben M. Faul
 *
 */
public class DeleteCampaign extends BasicCommand {
	public DeleteCampaign() {
		
	}
	
	public DeleteCampaign(String id) {
		super();
		this.id = id;
		cmd = Controller.DEL_CAMPAIGN;
		status = "ok";
	}
}
