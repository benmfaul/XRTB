package com.xrtb.commands;

import org.codehaus.jackson.map.ObjectMapper;

import com.xrtb.bidder.Controller;
import com.xrtb.common.Configuration;

/**
 * This is the basic command and response object in POJO form of commands is sent over REDIS
 * @author Ben M. Faul
 *
 */
public class BasicCommand {
	/** Default command, -1 means uninitialized. Extending objects need to set this. */
	public Integer cmd = -1;
	/** The instance name obtained from the configurarion */
	public String from;
	/** The id of whom the response is sent to */
	public String to = "*";
	/** A unique ID used for this command */
	public String id = "na";
	/** The message that is associated with the command */
	public String msg = "undefined";
	/** The return status code, assume the best */
	public String status = "ok";
	/** The type of the return, we assume status */
	public String type = "status";
	
	/** The target, if any. Corresponds to instance name. If null, all bidders respond, otherwise, only those bidders matching will execute ans respond */
	public String target = "na";
	
	/**
	 * Empty constructor. Sets the command from to the bidder's instance name.
	 * own command/command response.
	 */
	public BasicCommand() {

	}
	
	/**
	 * Constructor, specifying an arbitrary name.
	 * @param to the instance string of the sender.
	 */
	public BasicCommand(String to) {
		this.to = to;
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
	
	/** 
	 * Set the command target.
	 * @param target String. the REGEX target for the command.
	 */
	public void setTarget(String target) {
		this.target = target;
	}
}
