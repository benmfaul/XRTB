package com.xrtb.pojo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

/**
 * An accumulator for exchange level statistics.
 * @author Ben M. Faul
 *
 */

public class Accumulator {

	public LongAdder bids = new LongAdder();
	public LongAdder requests = new LongAdder();
	public LongAdder wins = new LongAdder();
	public LongAdder errors = new LongAdder();
	public double qps = 0;
	
	private long deltaBids = 0;
	private long deltaRequests = 0;
	private long deltaWins = 0;
	private long deltaErrors = 0;

	String name;
	
	/**
	 * Accumulates stats for an exchange.
	 * @param name String. The name of the exchange.
	 */
	public Accumulator(String name) {
		this.name = name;
	}
	
	/**
	 * Returns the reporting map.
	 * @return Map. The hashmap of the report. 
	 */
	public Map getMap() {
		Map m = new HashMap();
		m.put("requests", requests.sum());
		m.put("bids", bids.sum());
		m.put("wins",wins.sum());
		m.put("errors", errors.sum());
		m.put("name", name);
		m.put("qps", qps);
		return m;
	}
	
	/**
	 * Computes deltas. Call once per reporting period, or it will skew the results.
	 */
	public void delta() {
		deltaBids = bids.longValue() - deltaBids;
		deltaRequests = requests.longValue() - deltaRequests;
		deltaWins = wins.longValue() - deltaWins;
		deltaErrors = errors.longValue() - deltaErrors;
	}
	
	/**
	 * Sets and returns delta qps. Only call this once per reporting period.
	 * @param time double. The time in seconds.
	 * @return double. The QPS. Rounds to 2 places.
	 */
	public double getDelta(double time) {
		if (time == 0)
			return 0;
		
		delta();
		
		long total = deltaBids + deltaRequests;;
		if (total == 0)
			return 0;
		qps = total/time;
		
		 BigDecimal bd = new BigDecimal(qps);
		 bd = bd.setScale(2, RoundingMode.HALF_UP);
		 qps =  bd.doubleValue();
		    
		return qps;
		
	}
}
