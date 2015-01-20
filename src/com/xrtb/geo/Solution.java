package com.xrtb.geo;

public class Solution {
	
	public String state;
	public String county;
	public String city;
	public int code;
	public double lon;
	
	public Solution() {
		
	}
	
	public String toString() {
		String buf = "Code="+code+",state=" + state + ", county="+county;
		return buf;
	}

}
