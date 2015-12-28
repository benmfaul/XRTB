package com.xrtb.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.bidder.Controller;

/**
 * A class that is used to delete a creative from a campaign.
 * @author Ben M. Faul
 *
 */

public class DeleteCreative extends BasicCommand {
	public String owner = null;
	public DeleteCreative() {
		super();
		cmd = Controller.DELETE_CREATIVE;
		msg = "Delete Creative issued";
	}
	
	public DeleteCreative(String s) {
		super(s);
		cmd = Controller.DELETE_CREATIVE;
		msg = "Delete Creative Issued";
	}
	
	@Override
	public String toString() {
		ObjectMapper mapper = new ObjectMapper();
		String jsonString;
		try {
			jsonString = mapper.writeValueAsString(this);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return jsonString;
	}
}
