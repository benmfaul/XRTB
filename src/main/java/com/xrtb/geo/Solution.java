package com.xrtb.geo;

/**
 * Contains the solution to a GPS to zipcode, county, city, and state query.
 * @author Ben M. Faul
 *
 */
public class Solution {
	
	public String state;
	public String county;
	public String city;
	public int code;
	public double lon;
	
	public Solution() {
		
	}
	
	public String toString() {
		String buf = "code="+code+",state=" + state + ", county="+county+",city="+city;
		return buf;
	}

}
