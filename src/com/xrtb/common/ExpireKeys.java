package com.xrtb.common;

import java.util.ArrayList;
import java.util.List;

/** Singleton for holding SPEC and EXPIRY keys. Keep from generating tons of strings on the heap when
 * handling frequency keys.
 * Created by Ben M. Faul on 8/10/17.
 */
public enum ExpireKeys {
    INSTANCE;

    /** The spec keys. It's just SPEC + i */
    private static volatile List<String> spec = new ArrayList<>();

    /** The expiry key. It's just EXPIRY + i */
    private static volatile List<String> expiry = new ArrayList<>();

    /** The time-unit key. It's just TU + i */
    private static volatile List<String> tus = new ArrayList<>();

    /** The base of the spec key */
    private static final String SPEC = "SPEC";

    /** The base of the expiry key */
    private static final String EXPIRY = "EXPIRY";

    /** The base of the time-unit key */
    private static final String TU = "TU";

    /**
     * Return the instance of the Keys.
     * @return ExpireKeys. The instance that holds the 2 keys
     */
    public static ExpireKeys getInstance() {
        if (spec.size()==0) {
            compile(128);
        }
        return INSTANCE;
    }

    /**
     * Generate keys. Should generate 1 for each campaign in the system,
     * @param size int. The number of keys to generate.
     */
    public static void compile(int size) {
        for (int i = spec.size(); i < size; i++) {
            spec.add(SPEC + i);
            expiry.add(EXPIRY + i);
            tus.add(TU + i);
        }
    }

    /**
     * Return the spec key at i.
     * @param i int. The the index of the spec key to return. If the key does not exist, new keys are
     *          compiled to handle the projected overflow.
     * @return String. The key SPEC + i
     */
    public String getSpecKey(int i) {
        if (i > spec.size())
            compile(i + 16);
        return spec.get(i);
    }

    /**
     * Return the expire key at
     * @param i int. The index of he expire key to return. If the key does not exist, new keys are
     *          compiled to handle the overflow.
     * @return String. The key EXPIRY + i
     */
    public String getExpireKey(int i) {
        if (i > spec.size())
            compile(i + 16);
        return expiry.get(i);
    }

    /**
     * Return the time-unit key at
     * @param i int. The index of he time-unit key to return. If the key does not exist, new keys are
     *          compiled to handle the overflow.
     * @return String. The key EXPIRY + i
     */
    public String getTimeUnitKey(int i) {
        if (i > spec.size())
            compile(i + 16);
        return tus.get(i);
    }
}
