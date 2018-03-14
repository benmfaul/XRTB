package com.xrtb.common;

import com.xrtb.tools.XORShiftRandom;

import java.util.*;

/**
 * A class that chooses deals. For duplicates you can choose random or highest (and random of dup highest). It extends
 * List simply as a convenience for configuration.
 * @author Ben M. Faul
 *
 */
public class Deals extends ArrayList<Deal> {

	// Serialize id
	private static final long serialVersionUID = 1L;

	// Random number generator
	private static XORShiftRandom rand = new XORShiftRandom();
	
	// A map of the deals
	private Map<String,Deal> map = new HashMap<String,Deal>();
	// Set of ids in the deals list
	private Set<String> s2 = new HashSet<String>();
	
	/**
	 * Default constructor for Jackson to use.
	 */
	public Deals() {
		
	}
	
	/**
	 * Find a random deal in an intersection with a list of ids.
	 * @param ids List. A list of String ids.
	 * @return Deal. A random deal from the intersection of ids with this set.
	 */
	public Deal findDealRandom(List<String> ids) {
		Set<String> intersection = new HashSet<String>(ids); // use the copy constructor
		intersection.retainAll(s2);
		if (intersection.size()==0)
			return null;
		
		int x = rand.random(intersection.size());
		List<String> nameList = new ArrayList<String>(intersection);
		String key = nameList.get(x);
		return map.get(key);
	}

    /**
     * this is the impression list, the deals are the deals in the creative
     * @param deals
     * @return
     */
	public Deal findDealHighestList(List<Deal> deals) {
		List<String> list = new ArrayList();
		for (int i=0;i<deals.size();i++) {
			list.add(deals.get(i).id);
		}
		Deal me =  findDealHighest(list);
		if (me == null)
		    return null;

		for (int i = 0; i < deals.size(); i++) {
		    Deal test = deals.get(i);
		    if (test != null && test.id.equals(me.id)) {
		        if (me.price >= test.price) {
		            return me;
                }
            }
        }
		return null;
	}
	
	/**
	 * Return the highest price deal in the list of ids that match. If duplicates are found, then choose one of those at random.
	 * @param ids List. A list of ids to match.
	 * @return Deal. The hoghest price deal that matches.
	 */
	public Deal findDealHighest(List<String> ids) {
		Set<String> intersection = new HashSet<String>(ids); // use the copy constructor
		intersection.retainAll(s2);
		if (intersection.size()==0)
			return null;
		
		List<String> nameList = new ArrayList<String>(intersection);
		List<Deal> candidates = new ArrayList<Deal>();
		Deal x = null;
		for (int i=0;i< nameList.size();i++) {
			String key = nameList.get(i);
			Deal test = map.get(key);
			if (x == null || x.price < test.price) {
				x = test;
				candidates.clear();
				candidates.add(x);
			} else {
				if (x.price == test.price)
					candidates.add(test);
			}
		}
		if (candidates.size()==1)
			return x;
		int r = rand.random(candidates.size());
		return candidates.get(r);
	}
	
	@Override
	public boolean add(Deal d) {
		s2.add(d.id);
		map.put(d.id,d);
		return super.add(d);
	}
	
	public static void main(String args[]) {
		
		Deals deals = new Deals();
		Deal a = new Deal("a",1);
		Deal b = new Deal("b",2);
		deals.add(a);
		deals.add(b);
		
		List test = new ArrayList();
		test.add("c");
		Deal x = deals.findDealRandom(test);
		System.out.println(x);
		
		test.add("a");
		x = deals.findDealRandom(test);
		System.out.println(x.id);
		
		test.add("b");
		
		for (int i=0;i<10;i++) {
			x = deals.findDealRandom(test);
			System.out.println(x.id);
		}
		
		for (int i=0;i<10;i++) {
			x = deals.findDealHighest(test);
			System.out.println(x.id);
		}
		
		Deal z = new Deal("z",2);
		deals.add(z);
		test.add("z");
		
		for (int i=0;i<10;i++) {
			x = deals.findDealHighest(test);
			System.out.println(x.id);
		}
	}
}
