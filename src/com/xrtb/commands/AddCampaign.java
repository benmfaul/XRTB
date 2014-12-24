package com.xrtb.commands;

import com.xrtb.common.Campaign;

/**
 * A class that is used to encapsulate a REDIS command for adding a campaign to the bidder.
 * GSON will be used to create the structure.
 * @author Ben M. Faul
 *
 */
public class AddCampaign extends BasicCommand {
	
	/** The campaign attached to this command. */
	public Campaign campaign;		
	
	/**
	 * Empty constructor for gson
	 */
	public AddCampaign() {
		
	}
	
	/**
	 * 
	 * @param c Campaign. This is the campaign to add to the command.
	 */
	public AddCampaign(Campaign c) {
		campaign = c;
	}
}
