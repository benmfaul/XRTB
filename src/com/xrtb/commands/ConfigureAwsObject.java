package com.xrtb.commands;

import com.xrtb.bidder.Controller;
import com.xrtb.common.Campaign;

/**
 * A class that is used to encapsulate a 0MQ command for adding a campaign to the bidder.
 * Jackson will be used to create the structure.
 * @author Ben M. Faul
 *
 */
public class ConfigureAwsObject extends BasicCommand {
			
	/**
	 * Empty constructor for Jackson
	 */
	public ConfigureAwsObject() {
		super();
		cmd = Controller.CONFIGURE_AWS_OBJECT;
		msg = "An AWS object is being configured in the system";
	}

	/**
	 * Configure an AWS Object.
	 * @param to String. The bidder that will execute the command.
	 * @param name String. The name of the owner of the campaign.
	 * @param target String. The command to execute.
	 */
	public ConfigureAwsObject(String to, String name, String target) {
		super(to);
		cmd = Controller.CONFIGURE_AWS_OBJECT;
		status = "ok";
		this.owner = name;
		this.target = target;
		msg = "An AWS OBJECT is befing configured: " + name +"/" + target;
	}
}
