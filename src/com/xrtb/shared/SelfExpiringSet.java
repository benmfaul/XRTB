package com.xrtb.shared;

import java.util.Set;

/**
 * Interface for the self expiring map
 * @author Ben M. Faul
 * @param <K> the Key type
 */
public interface SelfExpiringSet<K> extends Set<K> {

    /**
     * Renews the specified key, setting the life time to the initial value.
     *
     * @param key
     * @return true if the key is found, false otherwise
     */
    public boolean renewKey(K key);

    /**
     * Associates the given key to the given value in this map, with the specified life
     * times in milliseconds.
     *
     * @param key

     * @param lifeTimeMillis
     * @return a previously associated object for the given key (if exists).
     */
    public boolean add(K key, long lifeTimeMillis);
        
}