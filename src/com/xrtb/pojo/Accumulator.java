package com.xrtb.pojo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

public class Accumulator {

	public LongAdder bids = new LongAdder();
	public LongAdder requests = new LongAdder();
	public LongAdder wins = new LongAdder();
	public LongAdder errors = new LongAdder();
	String name;
	
	public Accumulator(String name) {
		this.name = name;
	}
	
	public Map getMap() {
		Map m = new HashMap();
		m.put("requests", requests.sum());
		m.put("bids", bids.sum());
		m.put("wins",wins.sum());
		m.put("errors", errors.sum());
		m.put("name", name);
		return m;
	}
}
