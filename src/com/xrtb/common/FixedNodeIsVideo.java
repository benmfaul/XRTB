package com.xrtb.common;

import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.Impression;
import com.xrtb.probe.Probe;

/**
 * Fixed node implements a chunk of fixed code, once found in the Creative
 * Processes a vide ad, if possible.
 */
public class FixedNodeIsVideo extends Node {

    public FixedNodeIsVideo() {
        super();
    }

    @Override
    public boolean test(BidRequest br, Creative creative, String adId, Impression imp, StringBuilder errorString, Probe probe) throws Exception {
        if (creative.isVideo() && imp.video == null) {                                           // NodeIsVideo
            probe.process(br.getExchange(), adId, creative.impid, Probe.BID_CREAT_IS_VIDEO);
            if (errorString != null)
                errorString.append(Probe.BID_CREAT_IS_VIDEO);
            falseCount.incrementAndGet();
            return false;
        }
        return true;
    }
}
