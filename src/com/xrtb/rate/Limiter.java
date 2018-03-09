package com.xrtb.rate;

import com.c1x.bidder3.rtb.common.Campaign;

import java.util.HashMap;
import java.util.Map;

/**
 * A rate limiting governer. A singleton object that keeps up with current spend rates for campaigns
 * in the system.
 * @author Ben M. Faul
 */
public enum Limiter {

    /** The singleton's instance */
    INSTANCE;

    /** Default spend limit, one dollar per minute*/
    final long DEFAULT_SPEND_LIMIT = 16667;

    /** Map of entries being tracked */
    Map<String,Entry> map = new HashMap();

    /** Return the singleton's instance */
    public static Limiter getInstance() {
        return INSTANCE;
    }

    /**
     * Given the campaign id, can it even bid now?
     * @param id String. The campaign id.
     * @param price long. The price in 1M.
     * @return boolean. Returns true ig you can bid, else false.
     */
    public boolean canBid(String id, long price) {
        Entry e = map.get(id);
        if (e == null)
            return false;

        return e.canBid(price);
    }

    /**
     * When you get a win notification, add the spend to this to keep up with the price.
     * @param id String. The campaign id.
     * @param price long. The price in 1M.
     */
    public void addSpend(String id, long price) {
        Entry e = map.get(id);
        if (e == null)
            return;
        e.addSpend(price);
    }

    /**
     * When you get a win notification, add the spend to this to keep up with the price.
     * Note the ecpm is is cost per 1,000. Now, the multiplication factor is 1000000
     * for converting price to micros. But the actual cost is ecpm/1000, so instead of
     * multiplying by 1000000 we multiply bu to get the actual cost for the single
     * impression in micros.
     * @param id String. The campaign id.
     * @param cpm double. The cpm cost.
     */
    public void addSpend(String id, double cpm) {
        long actual = (long)(cpm * 1000);
        addSpend(id,actual);
    }

    /**
     * Add a campaign to be tracked for spend limits, using the default spend limit of $1/minute
     * @param c Campaign. The campaign that is being tracked.
     */
    public void addCampaign(Campaign c) {
        Entry e = new Entry(c, c.effectiveSpendRate);
        map.put(c.adId,e);
    }

    /**
     * Add a campaign to be tracked for spend limits, using the default spend limit of $1/minute
     * @param c Campaign. The campaign that is being tracked.
     * @param limit long. The campaign spend limit in dollars per minute per second.
     */
    public void addCampaign(Campaign c, long limit) {
        Entry e = new Entry(c, limit);
        map.put(c.adId,e);
    }

    /**
     * Return the ad type for this campaign/creative.
     * @param adid String. The campaign ad id.
     * @param crid String. The campaign creative id.
     * @return String. The type, e.g. native, video, audio, banner, or unknown
     */
    public String getAdType(String adid, String crid) {
        Entry e = map.get(adid);
        if (e == null)
            return Entry.UNKNOWN;
        return e.getAdType(crid);
    }

    public void setSpendRate(String adid, long price) {
        Entry e = map.get(adid);
        if (e == null)
            return;
        e.setSpendRate(price);
    }

    /**
     * Delete a campaign
     * @param id String. The campaign id of the campaign to delete
     */
    public void deleteCampaign(String id) {
        map.remove(id);
    }


    /**
     * Clear all entries
     */
    public void clear() {
        map.clear();
    }
}


