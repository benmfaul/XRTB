package com.xrtb.bidder;

import org.redisson.Redisson;

import com.xrtb.commands.LogMessage;

/**
 * A type of Publisher, but used specifically for logging, contains the instance name
 * and the current time in EPOCH.
 * 
 * @author Ben M. Faul
 *
 */
public class LogPublisher extends Publisher {

	/**
	 * Constructor for logging class.
	 * @param redisson Redisson. The REDIS connection.
	 * @param channel String. The topic name to publish on.
	 * @throws Exception on redisson errors.
	 */
	public LogPublisher(Redisson redisson, String channel) throws Exception  {
		super(redisson,channel);
	}

	public void run() {
		Object msg = null;
		while(true) {
			try {
				if ((msg = queue.poll()) != null) {
					if (msg instanceof String) {
						System.out.println(msg);
					}
					logger.publish(msg);
				}
				Thread.sleep(1);
			} catch (Exception e) {
				if (e.toString().contains("Connection is closed"))
					return;
				e.printStackTrace();
				return;
			}
		}
	}
}