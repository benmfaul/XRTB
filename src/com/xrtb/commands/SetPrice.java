package com.xrtb.commands;

import com.xrtb.bidder.Controller;

/**
 * A class that is used to get the price in a creative.
 * @author Ben M. Faul
 *
 */

public class SetPrice extends BasicCommand {
	public SetPrice() {
		super();
		cmd = Controller.SET_PRICE;
		msg = "Get Price issued";
	}
	
	public SetPrice(String to, String owner, String campaign, String creative, double price) {
		super(to);
		this.owner = owner;
		this.name = campaign;
		this.target = creative;
		this.price = price;
		cmd = Controller.SET_PRICE;
		msg = "Get Price Issued";
	}
}
