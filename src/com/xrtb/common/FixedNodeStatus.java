package com.xrtb.common;

import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.Impression;
import com.xrtb.probe.Probe;

/**
 * Fixed node implements a chunk of fixed code, once found in the Creative
 * This handles status of the creative.
 */
public class FixedNodeStatus extends Node {

    public FixedNodeStatus() {
        super();
        name = "FixedNodeStatus";
    }

    @Override
    public boolean test(BidRequest br, Creative creative, String adId, Impression imp, StringBuilder errorString, Probe probe) throws Exception {
        if (!creative.ALLOWED_STATUS.equalsIgnoreCase(creative.status)) {
            probe.process(br.getExchange(), adId, creative.impid,Probe.CREATIVE_NOTACTIVE );
            if (errorString != null) {
                errorString.append(Probe.CREATIVE_NOTACTIVE);
            }
            falseCount.incrementAndGet();
            return false;
        }
        return true;
    }
}
