package com.xrtb.pojo;

/**
 * A simple class used to log no bids. It carried information about the 
 * @author ben
 *
 */
public class NobidResponse {

	public String id;
	public String exchange;
	public long time;
	
	public NobidResponse() {
		
	}
	
	public NobidResponse(String id, String exchange) {
		this.id = id;
		this.exchange = exchange;
		time = System.currentTimeMillis();
	}
}
