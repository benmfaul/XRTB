package com.xrtb.exchanges.openx;

import com.xrtb.bidder.SelectedCreative;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.common.Creative;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;
import com.xrtb.pojo.Impression;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

/**
 * Created by ben on 7/24/17.
 */
public class OpenXBidResponse extends BidResponse {

    public OpenXBidResponse(BidRequest br, Impression imp, Campaign camp, Creative creat,
                            String oidStr, double price, String dealId, int xtime) throws Exception {
        super(br, imp, camp, creat, oidStr, price, dealId, xtime);
        timestamp = System.currentTimeMillis();
    }

    public OpenXBidResponse(BidRequest br, List<SelectedCreative> multi, int xtime) throws Exception {
        super(br, multi, xtime);
        timestamp = System.currentTimeMillis();
    }

    public OpenXBidResponse(JsonNode root) {
        ArrayNode seatBids = (ArrayNode) root.path("seatbid");
        for (int i = 0; i < seatBids.size(); i++) {
            JsonNode seatBid = seatBids.get(i);
            ArrayNode bids = (ArrayNode) seatBid.path("bid");
            for (int j = 0; j < bids.size(); j++) {
                JsonNode bid = bids.get(j);
                String adm = bid.path("adm").asText().replace("AUCTION_PRICE", "AUCTION_PRICE:OXCRYPT");
                ((ObjectNode) bid).put("adm", adm);
                String nurl = bid.path("nurl").asText().replace("AUCTION_PRICE", "AUCTION_PRICE:OXCRYPT");
                ((ObjectNode) bid).put("nurl", nurl);
            }
        }
        response = new StringBuilder(root.toString());
    }

    @Override
    public void makeResponse(double price) throws Exception {

        this.crid = creat.impid;
        this.domain = br.siteDomain;

        timestamp = System.currentTimeMillis();
        /** Set the response type ****************/
        if (imp.nativead)
            this.adtype = "native";
        else if (imp.video != null)
            this.adtype = "video";
        else
            this.adtype = "banner";
        /******************************************/

        /** The configuration used for generating this response */
        Configuration config = Configuration.getInstance();
        // StringBuilder nurl = new StringBuilder();
        StringBuilder linkUrlX = new StringBuilder();
        linkUrlX.append(config.redirectUrl);
        linkUrlX.append("/");
        linkUrlX.append(oidStr.replaceAll("#", "%23"));
        linkUrlX.append("/?url=");

        // //////////////////////////////////////////////////////////////////

        if (br.lat != null)
            lat = br.lat.doubleValue();
        if (br.lon != null)
            lon = br.lon.doubleValue();
        seat = br.getExchange();

        response = new StringBuilder("{");
        response.append("\"id\":\"");
        response.append(oidStr); // backwards?
        response.append("\",\"bidid\":\"");
        response.append(br.id);

        response.append(",\"seatbid\":[{\"seat\":\"");
        response.append(Configuration.getInstance().seats.get(exchange));
        response.append("\",");

        response.append("\"bid\":[{\"impid\":\"");
        response.append(impid);                            // the impression id from the request
        response.append("\",\"id\":\"");
        response.append(br.id);                        // the request bid id
        response.append("\"");

		/*
         * if (camp.encodedIab != null) { response.append(",");
		 * response.append(camp.encodedIab); }
		 */

        if (creat.currency != null && creat.currency.length() != 0) { // fyber
            response.append(",");
            response.append("\"cur\":\"");
            response.append(creat.currency);
            response.append("\"");
        }

        response.append(",\"price\":");
        response.append(price);
        response.append(",\"adid\":\"");

        if (creat.alternateAdId == null)
            response.append(adid);
        else
            response.append(creat.alternateAdId);

        response.append("\",\"cid\":\"");
        response.append(adid);
        response.append("\",\"crid\":\"");
        response.append(creat.impid);
        if (dealId != null) {
            response.append("\",\"dealid\":\"");
            response.append(dealId);
        }
        response.append("\",\"iurl\":\"");
        response.append(imageUrl);
        response.append("\",\"adomain\": [\"");
        response.append(camp.adomain);

        response.append("\"],\"adm\":\"");
        if (this.creat.isVideo()) {
            if (br.usesEncodedAdm) {
                response.append(this.creat.encodedAdm);
                this.forwardUrl = this.creat.encodedAdm;   // not part of protocol, but stuff here for logging purposes
            } else {
                //System.out.println(this.creat.unencodedAdm );
                response.append(this.creat.unencodedAdm);
                this.forwardUrl = this.creat.unencodedAdm;
            }
        } else if (this.creat.isNative()) {
            if (br.usesEncodedAdm) {
                nativeAdm = this.creat.getEncodedNativeAdm(br);
            } else {
                nativeAdm = this.creat.getUnencodedNativeAdm(br);
            }
            response.append(nativeAdm);
        } else {
            response.append(getTemplate());
        }

        response.append("\"}]}],");
        response.append("\",\"cur\":\"");
        response.append(creat.cur);

        response.append("\"}");

        this.cost = price; // pass this along so the bid response object
        // has a copy of the price
        macroSubs(response);
    }
}
