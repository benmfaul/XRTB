package com.xrtb.commands;

import java.util.ArrayList;
import java.util.List;

import com.xrtb.bidder.Controller;

/**
 * This is the echo command and response format.
 * @author Ben M. Faul
 *
 */
public class Echo extends Basic {
	public Integer cmd = Controller.ECHO;
	public List<String> campaigns = new ArrayList<String>();
	public String status = "ok";
	
	public Echo() {
		super();
	}
}
