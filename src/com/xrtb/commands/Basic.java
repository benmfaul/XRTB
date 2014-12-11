package com.xrtb.commands;

import org.codehaus.jackson.map.ObjectMapper;

import com.xrtb.bidder.Controller;
import com.xrtb.bidder.RTBServer;
import com.xrtb.common.Configuration;

/**
 * This is the basic command and response object in POJO form.
 * @author Ben M. Faul
 *
 */
public class Basic {
	public Integer cmd = -1;
	public String from = Configuration.instanceName;
	public String to;
	public String id;
	public String msg;
	public String status = "ok";
	
	/**
	 * Empty constructor. Manipulate the fields for creating your
	 * own command/command response.
	 */
	public Basic() {

	}
	
	/**
	 * Returns a JSON representation of the command/command response.
	 */
	
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
