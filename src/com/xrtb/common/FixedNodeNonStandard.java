package com.xrtb.common;

import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.Impression;
import com.xrtb.probe.Probe;

/**
 * Fixed node implements a chunk of fixed code, once found in the Creative
 * Handles a non standard. Like appnexus and stroer which require additional configuration before they can be used.
 */
public class FixedNodeNonStandard extends Node {

    public FixedNodeNonStandard() {
        super();
    }

    @Override
    public boolean test(BidRequest br, Creative creative, String adId, Impression imp, StringBuilder errorString, Probe probe) throws Exception {

        boolean test = br.checkNonStandard(creative, errorString);
        if (!test)
            falseCount.incrementAndGet();
        return test;
    }
}
