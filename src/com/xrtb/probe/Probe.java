package com.xrtb.probe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Probe {

	public static Map<String, ExchangeProbe> probes;
	
	public Probe() {
		probes = new HashMap();
	}
	
	public ExchangeProbe add(String exchange) {
		ExchangeProbe probe = probes.get(exchange);
		if (probe == null) {
			probe = new ExchangeProbe(exchange);
			probes.put(exchange, probe);
		}
		return probe;
	}
	
	public void process(String exchange, String campaign, String creative, StringBuilder br) {
		ExchangeProbe probe = probes.get(exchange);
		if (probe == null) {
			probe = add(exchange);
		}
		probe.process(campaign, creative, br);
	}
	
	
	public void incrementTotal(String exchange, String campaign) {
		ExchangeProbe probe = probes.get(exchange);
		probe.incrementTotal(campaign);
	}
	
	public void incrementBid(String exchange, String campaign) {
		ExchangeProbe probe = probes.get(exchange);
		probe.incrementBids(campaign);
	}
	
	public void process(String exchange, String campaign, String creative) {
		ExchangeProbe probe = probes.get(exchange);
		if (probe == null) {
			probe = add(exchange);
		}
		probe.process(campaign, creative);
	}
	
	public String report() {
		StringBuilder report = new StringBuilder();
		for (Map.Entry<String, ExchangeProbe> entry : probes.entrySet()) {
			String key = entry.getKey();
			report.append(key);
			report.append("\n");
			report.append(entry.getValue().report());
		}		
		return report.toString();
	}
	
	public List getMap() {
		List list = new ArrayList();
		for (Map.Entry<String, ExchangeProbe> entry : probes.entrySet()) {
			Map m = new HashMap();
			String key = entry.getKey();
			m.put("exchange", key);
			m.put("bids", entry.getValue().getBids());
			m.put("total", entry.getValue().getTotal());
			m.put("campaigns",entry.getValue().getMap());
			list.add(m);
		}		
		return list;
	}
	
	public String getTable() {
		StringBuilder table = new StringBuilder();
		
		table.append("<table border='1'>\n");
		
		List list = new ArrayList();
		for (Map.Entry<String, ExchangeProbe> entry : probes.entrySet()) {
			Map m = new HashMap();
			String key = entry.getKey();
			table.append("<tr><td>");
			table.append(key);
			table.append("</td>");
			table.append("<td>");
			table.append(entry.getValue().getTable());
			table.append("</td></tr>\n");
		}	
		table.append("</table>");
		return table.toString();

	}
}
