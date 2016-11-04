package com.xrtb.tools.logmaster;

import java.util.HashMap;
import java.util.Map;

public class Slice {

	public Map<String, Integer> bids = new HashMap();
	public Map<String, Integer> wins = new HashMap();
	public Map<String, Double> cost = new HashMap();
	public Map<String, Integer> pixels = new HashMap();
	public Map<String, Integer> clicks = new HashMap();
	
	
	public Slice() {
		
	}
	
	public void clear() {
		bids = new HashMap();
		wins = new HashMap();
		cost = new HashMap();
	}
}
