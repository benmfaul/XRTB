package com.xrtb.common;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import edu.emory.mathcs.backport.java.util.concurrent.locks.ReentrantLock;
import redis.clients.jedis.Jedis;

/**
 * A threas safe class that sanely handles broken resources in Jedis. Stupid jedis pool is brain dead.
 * @author Ben M. Faul
 *
 */
public class MyJedisPool {
    private final BlockingQueue<Jedis> pool;
    private final ReentrantLock lock = new ReentrantLock();
    private int createdObjects = 0;
    private int size = 64;
    
    String host = "localhost";
    int port = 6379;
    
    /**
     * Create a default pool. We create 64 by default.
     */
    public MyJedisPool() {
    	this(64,true);
   
    }
    
    /**
     * Create a jedis pool to the host and port, with default number of connections.
     * @param host String. The hostname.
     * @param port int. The port to connect to.
     */
    public MyJedisPool(String host, int port) {
    	this(64, true);
    	this.host = host;
    	this.port = port;
    }
  
    /**
     * Create a jedis pool to the host and port, with specified number of connections.
     * @param host String. The hostname.
     * @param port int. The port to connect to.
     * @param size int. The number of JEDIS objects to keep around.
     */
    public MyJedisPool(String host, int port, int size) {
        this(size, true);
        this.host = host;
        this.port = port;
    }

    /**
     * Create the pool.
     * @param size int. The size of the pool at max.
     * @param dynamicCreation boolean. Allow dynamic creations
     */
    protected MyJedisPool(int size, Boolean dynamicCreation) {
        // Enable the fairness; otherwise, some threads
        // may wait forever.
        pool = new ArrayBlockingQueue(size,true);
        this.size = size;
        if (!dynamicCreation) {
            lock.lock();
        }
    }

    /**
     * Get a resource.
     * @return Jedis. The connection to JEDIS.
     * @throws Exception on connection errors.
     */
    public Jedis getResource() throws Exception {
    	if (pool.size() == 0) {
    		  ++createdObjects;
              return createObject();
    	}
     
        return pool.take();
    }

    /**
     * Return a resource to the pool.
     * @param resource Jedis. The thing to return.
     * @throws Exception on addition errors.
     */
    public void returnResource(Jedis resource) throws Exception {
        // Will throws Exception when the queue is full,
        // but it should never happen.
        pool.add(resource);
    }

    /**
     * Create the pool, when dynamic. Will open the connections and stuff them all in.
     */
    public void createPool() {
        if (lock.isLocked()) {
            for (int i = 0; i < size; ++i) {
                pool.add(createObject());
                createdObjects++;
            }
        }
    }

    /**
     * Create an object for the pool.
     * @return Jedis. The new resource.
     */
    protected Jedis  createObject() {
    	Jedis jedis = new Jedis(host,port);
    	jedis.connect();
    	return jedis;
    }
    
    /**
     * Publish from the pool.
     * @param channel String. The channel to publish on.
     * @param message String. The message to publish.
     */
    public void publish(String channel, String message) {
    	Jedis j = null;
    	try {
    		j = getResource();
    		j.publish(channel, message);
    	} catch (Exception error) {
    		j.disconnect();
    		j.connect();
    		j.publish(channel, message);
    	}
    	try {
			returnResource(j);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /**
     * Do sismember from the pool.
     * @param key String. The set we are checking.
     * @param value. String. The value we are checking membership on.
     * @return boolean. Returns true if in the set, else false.
     */
    public boolean sismember(String key, String value) {
    	boolean t = false;
    	Jedis j = null;
    	try {
    		j = getResource();
    		t = j.sismember(key, value);
    	} catch (Exception error) {
    		j.disconnect();
    		j.connect();
    		t = j.sismember(key, value);
    	}
    	try {
			returnResource(j);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return t;
    }
}
