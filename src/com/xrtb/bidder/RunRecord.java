package com.xrtb.bidder;

import java.util.ArrayList;
import java.util.List;

/**
 * A Performance logging class (kinda? accurate)
 * Creates a single line of text on stdout that shows the performance between marked lines of code.
 * The first column is the name of the measurement. The second - N lines are delta microseconds from the last measure.
 * The total column is the total microseconds between the measurement.
 * @author Ben M. Faul
 *
 */
public class RunRecord {
	double start = System.nanoTime();
	List<Log> logs = new ArrayList();
	
	/**
	 * Constructor
	 * @param name String. The name of this log, will
	 */
	public RunRecord(String name) {
		logs.add(new Log(name,start = System.nanoTime()));
	}
	
	public void add(String name) {
		logs.add(new Log(name,start = System.nanoTime()));
	}
	
	public void dump() {
		int i = 0;
		double start = logs.get(0).time;
		String name = logs.get(0).name;
		double count = 0;
		
		i = 0;
		for (Log l : logs) {
			double delta = 0;
			delta = (l.time - start);
			start  = l.time;
			count += delta/1000;
			if (i == 0) 
				System.out.print(l.name);
			else 
				System.out.print(l.name + ":" + (delta/1000));
			if (i +1 <= logs.size())
				System.out.print(",");
			i++;
		}
		count /= 1000;
		System.out.println("total:"+count);

	}
}

class Log {
	public String name;
	public double time;
	
	public Log(String name, double d) {
		this.name = name;
		this.time = d;
	}
}