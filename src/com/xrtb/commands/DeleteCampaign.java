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
		msg = "A campaign is being deleted from the system";
		name = "DeleteCampaign";
	}
	
	/**
	 * Delete a campaign from the database.
	 * @param to String. The bidder that will host the command.
	 * @param owner String. The owner of the campaign.
	 * @param id String. The casmpaign adid to delete.
	 */
	public DeleteCampaign(String to, String owner, String id) {
		super(to);
		this.owner = owner;
		target = id;
		cmd = Controller.DEL_CAMPAIGN;
		status = "ok";
		msg = "A campaign is being deleted from the system: " + id;
		name = "DeleteCampaign";
	}
}
