package com.xrtb.tools.explorer;

import java.util.List;
import java.util.Map;

public class Average extends Counter {

	double total;
	double highest;
	double lowest;
	
	ANode masterNode;

	public Average(String h) throws Exception {
		masterNode = new ANode(h, h, "EXISTS", null);
		title = h.toString();
	}

	public void process(Map m) throws Exception {
		Object result = null;
		String str = "";
		StringBuilder sb = new StringBuilder("");
		result = masterNode.interrogate(0, m);
		if (result == null)
			return;

		if (result instanceof Integer) {
			Integer x = (Integer)result;
			if (highest < x)
				highest = x;
			if (lowest > x)
				lowest = x;
			
			total += x;
		}
		if (result instanceof Double) {
			double x = (Double)result;
			if (highest < x)
				highest = x;
			if (lowest > x)
				lowest = x;
			
			total += (Double) result;
		}
		count++;

	}

	public void report() {
		double ratio = ((double) count / (double) Anlz.size() * 100.0);
		System.out.printf("\n%s: %d(%.3f%%)", title, count, ratio);
		if (Anlz.addCr)
			System.out.println();
		
		if (count == 0) {
			System.out.printf("Average = 0");
			return;
		}
			
		double average = total / (double) count;
		System.out.printf("Average = %.3f, Highest = %.3f, Lowest = %.3f\n\n", average, highest, lowest);
	}
}
