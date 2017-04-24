package com.xrtb.commands;

import com.xrtb.bidder.Controller;

/**
 * A class that is used to get the price in a creative.
 * @author Ben M. Faul
 *
 */

public class GetPrice extends BasicCommand {
	
	/**
	 * Default constructor.
	 */
	public GetPrice() {
		super();
		cmd = Controller.GET_PRICE;
		msg = "Get Price issued";
	}
	
	/**
	 * A command to query the price of a campaign.
	 * @param to String. The bidder that will host this command.
	 * @param owner String. The owner of the campaign.
	 * @param campaign String. The campaignid in question.
	 * @param creative String. The creative impid to retrieve the price from.
	 */
	public GetPrice(String to, String owner, String campaign, String creative) {
		super(to);
		this.owner = owner;
		this.name = campaign;
		this.target = creative;
		cmd = Controller.GET_PRICE;
		msg = "Get Price Issued";
	}
}
