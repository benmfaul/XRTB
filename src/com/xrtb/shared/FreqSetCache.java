package com.xrtb.shared;

import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;

import java.util.concurrent.TimeUnit;

/**
 * A class to replace the self expiring hash set. Use cache2k so we don't have to support more home brew code.
 * Created by Ben M. Faul on 1/1/18.
 */
public class FreqSetCache {

    /** The cache to contain the frequency cap - emulates a Set */
    private volatile Cache cache = new Cache2kBuilder<String,Object>(){}.expireAfterWrite(300, TimeUnit.SECONDS).build();

    /** The default expiration time if you add a key */
    private long expire = 900;


    public FreqSetCache() {

    }

    /**
     * Add a key, using default timeout. Warning: overrides the expiration and resets to new value/
     * @param id String. The id to set.
     */
    public void add(String id) {
        cache.invoke(id,
                e -> e.setValue(id).setExpiry(System.currentTimeMillis() + expire));
    }

    /**
     * Add a key with a custom timeout.
     * @param id String id.
     * @param timeout long. The time to expire.
     */
    public void add(String id, long timeout) {
        cache.invoke(id,
                e -> e.setValue(id).setExpiry(System.currentTimeMillis() + timeout));

    }

    /**
     * Does this cache contain this key?
     * @param id String. The id to search for.
     * @return boolean. Returns true of the id exists.
     */
    public boolean contains(String id) {
        return cache.containsKey(id);
    }

    /**
     * Clear the cache.
     */
    public void clear() {
        cache.clear();
    }

}
