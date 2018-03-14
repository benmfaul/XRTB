package com.xrtb.common;

import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.Impression;
import com.xrtb.probe.Probe;

/**
 * Fixed node implements a chunk of fixed code, once found in the Creative
 * This handless exchange specific.
 */
public class FixedNodeExchange extends Node {

    public FixedNodeExchange() {
        super();
        name = "FixedNodeExchange";
    }

    @Override
    public boolean test(BidRequest br, Creative creative, String adId, Impression imp, StringBuilder errorString, Probe probe) throws Exception {
        if (creative.exchange != null && creative.exchange.equals(br.getExchange())==false) {
            probe.process(br.getExchange(), adId, creative.impid,Probe.WRONG_EXCHANGE );
            if (errorString != null) {
                errorString.append(Probe.WRONG_EXCHANGE);
            }
            falseCount.incrementAndGet();
            return false;
        }
        return true;
    }
}
