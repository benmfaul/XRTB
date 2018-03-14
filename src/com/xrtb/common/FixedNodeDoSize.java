package com.xrtb.common;

import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.Format;
import com.xrtb.pojo.Impression;
import com.xrtb.probe.Probe;

/**
 * Fixed node implements a chunk of fixed code, once found in the Creative
 * This handless banner, instl and formats
 */
public class FixedNodeDoSize extends Node {

    public FixedNodeDoSize() {
        super();
    }

    @Override
    public boolean test(BidRequest br, Creative creative, String adId, Impression imp, StringBuilder errorString, Probe probe) throws Exception {
        if (imp.nativePart == null) {                                         // FixedNodeDoSize
            if ((imp.w == null || imp.h == null) || imp.format != null) {
                // we will match any size if it doesn't match...
                if (imp.instl != null && imp.instl.intValue() == 1) {
                    Node n = creative.findAttribute("imp.0.instl");
                    if (n != null) {
                        if (n.intValue() == 0) {
                            probe.process(br.getExchange(), adId, creative.impid, Probe.WH_INTERSTITIAL);
                            if (errorString != null) {
                                errorString.append(Probe.WH_INTERSTITIAL);
                                falseCount.incrementAndGet();
                                return false;
                            }
                        }
                    } else {
                        probe.process(br.getExchange(), adId, creative.impid, Probe.WH_INTERSTITIAL);
                        if (errorString != null) {
                            errorString.append(Probe.WH_INTERSTITIAL);
                            falseCount.incrementAndGet();
                            return false;
                        }
                    }
                } else if (imp.format != null) {
                    Format f = Format.findFit(imp,creative);
                    if (f != null) {
                        creative.strW = Integer.toString(f.w);
                        creative.strH = Integer.toString(f.h);
                    } else {
                        falseCount.incrementAndGet();
                        return false;
                    }
                }
            } else {
                if (creative.dimensions == null || creative.dimensions.size()==0) {
                    creative.strW = Integer.toString(imp.w);
                    creative.strH = Integer.toString(imp.h);
                } else {
                    Dimension d = creative.dimensions.getBestFit(imp.w, imp.h);
                    if (d == null) {
                        probe.process(br.getExchange(), adId, creative.impid, Probe.WH_MATCH);
                        if (errorString != null)
                            errorString.append(Probe.WH_MATCH);
                        falseCount.incrementAndGet();
                        return false;
                    }
                    creative.strW = Integer.toString(d.leftX);
                    creative.strH = Integer.toString(d.leftY);
                }
            }
        }

        if (imp.instl.intValue() == 1 && creative.strW == null) {
            probe.process(br.getExchange(), adId, creative.impid, Probe.WH_INTERSTITIAL);
            if (errorString != null)
                errorString.append(Probe.WH_INTERSTITIAL);
            falseCount.incrementAndGet();
            return false;
        }
        return true;
    }
}
