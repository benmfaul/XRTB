package com.xrtb.commands;
import java.util.ArrayList;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.xrtb.bidder.Controller;
import com.xrtb.common.Campaign;
import com.xrtb.tools.DbTools;

/**
 * This is the echo command and response format. It provides basic statistical info plus
 * all of the campaigns currently loaded in the system.
 * @author Ben M. Faul
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Echo extends BasicCommand {
	/** The list of campaign objects, that are currently loaded in the systen */
	public List<Campaign> campaigns = new ArrayList();;
	/** The current setting of percentage */
	public int percentage;
	/** Indicates whether the bidder is processing any bid requests */
	public boolean stopped;
	/** the count of bid requests procesed */
	public long request;
	/** The count of bids currently send */
	public long bid;
	/** The count of no-bids current sent */
	public long nobid;
	/** The number of win notifications */
	public long win;
	/** The count of errors accessing the bidder */
	public long error;
	/** Number of total requests */
	public long handled;
	/** Number of unknown requests */
	public long unknown;
	/** The current log level */
	public int loglevel;
	/** The number of clicks */
	public long clicks;
	/** The number of pixels */
	public long pixel;
	/** the adpsend */
	public double adspend;
	/** relative qps */
	public double qps;
	/** avg xtime */
	public double avgx;
	/** Fraud count */
	public long fraud;
	/** Number of threads */
	public int threads;
	/** Percentage of memory used by the VM */
	public String memory;
	/** Percentage of disk free */
	public String freeDisk;
	/** Disk usage percentage */
	public String cpu;
	/** Summary stats by exchanges */
	public List<Map>exchanges;
	
	public Echo() {
		super();
		cmd = Controller.ECHO;
		status = "ok";
	}
	
	public Echo(String s) {
		super(s);
		cmd = Controller.ECHO;
		status = "ok";
	}
	
	/**
	 * Return a pretty printed JSON object
	 * @return String. A pretty printed JSON string of this object 
	 */
	public String toJson() {
		try {
			return DbTools.mapper.writer().withDefaultPrettyPrinter().writeValueAsString(this);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String [] args) {
		Echo e = new Echo();
		System.out.println(e);
	}
}
