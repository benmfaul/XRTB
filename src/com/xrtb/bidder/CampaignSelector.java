package com.xrtb.bidder;

import java.util.ArrayList;

import java.util.List;
import java.util.Random;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.common.Creative;
import com.xrtb.common.Node;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;
import com.xrtb.pojo.Impression;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Class used to select campaigns based on a given bid
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

	//  Time high water mark in ms.
	public static volatile int highWaterMark = 100;

	// Executor for handling creative attributes.
	static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

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
	public static CampaignSelector getInstance() {
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

	
	public BidResponse getMaxConnections(BidRequest br) throws Exception {
		
		
		// Don't proces if there was an error forming the original bid request.
		if (br.notABidRequest())
			return null;
		
		// RunRecord record = new RunRecord("Campaign-Selector");
		if (br.blackListed)
			return null;

		long xtime = System.currentTimeMillis();
		Campaign test = null;
		SelectedCreative select = null;
		int kount = 0;

		List<Campaign> list = new ArrayList<Campaign>(config.campaignsList);
		Collections.shuffle(list);
		List<SelectedCreative> candidates = new ArrayList<SelectedCreative>();
		boolean exchangeIsAdx = br.getExchange().equals("adx");
		while (kount < list.size()) {
			try {
				test = list.get(kount);
			} catch (Exception error) {
				Controller.getInstance().sendLog(3, "CampaignSelector:getMaxConnections",
						"Campaign was stale, in the selection list");
				return null;
			}
			
			if (test.isAdx == exchangeIsAdx) {

				CampaignProcessor p = new CampaignProcessor(test, br, null, null);

				// executor.execute(p);
				p.run();

				select = p.getSelectedCreative();
				if (select != null) {
					if (Configuration.multibid)
						candidates.add(select);
					else
						break;
				}
			}
			kount++;
		}

		
		if (select == null && candidates.size() == 0)
			return null;

		xtime = System.currentTimeMillis() - xtime;
		// BidResponse winner = br.buildNewBidResponse(select.getCampaign(),
		// select.getCreative(), (int)xtime);
		BidResponse winner = null;
		
		if (!Configuration.multibid)
			winner = br.buildNewBidResponse(select.getImpression(), select.getCampaign(), select.getCreative(), select.getPrice(),
				select.getDealId(), (int) xtime);
		else {
			winner = br.buildNewBidResponse(select.getImpression(), candidates, (int) xtime);
		}
		

		winner.capSpec = select.capSpec;
		// winner.forwardUrl = select.forwardUrl; //
		// select.getCreative().forwardurl;

		try {
			if (Configuration.getInstance().printNoBidReason)
				Controller.getInstance().sendLog(Configuration.getInstance().logLevel,
						"CampaignProcessor:run:campaign-selected-winner",
						select.campaign.adId + "/" + select.creative.impid);
		} catch (Exception error) {

		}

		return winner;
	}

	/**
	 * Heuristic adjustment
	 */
	public static void adjustHighWaterMark() {
		if (RTBServer.avgBidTime > 30) {
			if (highWaterMark > Configuration.getInstance().campaignsList.size())
				highWaterMark = Configuration.getInstance().campaignsList.size();
			highWaterMark -= 5;
		} else {
			if (highWaterMark < Configuration.getInstance().campaignsList.size())
				highWaterMark += 1;
			else
				highWaterMark = Configuration.getInstance().campaignsList.size();
		}
		// System.out.println("--->HIGH WATER MARK: " + highWaterMark);
	}

	/**
	 * Choose a random selection of
	 * 
	 * @return
	 */
	List<Campaign> randomizedList() {
		List<Campaign> myList = new ArrayList<Campaign>();

		/*
		 * if (highWaterMark >= config.campaignsList.size()) return
		 * config.campaignsList;
		 * 
		 * for (int i=0;i<highWaterMark;i++) { int index =
		 * randomGenerator.nextInt(config.campaignsList.size());
		 * myList.add(config.campaignsList.get(index)); }
		 */
		int index = randomGenerator.nextInt(config.campaignsList.size());
		myList.add(config.campaignsList.get(index));
		return myList;
	}

	/**
	 * Creates a forced bid response on the specified bid request. owner,
	 * campaign and creative.
	 * 
	 * @param br
	 *            BidRequest. The request from the exchange.
	 * @param owner
	 *            String. The account owner of the campaign.
	 * @param campaignName
	 *            String. The campaign adid.
	 * @param creativeName
	 *            String. The creative id in the campaign.
	 * @return BidResponse. The response from the
	 */
	public BidResponse getSpecific(BidRequest br, String owner, String campaignName, String creativeName)
			throws Exception {
		long xtime = System.currentTimeMillis();
		Campaign camp = null;
		Creative creative = null;
		for (Campaign c : config.campaignsList) {
			if (c.owner.equals(owner) && c.adId.equals(campaignName)) {
				camp = c;
				break;
			}
		}
		if (camp == null) {
			System.out.println("Can't find specification " + owner + "/" + campaignName);
			return null;
		}
		for (Creative cr : camp.creatives) {
			if (cr.impid.equals(creativeName)) {
				creative = cr;
				break;
			}
		}
		if (creative == null) {
			System.out.println("Can't find creative " + creative + " for " + owner + "/" + campaignName);
			return null;
		}

		String h = creative.strH;
		String w = creative.strW;
		int oldH = creative.h;
		int oldW = creative.w;
		
		Impression imp = br.getImpression(0);

		creative.strW = "" + imp.w;
		creative.strH = "" + imp.h;
		creative.w = imp.w;
		creative.h = imp.h;

		try {
			for (int i = 0; i < camp.attributes.size(); i++) {
				Node n = camp.attributes.get(i);
				if (n.test(br) == false) {
					if (Configuration.getInstance().printNoBidReason)
						Controller.getInstance().sendLog(5, "CampaignProcessor:run:attribute-failed", camp.adId + "/"
								+ creative.impid + ": " + n.hierarchy + " doesn't match the bidrequest");
					creative.strH = h;
					creative.strW = w;

					creative.w = oldW;
					creative.h = oldH;
					return null; // don't bid
				}
			}
		} catch (Exception error) {
			error.printStackTrace();
		}

		xtime = System.currentTimeMillis() - xtime;
		BidResponse winner = br.buildNewBidResponse(imp, camp, creative, creative.price, null, (int) xtime);

		creative.strH = h;
		creative.strW = w;

		creative.w = oldW;
		creative.h = oldH;
		return winner;
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
		for (int i = 0; i < config.campaignsList.size(); i++) {
			Campaign camp = config.campaignsList.get(i);
			if (camp.owner.equals(campaign.owner) && camp.adId.equals(campaign.adId)) {
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
