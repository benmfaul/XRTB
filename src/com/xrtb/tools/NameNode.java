package com.xrtb.tools;

import com.xrtb.RedissonClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * A class that keeps a set of bidders in a scored sorted set. Bidders add themselves to the
 * pool with their timestamp as a score. Once every 5 seconds they wake up and update their own score.
 * Each bidder also ages the set, and anyone who has a timestamp over 60 seconds old is deleted from the set.
 * If a bidder is not in the pool, it is considered down.
 * 
 * @author Ben M. Faul
 *
 */
public class NameNode implements Runnable {

	/** name of the scored sorted set in REDIS. Redisson does not 'directly' support this construct */
	public static final String BIDDERSPOOL = "bidderspool";
	/** Expiry in seconds from now */
	public static final long INTERVAL = 30000;
	/** Time to pause */
	public static final long PAUSE = 5000;
	
	/** The redis connection */
	static RedissonClient redis;
	/** My thread */
	Thread me;
	/** My name. Note, a master has no name, it is null */
	String name;
	/** A latch to keep parent objects from stopping this thread before the redis operations have completed */
	CountDownLatch latch = new CountDownLatch(1);
	
	public static void main(String[] args) throws Exception {
		redis = new RedissonClient();
		NameNode x = new NameNode("ben",redis);
		x.addMember("peter");
		x.addMember("clarissa");

		Thread.sleep(9000);
		System.out.println(x.getMembers());

	}

	public NameNode(String name) throws Exception {
        this.name = name;
        redis = new RedissonClient();

        redis.addList(BIDDERSPOOL,new ArrayList());

        me = new Thread(this);
        me.start();
        latch.await();
    }
	
	/**
	 * Creates a master node.
	 * @param client RedissonClient. The Aerospike redisson object.
	 * @throws Exception if the node fails to start.
	 */
	public NameNode(RedissonClient client) throws Exception {
		redis = client;
		name = null;
		
		me = new Thread(this);
		me.start();
		latch.await();
	}
	
	/**
	 * Creates a named node (a bidder)
	 * @param name String. The instance name of this node.
	 * @param client RedissonClient. An aerospike redisson object.
	 * @throws Exception if class fails to start.
	 */
	public NameNode(String name, RedissonClient client) throws Exception {
		this.name = name;
		redis = client;

        addMember(name);
		me = new Thread(this);
		me.start();
		latch.await();
	}
	
	/**
	 * Remove a name from the pool
	 * @param name String. The name of the bidder to remove.
	 */
	public void remove(String name) throws Exception {
		System.out.println("-------------> REMOVING NAME: " + name);
		List<String> biddersList = redis.getList(BIDDERSPOOL);
		redis.del(name);
		biddersList.remove(name);
		redis.addListExpire(BIDDERSPOOL,biddersList,300);
	}

	public void addMember(String name) throws Exception {
	    if (name == null)
	        return;

	    this.name = name;
        List<String> biddersList = redis.getList(BIDDERSPOOL);
        if (biddersList == null) {
            biddersList = new ArrayList();
        }
        if (biddersList.contains(name)==false) {
            biddersList.add(name);
            redis.addListExpire(BIDDERSPOOL,biddersList,300);
        }
        redis.set(name,name,60);
    }
	
	/**
	 * Periodic processing
	 */
	public void run() {
	    try {
            addMember(name);
        } catch (Exception error) {
	        error.printStackTrace();
        }
		while(true) {
			try {
                List<String> biddersList = redis.getList(BIDDERSPOOL);
                if (biddersList != null) {
                    List<String> dels = new ArrayList();
                    for (String test : biddersList) {
                        if (test != null) {
                            if (redis.get(test) == null) {
                                dels.add(test);
                            }
                        }
                    }
                    biddersList.remove(dels);
                    redis.addListExpire(BIDDERSPOOL, biddersList, 300);
                }
			} catch (Exception e) {
				e.printStackTrace();
				//log(1,"NameNodeManager", "INTERRUPT: " + name);
				System.out.println("*** ERROR UPDATING NAME NODE: " + e);
				//AerospikeHandler.reset();
				//if (name != null)
				//	redis.zrem(BIDDERSPOOL, name);
				//return;
			}
			latch.countDown();						// doesn't do anything after the first time
			try {
				Thread.sleep(PAUSE);
			} catch (InterruptedException e) {
				return;
			}
			
		}
	}
	
	/**
	 * A simple logger. Override with your log mechanism.
	 * @param level int. The logging level.
	 * @param location String. The location of where the log originated.
	 * @param msg String. The message of the log.
	 */
	public void log(int level, String location, String msg) {
		String name = "Master";
		if (this.name != null)
			name = this.name;
		System.out.println(name + ": " + location + " : " + msg);
	}

	public Map getStatus(String key) throws Exception {
	    Map m = (Map)redis.get(key);
	    return m;
    }
	
	/**
	 * Get a list of bidders on the system.
	 * @return List. A list of bidders by their instance names.
	 * @throws Exception on Redis errors (except cast error when the key is empty)
	 */
	public  List<String> getMembers()  throws Exception {
		try {
			return getMembers(redis);
		} catch (Exception error) {
			if (error.toString().contains("Long cannot be cast to java.util.List")) {
                return (List<String>) new ArrayList();
			}
			throw error;
		}
	}

    /**
     * Determines if this name is a member,
     * @param name String. The name of the member.
     * @return boolean. Returns true if it is a member.
     * @throws Exception on redisson errors.
     */
	public boolean isMember(String name) throws Exception {
	    List<String> members = getMembers();
	    if (members.contains(name))
	        return true;
	    return false;
    }


	public static List<String> getMembers(RedissonClient redis) throws Exception {
		return redis.getList(BIDDERSPOOL);
	}
	
	/**
	 * Stops this thread.
	 */
	public void stop() {
	    removeYourself();
		me.interrupt();
	}
	
	/**
	 * Stop this thread
	 */
	public void halt() {
		stop();
	}
	
	/** 
	 * Remove yourself from the pool
	 * 
	 */
	public void removeYourself() {
		try {
		    remove(name);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
