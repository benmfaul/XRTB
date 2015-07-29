package com.xrtb.bidder;

import java.util.UUID;
import java.util.concurrent.Callable;






import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
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
public class CampaignProcessor implements Runnable {
	/** The campaign used by this processor object */
	Campaign camp;
	
	/** The bid request that will be used by this processor object */
	BidRequest br;
	
	/** The response that will be created from the processing of the request with the campaign. */
	BidResponse response;
	
	/** The unique ID assigned to the bid response. This is probably not needed TODO: Need to remove this */
	UUID uuid = UUID.randomUUID();
	
	SelectedCreative selected = null;
	Thread me = null;
	
	boolean done = false;
	/**
	 * Constructor.
	 * @param camp Campaign. The campaign to process
	 * @param br. BidRequest. The bid request to apply to this campaign.
	 */
	public CampaignProcessor(Campaign camp, BidRequest br) {
		this.camp = camp;
		this.br = br;
		start();
	}
	
	public void start() {
		me = new Thread(this);
		me.start();
	}
	

	public void run() {
		StringBuilder err = new StringBuilder();
	//	RunRecord rec = new RunRecord("Selector");
		Creative selectedCreative = null;
		if (camp == null) {
			done = true;
			return;
		}
		
		/**
		 * See if there is a creative that matches first
		 */
		for (Creative create : camp.creatives) {
			if (create.process(br,err)) {
				selectedCreative = create;
				break;
			} else {
				if (Configuration.getInstance().printNoBidReason)
					try {
						Controller.getInstance().sendLog(5, "CampaignProcessor:run:creative-failed",camp.adId + ":" + create.impid +  " " +  err.toString());
					} catch (Exception e) {
						e.printStackTrace();
					}
				err.setLength(0);
			}
		}
		
	//	rec.add("creative");
		
		if (selectedCreative == null) {
			done = true;
			return;
		}
		
		/**
		 * Ok, we found a creative, now, see if the other attributes match
		 */
		
		try {
		for (int i=0;i<camp.attributes.size();i++) {
			Node n = camp.attributes.get(i);
			if (n.test(br) == false) {
				if (Configuration.getInstance().printNoBidReason)
					Controller.getInstance().sendLog(5, "CampaignProcessor:run:attribute-failed",camp.adId + ":" + n.hierarchy +
							" doesn't match the bidrequest");
				done = true;
				return;
			}
		}
		} catch (Exception error) {
			error.printStackTrace();
			done = true;
			return;
		}
	//	rec.add("nodes");
		selected = new SelectedCreative(camp,selectedCreative);
		done = true;
		
		try {
		if (Configuration.getInstance().printNoBidReason)
			Controller.getInstance().sendLog(5, "CampaignProcessor:run:campaign:is-candidate",camp.adId);
		} catch (Exception error) {
			
		}
		
//		rec.add("select");
	//	rec.dump();
	}
	
	/**
	 * Is the campaign processing done?
	 * @return boolean. Returns true when the processing is complete.
	 */
	public boolean isDone() {
		return done;
	}
	
	/**
	 * Terminate the thread processing if c == true.
	 * @param c boolean. Set to true to cancel
	 */
	public void cancel(boolean c) {
		if (c)
			me.interrupt();
	}
	
	/**
	 * Return the selected creative.
	 * @return SelectedCreative. The creative returned by the processor.
	 */
	public SelectedCreative getSelectedCreative() {
		return selected;
	}
	
	public SelectedCreative call() {
		while(true) {
			if (isDone())
				return selected;
			try {
				me.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			
		}
	}
	
}
