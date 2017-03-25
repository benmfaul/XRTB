package com.xrtb.commands;

import com.xrtb.bidder.Controller;

/**
 * A class that is used to get the price in a creative.
 * @author Ben M. Faul
 *
 */

public class GetPrice extends BasicCommand {
	public GetPrice() {
		super();
		cmd = Controller.GET_PRICE;
		msg = "Get Price issued";
	}
	
	public GetPrice(String to, String owner, String campaign, String creative) {
		super(to);
		this.owner = owner;
		this.name = campaign;
		this.target = creative;
		cmd = Controller.GET_PRICE;
		msg = "Get Price Issued";
	}
}
