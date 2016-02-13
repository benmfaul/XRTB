package com.xrtb.tools;


public class Quartet implements Comparable {
	String iab;
	int count;
	double percent;
	String description;

	public Quartet(String iab, int count, int total) {
		this.iab = iab;
		this.count = count;
		this.percent = (double) count / (double) total * 100.0;
		this.description = IABCategories.get(iab);
	}

	@Override
	public int compareTo(Object o) {
		Quartet x = (Quartet) o;
		if (count == x.count)
			return 0;
		if (count > x.count)
			return -1;
		return 1;
	}
}