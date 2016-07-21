package com.xrtb.tools.explorer;

public class Tuple implements Comparable {
	public String site;
	public int count;
	public double percent;

	public Tuple(String site, int count, int total) {
		this.site = site;
		this.count = count;
		this.percent = (double) count / (double) total * 100.0;
	}

	public int compareTo(Object o) {
		Tuple x = (Tuple) o;
		if (count == x.count)
			return 0;
		if (count > x.count)
			return -1;
		return 1;
	}
}
