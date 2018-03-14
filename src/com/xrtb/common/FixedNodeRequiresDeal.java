package com.xrtb.common;

import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.Impression;
import com.xrtb.probe.Probe;

/**
 * Fixed node implements a chunk of fixed code, once found in the Creative
 * Determines if a creative requires a deal.
 */
public class FixedNodeRequiresDeal extends Node {

    public FixedNodeRequiresDeal() {
        super();
    }

    @Override
    public boolean test(BidRequest br, Creative creative, String adId, Impression imp, StringBuilder errorString, Probe probe) throws Exception {

        if (creative.price == 0 && (creative.deals == null || creative.deals.size() == 0)) {
            probe.process(br.getExchange(), adId, creative.impid, Probe.DEAL_PRICE_ERROR);
            if (errorString != null) {
                errorString.append(Probe.DEAL_PRICE_ERROR);
            }
            falseCount.incrementAndGet();
            return false;
        }
        return true;
    }
}
