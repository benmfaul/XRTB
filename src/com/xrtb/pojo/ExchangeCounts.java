package com.xrtb.pojo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Singleton used to keep up with exchange statistics
 * @author Ben M. Faul
 */
public enum ExchangeCounts {

	/** The instance variable */
	INSTANCE;

	/** The map of accumulators, one for each exchange, keyed by exchange */
	ConcurrentHashMap<String, Accumulator> map = new ConcurrentHashMap();
	/** A copy of the exchange keys */
	Set<String> exchanges = new HashSet<String>();
	/** The current time */
	long time = System.currentTimeMillis();

	/**
	 * Return the instance of the counter.
	 * @return ExchangeCounts. The instance of this enum that keeps up with the counts.
	 */
	public static ExchangeCounts getInstance() {
		return INSTANCE;
	}

	/**
	 * Increment the bid count.
	 * @param exchange String. The exchange that is being incremented.
	 */
	public void incrementBid(String exchange) {
		Accumulator a =  map.get(exchange);
		if (a == null) {
			exchanges.add(exchange);
			a = new Accumulator(exchange);
			map.put(exchange, a);
		}
		a.bids.increment();
	}

	/**
	 * Increment the wins for an exchange
	 * @param exchange String. The exchange to increment.
	 */
	public void incrementWins(String exchange) {
		Accumulator a =  map.get(exchange);
		if (a == null) {
			exchanges.add(exchange);
			a = new Accumulator(exchange);
			map.put(exchange, a);
		}
		a.wins.increment();	
	}

	/**
	 * Increment the requests count.
	 * @param exchange String. The exchange name.
	 */
	public void incrementRequest(String exchange) {
		Accumulator a =  map.get(exchange);
		if (a == null) {
			exchanges.add(exchange);
			a = new Accumulator(exchange);
			map.put(exchange, a);
		}
		a.requests.increment();
	}

	/**
	 * Increment an error
	 * @param exchange String. The exchange name.
	 */
	public void incrementError(String exchange) {
		Accumulator a =  map.get(exchange);
		if (a == null) {
			exchanges.add(exchange);
			a = new Accumulator(exchange);
			map.put(exchange, a);
		}
		a.errors.increment();
	}

	/**
	 * Return the list of exchanges with their performance parameters
	 * @return List. The exchanges performances as a list of mnaps.
	 */
	public List<Map> getList() {
		List<Map> list = new ArrayList();
		long now = System.currentTimeMillis() - time;
		String[] array = exchanges.toArray(new String[exchanges.size()]);
		for (int i = 0; i < array.length; i++) {
			String exchange = array[i];
			if (exchange != null) {
				Accumulator x = map.get(exchange);
				x.getDelta(now/1000);
				list.add(x.getMap());
			}
		}
		time = System.currentTimeMillis();
		return list;	
	}

	public static void reset() {
		for (String s : ExchangeCounts.getInstance().exchanges) {
			Accumulator a = ExchangeCounts.getInstance().map.get(s);
			a.reset();
		}
	}
}
