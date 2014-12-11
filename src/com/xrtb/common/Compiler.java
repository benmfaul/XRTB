package com.xrtb.common;

import java.util.ArrayList;
import java.util.List;

import com.xrtb.pojo.BidRequest;

public class Compiler {
	
	private static Compiler instance;

	// The list here is sorted, and it is in this order the bid request keys
	// are queried and catenated into the hash key
	private List<String> keyUnion = new ArrayList<String>(); // the union of all keys seen by the compiler
	
	/**
	 * Private constructor, class has no public constructor.
	 */
	private Compiler() {

	}
	
	public static Compiler getInstance() {
		if (instance == null) {
			synchronized (Configuration.class) {
				if (instance == null) {
					instance = new Compiler();
				}
			}
		}
		return instance;
	}
	
	/**
	 * Clears the compiler
	 * 
	 */
	public void clear() {
		
	}
	
	/**
	 * remove a campaign
	 */
	public void removeCampaign() {
		
	}
	
	public void addCampaign() {
		
	}
	
	static StringBuffer union = new StringBuffer();
	public void compileBidRequestKeys() {

	}
	
	public void getKeyUnion() {

	}
}
