package com.xrtb.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.bidder.Controller;

/**
 * A class that is used to delete a creative from a campaign.
 * @author Ben M. Faul
 *
 */

public class DeleteCreative extends BasicCommand {
	public DeleteCreative() {
		super();
		cmd = Controller.DELETE_CREATIVE;
		msg = "Delete Creative issued";
	}
	
	public DeleteCreative(String to, String owner, String campaign, String creative) {
		super(to);
		this.owner = owner;
		this.name = campaign;
		this.target = creative;
		cmd = Controller.DELETE_CREATIVE;
		msg = "Delete Creative Issued";
	}
}
