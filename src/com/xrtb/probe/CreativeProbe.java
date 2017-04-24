package com.xrtb.probe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

public class CreativeProbe {

	String creative;
	Map<String, LongAdder> probes;
	LongAdder total = new LongAdder();
	LongAdder bid = new LongAdder();

	public CreativeProbe() {

	}

	public CreativeProbe(String creative) {
		this.creative = creative;
		probes = new HashMap();
	}

	public void process(StringBuilder br) {
		String key = br.toString();
		LongAdder ad = probes.get(key);
		if (ad == null) {
			ad = new LongAdder();
			probes.put(key, ad);
		}
		ad.increment();
		total.increment();
	}

	public void process() {
		total.increment();
		bid.increment();
	}

	public String report() {
		StringBuilder report = new StringBuilder();
		report.append("\t\t\ttotal = ");
		report.append(total.sum());
		report.append(", bids = ");
		report.append("\n");
		for (Map.Entry<String, LongAdder> entry : probes.entrySet()) {
			String key = entry.getKey();
			report.append("\t\t\t");
			report.append(key);
			report.append(" = ");
			LongAdder ad = entry.getValue();
			report.append(ad.sum());
			report.append(",   (");
			double v = total.sum();
			double vx = ad.sum();
			report.append(100 * vx / v);
			report.append(")\n");
		}

		return report.toString();
	}

	public List getMap() {
		Map x = new HashMap();
		List list = new ArrayList();
		for (Map.Entry<String, LongAdder> entry : probes.entrySet()) {
			String key = entry.getKey();
			x = new HashMap();
			x.put("name", key);
			x.put("count", entry.getValue().sum());
			list.add(x);
		}
		return list;
	}

	public String getTable() {
		double nobids = total.sum() - bid.sum();
		StringBuilder table = new StringBuilder("<table border='1'>");         
		table.append("<tr><td>total</td><td>");
		table.append(total.sum());
		table.append("</td></tr>");
		table.append("<tr><td>bids</td><td>");
		table.append(bid.sum());
		table.append("</td></tr>");
		table.append("<tr><td>no bids:</td><td>");
		table.append((total.sum() - bid.sum()));
		table.append("</td></tr>");
		if (probes.entrySet().size()> 0) {
		table.append("<table>");
		table.append("<tr><td>Reasons</td><td><table border='1'><th>Reason</th><th>Count</th><th>Percent</th>");
			for (Map.Entry<String, LongAdder> entry : probes.entrySet()) {
				table.append("<tr><td>");
				String key = entry.getKey();
				table.append(key);
				table.append("</td><td>");
				table.append(entry.getValue().sum());
				table.append("</td><td>");
				table.append((entry.getValue().sum()/nobids * 100));
				table.append("</td></tr>");
			}
			table.append("</table></td></tr></td></tr>");
		}
		else {
			table.append("</td></tr></td></tr>");
		}
		table.append("</table>");
		return table.toString();
	}
}
