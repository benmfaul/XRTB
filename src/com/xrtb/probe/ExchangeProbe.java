package com.xrtb.probe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

/**
 * Keeps up with all the campaign probes within an exchange.
 * @author Ben M. Faul
 *
 */
public class ExchangeProbe {

	String exchange;
	LongAdder total = new LongAdder();
	LongAdder bids = new LongAdder();
	
	Map<String, CampaignProbe> probes;
	
	public ExchangeProbe() {
		
	}
	
	public ExchangeProbe (String exchange) {
		this.exchange = exchange;
		probes = new HashMap();
	}
	
	/**
	 * Reset the probe for the exchange. Sets total and bids to 0 and then resets the campaign probes.
	 */
	public void reset() {
		for (Map.Entry<String, CampaignProbe> entry : probes.entrySet()) {
			entry.getValue().reset();
		}
		total = new LongAdder();
		bids = new LongAdder();

	}
	
	public void process(String campaign, String creative, StringBuilder br) {
		CampaignProbe probe = probes.get(campaign);
		if (probe == null) {
			probe = new CampaignProbe(campaign);
			probes.put(campaign, probe);
		}
		probe.process(creative, br);
	}
	
	public void process(String campaign, String creative) {
		CampaignProbe probe = probes.get(campaign);
		if (probe == null) {
			probe = new CampaignProbe(campaign);
			probes.put(campaign, probe);
		}
		probe.process(creative);
	}
	
	public void incrementTotal(String campaign) {
		total.increment();
		CampaignProbe p = probes.get(campaign);
		if (p == null) {
			p = new CampaignProbe(campaign);
			probes.put(campaign, p);
		}
		p.incrementTotal();
	}
	
	public void incrementBids(String campaign) {
		bids.increment();
		CampaignProbe p = probes.get(campaign);
		p.incrementBids();
	}
	
	public String report() {
		StringBuilder report = new StringBuilder();
		for (Map.Entry<String, CampaignProbe> entry : probes.entrySet()) {
			String key = entry.getKey();
			report.append("\t");
			report.append(key);;
			report.append("\n");
			report.append(entry.getValue().report());
		}
		
		return report.toString();
	}
	
	public void reportCsv(StringBuilder sb) {
		long ztotal = 0;
		for (Map.Entry<String, CampaignProbe> entry : probes.entrySet()) {
			ztotal += entry.getValue().getSumTotal();
		}
		String pre = System.currentTimeMillis() + "," + ztotal + ","  + exchange + "," + bids.sum()+",";
		for (Map.Entry<String, CampaignProbe> entry : probes.entrySet()) {
			entry.getValue().reportCsv(sb,pre);
		}
	}
	
	public long getTotal() {
		return total.sum();
	}
	
	public long getBids() {
		return bids.sum();
	}
	
	public List getMap() {
		List list = new ArrayList();
		for (Map.Entry<String, CampaignProbe> entry : probes.entrySet()) {
			Map x = new HashMap();
			String key = entry.getKey();
			x.put("campaign",key);
			x.put("bids", entry.getValue().getBids());
			x.put("total", entry.getValue().getTotal());
			x.put("creatives",entry.getValue().getMap());
			list.add(x);
		}
		return list;
	}
	
	public String getTable() {
		StringBuilder table = new StringBuilder("<table border='1'>");
		for (Map.Entry<String, CampaignProbe> entry : probes.entrySet()) {
			Map x = new HashMap();
			String key = entry.getKey();
			table.append("<tr><td>");
			table.append(key);
			table.append("</td><td>");
			table.append(entry.getValue().getTable());
			table.append("</td></tr>");
		}
		table.append("</table>");
		return table.toString();
	}
	
}
