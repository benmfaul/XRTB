package com.xrtb.common;

import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.Impression;
import com.xrtb.probe.Probe;

/**
 * Fixed node implements a chunk of fixed code, once found in the Creative
 * Determines if this is a native ad.
 */
public class FixedNodeIsNative extends Node {

    public FixedNodeIsNative() {
        super();
    }

    @Override
    public boolean test(BidRequest br, Creative creative, String adId, Impression imp, StringBuilder errorString, Probe probe) throws Exception {
        if (creative.isNative() && imp.nativePart == null) {                                   // NodeIsNative
            probe.process(br.getExchange(), adId, creative.impid, Probe.BID_CREAT_IS_NATIVE);
            if (errorString != null)
                errorString.append(Probe.BID_CREAT_IS_NATIVE);
            falseCount.incrementAndGet();
            return false;
        }
        return true;
    }
}
