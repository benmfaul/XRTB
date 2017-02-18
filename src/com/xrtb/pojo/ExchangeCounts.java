package com.xrtb.pojo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ExchangeCounts {

	ConcurrentHashMap<String, Accumulator> map = new ConcurrentHashMap();
	Set<String> exchanges = new HashSet<String>();
	
	public void incrementBid(String exchange) {
		Accumulator a =  map.get(exchange);
		if (a == null) {
			exchanges.add(exchange);
			a = new Accumulator(exchange);
			map.put(exchange, a);
		}
		a.bids.increment();
	}
	
	public void incrementWins(String exchange) {
		Accumulator a =  map.get(exchange);
		if (a == null) {
			exchanges.add(exchange);
			a = new Accumulator(exchange);
			map.put(exchange, a);
		}
		a.wins.increment();	
	}
	
	public void incrementRequest(String exchange) {
		Accumulator a =  map.get(exchange);
		if (a == null) {
			exchanges.add(exchange);
			a = new Accumulator(exchange);
			map.put(exchange, a);
		}
		a.requests.increment();
	}
	
	public void incrementError(String exchange) {
		Accumulator a =  map.get(exchange);
		if (a == null) {
			exchanges.add(exchange);
			a = new Accumulator(exchange);
			map.put(exchange, a);
		}
		a.errors.increment();
	}
	
	public List<Map> getList() {
		List<Map> list = new ArrayList();
		String[] array = exchanges.toArray(new String[exchanges.size()]);
		for (int i = 0; i < array.length; i++) {
			String exchange = array[i];
			if (exchange != null) {
				Accumulator x = map.get(exchange);
				list.add(x.getMap());
			}
		}
		return list;	
	}
}
