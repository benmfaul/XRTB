package com.xrtb.probe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

/**
 * The campaign probe. Keeps up with why the campaign is not bidding.
 * @author ben
 *
 */
public class CampaignProbe {

	String campaign;
	LongAdder total = new LongAdder();
	LongAdder bids = new LongAdder();
	Map<String, CreativeProbe> probes;
	
	public CampaignProbe() {
		
	}
	
	public CampaignProbe(String campaign) {
		this.campaign = campaign;
		probes = new HashMap();
	}
	
	public void reset() {
		for (Map.Entry<String, CreativeProbe> entry : probes.entrySet()) {
			entry.getValue().reset();
		}
		total = new LongAdder();
		bids = new LongAdder();

	}
	
	public void process(String creative, StringBuilder br) {
		CreativeProbe probe = probes.get(creative);
		if (probe == null) {
			probe = new CreativeProbe(creative);
			probes.put(creative, probe);
		}
		probe.process(br);
	}
	
	public void process(String creative) {
		CreativeProbe probe = probes.get(creative);
		if (probe == null) {
			probe = new CreativeProbe(creative);
			probes.put(creative, probe);
		}
		probe.process();
		//total.increment();
	}
	
	public void incrementTotal() {
		total.increment();
	}
	
	public void incrementBids() {
		bids.increment();
	}
	
	public String report() {
		StringBuilder report = new StringBuilder();
		for (Map.Entry<String, CreativeProbe> entry : probes.entrySet()) {
			String key = entry.getKey();
			report.append("\t\t");
			report.append(key);
			report.append("\n");
			report.append(entry.getValue().report());
		}
		
		return report.toString();
	}
	
	public long getSumBids() {
		long z = 0;
		for (Map.Entry<String, CreativeProbe> entry : probes.entrySet()) {
			z += entry.getValue().getSumBids();
		}
		return z;
	}
	
	
	public long getSumTotal() {
		long z = 0;
		for (Map.Entry<String, CreativeProbe> entry : probes.entrySet()) {
			z += entry.getValue().getSumTotal();
		}
		return z;
	}
	
	public void reportCsv(StringBuilder sb, String pre) {
		pre += campaign + "," + bids.sum() + ",";
		for (Map.Entry<String, CreativeProbe> entry : probes.entrySet()) {
			entry.getValue().reportCsv(sb,pre);
		}
	}
	
	public long getBids() {
		return bids.sum();
	}
	
	public long getTotal() {
		return total.sum();
	}
	
	public List getMap() {
		List list = new ArrayList();
		
		for (Map.Entry<String, CreativeProbe> entry : probes.entrySet()) {
			Map x = new HashMap();
			String key = entry.getKey();
			x.put("creative", key);
			x.put("reasons", entry.getValue().getMap());
			x.put("total", entry.getValue().total.sum());
			x.put("bids", entry.getValue().bid.sum());
			list.add(x);
		}
		return list;
	}
	
	public String getTable() {
		List list = new ArrayList();
		
		StringBuilder table = new StringBuilder("<table border='1'>");
		
		for (Map.Entry<String, CreativeProbe> entry : probes.entrySet()) {
			Map x = new HashMap();
			String key = entry.getKey();
			table.append("<tr><td>");
			table.append(key);
			table.append("</td><td>");;
			table.append( entry.getValue().getTable());
			table.append("</td></tr>");
		}
		table.append("</table>");
		return table.toString();
	}
}
