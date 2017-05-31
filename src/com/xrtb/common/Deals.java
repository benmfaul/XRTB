package com.xrtb.common;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.xrtb.tools.XORShiftRandom;

public class Deals extends ArrayList<Deal> {

	private static XORShiftRandom rand = new XORShiftRandom();
	
	private Map<String,Deal> map = new HashMap();
	private Set<String> s2;
	
	public Deals() {
		
	}
	
	public Deal findDeal(List<String> ids) {
		Set<String> intersection = new HashSet<String>(ids); // use the copy constructor
		intersection.retainAll(s2);
		if (intersection.size()==0)
			return null;
		int x = rand.random(intersection.size());
		List<String> nameList = new ArrayList<String>(intersection);
		String key = nameList.get(x);
		return map.get(key);
	}
	
	@Override
	public boolean add(Deal d) {
		s2.add(d.id);
		map.put(d.id,d);
		return add(d);
	}
}
