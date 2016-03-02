package com.xrtb.bidder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.common.Creative;
import com.xrtb.common.Node;
import com.xrtb.pojo.BidRequest;

/**
 * CampaignProcessor. Given a campaign, process it into a bid. The
 * CampaignSelector creates a CampaignProcessor, which is given a bid request
 * and a campaign to analyze. The CampaignSelector creates one CampaignProcessor
 * for each Campaign in the system. The Selector creates Future tasks and calls
 * the processor. The call() method loops through the Nodes that define the
 * constraints of the campaign. If all of the Nodes test() method returns true,
 * then the call() returns a SelectedCreative object that identifies the
 * campaign and the creative within that campaign, that the caller will use to
 * create a bid response. However, if any Node test returns false the call()
 * function will return null - meaning the campaign is not applicable to the
 * bid.
 * 
 * @author Ben M. Faul
 *
 */
public class CampaignProcessor implements Runnable {
	static Random randomGenerator = new Random();
	
	/** The campaign used by this processor object */
	Campaign camp;

	/** The bid request that will be used by this processor object */
	BidRequest br;

	/**
	 * The unique ID assigned to the bid response. This is probably not needed
	 * TODO: Need to remove this
	 */
	UUID uuid = UUID.randomUUID();

	SelectedCreative selected = null;
	Thread me = null;

	boolean done = false;
	AbortableCountDownLatch latch;
	CountDownLatch flag;

	/**
	 * Constructor.
	 * 
	 * @param camp
	 *            Campaign. The campaign to process
	 * @param br
	 *            . BidRequest. The bid request to apply to this campaign.
	 */
	public CampaignProcessor(Campaign camp, BidRequest br, CountDownLatch flag,
			AbortableCountDownLatch latch) {
		this.camp = camp;
		this.br = br;
		this.latch = latch;
		this.flag = flag;
		
		if (latch != null)
			start();
	}

	public void start() {
		me = new Thread(this);
		me.start();
	}

	public void run() {
		boolean printNoBidReason = Configuration.getInstance().printNoBidReason;
		int logLevel = 5;
		StringBuilder err = null;
		if (printNoBidReason || br.id.equals("123")) {
			err = new StringBuilder();
			printNoBidReason = true;
			if (br.id.equals("123"))
				logLevel = 1;
		}
		// RunRecord rec = new RunRecord("Selector");
		Creative selectedCreative = null;

		if (flag != null) {
			try {
				flag.await();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				if (latch != null)
					latch.countNull();
				return;
			}
		}
		/**
		 * See if there is a creative that matches first
		 */
		if (camp == null) {
			if (latch != null)
				latch.countNull();
			return;
		}
		
		List<Creative> candidates = new ArrayList();

		Map<String,String> capSpecs = new ConcurrentHashMap();
		for (Creative create : camp.creatives) {
			if (create.process(br, capSpecs,err)) {
				candidates.add(create);
			} else {
				if (printNoBidReason)
					try {
						Controller.getInstance().sendLog(
								logLevel,
								"CampaignProcessor:run:creative-failed",
								camp.adId + ":" + create.impid + " "
										+ err.toString());
					} catch (Exception e) {
						e.printStackTrace();
					}
				if (err != null)
					err.setLength(0);
			}
		}

		// rec.add("creative");

		if (candidates.size() == 0) {
			done = true;
			if (latch != null)
				latch.countNull();
			return;
		}
		
		int index = 0;
		
		if (candidates.size() > 1)
			index = randomGenerator.nextInt(candidates.size());
		
		// System.err.println("------>INDEX = " + index + "/" + candidates.size());
		
		/**
		 * Ok, we found a creative, now, see if the other attributes match
		 */

		try {
			for (int i = 0; i < camp.attributes.size(); i++) {
				Node n = camp.attributes.get(i);
				
				if (n.test(br) == false) {
					if (printNoBidReason)
						Controller.getInstance().sendLog(
								logLevel,
								"CampaignProcessor:run:attribute-failed",
								camp.adId + ":" + n.hierarchy
										+ " doesn't match the bidrequest");
					done = true;
					if (latch != null)
						latch.countNull();
					return;
				}
			}
		} catch (Exception error) {
			error.printStackTrace();
			done = true;
			if (latch != null)
				latch.countNull();
			return;
		}
		// rec.add("nodes");
		
		if (printNoBidReason) {
			String str = "";
			for (Creative c : candidates) {
				str += c.impid + " ";
			}
			try {
					Controller.getInstance().sendLog(logLevel,
							"CampaignProcessor:run:campaign:is-candidate",
							camp.adId + ", creatives = " + str);
			} catch (Exception error) {

			}
		}
		
		
		Creative creative = candidates.get(index);
		selected = new SelectedCreative(camp, creative);
		selected.capSpec = capSpecs.get(creative.impid);
		done = true;

		try {
			if (printNoBidReason)
				Controller.getInstance().sendLog(logLevel,
						"CampaignProcessor:run:campaign:is-candidate-selected-creative",
						camp.adId + "/" + creative.impid);
		} catch (Exception error) {

		}
		if (latch != null)
			latch.countDown(selected); 
	}

	/**
	 * Is the campaign processing done?
	 * 
	 * @return boolean. Returns true when the processing is complete.
	 */
	public boolean isDone() {
		return done;
	}

	/**
	 * Terminate the thread processing if c == true.
	 * 
	 * @param c
	 *            boolean. Set to true to cancel
	 */
	public void cancel(boolean c) {
		if (c)
			me.interrupt();
	}

	/**
	 * Return the selected creative.
	 * 
	 * @return SelectedCreative. The creative returned by the processor.
	 */
	public SelectedCreative getSelectedCreative() {
		return selected;
	}

	public SelectedCreative call() {
		while (true) {
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
