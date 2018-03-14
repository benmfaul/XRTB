package com.xrtb.common;

import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.Impression;
import com.xrtb.probe.Probe;

/**
 * Fixed node implements a chunk of fixed code, once found in the Creative
 * This handles video.
 */
public class FixedNodeDoVideo extends Node {

    public FixedNodeDoVideo() {
        super();
    }

    @Override
    public boolean test(BidRequest br, Creative creative, String adId, Impression imp, StringBuilder errorString, Probe probe) throws Exception {
        if (imp.video != null) {                                                // FixedNodeDoVideo
            if (imp.video.linearity != -1 && creative.videoLinearity != null) {
                if (imp.video.linearity != creative.videoLinearity) {
                    probe.process(br.getExchange(), adId, creative.impid, Probe.VIDEO_LINEARITY);
                    if (errorString != null)
                        errorString.append(Probe.VIDEO_LINEARITY);
                    falseCount.incrementAndGet();
                    return false;
                }
            }
            if (imp.video.minduration != -1) {
                if (creative.videoDuration != null) {
                    if (!(creative.videoDuration.intValue() >= imp.video.minduration)) {
                        probe.process(br.getExchange(), adId, creative.impid, Probe.VIDEO_TOO_SHORT);
                        if (errorString != null)
                            errorString.append(Probe.VIDEO_TOO_SHORT);
                        falseCount.incrementAndGet();
                        return false;
                    }
                }
            }
            if (imp.video.maxduration != -1) {
                if (creative.videoDuration != null) {
                    if (!(creative.videoDuration.intValue() <= imp.video.maxduration)) {
                        probe.process(br.getExchange(), adId, creative.impid, Probe.VIDEO_TOO_LONG);
                        if (errorString != null)
                            errorString.append(Probe.VIDEO_TOO_LONG);
                        falseCount.incrementAndGet();
                        return false;
                    }
                }
            }
            if (imp.video.protocol.size() != 0) {
                if (creative.videoProtocol != null) {
                    if (imp.video.protocol.contains(creative.videoProtocol) == false) {
                        probe.process(br.getExchange(), adId, creative.impid, Probe.VIDEO_PROTOCOL);
                        if (errorString != null)
                            errorString.append(Probe.VIDEO_PROTOCOL);
                        falseCount.incrementAndGet();
                        return false;
                    }
                }
            }
            if (imp.video.mimeTypes.size() != 0) {
                if (creative.videoMimeType != null) {
                    if (imp.video.mimeTypes.contains(creative.videoMimeType) == false) {
                        probe.process(br.getExchange(), adId, creative.impid, Probe.VIDEO_MIME);
                        if (errorString != null)
                            errorString.append(Probe.VIDEO_MIME);
                        falseCount.incrementAndGet();
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
