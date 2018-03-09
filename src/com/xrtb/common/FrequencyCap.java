package com.xrtb.common;

import com.xrtb.bidder.Controller;
import com.xrtb.pojo.BidRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * A calls that handles frequency capping in Aerospike.
 * Created by Ben M. Faul on 7/21/17.
 */
public class FrequencyCap {
    protected static final Logger logger = LoggerFactory.getLogger(FrequencyCap.class);

    /** Cap specification */
    public List<String> capSpecification;
    /** Cap frequency count */
    public int capFrequency = 0;
    /** Cap timeout in HOURS */
    public int capTimeout; // is a string, cuz its going into redis
    /** Cap time unit, will be one of ("minutes", "hours", "days", "lifetime") */
    public String capTimeUnit;

    /** The computed capKey that will be stored in aerospike */
    public String capKey;

    /**
     * Default constructor for JSON
     */
    public FrequencyCap() {

    }

    /**
     * Return a copy of this frequency acap.
     * @return FrequencyCap. The new frequency cap to return.
     */
    public FrequencyCap copy() {
        FrequencyCap c = new FrequencyCap();
        c.capSpecification = capSpecification;         // can reuse this
        c.capFrequency = capFrequency;
        c.capTimeout = capTimeout;
        c.capTimeUnit = capTimeUnit;
        return c;
    }

    /**
     * Is this creative capped on the IP address in this bid request?
     * @param br BidRequest. The bid request to query.
     * @param capSpecs Map. The current cap spec.
     * @param adId String. The ad id being frequency checked.
     * @return boolean. Returns true if the IP address is capped, else false.
     */
    public boolean isCapped(BidRequest br, Map<String, String> capSpecs, String adId) {
        if (capSpecification == null)
            return false;

        StringBuilder value = new StringBuilder();
        try {
            for (int i=0;i<capSpecification.size();i++) {
                value.append(BidRequest.getStringFrom(br.database.get(capSpecification.get(i))));
            }
            if (value == null)
                return false;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }

        StringBuilder bs = new StringBuilder("capped_");
        bs.append(adId);
        bs.append(value);
        int k = 0;
        String cap = null;
        try {
            cap = bs.toString();
            //System.out.println("---------------------> " + cap);
            capSpecs.put(adId, cap);
            k = getCapValue(cap);
            if (k < 0)
                return false;
        } catch (Exception e) {
            logger.error("ERROR GETTING FREQUENCY CAP: {}, error: {}", cap,e.toString());
            return true;
        }

        if (k >= capFrequency)
            return true;
        return false;

    }

    /**
     * Returns the number of seconds between the date string and now.
     * @param dateString String. The number of SECONDS from now (e.g. "15") OR a date string as "yyyy-MM-dd hh:mm", which will return
     *                   the number of SECONDS between the now and the dateStting. If you pass in "1" it will return 1. If you send in
     *                   yyyy-mm-hh then the result returns is datetime ((converted to milliseconds) - epoch (in millliseconds)/1000 thus
     *                   returning seconds.
     * @return int. The number of seconds to apply for the expiry.
     * @throws Exception on date parsing errors.
     */
    public static int returnTimeout(String dateString) throws Exception {
        int n = 0;
        try {
            n = Integer.parseInt(dateString);
        } catch (Exception error) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
                Date parsedDate = dateFormat.parse(dateString);
                long now = System.currentTimeMillis();

                n = (int) (now - parsedDate.getTime()) / 1000;
            } catch (Exception error1) {
                throw error1;
            }
        }
        return n;
    }

    /**
     * Return the Cap value
     *
     * @param capSpec String key for the count
     * @return int. The Integer value of the capSpec
     */
    public static int getCapValue(String capSpec) throws Exception {
        Number cap = Controller.bidCachePool.getNumber(capSpec);
        return cap != null? cap.intValue() : -1;
    }

    /**
     * Handle expiration of a cap specification.
     * @param capSpec String. The frequency specification.
     * @param capTimeout int. The number of the seconds to timeout the specification.
     * @param capTimeUnit String. Cap time unit, will be one of ("minutes", "hours", "days", "lifetime").
     * @throws Exception on Aerospike errors.
     */
    public static void handleExpiry(String capSpec, int capTimeout, String capTimeUnit) throws Exception {
        Controller.bidCachePool.increment(capSpec, capTimeout, capTimeUnit);
    }
}
