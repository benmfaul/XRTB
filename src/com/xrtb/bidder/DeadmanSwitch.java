package com.xrtb.bidder;

import com.aerospike.client.AerospikeClient;
import com.aerospike.redisson.AerospikeHandler;
import com.aerospike.redisson.RedissonClient;
import com.xrtb.commands.StartBidder;
import com.xrtb.commands.StopBidder;
import com.xrtb.common.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class used to stop runaway bidders. If deadmanswitch is set in the startup
 * json, it must be present in the REDIs store before the bidder will bid. The
 * switch is set by the accounting program.
 * 
 * @author Ben M. Faul
 *
 */
public class DeadmanSwitch implements Runnable {

	/** The shared database object */
	RedissonClient redisson;
	/** My thread */
	Thread me;

	/* The key we are looking for */
	String key;

	/** Was a stop sent? */
	boolean sentStop = false;

	/** Are we in testmode */
	public static boolean testmode = false;

	/** The logging object */
	static final Logger logger = LoggerFactory.getLogger(DeadmanSwitch.class);

	public DeadmanSwitch(RedissonClient redisson, String key) {
		this.redisson = redisson;
		this.key = key;
		me = new Thread(this);
		me.start();
	}

	public DeadmanSwitch(String host, int port, String key) {
		AerospikeHandler spike = AerospikeHandler.getInstance(host, port, 300);
		redisson = new RedissonClient(spike);

		this.key = key;
		me = new Thread(this);
		me.start();
	}

	public DeadmanSwitch() {

	}

	@Override
	public void run() {
		while (true) {
			try {
				if (canRun() == false) {
					if (sentStop == false) {
						try {
							if (!testmode) {
								logger.warn("DeadmanSwitch, Switch error: {} does not exist, no bidding allowed!",key);
								StopBidder cmd = new StopBidder();
								cmd.from = Configuration.getInstance().instanceName;
								Controller.getInstance().stopBidder(cmd);
							} else {
								System.out.println("Deadman Switch is thrown");
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					sentStop = true;
				} else {
					if (sentStop) {
						sentStop = false;
						if (RTBServer.stopped) {
							RTBServer.stopped = false;
							StartBidder cmd = new StartBidder();
							cmd.from = Configuration.getInstance().instanceName;
							try {
								Controller.getInstance().startBidder(cmd);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public String getKey() {
		return key;
	}

	public boolean canRun() {
		String value = null;
		try {
			value = redisson.get(key);
			if (value == null)
				Thread.sleep(2000);
			value = redisson.get(key);
		} catch (Exception error) {
			System.out.println("*** Error retrieving deadman switch");
		}
		//System.out.println("=========> Accounting: " + value);
		if (value == null) {
			return false;
		}
		return true;
	}
}
