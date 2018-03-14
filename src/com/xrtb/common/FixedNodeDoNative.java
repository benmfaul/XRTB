package com.xrtb.common;

import com.xrtb.nativeads.assets.Entity;
import com.xrtb.nativeads.creative.Data;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.Impression;
import com.xrtb.probe.Probe;

/**
 * Fixed node implements a chunk of fixed code, once found in the Creative
 * This node handles native ads. It can be further broken up too.
 */
public class FixedNodeDoNative extends Node {

    public FixedNodeDoNative() {
        super();
    }

    @Override
    public boolean test(BidRequest br, Creative creative, String adId, Impression imp, StringBuilder errorString, Probe probe) throws Exception {
        if (creative.isNative()) {                                                        // FixedNodeDoNative
            if (imp.nativePart.layout != 0) {
                if (imp.nativePart.layout != creative.nativead.nativeAdType) {
                    probe.process(br.getExchange(), adId, creative.impid, Probe.BID_CREAT_IS_BANNER);
                    if (errorString != null)
                        errorString.append(Probe.NATIVE_LAYOUT);
                    falseCount.incrementAndGet();
                    return false;
                }
            }
            if (imp.nativePart.title != null) {
                if (imp.nativePart.title.required == 1 && creative.nativead.title == null) {
                    probe.process(br.getExchange(), adId, creative.impid, Probe.NATIVE_TITLE);
                    if (errorString != null)
                        errorString.append(Probe.NATIVE_TITLE);
                    falseCount.incrementAndGet();
                    return false;
                }
                if (creative.nativead.title.title.text.length() > imp.nativePart.title.len) {
                    probe.process(br.getExchange(), adId, creative.impid, Probe.NATIVE_TITLE_LEN);
                    if (errorString != null)
                        errorString.append(Probe.NATIVE_TITLE_LEN);
                    falseCount.incrementAndGet();
                    return false;
                }
            }

            if (imp.nativePart.img != null && creative.nativead.img != null) {
                if (imp.nativePart.img.required == 1 && creative.nativead.img == null) {
                    probe.process(br.getExchange(), adId, creative.impid, Probe.NATIVE_WANTS_IMAGE);
                    if (errorString != null)
                        errorString.append(Probe.NATIVE_WANTS_IMAGE);
                    falseCount.incrementAndGet();
                    return false;
                }
                if (creative.nativead.img.img.w != imp.nativePart.img.w) {
                    probe.process(br.getExchange(), adId, creative.impid, Probe.NATIVE_IMAGEW_MISMATCH);
                    if (errorString != null)
                        errorString.append(Probe.NATIVE_IMAGEW_MISMATCH);
                    falseCount.incrementAndGet();
                    return false;
                }
                if (creative.nativead.img.img.h != imp.nativePart.img.h) {
                    probe.process(br.getExchange(), adId, creative.impid, Probe.NATIVE_IMAGEH_MISMATCH);
                    if (errorString != null)
                        errorString.append(Probe.NATIVE_IMAGEH_MISMATCH);
                    falseCount.incrementAndGet();
                    return false;
                }
            }

            if (imp.nativePart.video != null) {
                if (imp.nativePart.video.required == 1 || creative.nativead.video == null) {
                    probe.process(br.getExchange(), adId, creative.impid, Probe.NATIVE_WANTS_VIDEO);
                    if (errorString != null)
                        errorString.append(Probe.NATIVE_WANTS_VIDEO);
                    falseCount.incrementAndGet();
                    return false;
                }
                if (creative.nativead.video.video.duration < imp.nativePart.video.minduration) {
                    probe.process(br.getExchange(), adId, creative.impid, Probe.NATIVE_AD_TOO_SHORT);
                    if (errorString != null)
                        errorString.append(Probe.NATIVE_AD_TOO_SHORT);
                    falseCount.incrementAndGet();
                    return false;
                }
                if (creative.nativead.video.video.duration > imp.nativePart.video.maxduration) {
                    probe.process(br.getExchange(), adId, creative.impid, Probe.NATIVE_AD_TOO_LONG);
                    if (errorString != null)
                        errorString.append(Probe.NATIVE_AD_TOO_LONG);
                    falseCount.incrementAndGet();
                    return false;
                }
                if (imp.nativePart.video.linearity != null
                        && imp.nativePart.video.linearity.equals(creative.nativead.video.video.linearity) == false) {
                    probe.process(br.getExchange(), adId, creative.impid, Probe.NATIVE_LINEAR_MISMATCH);
                    if (errorString != null)
                        errorString.append(Probe.NATIVE_LINEAR_MISMATCH);
                    falseCount.incrementAndGet();
                    return false;
                }
                if (imp.nativePart.video.protocols.size() > 0) {
                    if (imp.nativePart.video.protocols.contains(creative.nativead.video.video.protocol)) {
                        probe.process(br.getExchange(), adId, creative.impid, Probe.NATIVE_AD_PROTOCOL_MISMATCH);
                        if (errorString != null)
                            errorString.append(Probe.NATIVE_AD_PROTOCOL_MISMATCH);
                        falseCount.incrementAndGet();
                        return false;
                    }
                }

            }

            for (Data datum : imp.nativePart.data) {
                Integer val = datum.type;
                Entity e = creative.nativead.dataMap.get(val);
                if (datum.required == 1 && e == null) {
                    probe.process(br.getExchange(), adId, creative.impid, Probe.NATIVE_AD_PROTOCOL_MISMATCH);
                    if (errorString != null)
                        errorString.append(Probe.NATIVE_AD_DATUM_MISMATCH);
                    falseCount.incrementAndGet();
                    return false;
                }
                if (e != null) {
                    if (e.value.length() > datum.len) {
                        probe.process(br.getExchange(), adId, creative.impid, Probe.NATIVE_AD_PROTOCOL_MISMATCH);
                        if (errorString != null)
                            errorString.append(Probe.NATIVE_AD_DATUM_MISMATCH);
                        falseCount.incrementAndGet();
                        return false;
                    }
                }

            }
        }
        return true;
    }
}
