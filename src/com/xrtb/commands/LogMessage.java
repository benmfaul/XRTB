package com.xrtb.commands;

/**
 * Creates a log message for various events that happen in the RTB4FREE server.
 * @author Ben M. Faul
 *
 */

public class LogMessage {

	public int sev;
	public String field;
	public String message;
	public String source;
	long time;
	
	/**
	 * Default constructor.
	 */
	public LogMessage() {
		
	}
	
	/**
	 * A container for a logging message.
	 * @param sev int. The urgency of the message. 1 is most urgent. 3 is normal run stuff. 5 and 6 are debug.
	 * @param instance String. The instance sending the message.
	 * @param field String. The field identifier in the message, e.g. the key.
	 * @param message String. The log message.
	 */
	public LogMessage(int sev, String instance, String field, String message) {
		this.sev = sev;
		this.field = field;
		this.message = message;
		this.source = instance;
		time = System.currentTimeMillis();
	}
}
