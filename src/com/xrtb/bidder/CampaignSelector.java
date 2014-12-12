package com.xrtb.bidder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;

/**
 * A singleton object that is used to select campaigns based on a given bid
 * request.
 * 
 * @author Ben M. Faul
 * 
 */
public class CampaignSelector {
	Configuration config;
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
	 * @throws Exception. Throws
	 *             exceptions if the configuration object is bad.
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
	 * Given a bid request, select a campaign for bidding on it.
	 * 
	 * @param br
	 *            BidRequest. The bid request object of an RTB request.
	 * @return Campaign. The campaign to use to construct the response.
	 */
	public BidResponse get(BidRequest br) {
		Iterator<Campaign> it = config.campaigns.iterator();
		List<BidResponse> candidates = new ArrayList();
		ExecutorService executor = Executors
				.newFixedThreadPool(config.campaigns.size());
		Random randomGenerator = new Random();
		List<FutureTask<BidResponse>> tasks = new ArrayList();
		while (it.hasNext()) {
			Campaign c = it.next();
			c.br = br;
			FutureTask<BidResponse> futureTask = new FutureTask<BidResponse>(new CampaignProcessor(c,br));
			tasks.add(futureTask);
			executor.execute(futureTask);
		}

		long start = System.currentTimeMillis();
		while (tasks.size() > 0) {
			if (1==0/*System.currentTimeMillis() - start > config.timeout*/) {
				for (FutureTask<BidResponse> camp : tasks) {
					camp.cancel(true);
				}
				tasks.clear();
			} else
				for (int i=0;i<tasks.size();i++) {
					FutureTask<BidResponse> camp = tasks.get(i);
					try {
						if (camp.isDone()) {
							BidResponse test = camp.get();
							if (test != null) {
								candidates.add(test);
							}
							tasks.remove(camp);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
		}
		executor.shutdown();
		if (candidates.size()==0)
			return null;
		
        int index = randomGenerator.nextInt(candidates.size());
        BidResponse winner = candidates.get(index);
		// Candidates now have the campaigns that matched within
		// time time limit

		return winner;
	}

	/**
	 * Adds a campaign to the list of usable campaigns.
	 * 
	 * @param c
	 *            . Campaign. A new campaign to add.
	 */
	public void add(Campaign c) {
		config.campaigns.add(c);
	}

	/**
	 * Clear all the campaigns of the selector.
	 */
	public void clear() {
		config.campaigns.clear();
	}

	/**
	 * Returns the number of campaigns in the selector.
	 * 
	 * @return int. The number of campaigns in use by the selector.
	 */
	public int size() {
		return config.campaigns.size();
	}
	
	/**
	 * Returns the set of campaigns in this selector object.
	 * @return Campaign<Set>. The campaigns set.
	 */
    public Set<Campaign> getCampaigns() {
    	return config.campaigns;
    }
}
