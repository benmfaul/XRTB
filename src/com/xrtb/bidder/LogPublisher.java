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
	 * @param conn Jedis. The REDIS connection.
	 * @param channel String. The topic name to publish on.
	 */
	public LogPublisher(Redisson redisson, String channel) throws Exception  {
		super(redisson,channel);
	}

	public void run() {
		String str = null;
		LogMessage msg = null;
		while(true) {
			try {
				if ((msg = (LogMessage)queue.poll()) != null) {
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