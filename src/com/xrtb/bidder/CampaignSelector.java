package com.xrtb.bidder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;

/**
 * A singleton object that is used to select campaigns based on a given bid
 * request. The selector, through the get() request will determine which
 * campaigns/creatives match a bid request. If there is more than one creative
 * found, then one is selected at random, and then the BidRequest object is
 * returned. If no campaign matched, then null is returned.
 * 
 * @author Ben M. Faul
 * 
 */
public class CampaignSelector {

	static Random randomGenerator = new Random();
	/** The configuration object used in this selector */
	Configuration config;

	/** The instance of the singleton */
	static CampaignSelector theInstance;

	/**
	 * Empty private constructor.
	 */
	private CampaignSelector() {

	}

	/**
	 * Returns the singleton instance of the campaign selector.
	 * 
	 * @return CampaignSelector. The object that selects campaigns
	 * @throws Exception
	 *             if there was an error loading the configuration file.
	 */
	public static CampaignSelector getInstance() throws Exception {
		if (theInstance == null) {
			synchronized (CampaignSelector.class) {
				if (theInstance == null) {
					theInstance = new CampaignSelector();
					theInstance.config = Configuration.getInstance();
				}
			}
		}
		return theInstance;
	}

	/**
	 * Given a bid request, select a campaign to use in bidding. This method
	 * will create a list of Future tasks, each given a campaign and the bid
	 * request, which will then determine of the campaign is applicable to the
	 * request. If more than one campaign matches, then more than one Future
	 * task will return a non-null object 'SelectedCreative' which can be used
	 * to make a bid, in the multiples case one of the SelectedCreatives is
	 * chosen at random, then the bid response is created and returned.
	 * 
	 * @param br
	 *            BidRequest. The bid request object of an RTB bid request.
	 * @return Campaign. The campaign to use to construct the response.
	 */
	/*public BidResponse get(BidRequest br) {

		// RunRecord record = new RunRecord("Campaign-Selector");

		Iterator<Campaign> it = config.campaignsList.iterator();
		List<SelectedCreative> candidates = new ArrayList();
		List<CampaignProcessor> tasks = new ArrayList();
		
		CountDownLatch latch=new CountDownLatch(config.campaignsList.size());
		
		for (Campaign c : config.campaignsList) {
			CampaignProcessor p = new CampaignProcessor(c, br, latch);
			tasks.add(p);
		}
		
		// 13%
		long start = System.currentTimeMillis();
		
		try {
			latch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (CampaignProcessor proc : tasks) {
			if (proc.selected != null) {
				candidates.add(proc.selected);
			}
		}
	
		// record.add("candidates"); ///////////////////////////////////////////////////////////
		
		if (candidates.size() == 0)
			return null;

		int index = randomGenerator.nextInt(candidates.size());
		SelectedCreative select = candidates.get(index);
		BidResponse winner = new BidResponse(br, select.getCampaign(), select.getCreative(), br.id );

		winner.forwardUrl = select.getCreative().forwardurl;

		// record.add("forward-url");
		// record.dump();

		try {
			if (Configuration.getInstance().printNoBidReason)
				Controller.getInstance().sendLog(5,
						"CampaignProcessor:run:campaign-selected",
						select.campaign.adId);
		} catch (Exception error) {

		}

		return winner;
	} */
	
	/**
	 * Given a bid request, select a campaign to use in bidding. This method
	 * will create a list of Future tasks, each given a campaign and the bid
	 * request, which will then determine of the campaign is applicable to the
	 * request. If more than one campaign matches, then more than one Future
	 * task will return a non-null object 'SelectedCreative' which can be used
	 * to make a bid, in the multiples case one of the SelectedCreatives is
	 * chosen at random, then the bid response is created and returned.
	 * 
	 * @param br
	 *            BidRequest. The bid request object of an RTB bid request.
	 * @return Campaign. The campaign to use to construct the response.
	 */
	public BidResponse get(BidRequest br) {

		// RunRecord record = new RunRecord("Campaign-Selector");
		AbortableCountDownLatch latch=new AbortableCountDownLatch(1, config.campaignsList.size());
		CountDownLatch throttle= new CountDownLatch(1);
		
		for (Campaign c : config.campaignsList) {
			new CampaignProcessor(c, br, throttle, latch);
		}
		throttle.countDown();
		try {
			// long start = System.currentTimeMillis();
			latch.await();
			SelectedCreative select = latch.getCreative();
			BidResponse winner = new BidResponse(br, select.getCampaign(), select.getCreative(), br.id );

			winner.forwardUrl = select.getCreative().forwardurl;

			// record.add("forward-url");
			// record.dump();

			try {
				if (Configuration.getInstance().printNoBidReason)
					Controller.getInstance().sendLog(5,
							"CampaignProcessor:run:campaign-selected",
							select.campaign.adId);
			} catch (Exception error) {

			}

			return winner;
		} catch (InterruptedException e) {
			// An interrupt occurs if no creative was found
		}
		
		return null;
	}

	/**
	 * Adds a campaign to the list of usable campaigns.
	 * 
	 * @param campaign
	 *            . A new campaign to add.
	 */
	public void add(Campaign campaign) throws Exception {
		boolean state = RTBServer.stopped;
		Thread.sleep(100);
		RTBServer.stopped = true;
		for (int i=0; i<config.campaignsList.size();i++) {
			Campaign camp = config.campaignsList.get(i);
			if (camp.owner.equals(campaign.owner) &&
					camp.adId.equals(campaign.adId)) {
				config.campaignsList.remove(i);
				config.campaignsList.add(campaign);
				RTBServer.stopped = state;
				return;
			}
			
		}
		RTBServer.stopped = state;
		config.campaignsList.add(campaign);
	}

	/**
	 * Clear all the campaigns of the selector.
	 */
	public void clear() {
		config.campaignsList.clear();
	}

	/**
	 * Returns the number of campaigns in the selector.
	 * 
	 * @return int. The number of campaigns in use by the selector.
	 */
	public int size() {
		return config.campaignsList.size();
	}

	/**
	 * Returns the set of campaigns in this selector object.
	 * 
	 * @return List. The campaigns set.
	 */
	public List<Campaign> getCampaigns() {
		return config.campaignsList;
	}
}
