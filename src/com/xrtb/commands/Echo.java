package com.xrtb.commands;
import java.util.List;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xrtb.bidder.Controller;
import com.xrtb.common.Campaign;

/**
 * This is the echo command and response format. It provides basic statistical info plus
 * all of the campaigns currently loaded in the system.
 * @author Ben M. Faul
 *
 */
public class Echo extends BasicCommand {
	/** The list of campaign objects, that are currently loaded in the systen */
	public List<Campaign> campaigns;
	/** The current setting of percentage */
	public int percentage;
	/** Indicates whether the bidder is processing any bid requests */
	public boolean stopped;
	/** The count of bids currently send */
	public long bid;
	/** The count of no-bids current sent */
	public long nobid;
	/** The count of errors accessing the bidder */
	public long error;
	/** Number of total requests */
	public long handled;
	/** Number of unknown requests */
	public long unknown;
	
	public Echo() {
		super();
		cmd = Controller.ECHO;
		status = "ok";
	}
	
	/**
	 * Return a pretty printed JSON object
	 * @return String. A pretty printed JSON string of this object 
	 */
	public String toJson() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		campaigns = null;
		return gson.toJson(this);
	}
	
	public static void main(String [] args) {
		Echo e = new Echo();
		System.out.println(e);
	}
}
