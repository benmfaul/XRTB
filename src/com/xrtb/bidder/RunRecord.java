package com.xrtb.bidder;

import java.util.ArrayList;
import java.util.List;

public class RunRecord {
	long start = System.nanoTime();
	List<Log> logs = new ArrayList();
	
	public RunRecord(String name) {
		logs.add(new Log(name,start = System.nanoTime()));
	}
	
	public void add(String name) {
		logs.add(new Log(name,start = System.nanoTime()));
	}
	
	public void dump() {
		int i = 0;
		long start = logs.get(0).time;
		String name = logs.get(0).name;
		long count = 0;
		
	/*	for (Log l : logs) {
			System.out.print(l.name);
			if (i +1 < logs.size())
					System.out.print(",");
			i++;
		} */
	//	System.out.println(",total");
		i = 0;
		for (Log l : logs) {
			long delta = 0;
			delta = (l.time - start);
			start  = l.time;
			count += delta/1000;
			System.out.print((delta/1000));
			if (i +1 <= logs.size())
				System.out.print(",");
			i++;
		}
		System.out.println(count);

	}
}

class Log {
	public String name;
	public long time;
	
	public Log(String name, long time) {
		this.name = name;
		this.time = time;
	}
}