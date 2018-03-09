package com.xrtb.rate;

import com.xrtb.common.Campaign;
import com.xrtb.common.Creative;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;


/**
 * A class that represents a price limiting entry in the bidder. It is used to track the current
 * spend in 1M long value against an assigned spend rate in 1 second increments. Call canBid() to find
 * out if you can even bid, then call addSpend() on wins.
 * @author Ben M. Faul
 */
public class Entry {

    /** The unknown marker, when we don't know what this creative is (e.g. banner, video, native) */
    public static final String UNKNOWN = "unknown";


    /** The adder for the price in micros */
    private final LongAdder v;

    /** The spend rate assigned to this entry */
    private volatile long spendRate;

    /** The map of ad types */
    private final Map<String,String> adtypes;

    /** The last nano time of the sample */
    private long lastTime = 0;

    /** The number of nanoseconds in a second */
    public final static long divisor = 1000000000;

    private final LongAdder total;

    /** Logging object */
    static final Logger logger = LoggerFactory.getLogger(Entry.class);
    long lastOverflowTime;

    /**
     * Create a rate limiting entry.
     * @param c Campaign. The campaign this belongs to
     * @param spendRate long. The price * 1M
     */
    public Entry(final Campaign c, long spendRate) {
        this.spendRate = spendRate;
        adtypes = new HashMap<String,String>();
        for (Creative cr : c.creatives) {
            String type = UNKNOWN;
            if (cr.isVideo())
                type = "video";
            else if (cr.isNative())
                type = "native";
            else
                type = "banner";
            adtypes.put(cr.impid,type);
        }
        v = new LongAdder();
        total = new LongAdder();
    }

    /**
     * Given this price, can we still bid?
     * @param price long. The price on 1M
     * @return boolean. Returns true if you can still bid.
     */
    public boolean canBid(long price) {
        long now = System.nanoTime();
        long  result = now - lastTime;
        if (result > divisor) {
            lastTime = now;
            long leftOvers = v.sumThenReset() - spendRate;
            if (leftOvers > 0) {
                v.add(leftOvers);
                return false;
            }
            return true;
        }

        return price + v.sum() <= spendRate;
    }

    /**
     * Rturn the ad type of this creative, in this campaign
     * @param crid String. The creative ID.
     * @return String. The ad type (eg banner, video, audio, native
     *
     */
    public String getAdType(String crid) {
        String adtype = adtypes.get(crid);
        if (adtype == null)
            adtype = UNKNOWN;

        return adtype;
    }

    /**
     * Set the spend rate for this entry
     * @param price long. The price in 1M.
     */
    public void setSpendRate(long price) {
        spendRate = price;
    }

    /**
     * Get the spend rate.
     * @return long. The current spend rate.
     */
    public long getSpendRate() {
        return spendRate;
    }

    /**
     * Add spend to the entry
     * @param price long. The amount to add to the running 1 second total - in 1M
     */
    public void addSpend(long price) {
        v.add(price);
        total.add(price);
    }

    /**
     * A little test program.
     * @param args String[]. Not used.
     */
    public static void main( String [] args)  {
        Campaign c = new Campaign();
        c.adId = "test";
        Entry e = new Entry(c,(long)(.0833*1000000));
        System.out.println("MAX SPEND/SECOND: " + e.spendRate);

        double time = System.currentTimeMillis();
        long v = (long)(.01875 * 1000000);
        System.out.println("Value added: " + v);
        for (int i=0;i<200000000;i++)  {
            if (e.canBid(v))
                e.addSpend(v);
        }
        time = System.currentTimeMillis() - time;
        time /= 1000;
        System.out.println("Time: " + time);

        int tt = (int)time + 1;
        System.out.println("Rate: " + (e.total.sum()/tt));
        System.out.println("Should be <= " + (e.getSpendRate()));
    }
}
