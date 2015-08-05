package com.xrtb.commands;

import com.xrtb.bidder.Controller;

/**
 * A command to set the log level through REDIS
 * @author Ben M. Faul
 *
 */
public class LogLevel extends BasicCommand {

	/**
	 * Empty constructor 
	 */
	public LogLevel() {
		this.cmd = Controller.SETLOGLEVEL;
	}
	
	/**
	 * Constructor of the set log level command.
	 * @param to String. To whom is this directed.
	 * @param level int. The new log level.
	 */
	public LogLevel(String to, String level) {
		this.to = to;
		this.target = level;
		this.cmd = Controller.SETLOGLEVEL;
	}
}
