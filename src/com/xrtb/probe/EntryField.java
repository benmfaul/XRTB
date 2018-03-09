package com.xrtb.probe;

/**
 * A class that provides for sortable reasons
 */

public class EntryField implements Comparable<EntryField> {
    /** The key value */
    public String key;
    /** The numeric value */
    public long value;

    /**
     * Create a sortable reasons key
     * @param key String. The reason.
     * @param v long. The count
     */
    public EntryField(String key, long v) {
        this.key = key;
        this.value = v;
    }

    /**
     * Comparison for sort
     * @param o EntryField. The other thing we are comparing.
     * @return int. Returns 0 if ==, if > that o then 1, else less than so returns -1
     */
    @Override
    public int compareTo(EntryField o) {
        if (this.value == o.value)
            return 0;
        if (this.value > o.value)
            return 1;
        return -1;
    }
}
