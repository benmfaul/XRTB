package com.xrtb;

/**
 * Created by ben on 1/1/18.
 */
public class RedissonObjectMessage {
    /** The key to use */
    public String key;
    /** The source, cache, or cacheDb */
    public String source;
    /** The type, as in set, hash, etc */
    public String type;
    /** Object. the actual object to set */
    public Object value;
    /** The operation to perform */
    public String op;
    /** The timeout value - the actual UTC. -1 of no timeout */
    public long timeout = -1;
}
