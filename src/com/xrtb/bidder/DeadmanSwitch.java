package com.xrtb.bidder;

import com.xrtb.RedissonClient;
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

    /**
     * Initialize the deadman switch. Used by the bidders and crosstalk to make sure that crosstalk is up before you
     * allow it them to bid.
     * @param redisson RedissonClient. The redisson object that controls the key.
     * @param key String. The key
     */
	public DeadmanSwitch(RedissonClient redisson, String key) {
		this.redisson = redisson;
		this.key = key;
	}

    /**
     * Start the deadman switch. Bidders use this. Crosstalk doesn't use the runnable part.
     */
	public void start() {
        me = new Thread(this);
        me.start();
    }


    /**
     * Run the deadman switch.
     */
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

    /**
     * Deelete the deadman switch key.
     * @throws Exception on redisson errors.
     */
	public void deleteKey() throws Exception {
		redisson.del(key);
	}

    /**
     * Return the value of the deadman switch key.
     * @return Object. The value of the deadman switch.
     * @throws Exception on redisson errors.
     */
	public Object getKey() throws Exception {
		return redisson.get(key);
	}

    /**
     * Update the key with this value. In effect, renews the timeout (60s)
     * @param value String. The value to set the key tp.
     * @throws Exception
     */
	public void updateKey(String value) throws Exception {
	    redisson.set(key,value,60000);
    }

    /**
     * Can the bidder run? If deadman switch is null, no you can't!
     * @return boolean. Returns true if you can run, else returns false.
     */
	public boolean canRun() {
		String value = null;
		try {
			value = (String) redisson.get(key);
			if (value == null)
				Thread.sleep(2000);
			value = (String)redisson.get(key);
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
