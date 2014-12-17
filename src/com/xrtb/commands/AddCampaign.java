package com.xrtb.commands;

import com.xrtb.common.Campaign;

/**
 * A class that is used to encapsulate a REDIS command for adding a campaign to the bidder
 * @author Ben M. Faul
 *
 */
public class AddCampaign extends BasicCommand {
	public Campaign campaign;
	
	/**
	 * Empty constructor for gson
	 */
	public AddCampaign() {
		
	}
	
	/**
	 * 
	 * @param c
	 */
	public AddCampaign(Campaign c) {
		campaign = c;
	}
}
