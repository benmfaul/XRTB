package com.xrtb.bidder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.common.Creative;
import com.xrtb.common.Node;
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

	public static volatile int highWaterMark = 100;

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
	public BidResponse getHeuristic(BidRequest br) {
		boolean printNoBidReason = Configuration.getInstance().printNoBidReason;
		int logLevel = 5;

		if (br.forensiqPassed() == false) {
			if (printNoBidReason) {
				try {
					Controller
							.getInstance()
							.sendLog(
									logLevel,
									"CampaignProcessor:run:campaign:bid-fraud",
									"This id is fraudulent: " + br.id);
				} catch (Exception e) {
					// TODO Auto-generated catch block
				}
			}
			return null;
		}
		
		
		if (printNoBidReason || br.id.equals("123")) {
			printNoBidReason = true;
			if (br.id.equals("123")) {
				logLevel = 1;
			}
		}
		// RunRecord record = new RunRecord("Campaign-Selector");

		Iterator<Campaign> it = config.campaignsList.iterator();
		List<SelectedCreative> candidates = new ArrayList();
		List<CampaignProcessor> tasks = new ArrayList();

		// CountDownLatch latch=new CountDownLatch(config.campaignsList.size());

		List<Campaign> list = randomizedList();

		AbortableCountDownLatch latch = new AbortableCountDownLatch(
				list.size(), -1);

		for (Campaign c : list) {
			CampaignProcessor p = new CampaignProcessor(c, br, null, latch);
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

		// record.add("candidates");
		// ///////////////////////////////////////////////////////////

		if (candidates.size() == 0)
			return null;

		int index = randomGenerator.nextInt(candidates.size());

		// System.err.println("------>INDEX = " + index + "/" +
		// candidates.size());
		if (candidates.size() > 1 && printNoBidReason) {
			String str = "";
			for (SelectedCreative c : candidates) {
				str = c.campaign.adId + "/" + c.creative.impid + " ";
			}
			try {
				Controller.getInstance().sendLog(logLevel,
						"CampaignProcessor:run:campaign-selected-candidates: ",
						str);
			} catch (Exception e) {

			}

		}

		SelectedCreative select = candidates.get(index);
	//	if (select.campaign.forensiq) {
	/*		if (br.forensiqPassed() == false) {
				if (printNoBidReason) {
					try {
						Controller
								.getInstance()
								.sendLog(
										logLevel,
										"CampaignProcessor:run:campaign:bid-fraud",
										"This id is fraudulent: " + br.id);
					} catch (Exception e) {
						// TODO Auto-generated catch block
					}
				}
				return null;
			} */
//		}
		BidResponse winner = new BidResponse(br, select.campaign,select.creative, br.id);

		winner.capSpec = select.capSpec;
		winner.forwardUrl = select.getCreative().forwardurl;

		// record.add("forward-url");
		// record.dump();

		try {
			if (printNoBidReason)
				Controller.getInstance().sendLog(logLevel,
						"CampaignProcessor:run:campaign-selected-winner",
						select.campaign.adId + "/" + select.creative.impid);
		} catch (Exception error) {

		}

		return winner;
	}

	public BidResponse getMaxConnections(BidRequest br) {

		if (br.id.equals("123"))
			return getHeuristic(br);

		// RunRecord record = new RunRecord("Campaign-Selector");

		List<Campaign> list = randomizedList();

		long xtime = System.currentTimeMillis();
		Campaign test = null;
		List<Integer> dups = new ArrayList();
		SelectedCreative select = null;
		while ((System.currentTimeMillis() - xtime) < 13) {
			int index = randomGenerator.nextInt(config.campaignsList.size());
			if (dups.contains(index) == false) {
				test = config.campaignsList.get(index);
				CampaignProcessor p = new CampaignProcessor(test, br, null,
						null);
				p.run();
				select = p.getSelectedCreative();
				if (select != null)
					break;
				dups.add(index);
			}
		}

		if (select == null)
			return null;

		BidResponse winner = new BidResponse(br, select.getCampaign(),
				select.getCreative(), br.id);

		winner.capSpec = select.capSpec;
		winner.forwardUrl = select.getCreative().forwardurl;

		try {
			if (Configuration.getInstance().printNoBidReason)
				Controller.getInstance().sendLog(
						Configuration.getInstance().logLevel,
						"CampaignProcessor:run:campaign-selected-winner",
						select.campaign.adId + "/" + select.creative.impid);
		} catch (Exception error) {

		}

		return winner;
	}

	/**
	 * Hueristic adjustment
	 */
	public static void adjustHighWaterMark() {
		if (RTBServer.avgBidTime > 30) {
			if (highWaterMark > Configuration.getInstance().campaignsList
					.size())
				highWaterMark = Configuration.getInstance().campaignsList
						.size();
			highWaterMark -= 5;
		} else {
			if (highWaterMark < Configuration.getInstance().campaignsList
					.size())
				highWaterMark += 1;
			else
				highWaterMark = Configuration.getInstance().campaignsList
						.size();
		}
		// System.out.println("--->HIGH WATER MARK: " + highWaterMark);
	}

	/**
	 * Choose a random selection of
	 * 
	 * @return
	 */
	List<Campaign> randomizedList() {
		List<Campaign> myList = new ArrayList();

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
	public BidResponse XXXget(BidRequest br) {

		// RunRecord record = new RunRecord("Campaign-Selector");
		AbortableCountDownLatch latch = new AbortableCountDownLatch(1,
				config.campaignsList.size());
		CountDownLatch throttle = new CountDownLatch(1);

		for (int i = 0; i < config.campaignsList.size(); i++) {
			Campaign c = config.campaignsList.get(i);
			new CampaignProcessor(c, br, throttle, latch);
		}
		throttle.countDown();
		try {
			// long start = System.currentTimeMillis();
			latch.await();
			SelectedCreative select = latch.getCreative();
			BidResponse winner = new BidResponse(br, select.getCampaign(),
					select.getCreative(), br.id);

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
	public BidResponse getSpecific(BidRequest br, String owner,
			String campaignName, String creativeName) {
		Campaign camp = null;
		Creative creative = null;
		for (Campaign c : config.campaignsList) {
			if (c.owner.equals(owner) && c.adId.equals(campaignName)) {
				camp = c;
				break;
			}
		}
		if (camp == null) {
			System.out.println("Can't find specification " + owner + "/"
					+ campaignName);
			return null;
		}
		for (Creative cr : camp.creatives) {
			if (cr.impid.equals(creativeName)) {
				creative = cr;
				break;
			}
		}
		if (creative == null) {
			System.out.println("Can't find creative " + creative + " for "
					+ owner + "/" + campaignName);
			return null;
		}

		String h = creative.strH;
		String w = creative.strW;
		int oldH = creative.h;
		int oldW = creative.w;

		creative.strW = "" + br.w;
		creative.strH = "" + br.h;
		creative.w = br.w;
		creative.h = br.h;

		try {
			for (int i = 0; i < camp.attributes.size(); i++) {
				Node n = camp.attributes.get(i);
				if (n.test(br) == false) {
					if (Configuration.getInstance().printNoBidReason)
						Controller.getInstance().sendLog(
								5,
								"CampaignProcessor:run:attribute-failed",
								camp.adId + ":" + n.hierarchy
										+ " doesn't match the bidrequest");
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

		BidResponse winner = new BidResponse(br, camp, creative, br.id);

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
			if (camp.owner.equals(campaign.owner)
					&& camp.adId.equals(campaign.adId)) {
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
