package com.xrtb.bidder;

import java.util.UUID;
import java.util.concurrent.Callable;



import com.xrtb.common.Campaign;
import com.xrtb.common.Creative;
import com.xrtb.common.Node;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;

/**
 * CampaignProcessor. 
 * Given a campaign, process it into a bid.
 * @author Ben M. Faul
 *
 */
public class CampaignProcessor implements Callable<BidResponse> {
	Campaign camp;
	BidRequest br;
	BidResponse response;
	UUID uuid = UUID.randomUUID();
	/**
	 * Constructor.
	 * @param camp Campaign. The campaign to process
	 * @param br. BidRequest. The bid request to apply to this campaign.
	 */
	public CampaignProcessor(Campaign camp, BidRequest br) {
		this.camp = camp;
		this.br = br;
	}
	
	/**
	 * Return the bid if the campaign can bid on the request.
	 */
	@Override
	public BidResponse call() throws Exception {
		Creative selectedCreative = null;
		for (Creative create : camp.creatives) {
			if (br.w == create.w && br.h == create.h)
				selectedCreative = create;
		}
		if (selectedCreative == null)
			return null;
		
		for (int i=0;i<camp.nodes.size();i++) {
			Node n = camp.nodes.get(i);
			if (n.test(br) == false)
				return null;
		}
		BidResponse response = new BidResponse(br,camp,selectedCreative,uuid.toString());
		response.forwardUrl = selectedCreative.forwardUrl;
		response.adm = camp.template;
		response.makeResponse();
		return response;
	}
	
}
