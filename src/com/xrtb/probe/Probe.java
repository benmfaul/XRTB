package com.xrtb.probe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

/**
 * Reason you don't bid probe.
 * @author Ben M. Faul
 *
 */
public class Probe {

	public static volatile Map<String, ExchangeProbe> probes;
	
	public static final StringBuilder DEAL_PRICE_ERROR = new StringBuilder("This creative price is 0, with no set deals\n");
	public static final StringBuilder PRIVATE_AUCTION_LIMITED = new StringBuilder("This creative price is 0, with no set deals, and this is a private auction\n");
	public static final StringBuilder NO_WINNING_DEAL_FOUND = new StringBuilder("Error in finding the winning deal in the bid request\n");
	public static final StringBuilder NO_APPLIC_DEAL = new StringBuilder("This creative price is 0, with no matching deals in the bid request, and is a private auction\n");
	public static final StringBuilder BID_FLOOR = new StringBuilder("Bid floor greater than bid\n");
	public static final StringBuilder BID_CREAT_IS_VIDEO = new StringBuilder("Creative is video, request is not\n");
	public static final StringBuilder BID_CREAT_IS_BANNER = new StringBuilder("Creative is banner, request is not\n");
	public static final StringBuilder BID_CREAT_IS_NATIVE = new StringBuilder("Creative is native content, request is not\n");
	public static final StringBuilder NATIVE_LAYOUT = new StringBuilder("Native ad layouts don't match\n");
	public static final StringBuilder NATIVE_TITLE = new StringBuilder("Native ad request wants a title, creative has none\n");
	public static final StringBuilder NATIVE_TITLE_LEN = new StringBuilder("Native ad title length is too long\n");
	public static final StringBuilder NATIVE_WANTS_IMAGE = new StringBuilder("Native ad request wants an img, creative has none\n");
	public static final StringBuilder NATIVE_IMAGEW_MISMATCH = new StringBuilder("Native ad img widths dont match\n");
	public static final StringBuilder NATIVE_IMAGEH_MISMATCH = new StringBuilder("Native ad img heights dont match\n");
	public static final StringBuilder NATIVE_WANTS_VIDEO = new StringBuilder("Native ad request wants a video, creative has none\n");
	public static final StringBuilder NATIVE_AD_TOO_SHORT = new StringBuilder("Native ad video duration is < what request wants");
	public static final StringBuilder NATIVE_AD_TOO_LONG = new StringBuilder("Native ad video duration is > what request wants\n");
	public static final StringBuilder NATIVE_LINEAR_MISMATCH = new StringBuilder("Native ad video linearity doesn't match the ad\n");
	public static final StringBuilder NATIVE_AD_PROTOCOL_MISMATCH = new StringBuilder("Native ad video protocol doesn't match the ad\n");
	public static final StringBuilder NATIVE_AD_DATUM_MISMATCH = new StringBuilder("Native ad data item mismatch\n");
	public static final StringBuilder WH_INTERSTITIAL = new StringBuilder("No width or height specified and campaign is not interstitial\n");
	public static final StringBuilder WH_MATCH = new StringBuilder("Creative  w or h attributes dont match\n");
	public static final StringBuilder VIDEO_LINEARITY = new StringBuilder("Video linearity does not match\n");
	public static final StringBuilder VIDEO_TOO_SHORT = new StringBuilder("Video Creative min duration not long enough\n");
	public static final StringBuilder VIDEO_TOO_LONG = new StringBuilder("Video Creative max duration too short\n");
	public static final StringBuilder VIDEO_PROTOCOL = new StringBuilder("Video Creative protocols don't match\n");
	public static final StringBuilder VIDEO_MIME = new StringBuilder("Video Creative mime type mismatch");
	public static final StringBuilder CREATIVE_MISMATCH = new StringBuilder("Creative mismatch: ");
	
	LongAdder total = new LongAdder();
	
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
	
	/**
	 * Reset the probes to 0.
	 */
	public void reset() {
		for (Map.Entry<String, ExchangeProbe> entry : probes.entrySet()) {
			entry.getValue().reset();
		}
		total.reset();
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
		if (probe == null) {
			probe = add(exchange);
		}
		probe.incrementTotal(campaign);
		total.increment();
	}
	
	public void incrementBid(String exchange, String campaign) {
		ExchangeProbe probe = probes.get(exchange);
		if (probe == null) {
			probe = add(exchange);
		}
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
	
	public String reportCsv() {
		StringBuilder report = new StringBuilder();
		for (Map.Entry<String, ExchangeProbe> entry : probes.entrySet()) {
			entry.getValue().reportCsv(report,total.sum());
		}		
		return report.toString();
	}
	
	/**
	 * Return a List of objects that denote the exchange, bids, total, and a list of maps of the campaigns.
	 * @return List. The list of report maps for the exchanges.
	 */
	public List<Map<String,Object>> getMap() {
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		for (Map.Entry<String, ExchangeProbe> entry : probes.entrySet()) {
			Map<String,Object> m = new HashMap<String,Object>();
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

class KKKV {
	
	Map<Object,Map> K1 = new HashMap();
	
	public KKKV() {
		
	}
	
	public Object get(String k1, String k2, String k3) {
		Map<Object,Map> x = K1.get(k1);
		if (x == null)
			return null;
		Map y = x.get(k2);
		if (y == null)
			return null;
		return y.get(k3);
	}
	
	public void put(String k1,  String k2,  String k3, Object v) {
		Map<Object,Map> x = K1.get(k1);
		if (x == null) {
			x = new HashMap();
			K1.put(k1, x);
		}
		Map y = x.get(k2);
		if (y == null) {
			y = new HashMap();
			y.put(k2, y);
		}
		Map z = (Map)y.get(k3);
		if (z == null) {
			z = new HashMap();
			z.put(k3,v);
		}
	}
	
}
