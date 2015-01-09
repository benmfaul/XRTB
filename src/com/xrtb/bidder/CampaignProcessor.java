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
 * Given a campaign, process it into a bid. The CampaignSelector creates a CampaignProcessor, which is given a bid request
 * and a campaign to analyze. The CampaignSelector creates one CampaignProcessor for each Campaign in the system. The
 * Selector creates Future tasks and calls the processor. The call() method loops through the Nodes that define the
 * constraints of the campaign. If all of the Nodes test() method returns true, then the call() returns a SelectedCreative
 * object that identifies the campaign and the creative within that campaign, that the caller will use to create a bid response.
 * However, if any Node test returns false the call() function will return null - meaning the campaign is not applicable to the bid.
 * @author Ben M. Faul
 *
 */
public class CampaignProcessor implements Callable<SelectedCreative> {
	/** The campaign used by this processor object */
	Campaign camp;
	
	/** The bid request that will be used by this processor object */
	BidRequest br;
	
	/** The response that will be created from the processing of the request with the campaign. */
	BidResponse response;
	
	/** The unique ID assigned to the bid response. This is probably not needed TODO: Need to remove this */
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
	 * Accessed by Future... Return the campaign and creative in the campaign if the campaign can bid on the request.
	 */
	@Override
	public SelectedCreative call() throws Exception {
		Creative selectedCreative = null;
		if (camp == null)
			return null;
		for (Creative create : camp.creatives) {
			if (br.w == create.w && br.h == create.h) {
				selectedCreative = create;
				break;
			}
		}
		if (selectedCreative == null)
			return null;
		
		for (int i=0;i<camp.attributes.size();i++) {
			Node n = camp.attributes.get(i);
			if (n.test(br) == false)
				return null;
		}
		SelectedCreative select = new SelectedCreative(camp,selectedCreative);
		return select;
	}
	
}
