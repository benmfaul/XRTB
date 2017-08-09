package com.xrtb.bidder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.common.Creative;
import com.xrtb.common.Node;
import com.xrtb.exchanges.appnexus.Appnexus;
import com.xrtb.pojo.BidRequest;
import com.xrtb.probe.Probe;

import edu.emory.mathcs.backport.java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	public static Probe probe = new Probe();
	
	/** The campaign used by this processor object */
	Campaign camp;

	/** The bid request that will be used by this processor object */
	BidRequest br;

	/**
	 * The unique ID assigned to the bid response. This is probably not needed
	 * TODO: Need to remove this
	 */
	UUID uuid = UUID.randomUUID();

	/** The selected creative at the end of the run, if onw is satisfied. */
	SelectedCreative selected = null;

	/** Is processing complete */
	boolean done = false;

	/** The count down latch */
	AbortableCountDownLatch latch;

	/** The flag for starting the countdown */
	CountDownLatch flag;

	/** The logging object */
	static final Logger logger = LoggerFactory.getLogger(CampaignProcessor.class);

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
//		me = new Thread(this);
//		me.start();
	}

	public void run() {
		boolean printNoBidReason = Configuration.getInstance().printNoBidReason;
		int logLevel = 5;
		StringBuilder err = null;
		if (printNoBidReason || br.id.equals("123")  || probe != null) {
			err = new StringBuilder();
			if (br.id.equals("123")) {
				logLevel = 1;
				printNoBidReason = true;
			}
		}
		// RunRecord rec = new RunRecord("Selector");

		if (flag != null) {
			try {
				flag.await();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				if (latch != null)
					latch.countNull();
				done = true;
				return;
			}
		}
		/**
		 * See if there is a creative that matches first
		 */
		if (camp == null) {
			if (latch != null)
				latch.countNull();
			done = true;
			return;
		}
		
		Node n = null;
		try {
			for (int i = 0; i < camp.attributes.size(); i++) {
				n = camp.attributes.get(i);
				
				if (n.test(br) == false) {
					if (probe != null) {
						probe.process(br.getExchange(), camp.adId, "Global", new StringBuilder(n.hierarchy));
					}
					if (printNoBidReason) 
						logger.info("camp.adId {} doesnt match the hierarchy: {}", camp.adId,n.hierarchy);
					done = true;
					if (latch != null)
						latch.countNull();
					selected = null;
					return;
				}
			}
		} catch (Exception error) {
			System.out.println("-----------> Campaign: " + camp.adId + ", ERROR IN NODE: " + n.name + ", Hierarchy = " + n.hierarchy);
			System.out.println(br.toString());
			error.printStackTrace();
			
			selected = null;
			done = true;
			if (latch != null)
				latch.countNull();
			return;
		}
		// rec.add("nodes");
		
		///////////////////////////
		
		Map<String,String> capSpecs = new ConcurrentHashMap();
		List<Creative> creatives = new ArrayList(camp.creatives);
		Collections.shuffle(creatives);
		StringBuilder xerr = new StringBuilder();
		for (Creative create : creatives) {
			
			if ((selected  = create.process(br, capSpecs, camp.adId,err, probe)) != null) {
				break;
			} else {
				if (probe != null) {
					probe.process(br.getExchange(), camp.adId, create.impid, err);
					if (printNoBidReason) {
						xerr.append(camp.adId);
						xerr.append("/");
						xerr.append(create.impid);
						xerr.append(" ===> ");
						xerr.append(err);
						xerr.append("\n");
					}
					err.setLength(0);
				}
			}
		}
		probe.incrementTotal(br.getExchange(), camp.adId);
		err = xerr;

		if (selected == null) {
			if (latch != null)
				latch.countNull();
			if (printNoBidReason)
				try {
					logger.info("nothing matches: {}",err.toString());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			done = true;
			if (err != null)
				err.setLength(0);
			return;
		}


		
		if (printNoBidReason) {
			String str = "";
			str += selected.impid + " ";
			try {
				logger.info("{} is candidate, creatives = {}", camp.adId, str);
			} catch (Exception error) {
				error.printStackTrace();
			}
		}
		
		selected.capSpec = capSpecs.get(selected.creative.impid);

		try {
			if (printNoBidReason && logLevel == 1) {
				logger.info("No match: {}",err.toString());
			}
		} catch (Exception error) {
			error.printStackTrace();
		}
		if (latch != null)
			latch.countDown(selected); 
		if (probe != null) {
			probe.process(br.getExchange(), camp.adId, selected.impid);
		}
		selected.campaign = this.camp;
		probe.incrementBid(br.getExchange(), camp.adId);
		done = true;
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
	//	if (c)
	//		me.interrupt();
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
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

		}
			
	}

}
